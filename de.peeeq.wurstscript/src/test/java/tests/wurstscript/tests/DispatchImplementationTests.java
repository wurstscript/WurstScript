package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.LuaDispatchPreparation;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

public class DispatchImplementationTests extends WurstScriptTest {
    @Test
    public void moduleOverridesKeepTheirGenericRootAlias() {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs().with("-lua"));
        WurstModel model = parseFiles(Collections.emptyList(), List.of(new CU("ModuleDispatch.wurst",
            String.join("\n", moduleOverrideSource()))), false, compiler);
        compiler.checkProg(model);
        assertTrue(gui.getErrorList().isEmpty(), gui.getErrorList().toString());
        compiler.translateProgToIm(model);
        var classes = compiler.getImProg().getClasses();
        var rootClass = classes.stream().filter(c -> c.getName().equals("Root")).findFirst().orElseThrow();
        var root = rootClass.getMethods().stream()
            .filter(m -> m.getImplementation().getParameters().size() == 2
                && m.getImplementation().getParameters().get(1).getType()
                    instanceof ImTypeVarRef)
            .findFirst().orElseThrow();
        var stringRoot = rootClass.getMethods().stream()
            .filter(m -> m != root && m.getImplementation().getParameters().size() == 2)
            .findFirst().orElseThrow();
        LuaDispatchPreparation.prepare(compiler.getImProg(), compiler.getImTranslator());
        for (String name : List.of("Child", "Grandchild")) {
            var clazz = classes.stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
            var method = clazz.getMethods().stream()
                .filter(m -> m.getImplementation().getParameters().size() == 2).findFirst().orElseThrow();
            assertTrue(method.getLuaMethodDispatchAliases().contains(root.getName()),
                name + " must expose the overridden generic root slot");
            assertFalse(method.getLuaMethodDispatchAliases().contains(stringRoot.getName()),
                name + " must not claim the unrelated string overload");
        }
    }

    private static String[] moduleOverrideSource() {
        return new String[]{
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "interface Root<T:>",
            "    function value(T input) returns int",
            "    function value(string input) returns int",
            "module Impl",
            "    function value(int input) returns int",
            "        return 42",
            "class Parent<U:> implements Root<U>",
            "    override function value(U input) returns int",
            "        return 10",
            "    override function value(string input) returns int",
            "        return 20",
            "class Child extends Parent<int>",
            "    use Impl",
            "class Grandchild extends Child",
            "    override function value(int input) returns int",
            "        return 43",
            "init",
            "    Root<int> a = new Child()",
            "    Root<int> b = new Grandchild()",
            "    if a.value(1) != 42",
            "        testFail(\"module override lost\")",
            "    if b.value(1) != 43",
            "        testFail(\"grandchild override lost\")",
            "    if b.value(\"x\") != 20",
            "        testFail(\"string overload changed\")",
            "    testSuccess()"};
    }

    @Test
    public void moduleOverridesThroughGenericInterfaceJass() {
        test().executeProg().lines(moduleOverrideSource());
    }

    @Test
    public void moduleOverridesThroughGenericInterfaceLua() {
        test().testLua(true).executeProg().lines(moduleOverrideSource());
    }

    @Test
    public void optimizedModuleOverridesThroughGenericInterfaceLua() {
        test().testLua(true).inline().localOptimizations().stacktraces().executeProg().lines(moduleOverrideSource());
    }

    static String[] moduleStateSource(boolean oldGenerics) {
        String parameter = oldGenerics ? "T" : "T:";
        List<String> lines = new ArrayList<>(List.of(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "class Owner",
            "constant earlyState = new FirstState()",
            "abstract class State<" + parameter + ">",
            "    abstract function update(T owner) returns int",
            "    abstract function update(string label) returns int",
            "class NoOpState<" + parameter + "> extends State<T>",
            "    override function update(T owner) returns int",
            "        return 0",
            "    override function update(string label) returns int",
            "        return 100",
            "class FSM<" + parameter + ">",
            "    State<T> current",
            "    construct(State<T> current)",
            "        this.current = current",
            "    @noinline function update(T owner) returns int",
            "        return current.update(owner)",
            "    @noinline function label() returns int",
            "        return current.update(\"label\")"));
        String[] names = {"First", "Second", "Third", "Fourth"};
        for (int i = 0; i < names.length; i++) {
            lines.addAll(List.of(
                "module " + names[i] + "Update",
                "    function update(Owner owner) returns int",
                "        return " + (i + 1),
                "class " + names[i] + "State extends NoOpState<Owner>",
                "    use " + names[i] + "Update"));
        }
        lines.addAll(List.of(
            "class DeepState extends FirstState",
            "    override function update(Owner owner) returns int",
            "        return 5",
            "class InheritsState extends FirstState",
            "init",
            "    let owner = new Owner()",
            "    let fsm = new FSM<Owner>(earlyState)",
            "    int sum = fsm.update(owner)",
            "    fsm.current = new SecondState()",
            "    sum += fsm.update(owner)",
            "    fsm.current = new ThirdState()",
            "    sum += fsm.update(owner)",
            "    fsm.current = new FourthState()",
            "    sum += fsm.update(owner)",
            "    fsm.current = new DeepState()",
            "    sum += fsm.update(owner)",
            "    fsm.current = new InheritsState()",
            "    sum += fsm.update(owner)",
            "    if sum != 16 or fsm.label() != 100",
            "        testFail(\"module state dispatch or overload lost\")",
            "    testSuccess()"));
        return lines.toArray(String[]::new);
    }

    private static void assertModuleStateBindings(String compiled) {
        var body = Pattern.compile("function\\s+FSM_\\w*update\\([^\\n]*\\)\\s*\\n([\\s\\S]*?)\\nend")
            .matcher(compiled);
        assertTrue(body.find(), "FSM.update must remain available for checking its actual dispatch slot");
        var call = Pattern.compile("__wurst_objectClass(?:_local\\d*)?\\[[^\\n]*?\\]\\.([A-Za-z0-9_]+)\\(")
            .matcher(body.group(1));
        assertTrue(call.find(), "FSM.update must dispatch through State's slot");
        String slot = call.group(1);
        for (String name : List.of("First", "Second", "Third", "Fourth", "Deep", "Inherits")) {
            var binding = Pattern.compile("(?m)^\\s*" + name + "State\\." + Pattern.quote(slot)
                + "\\s*=\\s*([A-Za-z0-9_]+)\\s*$").matcher(compiled);
            assertTrue(binding.find(), name + "State must bind the actual root slot " + slot);
            String owner = name.equals("Inherits") ? "First" : name;
            assertTrue(binding.group(1).contains(owner + "State_"),
                name + "State must bind its most-specific implementation: " + binding.group(1));
            assertFalse(binding.group(1).contains("NoOpState"), "the base implementation must not claim " + slot);
        }
    }

    @Test
    public void moduleStateSiblingsBindTheRootSlotOnBothTargets() throws IOException {
        test().testLua(true).luaOnly(false).executeProg().lines(moduleStateSource(false));
        assertModuleStateBindings(Files.readString(Path.of(
            "test-output/lua/DispatchImplementationTests_moduleStateSiblingsBindTheRootSlotOnBothTargets.lua"),
            StandardCharsets.UTF_8));
    }

    @Test
    public void oldGenericModuleStateSiblingsKeepTheirDispatchOnBothTargets() throws IOException {
        test().testLua(true).luaOnly(false).executeProg().lines(moduleStateSource(true));
        assertModuleStateBindings(Files.readString(Path.of(
            "test-output/lua/DispatchImplementationTests_oldGenericModuleStateSiblingsKeepTheirDispatchOnBothTargets.lua"),
            StandardCharsets.UTF_8));
    }

    @Test
    public void genericInterfaceImplementationsUseTheMostSpecificDeclaration() {
        String source = String.join("\n",
            "package Test",
            "interface Root<T:>",
            "    function value(T input) returns int",
            "class Parent<U:> implements Root<U>",
            "    override function value(U input) returns int",
            "        return 42",
            "class Child extends Parent<int>",
            "    override function value(int input) returns int",
            "        return 43",
            "class Inherits extends Parent<int>");
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        WurstModel model = parseFiles(Collections.emptyList(), List.of(new CU("Dispatch.wurst", source)), false, compiler);
        compiler.checkProg(model);
        assertTrue(gui.getErrorList().isEmpty(), gui.getErrorList().toString());
        List<ClassDef> classes = new ArrayList<>();
        List<FuncDef> methods = new ArrayList<>();
        model.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ClassDef c) {
                super.visit(c);
                classes.add(c);
            }

            @Override
            public void visit(FuncDef f) {
                super.visit(f);
                methods.add(f);
            }
        });
        FuncDef root = methods.stream().filter(f -> f.attrNearestClassDef() == null).findFirst().orElseThrow();
        ImTranslator translator = new ImTranslator(model, false, new RunArgs());
        var implementations = translator.getClassesWithImplementation(classes, root);
        assertEquals(implementations.size(), 3);
        for (ClassDef c : classes) {
            String owner = c.getName().equals("Child") ? "Child" : "Parent";
            assertEquals(implementations.get(c).attrNearestClassDef().getName(), owner);
        }
    }

    @Test
    public void boundGenericOverloadsKeepTheirDispatch() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Root<T:>",
            "    function value(T input) returns int",
            "    function value(string input) returns int",
            "class Parent<U:> implements Root<U>",
            "    override function value(U input) returns int",
            "        return 10",
            "    override function value(string input) returns int",
            "        return 20",
            "class Child extends Parent<int>",
            "    override function value(int input) returns int",
            "        return 42",
            "class Grandchild extends Child",
            "    override function value(int input) returns int",
            "        return 43",
            "init",
            "    Root<int> a = new Child()",
            "    Root<int> b = new Grandchild()",
            "    if a.value(1) == 42 and b.value(1) == 43 and b.value(\"x\") == 20",
            "        testSuccess()");
    }
}
