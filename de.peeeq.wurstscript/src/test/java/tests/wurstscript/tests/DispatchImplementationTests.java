package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class DispatchImplementationTests extends WurstScriptTest {
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
