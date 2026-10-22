package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.requests.MapRequest;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstio.TimeTaker;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.testng.Assert.*;

/**
 * An edit of nothing but bodies is moved into the unit of the model (BodyOnlyEdit): only that unit is checked again,
 * the units importing it keep what they resolved, which points at the same declarations, now at their new positions,
 * and the diagnostics and the compiled Lua are those of a build from scratch. Any other edit replaces the unit as
 * before.
 */
public class BodyOnlyEditTests {

    private static final String MOD = String.join("\n",
        "package Mod",
        "import NoWurst",
        "public module Counter",
        "    int count = 0",
        "    function bump() returns int",
        "        count++",
        "        return count",
        "");

    private static final String A = String.join("\n",
        "package A",
        "import NoWurst",
        "import public Mod",
        "public interface Shape",
        "    function area() returns int",
        "        return 1",
        "public class Box implements Shape",
        "    use Counter",
        "    int side",
        "    construct(int side)",
        "        this.side = side",
        "    override function area() returns int",
        "        return side * side",
        "public function f(int x) returns int",
        "    return x + 1",
        "public function g() returns int",
        "    return f(2)",
        "");

    private static final String B = String.join("\n",
        "package B",
        "import NoWurst",
        "import A",
        "init",
        "    let b = new Box(3)",
        "    b.bump()",
        "    BJDebugMsg(I2S(f(b.count) + b.area() + g()))",
        "");

    private Path project() throws IOException {
        Path root = Files.createTempDirectory("bodyOnlyEdit");
        Files.createDirectories(root.resolve("wurst"));
        write(root, "Mod", MOD);
        write(root, "A", A);
        write(root, "B", B);
        return root;
    }

    private static void write(Path root, String pkg, String text) throws IOException {
        Files.writeString(root.resolve("wurst/" + pkg + ".wurst"), text);
    }

    @Test
    public void aBodyEditChecksOnlyItsUnitAndKeepsWhatImportersResolved() throws IOException {
        Path root = project();
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        Log log = new Log(manager);
        manager.buildProject();
        assertEquals(manager.uncheckedCount(), 0, "the build checked every unit: " + log.current());
        FuncDef fBefore = linkedFunction(manager, "B", "f");
        NameLink countBefore = linkedName(manager, "B", "count");

        // a statement more in the body of area moves f and g down by a line
        String edited = A.replace("        return side * side",
            "        let s = side\n        return s * side");
        ModelManager.Changes changes = sync(manager, root, "A", edited);

        assertEquals(manager.bodyEdits(), 1, "the edit was moved into the unit of the model");
        assertEquals(manager.uncheckedCount(), 1, "only A is unchecked");
        manager.reconcile(changes);
        assertEquals(manager.uncheckedCount(), 0);
        assertEquals(manager.lastCheckedUnits(), 1, "the reconciliation checked A alone");

        FuncDef fAfter = linkedFunction(manager, "B", "f");
        assertSame(fAfter, fBefore, "B still points at the same declaration of f");
        assertSame(fAfter.attrCompilationUnit(), unit(manager, "A"), "which is in the unit of the model");
        assertEquals(fAfter.getSource().getLine(), 15, "at its new line");
        assertSame(linkedName(manager, "B", "count").getDef(), countBefore.getDef(),
            "B still points at the field Box got from the module");
        assertSameAsFreshBuild(root, manager, log);
    }

    @Test
    public void anErrorInAnEditedBodyIsReportedWhereAFreshBuildReportsIt() throws IOException {
        Path root = project();
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        Log log = new Log(manager);
        manager.buildProject();

        String broken = A.replace("    return x + 1", "    int unused = 0\n    string s = x\n    return x + 1");
        manager.reconcile(sync(manager, root, "A", broken));
        assertEquals(manager.bodyEdits(), 1);
        assertTrue(log.hasErrors(), "the error is reported");
        assertSameAsFreshBuild(root, manager, log);

        manager.reconcile(sync(manager, root, "A", A));
        assertEquals(manager.bodyEdits(), 2, "fixing it is a body edit too");
        assertFalse(log.hasErrors());
        assertSameAsFreshBuild(root, manager, log);
    }

    @Test
    public void editsInBodiesOfSeveralKindsAndInTheImportersMatchAFreshBuild() throws IOException {
        Path root = project();
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        Log log = new Log(manager);
        manager.buildProject();

        String a = A.replace("        this.side = side", "        this.side = side + 0\n        bump()");
        manager.reconcile(sync(manager, root, "A", a));
        assertSameAsFreshBuild(root, manager, log);
        a = a.replace("    return f(2)", "    // two\n    return f(2) + f(3)");
        manager.reconcile(sync(manager, root, "A", a));
        assertSameAsFreshBuild(root, manager, log);
        String b = B.replace("    b.bump()", "    b.bump()\n    b.bump()");
        manager.reconcile(sync(manager, root, "B", b));
        assertSameAsFreshBuild(root, manager, log);
        assertEquals(manager.bodyEdits(), 3);
    }

    @Test
    public void anEditOutsideOfBodiesReplacesTheUnit() throws IOException {
        Path root = project();
        ModelManagerImpl manager = new ModelManagerImpl(root.toFile(), new BufferManager());
        Log log = new Log(manager);
        manager.buildProject();
        Map<String, String> notBodyEdits = new LinkedHashMap<>();
        notBodyEdits.put("A", A.replace("public function f(int x)", "public function f(int y)")
            .replace("return x + 1", "return y + 1"));
        notBodyEdits.put("A ", A.replace("        return 1", "        return 2"));
        notBodyEdits.put("Mod", MOD.replace("        count++", "        count += 2"));
        notBodyEdits.put("A  ", A.replace("    int side", "    int side = 0"));
        notBodyEdits.put("A   ", A.replace("    return x + 1", "    return x +"));

        for (Map.Entry<String, String> e : notBodyEdits.entrySet()) {
            String pkg = e.getKey().trim();
            int reconciliationsBefore = manager.reconciliations();
            int bodyEditsBefore = manager.bodyEdits();
            manager.reconcile(sync(manager, root, pkg, e.getValue()));
            assertEquals(manager.bodyEdits(), bodyEditsBefore, "not a body edit: " + e.getValue());
            assertEquals(manager.reconciliations(), reconciliationsBefore + 1, "the unit was replaced");
            assertSameAsFreshBuild(root, manager, log);
            manager.reconcile(sync(manager, root, pkg, pkg.equals("Mod") ? MOD : A));
            assertSameAsFreshBuild(root, manager, log);
        }
    }

    // ---------------------------------------------------------------- helpers

    private static ModelManager.Changes sync(ModelManagerImpl manager, Path root, String pkg, String text)
        throws IOException {
        write(root, pkg, text);
        return manager.syncCompilationUnitContent(WFile.create(root.resolve("wurst/" + pkg + ".wurst").toFile()), text);
    }

    private static CompilationUnit unit(ModelManagerImpl manager, String pkg) {
        for (CompilationUnit cu : manager.getModel()) {
            if (cu.getCuInfo().getFile().endsWith(pkg + ".wurst")) {
                return cu;
            }
        }
        throw new AssertionError("no unit for " + pkg);
    }

    private static FuncDef linkedFunction(ModelManagerImpl manager, String pkg, String name) {
        List<FuncDef> found = new ArrayList<>();
        unit(manager, pkg).accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall call) {
                super.visit(call);
                FuncLink link = call.attrFuncLink();
                if (call.getFuncName().equals(name) && link != null && link.getDef() instanceof FuncDef def) {
                    found.add(def);
                }
            }
        });
        assertFalse(found.isEmpty(), "a call of " + name + " in " + pkg);
        return found.get(0);
    }

    private static NameLink linkedName(ModelManagerImpl manager, String pkg, String name) {
        List<NameLink> found = new ArrayList<>();
        unit(manager, pkg).accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ExprMemberVarDot e) {
                super.visit(e);
                if (e.getVarName().equals(name) && e.attrNameLink() != null) {
                    found.add(e.attrNameLink());
                }
            }
        });
        assertFalse(found.isEmpty(), "a read of " + name + " in " + pkg);
        return found.get(0);
    }

    private static void assertSameAsFreshBuild(Path root, ModelManagerImpl manager, Log log) {
        ModelManagerImpl fresh = new ModelManagerImpl(root.toFile(), new BufferManager());
        Log freshLog = new Log(fresh);
        fresh.buildProject();
        assertEquals(log.current(), freshLog.current(), "diagnostics");
        if (!freshLog.hasErrors()) {
            assertEquals(compile(root, manager, true), compile(root, fresh, false), "compiled Lua");
        }
    }

    private static String compile(Path root, ModelManagerImpl manager, boolean reuseCheck) {
        WurstGui gui = new WurstGuiLogger();
        RunArgs runArgs = new RunArgs("-lua");
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(new TimeTaker.Default(), root.toFile(), gui, null,
            runArgs);
        WurstModel model = manager.getModel();
        if (reuseCheck) {
            MapRequest.checkModel(manager, compiler, model, runArgs);
        } else {
            compiler.checkProg(model);
        }
        assertEquals(gui.getErrorCount(), 0, "check errors: " + gui.getErrorList());
        compiler.translateProgToIm(model);
        assertEquals(gui.getErrorCount(), 0, "translation errors: " + gui.getErrorList());
        LuaCompilationUnit lua = compiler.transformProgToLua();
        StringBuilder sb = new StringBuilder();
        lua.print(sb, 0);
        return sb.toString();
    }

    /** The diagnostics a client would show: the latest publication of each file. */
    private static final class Log {
        private final Map<String, List<String>> latest = new TreeMap<>();
        private final Set<String> errorFiles = new TreeSet<>();

        Log(ModelManagerImpl manager) {
            manager.onCompilationResult((PublishDiagnosticsParams params) -> {
                String file = params.getUri().substring(params.getUri().lastIndexOf('/') + 1);
                List<String> lines = new ArrayList<>();
                boolean errors = false;
                for (Diagnostic d : params.getDiagnostics()) {
                    lines.add(d.getRange() + " " + d.getSeverity() + " " + d.getMessage());
                    errors |= d.getSeverity() == DiagnosticSeverity.Error;
                }
                Collections.sort(lines);
                latest.put(file, lines);
                if (errors) {
                    errorFiles.add(file);
                } else {
                    errorFiles.remove(file);
                }
            });
        }

        Map<String, List<String>> current() {
            Map<String, List<String>> result = new TreeMap<>();
            latest.forEach((file, lines) -> {
                if (!lines.isEmpty()) {
                    result.put(file, lines);
                }
            });
            return result;
        }

        boolean hasErrors() {
            return !errorFiles.isEmpty();
        }
    }
}
