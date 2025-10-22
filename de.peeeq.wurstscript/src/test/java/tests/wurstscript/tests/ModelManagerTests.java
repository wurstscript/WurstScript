package tests.wurstscript.tests;

import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.GlobalCaches;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNot;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ModelManagerTests {

    @Test
    public void test() throws IOException, InterruptedException {
        File projectFolder = new File("./temp/testProject/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        String packageA_v1 = string(
            "package A",
            "import B",
            "import C",
            "public function a()",
            "	b()",
            "	c()"
        );


        String packageB_v1 = string(
            "package B",
            "import C",
            "public function b_old()",
            "	c()"
        );


        String packageC_v1 = string(
            "package C",
            "public function c_old()"
        );


        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileB = WFile.create(new File(wurstFolder, "B.wurst"));
        WFile fileC = WFile.create(new File(wurstFolder, "C.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));

        writeFile(fileA, packageA_v1);
        writeFile(fileB, packageB_v1);
        writeFile(fileC, packageC_v1);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());

        // keep error messages in a map:
        Map<WFile, String> results = keepErrorsInMap(manager);

        // first build the project
        manager.buildProject();


        assertThat(results.get(fileA), containsString("Reference to function b could not be resolved"));
        assertThat(results.get(fileA), containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), containsString("Reference to function c could not be resolved"));
        assertEquals("", results.get(fileC));

        // no assume we fix package B
        String packageB_v2 = packageB_v1.replace("b_old", "b");
        results.clear();
        ModelManager.Changes changes = manager.syncCompilationUnitContent(fileB, packageB_v2);
        manager.reconcile(changes);
        // the change of B should trigger rechecks of A and B, but not of C
        assertEquals(ImmutableSet.of(fileA, fileB), results.keySet());

        // reference to function b should be found now, other errors still the same
        assertThat(results.get(fileA), new IsNot<>(containsString("Reference to function b could not be resolved")));
        assertThat(results.get(fileA), containsString("Reference to function c could not be resolved"));
        assertThat(results.get(fileB), containsString("Reference to function c could not be resolved"));

        // no we fix package C:
        String packageC_v2 = packageC_v1.replace("c_old", "c");
        results.clear();
        changes = manager.syncCompilationUnitContent(fileC, packageC_v2);
        manager.reconcile(changes);

        // the change of B should trigger rechecks of A, B, and C
        assertEquals(ImmutableSet.of(fileA, fileB, fileC), results.keySet());
        // there should be no more errors
        assertEquals("", results.get(fileA));
        assertEquals("", results.get(fileB));
        assertEquals("", results.get(fileC));


    }

    @Test
    public void movingFiles() throws IOException { // #712
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);
        File subFolderX = new File(wurstFolder, "x");
        newCleanFolder(subFolderX);
        File subFolderY = new File(wurstFolder, "y");
        newCleanFolder(subFolderY);


        String packageA = string(
            "package A",
            "import Test",
            "init",
            "    foo()"
        );


        String packageTest = string(
            "package Test",
            "public function foo()"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileTest1 = WFile.create(new File(subFolderX, "Test.wurst"));
        WFile fileTest2 = WFile.create(new File(subFolderY, "Test.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageA);
        writeFile(fileTest1, packageTest);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> results = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();


        {
            // check that jump to decl in package A works correctly
            CompilationUnit cu = manager.getCompilationUnit(fileA);
            FunctionCall functionCallFoo = (FunctionCall) Utils.getAstElementAtPos(cu, 4, 5, false).get();
            FunctionDefinition def = functionCallFoo.attrFuncDef();
            assertEquals(WFile.create(def.attrSource().getFile()), fileTest1);
        }

        // no move the file to folder y
        fileTest1.getFile().delete();
        writeFile(fileTest2, packageTest);
        manager.removeCompilationUnit(fileTest1);
        manager.syncCompilationUnit(fileTest2);


        {
            // check that jump to decl in package A works correctly after the change
            CompilationUnit cu = manager.getCompilationUnit(fileA);
            FunctionCall functionCallFoo = (FunctionCall) Utils.getAstElementAtPos(cu, 4, 5, false).get();
            FunctionDefinition def = functionCallFoo.attrFuncDef();
            assertNotNull(def);
            assertEquals(WFile.create(def.attrSource().getFile()), fileTest2);
        }

    }


    @Test
    public void renamePackage() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageA = string(
            "package A",
            "import Test",
            "init",
            "    foo()"
        );


        String packageTest = string(
            "package Test",
            "public function foo()"
        );

        String packageTest2 = string(
            "package Testttt",
            "public function foo()"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "A.wurst"));
        WFile fileTest = WFile.create(new File(wurstFolder, "Test.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageA);
        writeFile(fileTest, packageTest);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileA), "");


        // no move the file to folder y
        writeFile(fileTest, packageTest2);
        manager.syncCompilationUnit(fileTest);

        assertThat(errors.get(fileA), CoreMatchers.containsString("The import 'Test' could not be resolved."));


    }

    @NotNull
    private Map<WFile, String> keepErrorsInMap(ModelManagerImpl manager) {
        // keep error messages in a map:
        Map<WFile, String> results = new HashMap<>();
        manager.onCompilationResult((PublishDiagnosticsParams res) -> {

            String errors = res.getDiagnostics().stream()
                .map(e -> e.toString())
                .collect(Collectors.joining("\n"));

            results.put(WFile.create(res.getUri()), errors);

            for (Diagnostic err : res.getDiagnostics()) {
                System.out.println("   " + err.getSeverity() + " in " + res.getUri() + ", line " + err.getRange().getStart().getLine() + "\n     " + err.getMessage());
            }
        });
        return results;
    }

    private void newCleanFolder(File f) throws IOException {
        FileUtils.deleteRecursively(f);
        Files.createDirectories(f.toPath());
    }

    private void writeFile(WFile f, String content) throws IOException {
        FileUtils.write(content, f.getFile());
    }


    private String string(String... lines) {
        return Utils.join(lines, "\n");
    }


    @Test
    public void changeModule() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "init",
            "    new B()",
            "class B",
            "    use A"
        );


        String packageT2 = string(
            "package T2",
            "int x = 5",
            "public module A",
            "    construct()",
            "        x = 6"
        );

        String packageT2updated = string(
            "package T2",
            "int x = 5",
            "public module A",
            "    construct()",
            "        x = 7"
        );

        WFile fileA = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileTest = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileA, packageT1);
        writeFile(fileTest, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileA), "");


        // now, update the module in package T2
        writeFile(fileTest, packageT2updated);
        manager.syncCompilationUnit(fileTest);

        assertEquals(errors.get(fileA), "");
        assertEquals(errors.get(fileTest), "");

        WurstModel model = manager.getModel();
        assertNotNull(model);

        // check that module instantiation contains updated constructor with "x = 7"
        model.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ClassDef c) {
                for (ModuleInstanciation mi : c.getModuleInstanciations()) {
                    String s = Utils.prettyPrint(mi.getConstructors());
                    assertThat(s, CoreMatchers.containsString("x = 7"));
                }
            }
        });

    }

    @Test
    public void changeModuleAbstractMethod() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "class CustomHero",
            "    use CustomHeroXpModule",
            "    int xp",
            "    override function getXp() returns int",
            "        return this.xp"
        );


        String packageT2 = string(
            "package T2",
            "public module CustomHeroXpModule",
            "    abstract function getXp() returns int"
        );

        String packageT2updated = string(
            "package T2",
            "public module CustomHeroXpModule",
            "    abstract function getXpOld() returns int"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertEquals(errors.get(fileT1), "");


        // now, update the module in package T2
        writeFile(fileT2, packageT2updated);
        manager.syncCompilationUnit(fileT2);

        // now, there should be errors in class CustomHero in fileA
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Non-abstract class CustomHero must implement the following functions:\\n    function getXpOld() returns int"));
        assertEquals(errors.get(fileT2), "");
    }


    @Test
    public void moduleErrorAtInit() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "class C",
            "    use M",
            "init",
            "    new C.foo()"
        );


        String packageT2 = string(
            "package T2",
            "public module Moo",
            "    function foo() returns int",
            "        return 1"
        );

        String packageT2updated = string(
            "package T2",
            "public module M",
            "    function foo() returns int",
            "        return 1"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Could not find type M."));
        assertEquals(errors.get(fileT2), "");


        // now, update the module in package T2
        writeFile(fileT2, packageT2updated);
        manager.syncCompilationUnit(fileT2);

        // now, the errors should be resolved
        assertEquals(errors.get(fileT1), "");
        assertEquals(errors.get(fileT2), "");
    }

    @Test
    public void moduleTransitive() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "import T2",
            "init",
            "    new C.foo()"
        );

        String packageT2 = string(
            "package T2",
            "import T3",
            "public class C extends D"
        );

        String packageT3 = string(
            "package T3",
            "import T4",
            "public class D",
            "    use M"
        );

        String packageT4 = string(
            "package T4",
            "public module M",
            "    function foo() returns int",
            "        return 1"
        );

        String packageT4updated = string(
            "package T4",
            "public module M",
            "    function bar() returns int",
            "        return 1"
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileT2 = WFile.create(new File(wurstFolder, "T2.wurst"));
        WFile fileT3 = WFile.create(new File(wurstFolder, "T3.wurst"));
        WFile fileT4 = WFile.create(new File(wurstFolder, "T4.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileT2, packageT2);
        writeFile(fileT3, packageT3);
        writeFile(fileT4, packageT4);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        // no errors expected
        assertEquals(errors.get(fileT1), "");
        assertEquals(errors.get(fileT2), "");
        assertEquals(errors.get(fileT3), "");
        assertEquals(errors.get(fileT4), "");


        // now, update the module in package T4
        writeFile(fileT4, packageT4updated);
        manager.syncCompilationUnit(fileT4);

        // now, the function foo in package T1 can no longer be called
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Could not find function foo"));
    }

    @Test
    public void keepTypeErrorsWhileEditing() throws IOException {
        File projectFolder = new File("./temp/testProject2/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);


        String packageT1 = string(
            "package T1",
            "init",
            "    foo()"
        );

        String packageT1updated = string(
            "package T1",
            "init",
            "    foo()",
            "    if ("
        );

        WFile fileT1 = WFile.create(new File(wurstFolder, "T1.wurst"));
        WFile fileWurst = WFile.create(new File(wurstFolder, "Wurst.wurst"));


        writeFile(fileT1, packageT1);
        writeFile(fileWurst, "package Wurst\n");


        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> errors = keepErrorsInMap(manager);


        // first build the project
        manager.buildProject();
        // should show the type error
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Reference to function foo could not be resolved."));


        // now, update package T1 and introduce a syntax error
        ModelManager.Changes changes = manager.syncCompilationUnitContent(fileT1, packageT1updated);
        manager.reconcile(changes);

        // now the errors should contain the syntax error and the type error
        assertThat(errors.get(fileT1), CoreMatchers.containsString("Reference to function foo could not be resolved."));
        assertThat(errors.get(fileT1), CoreMatchers.containsString("extraneous input '(' expecting NL"));
    }

    @Test
    public void runmapLikePipeline_cacheRegression_closerToRunMap() throws Exception {
        File projectFolder = new File("./temp/testProject_runmap_like_real/");
        File wurstFolder = new File(projectFolder, "wurst");
        newCleanFolder(wurstFolder);

        // --- Minimal LinkedList-style module ---
        String pkgLinkedList = string(
            "package LinkedListModule",
            "public module LinkedListModule",
            "    static function iterator() returns Iterator",
            "        return new Iterator()",
            "    static class Iterator",
            "        LinkedListModule.thistype current = null",
            "        function hasNext() returns boolean",
            "            return false",
            "        function next() returns LinkedListModule.thistype",
            "            return current",
            "        function close()",
            "            destroy this"
        );

        // --- Minimal string iterator stub to create competing iterator symbol ---
        String pkgStrIter = string(
            "package StrIter",
            "public function string.iterator() returns StringIterator",
            "    return new StringIterator(this)",
            "public class StringIterator",
            "    string str",
            "    construct(string s)",
            "        this.str = s",
            "    function hasNext() returns boolean",
            "        return false",
            "    function next() returns string",
            "        return \"\"",
            "    function close()",
            "        destroy this"
        );

        // --- Reproducer using both packages ---
        String pkgHello = string(
            "package Hello",
            "import LinkedListModule",
            "import StrIter",
            "",
            "class A",
            "    use LinkedListModule",
            "",
            "init",
            "    let itr = A.iterator()",
            "    while itr.hasNext()",
            "        let a = itr.next()",
            "        destroy a",
            "    itr.close()",
            "    let s = \"hello\".iterator()",
            "    while s.hasNext()",
            "        BJDebugMsg(s.next())",
            "    s.close()"
        );

        // A tiny JASS file so purge logic and JASS transform path look more like RunMap’s environment:
        String jass = "globals\nendglobals\n// war3map.j placeholder";

        WFile fileLinkedList = WFile.create(new File(wurstFolder, "LinkedListModule.wurst"));
        WFile fileStrIter    = WFile.create(new File(wurstFolder, "StrIter.wurst"));
        WFile fileHello      = WFile.create(new File(wurstFolder, "Hello.wurst"));
        WFile fileWurst      = WFile.create(new File(wurstFolder, "Wurst.wurst"));
        WFile fileWar3mapJ   = WFile.create(new File(wurstFolder, "war3map.j"));

        writeFile(fileLinkedList, pkgLinkedList);
        writeFile(fileStrIter,    pkgStrIter);
        writeFile(fileHello,      pkgHello);
        writeFile(fileWurst,      "package Wurst\n");
        writeFile(fileWar3mapJ,   jass);

        ModelManagerImpl manager = new ModelManagerImpl(projectFolder, new BufferManager());
        Map<WFile, String> diags = keepErrorsInMap(manager);

        // Baseline build (matches "no IDE errors")
        manager.buildProject();
        assertEquals(diags.get(fileHello), "", "initial build must be clean");

        // VSCode-like reconcile touch:
        ModelManager.Changes changes = manager.syncCompilationUnitContent(fileHello, pkgHello + "\n// touch");
        manager.reconcile(changes);
        assertEquals(diags.get(fileHello), "", "reconcile after harmless edit must be clean");

        // --- First RunMap-like compile on SAME model ---
        touchWar3mapJ(manager, fileWar3mapJ, " // pass 1");
        runRunmapLikeCompile_Closer(projectFolder, manager);
        assertLocalAIsClassA(manager.getCompilationUnit(fileHello));

        // --- Second RunMap-like compile (caches should not corrupt resolution) ---
        touchWar3mapJ(manager, fileWar3mapJ, " // pass 2");
        runRunmapLikeCompile_Closer(projectFolder, manager);
        assertLocalAIsClassA(manager.getCompilationUnit(fileHello));
    }


    /** Simulate RunMap.replaceBaseScriptWithConfig(..): rewrite war3map.j inside the SAME model. */
    private void touchWar3mapJ(ModelManagerImpl manager, WFile war3map, String suffix) throws IOException {
        String content = "globals\nendglobals\n// war3map.j placeholder" + suffix + "\n";
        manager.syncCompilationUnitContent(war3map, content);
    }

    /** Run a RunMap-like compile on the SAME model: purge imports, check → IM, then JASS transform. */
    private void runRunmapLikeCompile_Closer(File projectFolder, ModelManagerImpl manager) {
        WurstGui gui = new WurstGuiLogger();
        TimeTaker time = new TimeTaker.Default();
        WurstCompilerJassImpl compiler =
            new WurstCompilerJassImpl(time, projectFolder, gui, null, new RunArgs(java.util.Collections.emptyList()));

        WurstModel model = manager.getModel();

        // 2) Check program
        gui.sendProgress("Check program");
        compiler.checkProg(model);
        if (gui.getErrorCount() > 0) {
            throw new AssertionError("RunMap-like checkProg reported errors: " + gui.getErrorList());
        }

        // 3) Translate to IM
        compiler.translateProgToIm(model);
        if (gui.getErrorCount() > 0) {
            throw new AssertionError("RunMap-like translateProgToIm reported errors: " + gui.getErrorList());
        }

        // 4) (Optional but closer to reality) Transform to JASS
        compiler.transformProgToJass();
        // We don't run PJass here; just exercising additional phases & caches.
    }

    /** Same as MapRequest.purgeUnimportedFiles: keep wurst-folder CUs and any imported transitively, plus .j files. */
    private void purgeUnimportedFiles_likeRunMap(WurstModel model, ModelManagerImpl manager) {
        // Seed: files inside project root (wurst folder) OR .j files.
        java.util.Set<CompilationUnit> keep = model.stream()
            .filter(cu -> isInWurstFolder_likeRunMap(cu.getCuInfo().getFile(), manager) || cu.getCuInfo().getFile().endsWith(".j"))
            .collect(java.util.stream.Collectors.toSet());

        // Recursively add imported packages’ CUs (uses attrImportedPackage like RunMap)
        addImports_likeRunMap(keep, keep);
        model.removeIf(cu -> !keep.contains(cu));
    }

    private boolean isInWurstFolder_likeRunMap(String file, ModelManagerImpl manager) {
        java.nio.file.Path p = java.nio.file.Paths.get(file);
        java.nio.file.Path w = manager.getProjectPath().toPath(); // project root
        return p.startsWith(w)
            && java.nio.file.Files.exists(p)
            && Utils.isWurstFile(file);
    }

    private void addImports_likeRunMap(java.util.Set<CompilationUnit> result, java.util.Set<CompilationUnit> toAdd) {
        java.util.Set<CompilationUnit> imported =
            toAdd.stream()
                .flatMap(cu -> cu.getPackages().stream())
                .flatMap(p -> p.getImports().stream())
                .map(WImport::attrImportedPackage)
                .filter(java.util.Objects::nonNull)
                .map(WPackage::attrCompilationUnit)
                .collect(java.util.stream.Collectors.toSet());
        boolean changed = result.addAll(imported);
        if (changed) addImports_likeRunMap(result, imported);
    }

    /** Assert that 'let a = itr.next()' has type A and that next() returns A. */
    private void assertLocalAIsClassA(CompilationUnit cu) {
        final AtomicBoolean checked = new AtomicBoolean(false);
        final AtomicBoolean didFindA = new AtomicBoolean(false);
        final AtomicBoolean didFindString = new AtomicBoolean(false);

        cu.accept(new Element.DefaultVisitor() {
            @Override public void visit(LocalVarDef l) {
                if (!"a".equals(l.getNameId().getName())) { super.visit(l); return; }

                VarInitialization init = l.getInitialExpr();
                if (init == null) throw new AssertionError("local 'a' has no initializer");

                if (init instanceof ExprMemberMethod) {
                    WurstType t = ((ExprMemberMethod) init).attrTyp();
                    if (t instanceof WurstTypeClass) {
                        String cname = ((WurstTypeClass) t).getClassDef().getName();
                        assertEquals(cname, "A", "Expected local 'a' to be of class A");
                    } else {
                        throw new AssertionError("Expected class type for 'a', but got: " + t);
                    }
                }

                checked.set(true);
                super.visit(l);
            }

            @Override public void visit(ExprMemberMethodDot call) {
                if ("next".equals(call.getFuncName())) {
                    WurstType rt = call.attrTyp();
                    if (rt instanceof WurstTypeClass) {
                        String cname = ((WurstTypeClass) rt).getClassDef().getName();
                        assertEquals(cname, "A", "next() must return A");
                        didFindA.set(true);
                    } else if (rt instanceof WurstTypeString) {
                        String cname = ((WurstTypeString) rt).getName();
                        assertEquals(cname, "string", "next() must return string");
                        didFindString.set(true);
                    } else {
                        throw new AssertionError("next() must return class A or string, but was: " + rt);
                    }
                }
                super.visit(call);
            }
        });

        if (!checked.get()) throw new AssertionError("Did not find local var 'a' to assert.");
        if (!didFindA.get()) throw new AssertionError("Did not find call to next() returning class A.");
        if (!didFindString.get()) throw new AssertionError("Did not find call to next() returning string.");
    }



}
