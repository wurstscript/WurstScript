package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class ModuleTests extends WurstScriptTest {

    private static final String TEST_DIR = "./testscripts/valid/modules/";


    @Test
    public void simple() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "simple.wurst"), true);
    }

    @Test
    public void multi1() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "multi.wurst"), true);
    }

    @Test
    public void multi2() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "multi2.wurst"), true);
    }

    @Test
    public void override() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "override.wurst"), true);
    }

    @Test
    public void override2() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "override2.wurst"), true);
    }

    @Test
    public void diamond1() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "diamond.wurst"), true);
    }

    @Test
    public void diamond2() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "diamond2.wurst"), true);
    }

    @Test
    public void initdestroy() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "initdestroy.wurst"), true);
    }


    @Test
    public void modules_conflict() {
        testAssertErrorsLines(false, "ambiguous",
                "package test",
                "    module A",
                "        function foo() returns int",
                "            return 3",
                "    module B",
                "        function foo() returns int",
                "            return 4",
                "    class C",
                "        use A",
                "        use B",
                "        function test()",
                "            foo()",
                "endpackage"
        );
    }


    @Test
    public void modules_thistype() {
        // each function in a module should be either private or public
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    module A",
                "        function foo() returns thistype",
                "            return this",
                "    class C",
                "        use A",
                "    init",
                "        C c1 = new C()",
                "        C c2 = c1.foo()",
                "        if c1 == c2",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void modules_import() {
        // each function in a module should be either private or public
        testAssertOkLines(true,
                "package Blub",
                "    public module BlubModule",
                "        function foo() returns int",
                "            return 3",
                "endpackage",
                "package test",
                "    import Blub",
                "    native testSuccess()",
                "    class C",
                "        use BlubModule",
                "    init",
                "        C c = new C()",
                "        if c.foo() == 3",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void modules_call_global() {
        // each function in a module should be either private or public
        testAssertOkLines(true,
                "function random takes nothing returns int",
                "    return 3 // totally random, chosen by fair dice roll",
                "endfunction",
                "",
                "package test",
                "    native testSuccess()",
                "    module BlubModule",
                "        function foo() returns int",
                "            return random()",
                "    class C",
                "        use BlubModule",
                "    init",
                "        C c = new C()",
                "        if c.foo() == 3",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void modules_abstract() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    module A",
                "        abstract function foo() returns int",
                "    class C",
                "        use A",
                "        override function foo() returns int",
                "            return 3",
                "",
                "    init",
                "        C c = new C()",
                "        if c.foo() == 3",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void modules_abstract_err() {
        testAssertErrorsLines(false, "must implement",
                "package test",
                "    module A",
                "        abstract function foo() returns int",
                "    class C",
                "        use A",
                "endpackage"
        );
    }

    @Test
    public void modules_missing_override() {
        testAssertErrorsLines(false, "override",
                "package test",
                "    module A",
                "        abstract function foo() returns int",
                "    class C",
                "        use A",
                "        function foo() returns int",
                "            return 3",
                "endpackage"
        );
    }

    @Test
    public void modules_wrong_param_count1() {
        testAssertErrorsLines(false, "parameter",
                "package test",
                "    module A",
                "        function foo()",
                "            bar(3)",
                "        function bar()",
                "",
                "    class C",
                "        use A",
                "        function xyz()",
                "            foo()",
                "endpackage"
        );
    }

    @Test
    public void modules_wrong_param_count2() {
        testAssertErrorsLines(false, "not enough",
                "package test",
                "    module A",
                "        function foo()",
                "            bar(3)",
                "        function bar(int x, int y)",
                "",
                "    class C",
                "        use A",
                "        function xyz()",
                "            foo()",
                "endpackage"
        );
    }

    @Test
    public void modules_call_indirect() {
        testAssertOkLines(false,
                "package test",
                "    native testSuccess()",
                "    module A",
                "        function foo()",
                "            bar()",
                "        function bar()",
                "            testSuccess()",
                "    class C",
                "        use A",
                "        construct()",
                "            foo()",
                "    init",
                "        new C()",
                "endpackage"
        );
    }

    @Test
    public void static_external() {
        testAssertErrorsLines(false, "variable b",
                "package test",
                "    module A",
                "        static int b = 0",
                "    init",
                "        A.b = 0",
                "endpackage"
        );
    }

    @Test
    public void staticmeth_external() {
        testAssertErrorsLines(false, "Could not find function b",
                "package test",
                "    module A",
                "        static function b(int b)",
                "    init",
                "        A.b(0)",
                "endpackage"
        );
    }

    @Test
    public void moduleConstructor() { // see bug #150
        testAssertOkLines(false,
                "package Test",
                "module Test",
                "    construct()",
                "        skip",

                "class A",
                "    construct(int x)",
                "        skip",

                "class B extends A",
                "    use Test",
                "    construct()",
                "        super(3)"
        );
    }

    @Test
    public void overrideStatic() {
        testAssertErrorsLines(false, "override static function",
                "package Test",
                "module Test",
                "    abstract static function foo()",

                "class A",
                "    use Test",
                "    override function foo()",
                "        skip"
        );
    }

    @Test
    public void overrideStaticAndActuallyDoStuff() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "module Test",
            "    abstract static function foo() returns int",
            "    static function bar() returns int",
            "        return foo() * foo()",
            "class A",
            "    use Test",
            "    override static function foo() returns int",
            "        return 3",
            "init",
            "    if A.bar() == 9",
            "        testSuccess()"
        );
    }

    @Test
    public void localInModuleConstructor() {
        testAssertOkLines(false,
                "package Test",
                "module Test",
                "    construct()",
                "        int x = 5",
                "class A",
                "    use Test",
                "    construct()",
                "    construct(int i)"
        );
    }

    @Test
    public void localInModuleConstructor2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "int res = 1", // 1) res == 1
                "module Test",
                "    construct()",
                "        int x = 2",
                "        res += x", // 2) res == 3 // 4) res == 6
                "        int y = 1",
                "        res -= y", // 3) res == 2  // 5) res == 5
                "class A",
                "    use Test",
                "    construct()",
                "        res *= 2", // 3) res == 4
                "    construct(int i)",
                "        res *= i", // 6) res == 15
                "init",
                "    new A()",
                "    new A(3)",
                "    if res == 15",
                "        testSuccess()"
        );
    }


    @Test
    public void modulesInSubclasses() { // bug #390
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "native testFail(string message)",
                "module M",
                "    int i = 0",
                "class A",
                "    use M",
                "    function f()",
                "        i++",
                "        this.i++",
                "class B extends A",
                "    function g()",
                "        i++", // line 13
                "        this.i++",
                "        super.i++",
                "init",
                "    let b = new B()",
                "    A a = b",
                "    a.f()",
                "    b.g()",
                "    if a.i != 5",
                "        testFail(\"foo\")",
                "    testSuccess()");
    }

    @Test
    public void multi_modules() { // bug #542
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public module LinkedListModule",
                "    static function iterator() returns Iterator",
                "        return new Iterator()",
                "",
                "    static class Iterator",
                "",
                "        function hasNext() returns boolean",
                "            return false",
                "",
                "        function next() returns LinkedListModule.thistype",
                "            return null",
                "",
                "        function reset()",
                "",
                "        function close()",
                "",
                "public class MyClass",
                "    use MyModule",
                "",
                "",
                "    static function all()",
                "        for elem in MyClass",
                "            elem.do()",
                "",
                "    function do()",
                "",
                "public module MyModule",
                "    use LinkedListModule",
                "",
                "init",
                "    new MyClass",
                "    new MyClass",
                "    MyClass.all()",
                "    testSuccess()"
        );
    }

    @Test
    public void multi_modules2() { // bug #542
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public module LinkedListModule",
                "    static function iterator() returns Iterator",
                "        return new Iterator()",
                "",
                "    static class Iterator",
                "",
                "        function hasNext() returns boolean",
                "            return false",
                "",
                "public class MyClass",
                "    use MyModule",
                "",
                "public module MyModule",
                "    use LinkedListModule",
                "",
                "init",
                "    MyClass.iterator().hasNext()",
                "    testSuccess()"
        );
    }

    @Test
    public void nested_module_init() { // bug #542
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public module A",
                "    int x = 3",
                "    construct()",
                "        x = x - 1",
                "public module B",
                "    use A",
                "public class C",
                "    use B",
                "",
                "init",
                "    let c = new C()",
                "    if c.x == 2",
                "        testSuccess()"
        );
    }

    @Test
    public void nested_class_module() { // bug #542
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "abstract class C",
                "    abstract function foo()",
                "public module A",
                "    static class D extends C",
                "        override function foo()",
                "            testSuccess()",
                "    function test()",
                "        new D().foo()",
                "class E",
                "    use A",
                "init",
                "    let e = new E()",
                "    e.test()"
        );
    }

    @Test
    public void multiple_constructors() {
        testAssertErrorsLines(false, "Duplicate constructor",
                "package Test",
                "native testSuccess()",
                "public module A",
                "    int x = 3",
                "    construct()",
                "        x = x - 1",
                "    construct()",
                "        x = x + 1"
        );
    }

    @Test
    public void arg_constructor() {
        testAssertErrorsLines(false, "Module constructors must not have parameters",
                "package Test",
                "native testSuccess()",
                "public module A",
                "    int x = 3",
                "    construct(int i)",
                "        x = x - 1"
        );
    }

    @Test
    public void testModuleMemberInit() { // see #656
        testAssertOkLinesWithStdLib(true,
                "package Test",
                "import LinkedListModule",
                "public class TestClass",
                "    use LinkedListModule",
                "    construct()",
                "        print(\"constructed\")",
                "init",
                "    new TestClass()",
                "    print(\"size test:\" + TestClass.size.toString())",
                "endpackage",
                "package Test2",
                "import Test",
                "init",
                "    print(\"size test2:\" + TestClass.size.toString())",
                "    if TestClass.size == 1",
                "        print(\"size test3:\" + TestClass.size.toString())",
                "        testSuccess()",
                "        print(\"size test4:\" + TestClass.size.toString())",
                "endpackage"
        );
    }


    @Test
    public void stupidTest() { // see #656
        testAssertOkLinesWithStdLib(true,
                "package Test",
                "init",
                "    testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testModuleMemberInit2() { // see #656
        testAssertOkLinesWithStdLib(true,
                "package Test",
                "import LinkedListModule",
                "public class TestClass",
                "    use LinkedListModule",
                "    static TestClass a = new TestClass",
                "    construct()",
                "        print(\"constructed\")",
                "init",
                "    print(\"size test2:\" + TestClass.size.toString())",
                "    if TestClass.size == 1",
                "        testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void useModuleAsType() { // see #720
        testAssertErrorsLines(false, "Cannot use module type A in this context.",
                "package Test",
                "module A",
                "    function foo()",
                "class List<T>",
                "init",
                "    let l = new List<A>",
                "endpackage"
        );
    }

    @Test
    public void subclassModuleOnDestroy() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "module OnDestroy",
            "    ondestroy",
            "        testSuccess()",
            "public abstract class A",
            "public class B extends A",
            "    use OnDestroy",
            "init",
            "    A b = new B()",
            "    destroy b"
            );
    }
}
