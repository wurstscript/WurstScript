package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class GenericsModuleTests extends WurstScriptTest {


    @Test
    public void simpleGenericModule() {
        testAssertOkLines(false,
                "type unit extends handle",
                "package test",
                "    native testSuccess()",
                "    module M<T>",
                "        T t",
                "    class C",
                "        use M<unit>",
                "    init",
                "        C c = new C",
                "        unit u = null",
                "        c.t = u",
                "endpackage"
        );
    }

    @Test
    public void biggerModule() {
        testAssertOkLines(false,
                "type unit extends handle",
                "package test",
                "    native testSuccess()",
                "    module M<S,T>",
                "        private S a",
                "        private T b",
                "        function set(S a, T b) returns T",
                "            T temp = b",
                "            this.a = a",
                "            this.b = b",
                "            return temp",
                "    class C",
                "        use M<unit,real>",
                "    init",
                "        C c = new C",
                "        unit u = null",
                "        real r = c.set(u,1.23)",
                "endpackage"
        );
    }

    // Regression: FuncLink.create() was seeding the mapping with ALL visible type params
    // (including enclosing module/class type params), causing them to appear as unbound
    // inference variables on generic function calls. Only the function's own type params
    // should be seeded; module type params are resolved via receiver type matching.
    @Test
    public void genericModuleFunctionWithOwnTypeParam() {
        testAssertOkLines(false,
                "package test",
                "    native testSuccess()",
                "    module M<T>",
                "        T value",
                "        function map<S>(S s) returns S",
                "            return s",
                "    class C",
                "        use M<int>",
                "    init",
                "        C c = new C",
                "        string r = c.map(\"hello\")",
                "        if r == \"hello\"",
                "            testSuccess()",
                "endpackage"
        );
    }

    // Regression: FuncLink.withTypeArgBinding() only detected structural type changes,
    // missing the case where a type param is bound to itself (e.g. T→T in a generic class
    // using a generic module). The FuncLink was not updated, leaving stale type params.
    @Test
    public void genericModuleInGenericClassGet() {
        testAssertOkLines(false,
                "package test",
                "    native testSuccess()",
                "    module M<T>",
                "        T value",
                "        function get() returns T",
                "            return value",
                "    class C<S>",
                "        use M<S>",
                "    init",
                "        C<int> c = new C<int>",
                "        c.value = 42",
                "        int v = c.get()",
                "        if v == 42",
                "            testSuccess()",
                "endpackage"
        );
    }

    // Regression: static generic function in a generic module caused NPE due to
    // duplicate matchAgainstSupertype call passing potentially-null mapping to the
    // second call. FunctionSignature.fromNameLink also incorrectly added enclosing
    // module type params as inference variables.
    @Test
    public void staticGenericFunctionInGenericModule() {
        testAssertOkLines(false,
                "package test",
                "    native testSuccess()",
                "    module M<T>",
                "        static function wrap<S>(S s) returns S",
                "            return s",
                "    class C",
                "        use M<int>",
                "    init",
                "        int x = C.wrap(99)",
                "        if x == 99",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void genericInception() {
        testAssertOkLines(false,
                "type unit extends handle",
                "package test",
                "    native testSuccess()",
                "    module M<T>",
                "        T t",
                "    class C<S>",
                "        use M<S>",
                "    class D",
                "    init",
                "        C<D> c = new C<D>",
                "        c.t = new D",
                "endpackage"
        );
    }

}
