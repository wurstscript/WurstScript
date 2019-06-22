package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class ClassesExtTests extends WurstScriptTest {


    @Test
    public void extends_simple() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        function foo() returns int",
                "            return 3",
                "    class D extends C",
                "    init",
                "        if new D().foo() == 3",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void extends_override() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        function foo() returns int",
                "            return 3",
                "    class D extends C",
                "        override function foo() returns int",
                "            return 4",
                "    init",
                "        if new D().foo() == 4",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void extends_override2() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        function bar() returns int",
                "            return foo()",
                "        function foo() returns int",
                "            return 3",
                "    class D extends C",
                "        override function foo() returns int",
                "            return 4",
                "    init",
                "        if new D().bar() == 4",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void extends_override3() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        function foo() returns int",
                "            return 3",
                "    class D extends C",
                "    class E extends D",
                "        override function foo() returns int",
                "            return 4",
                "    init",
                "        D e = new E()",
                "        if e.foo() == 4",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void extends_override4() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        function foo() returns int",
                "            return 3",
                "    class D extends C",
                "        function bla() returns int",
                "            return foo()",
                "    class E extends D",
                "        override function foo() returns int",
                "            return 4",
                "    init",
                "        D e = new E()",
                "        if e.bla() == 4",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void extends_variables() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        int i = 5",
                "    class D extends C",
                "        function foo() returns int",
                "            return i+1",
                "    init",
                "        if new D().foo() == 6",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void privateVar() {
        testAssertErrorsLines(false, "Could not find variable i",
                "package test",
                "    native testSuccess()",
                "    class C",
                "        private int i = 5",
                "    class D extends C",
                "        function foo() returns int",
                "            return i+1",
                "endpackage"
        );
    }

    @Test
    public void privateFunc() {
        testAssertErrorsLines(false, "not visible",
                "package test",
                "    native testSuccess()",
                "    class C",
                "        private function foo()",
                "    class D extends C",
                "        function bar()",
                "            foo()",
                "endpackage"
        );
    }

    @Test
    public void privateFuncOverride() {
        testAssertOkLines(false,
                "package test",
                "    native testSuccess()",
                "    class C",
                "        private function foo() returns int",
                "            return 3",
                "        function bar() returns int",
                "            return foo() + 1",
                "    class D extends C",
                "        function foo() returns int",
                "            return 10",
                "    init",
                "        if new D().bar() == 4",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void constr1() {
        testAssertErrorsLines(false, "The extended class <Pair> does not expose",
                "package test",
                "    native testSuccess()",
                "    class Pair",
                "        int a",
                "        int b",
                "        construct(int a, int b)",
                "            this.a = a",
                "            this.b = b",
                "    class OtherPair extends Pair",
                "    init",
                "        if new OtherPair().a == 2",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void constr2() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class Pair",
                "        int a",
                "        int b",
                "        construct(int a, int b)",
                "            this.a = a",
                "            this.b = b",
                "    class OtherPair extends Pair",
                "        construct(int a, int b)",
                "            super(a,b)",
                "            skip",
                "    init",
                "        if new OtherPair(2, 3).a == 2",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void constr_super() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class Pair",
                "        int a",
                "        int b",
                "        construct(int a, int b)",
                "            this.a = a",
                "            this.b = b",
                "    class OtherPair extends Pair",
                "        construct(int a, int b)",
                "            super(a*2,b*2)",
                "    init",
                "        if new OtherPair(2, 3).a == 4",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void constr_super_wrong1() {
        testAssertErrorsLines(true, "Expected int",
                "package test",
                "    native testSuccess()",
                "    class Pair",
                "        int a",
                "        int b",
                "        construct(int a, int b)",
                "            this.a = a",
                "            this.b = b",
                "    class OtherPair extends Pair",
                "        construct(int a, int b)",
                "            super(a*2, \"bla\")",
                "    init",
                "        if new OtherPair(2, 3).a == 4",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "        ondestroy",
                "            x += 1",
                "    init",
                "        A a = new A()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void ondestroy_dynamicdispatch() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "        ondestroy",
                "            x += 1",
                "    init",
                "        B a = new A()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void ondestroy_dynamicdispatch2() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "    class X extends A",
                "        ondestroy",
                "            x += 1",
                "    init",
                "        B a = new X()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_dynamicdispatch3() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "        ondestroy",
                "            x += 1",
                "    class X extends A",
                "    init",
                "        B a = new X()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void ondestroy_dynamicdispatch4() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "        ondestroy",
                "            x += 1",
                "    class X extends A",
                "    init",
                "        A a = new X()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_dynamicdispatch5() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            x *= 2",
                "    class A extends B",
                "        ondestroy",
                "            x += 1",
                "    class X extends A",
                "    init",
                "        X a = new X()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_dynamicdispatchFrotty1() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class A extends T",
                "    class B extends T",
                "        ondestroy",
                "            s += \"B\"",
                "    abstract class T",
                "    string s=\"\"",
                "    init",
                "        T t = new A()",
                "        destroy t",
                "        if s == \"\"",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_dynamicdispatchFrotty2() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class A implements T",
                "    class B implements T",
                "        ondestroy",
                "            s += \"B\"",
                "    interface T",
                "        function f()",
                "            skip",
                "    string s=\"\"",
                "    init",
                "        T t = new A()",
                "        destroy t",
                "        if s == \"\"",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_dynamicdispatchFrotty3() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    abstract class C",
                "        ondestroy",
                "            s+=\"C\"",
                "    class A extends C implements T",
                "    class B extends C implements T",
                "        ondestroy",
                "            s += \"B\"",
                "    interface T",
                "        function f()",
                "            skip",
                "    string s=\"\"",
                "    init",
                "        T t = new A()",
                "        destroy t",
                "        if s == \"C\"",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroy_withVar() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        ondestroy",
                "            let y = 2",
                "            x *= y",
                "    class A extends B",
                "        ondestroy",
                "            let z = 1",
                "            x += z",
                "    class X extends A",
                "    init",
                "        X a = new X()",
                "        destroy a",
                "        if x == 6",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void ondestroyUsingThis() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        int y = 2",
                "        ondestroy",
                "            x *= y",
                "    class A extends B",
                "        ondestroy",
                "            x += y",
                "    init",
                "        A a = new A()",
                "        destroy a",
                "        if x == 8",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void superCall() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        int y = 2",
                "        function foo()",
                "            x *= y",
                "    class A extends B",
                "        override function foo()",
                "            x += y",
                "            super.foo()",
                "    init",
                "        A a = new A()",
                "        a.foo()",
                "        if x == 8",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void superCall2() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class B",
                "        int y = 2",
                "        function foo()",
                "            x *= y",
                "    class A extends B",
                "        override function foo()",
                "            x += y",
                "            B.foo()",
                "    init",
                "        A a = new A()",
                "        a.foo()",
                "        if x == 8",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testtest() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    int x = 2",
                "    class A",
                "        int y = 2",
                "        function foo()",
                "            x = 4",
                "    class B extends A",
                "        override function foo()",
                "            x = 5",
                "    class C extends B",
                "        override function foo()",
                "            x = 6",
                "    init",
                "        A a = new B()",
                "        a.foo()",
                "        if x == 5",
                "            testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void teststaticoverride() {
        testAssertErrorsLines(false, "Function foo does not override anything.",
                "package test",
                "    native testSuccess()",
                "    class A",
                "        static function foo()",
                "    class B extends A",
                "        override static function foo()",
                "endpackage"
        );
    }

    @Test
    public void teststaticoverride2() {
        testAssertErrorsLines(false, "Static functions cannot be abstract",
                "package test",
                "    native testSuccess()",
                "    abstract class A",
                "        abstract static function foo()",
                "endpackage"
        );
    }

    @Test
    public void teststaticoverride3() {
        testAssertErrorsLines(false, "Function foo does not override anything.",
                "package test",
                "    native testSuccess()",
                "    class A",
                "        static function foo()",
                "    class B extends A",
                "        override function foo()",
                "endpackage"
        );
    }

    @Test
    public void teststaticoverride4() {
        testAssertErrorsLines(false, "Function foo does not override anything.",
                "package test",
                "    native testSuccess()",
                "    class A",
                "        function foo()",
                "    class B extends A",
                "        override static function foo()",
                "endpackage"
        );
    }

    @Test
    public void testNoDispatch() {
        testAssertOkLines(false,
                "package test",
                "    class A",
                "        function foo(int i, int i2)",
                "    class B extends A",
                "        function foo(int i)",
                "    init",
                "        let a = new A()",
                "        let b = new B()",
                "        a.foo(1,1)",
                "        b.foo(1)",
                "endpackage"
        );
    }

    @Test
    public void testMultiArray() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class A",
                "        int array[5] foo",
                "    init",
                "        let a = new A()",
                "        a.foo[3] = 6",
                "        if a.foo[3] == 6",
                "            testSuccess()",
                "endpackage"
        );
    }

    // TODO @Test
    public void testMultiTuple() {
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    tuple v(int x, int y)",
                "    class A",
                "        v array[5] foo",
                "    init",
                "        let a = new A()",
                "        a.foo[3] = v(3,4)",
                "        if a.foo[3] == v(3,4)",
                "            testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void subTypeGeneric1() { // see #595
        testAssertOkLines(true,
                "package test",
                "    native testSuccess()",
                "    class A<X>",
                "    class B<T> extends A<T>",
                "    function A<int>.bla()",
                "        testSuccess()",
                "    init",
                "        let b = new B<int>",
                "        A<int> a = b",
                "        b.bla()",
                "endpackage"
        );
    }

    @Test
    public void subTypeGeneric2() { // see #595
        testAssertOkLines(false,
                "package test",
                "    class C",
                "    class A<X, Y>",
                "    class B<T> extends A<int, T>",
                "    init",
                "        let b = new B<C>",
                "        A<int, C> a = b",
                "endpackage"
        );
    }

    @Test
    public void subTypeGeneric3() {
        testAssertErrorsLines(false, "Cannot assign B<C> to A<C, integer>",
                "package test",
                "    class C",
                "    class A<X, Y>",
                "    class B<T> extends A<int, T>",
                "    init",
                "        let b = new B<C>",
                "        A<C, int> a = b",
                "endpackage"
        );
    }

    @Test
    public void subTypeGenericInterface() {
        testAssertOkLines(false,
                "package test",
                "    class C",
                "    interface A<X, Y>",
                "    class B<T> implements A<int, T>",
                "    init",
                "        let b = new B<C>",
                "        A<int, C> a = b",
                "endpackage"
        );
    }

    @Test
    public void subTypeGenericInterface2() {
        testAssertErrorsLines(false, "Cannot assign B<C> to A<C, integer>",
                "package test",
                "    class C",
                "    interface A<X, Y>",
                "    class B<T> implements A<int, T>",
                "    init",
                "        let b = new B<C>",
                "        A<C, int> a = b",
                "endpackage"
        );
    }

    @Test
    public void testArrayInitInClass() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class A",
                "    int array[2] a = [1,2]",
                "",
                "init ",
                "    let a = new A",
                "    if a.a[0] == 1 and a.a[1] == 2",
                "        testSuccess()",
                ""
        );
    }

    @Test
    public void testArrayInitInClassStatic() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class A",
                "    static int array a = [1,2]",
                "",
                "init ",
                "    if A.a[0] == 1 and A.a[1] == 2",
                "        testSuccess()",
                ""
        );
    }

    @Test
    public void testOverrideInterfaceAbstractClass() { // see #601
        testAssertOkLines(true,
                "package Test",
                "native println(string s)",
                "native testSuccess()",
                "interface MyInterface",
                "    function getStr() returns string",
                "",
                "abstract class MyInterfaceAbstract implements MyInterface",
                "    protected function getSome() returns string",
                "        return \"AAbstract\"",
                "",
                "class MyInterfaceAbstractImpl extends MyInterfaceAbstract",
                "    override function getStr() returns string",
                "        return getSome()",
                "",
                "class MyInterfaceImpl implements MyInterface",
                "    override function getStr() returns string",
                "        return \"MyInterfaceImpl\"",
                "",
                "init",
                "    MyInterface a = new MyInterfaceAbstractImpl",
                "    MyInterface b = new MyInterfaceImpl",
                "",
                "    println(a.getStr())",
                "    println(b.getStr())",
                "    if a.getStr() == \"AAbstract\" and b.getStr() == \"MyInterfaceImpl\"",
                "        testSuccess()"
        );
    }

    @Test
    public void testOverrideInterfaceAbstractClass2() { // see #602
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "interface MyInterface",
                "    function getStr() returns string",
                "",
                "abstract class MyInterfaceAbstract implements MyInterface",
                "    protected function getSome() returns string",
                "        return \"AAbstract\"",
                "",
                "    override abstract function getStr() returns string",
                "",
                "class MyInterfaceAbstractImpl extends MyInterfaceAbstract",
                "    override function getStr() returns string",
                "        return getSome()",
                "",
                "class MyInterfaceImpl implements MyInterface",
                "    override function getStr() returns string",
                "        return \"MyInterfaceImpl\"",
                "",
                "init",
                "    MyInterface a = new MyInterfaceImpl",
                "    MyInterface b = new MyInterfaceAbstractImpl",
                "    if a.getStr() == \"MyInterfaceImpl\" and b.getStr() == \"AAbstract\"",
                "        testSuccess()"
        );
    }

    @Test
    public void castToIntPointerArithmetic() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 1",
            "        construct(int x)",
            "            this.x = x",
            "    init",
            "        let a = new A(42)",
            "        let b = new A(43)",
            "        let i = a castTo int",
            "        let j = b castTo int",
            // don't do this please, we just allow this for backwards-compatibility
            "        if (((2*i + j) - j - i) castTo A).x == 42",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void castToIntGenerics() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 1",
            "        construct(int x)",
            "            this.x = x",
            "    function blub<T>(T x) returns int",
            "        return x castTo int",
            "    init",
            "        let a = new A(42)",
            "        let i = blub(a)",
            // don't do this please, we just allow this for backwards-compatibility
            "        if ((2*i - i) castTo A).x == 42",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void castToIntGenerics2() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 1",
            "        construct(int x)",
            "            this.x = x",
            "    class Cell<T>",
            "        int x",
            "        function set(T x)",
            "            this.x = x castTo int",
            "        function get() returns T",
            "            return x castTo T",
            "    init",
            "        let a = new A(42)",
            "        let c = new Cell<A>",
            "        c.set(a)",
            "        if c.get().x == 42",
            "            testSuccess()",
            "endpackage"
        );
    }

}
