package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class ClassesTests extends WurstScriptTest {

    private static final String TEST_DIR = "./testscripts/valid/classes/";
    private static final String TEST_DIR2 = "./testscripts/concept/";

    @Test
    public void classes1() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "Classes_1.wurst"), true);
    }

    @Test
    public void classes_construct() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "Classes_construct.wurst"), true);
    }

    @Test
    public void OverrideClass() throws IOException {
        testAssertOkFile(new File(TEST_DIR2 + "OverrideTest.wurst"), false);
    }

    @Test
    public void classes_lifecycle() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "Classes_lifecycle.wurst"), true);
    }

    @Test
    public void classes_method_implicit() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "Classes_method_implicit.wurst"), true);
    }

    @Test
    public void classes_method() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "Classes_method.wurst"), true);
    }

    @Test
    public void classes_static_func() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		function foo() returns int",
            "			return 3",
            "		static function bar()",
            "			foo()",
            "endpackage"
        );
    }

    @Test
    public void classes_static_var() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		static int size = 0",
            "		function foo() returns int",
            "			return this.size",
            "endpackage"
        );
    }

    @Test
    public void classes_static_var2() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		static int size = 0",
            "		function foo() returns int",
            "			this.size++",
            "			return this.size",
            "endpackage"
        );
    }


//	@Test
//	public void array_members() {
//		testAssertErrorsLines(false, "must be static",
//				"package test",
//				"	class C",
//				"		int array blub",
//				"endpackage"
//			);
//	}

    @Test
    public void code_members() {
        testAssertErrorsLines(false, "code members not allowed",
            "package test",
            "	class C",
            "		code c",
            "endpackage"
        );
    }

    @Test
    public void classes_static_var_get() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		int i = 3",
            "		static function foo() returns int",
            "			return i",
            "endpackage"
        );
    }

    @Test
    public void constantVars() {
        testAssertErrorsLines(false, "assign a new value",
            "package test",
            "	class C",
            "		constant int i = 1",
            "		function foo() returns int",
            "			i++",
            "			return i",
            "endpackage"
        );
    }

//		@Test
//		public void constantVars2() {
//			testAssertErrorsLines(false, "", // TODO "initial value",
//					"package test",
//					"	class C",
//					"		constant int i",
//					"endpackage"
//				);
//		}


    @Test
    public void classes_static_var_set() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		int i = 3",
            "		static function foo(int j)",
            "			i = j",
            "endpackage"
        );
    }

    @Test
    public void classes_static_var_set2() {
        testAssertErrorsLines(false, "static",
            "package test",
            "	class C",
            "		int i = 3",
            "		static function foo(int j)",
            "			this.i = j",
            "endpackage"
        );
    }


    @Test
    public void classes_double_defined() {
        testAssertErrorsLines(false, "already defined",
            "package test",
            "	class C",
            "		function foo() returns int",
            "			return 3",
            "		static function foo() returns int",
            "			return 4",
            "endpackage"
        );
    }

    @Test
    public void static_field() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class C",
            "		static int i = 3",
            "	init",
            "			C.i++",
            "			if C.i == 4",
            "				testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void static_field_other_package() {
        testAssertOkLines(true,
            "package Blub",
            "	public class C",
            "		static int i = 3",
            "endpackage",
            "package test",
            "	import Blub",
            "	native testSuccess()",
            "	init",
            "		C.i++",
            "		if C.i == 4",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void static_static_array_field() {
        testAssertOkLines(true,
            "package Blub",
            "	public class C",
            "		static int array xs",
            "		static function setX(int i, int x)",
            "			xs[i] = x",
            "		static function getX(int i) returns int",
            "			return xs[i]",
            "endpackage",
            "package test",
            "	import Blub",
            "	native testSuccess()",
            "	init",
            "		C.setX(7, 4)",
            "		if C.getX(7) == 4",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void ondestroy() {
        testAssertOkLines(false,
            "package test",
            "	native testSuccess()",
            "	class C",
            "		int i = 3",
            "		ondestroy",
            "			i = i + 1",
            "			testSuccess()",
            "	init",
            "		destroy new C()",
            "endpackage"
        );
    }


    @Test
    public void recyling() {
        test().executeProg(true)
            .executeProgOnlyAfterTransforms()
            .testLua(false)
            .lines(
                "package test",
                "	native testSuccess()",
                "	native println(string msg)",
                "	@extern native I2S(int i) returns string",
                "	class C",
                "		int i",
                "",
                "	init",
                "		C array cs",
                "		for int i = 0 to 6000",
                "			cs[i] = new C()",
                "		for int j = 0 to 6000",
                "			destroy cs[j]",
                "		for int k = 0 to 6000",
                "			cs[k] = new C()",
//                "			println(I2S(k) + \" --> \"  +I2S(cs[k] castTo int))",
                "		if cs[6000] castTo int <= 6001",
                "			testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void recyling2() {
        test().executeProg(true)
            .executeProgOnlyAfterTransforms()
            .testLua(false)
            .lines(
                "package test",
                "	native testSuccess()",
                "	native println(string msg)",
                "	native I2S(int i) returns string",
                "	class C",
                "		int i",
                "",
                "	init",
                "		C a = new C",
                "		destroy a",
                "		a = new C",
                "		destroy a",
                "		a = new C",
                "		C b = new C",
//				"		println(\"###### \" + I2S(a castTo int) + \" -- \" + I2S(b castTo int))",
                "		if a != b",
                "			testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void recyling_random() {
        test().executeProg(true)
            .executeProgOnlyAfterTransforms()
            .testLua(false)
            .lines(
                "package test",
                "	native testSuccess()",
                "	native println(string msg)",
                "	native testFail(string msg)",
                "	@extern native I2S(int i) returns string",
                "	@extern native GetRandomReal(real a, real b) returns real",
                "	@extern native GetRandomInt(int a, int b) returns int",
                "	class C",
                "		int alive",
                "		construct()",
                "			if alive == 1",
                "				testFail(\"already alive\")",
                "			alive = 1",
                "		ondestroy",
                "			alive = 2",
                "	C array cs",
                "	int count = 0",
                "	init",
                "		for i = 0 to 1000",
                "			if count < 100 and GetRandomReal(0,1) <= 0.5",
                "				cs[count] = new C",
                "				count++",
                "			if count > 0 and GetRandomReal(0,1) <= 0.1",
                "				let j = GetRandomInt(0,count-1)",
                "				destroy cs[j]",
                "				count--",
                "				cs[j] = cs[count]",
                "		testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void cast_class() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "	class B extends A",
            "	init",
            "		A a = new B()",
            "		if a instanceof B",
            "			B b = a castTo B",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void cast_class2() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "	class B extends A",
            "	class C extends B",
            "	init",
            "		A a = new C()",
            "		if a instanceof B",
            "			B b = a castTo B",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void cast_class_unrelated() {
        testAssertErrorsLines(true, "not directly related",
            "package test",
            "	native testSuccess()",
            "	class A",
            "	class B",
            "	init",
            "		A a = new A()",
            "		if a instanceof B",
            "			B b = a castTo B",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void big_instanceof() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "	class B extends A",
            "	class B1 extends B",
            "	class B2 extends B",
            "	class B2a extends B2",
            "	class B2b extends B2",
            "	class B2c extends B2",
            "	class B3 extends B",
            "	class B4 extends B",
            "	class B5 extends B",
            "	init",
            "		A a = new B2a()",
            "		if a instanceof B",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void override() {
        testAssertErrorsLines(false, "Function foo does not override anything",
            "package test",
            "	native testSuccess()",
            "	class A",
            "		override function foo()",
            "			skip",
            "endpackage"
        );
    }

    @Test
    public void override_valid() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		function foo() returns int",
            "			return 7",
            "	class B extends A",
            "		override function foo() returns int",
            "			return 8",
            "	init",
            "		A b = new B()",
            "		if b.foo() == 8",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void override_valid2() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		function foo() returns int",
            "			return 7",
            "	class B extends A",
            "		override function foo() returns int",
            "			return 8",
            "	init",
            "		A b = new A()",
            "		if b.foo() == 7",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void override_valid_trans() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		function foo() returns int",
            "			return 7",
            "	class B extends A",
            "		override function foo() returns int",
            "			return 8",
            "	class C extends B",
            "	init",
            "		A c = new C()",
            "		if c.foo() == 8",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void override_valid_void() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	int i",
            "	class A",
            "		function foo()",
            "			i = 7",
            "	class B extends A",
            "		override function foo()",
            "			i = 8",
            "	init",
            "		A b = new B()",
            "		b.foo()",
            "		if i == 8",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void override_valid_trans_big() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	native println(string msg)",
            "	native testFail(string msg)",
            "	class A",
            "		function foo() returns int",
            "			return 7",
            "	class B extends A",
            "		override function foo() returns int",
            "			return 8",
            "	class B1 extends B",
            "	class B2 extends B",
            "	class B11 extends B1",
            "	class B111 extends B11",
            "	class C extends A",
            "		override function foo() returns int",
            "			return 9",
            "	class C1 extends C",
            "	class C2 extends C",
            "	class C11 extends C1",
            "	class C111 extends C11",
            "	init",
            "		A a = new C11()",
            "		if a.foo() != 9",
            "			testFail(\"c11\")",
            "		a = new B11()",
            "		if a.foo() != 8",
            "			testFail(\"b11\")",
            "		a = new C()",
            "		if a.foo() != 9",
            "			testFail(\"C\")",
            "		a = new A()",
            "		if a.foo() != 7",
            "			testFail(\"A\")",


            "		testSuccess()",
            "endpackage"
        );
    }

    @Test(expectedExceptions = DebugPrintError.class)
    public void NPE() {
        test().executeProg()
            .executeProgOnlyAfterTransforms()
            .lines(
                "package test",
                "	native testSuccess()",
                "	class A",
                "		function foo() returns int",
                "			return 7",
                "	init",
                "		A a = null",
                "		if a.foo() == 7",
                "			testSuccess()",
                "endpackage");
    }

    @Test(expectedExceptions = DebugPrintError.class)
    public void destroyed() {
        test().executeProg()
            .executeProgOnlyAfterTransforms()
            .lines(
                "package test",
                "	native testSuccess()",
                "	class A",
                "		function foo() returns int",
                "			return 7",
                "	init",
                "		A a = new A()",
                "		let b = a",
                "		destroy b",
                "		if a.foo() == 7",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void abstract_class() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	int i",
            "	abstract class A",
            "		abstract function foo()",
            "	class B extends A",
            "		override function foo()",
            "			i = 8",
            "	init",
            "		A b = new B()",
            "		b.foo()",
            "		if i == 8",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void abstract_class2() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	int i",
            "	abstract class A",
            "		abstract function foo() returns int",
            "	class B extends A",
            "		override function foo() returns int",
            "			i = 8",
            "			return i",
            "	init",
            "		A b = new B()",
            "		if b.foo() == 8",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void abstract_fail() {
        testAssertErrorsLines(true, "Cannot create an instance of the abstract class A",
            "package test",
            "	native testSuccess()",
            "	int i",
            "	abstract class A",
            "		abstract function foo() returns int",
            "	init",
            "		A b = new A()",
            "		if b.foo() == 8",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void abstract_must_not_be_private() {
        testAssertErrorsLines(true, "Abstract functions must not be private",
            "package test",
            "	abstract class A",
            "		abstract private function foo()",
            "endpackage"
        );
    }

    @Test
    public void abstract_fail2() {
        testAssertErrorsLines(true, "must implement",
            "package test",
            "	native testSuccess()",
            "	int i",
            "	abstract class A",
            "		abstract function foo() returns int",
            "	abstract class B extends A",
            "	class C extends B",
            "endpackage"
        );
    }

    @Test
    public void construct_super() {
        testAssertErrorsLines(false, "Super call in a class which extends nothing",
            "package test",
            "	class C",
            "		construct(int x)",
            "			super()",
            "endpackage"
        );
    }

    @Test
    public void constructor_overloading() {
        testAssertOkLines(false,
            "type player extends handle",
            "package test",
            "	class A",
            "		construct(player p)",
            "			skip",
            "		construct(int y)",
            "			skip",
            "	class B extends A",
            "		construct (player p)",
            "			super(p)",
            "endpackage"
        );
    }

    @Test
    public void method_private() {
        testAssertErrorsLines(false, "foo is not visible here",
            "package test",
            "	class A",
            "		private function foo(int x)",
            "	init",
            "		new A().foo(5)",
            "endpackage"
        );
    }

    @Test
    public void constructor_private() {
        testAssertErrorsLines(false, "constructor for class A is not visible here",
            "package test",
            "	class A",
            "		private construct(int x)",
            "	init",
            "		new A(5)",
            "endpackage"
        );
    }

    @Test
    public void constructor_private_ok() {
        testAssertOkLines(false,
            "package test",
            "	class A",
            "		private construct(int x)",
            "		construct()",
            "		static function foo()",
            "			new A(5)",
            "	init",
            "		new A()",
            "endpackage"
        );
    }

    @Test
    public void constant_fields() {
        testAssertErrorsLines(false, "constant",
            "package test",
            "	class A",
            "		constant int i = 0",
            "	init",
            "		A a = new A()",
            "		a.i = 1",
            "endpackage"
        );
    }

    @Test
    public void constant_fields2() {
        testAssertErrorsLines(false, "Cannot assign",
            "package test",
            "	class A",
            "		constant int i = 1",
            "	init",
            "		A a = new A()",
            "		a.i = 1",
            "endpackage"
        );
    }


    @Test
    public void duplicateConstructor() {
        testAssertErrorsLines(false, "Duplicate constructor",
            "package test",
            "	class A",
            "		int i",
            "		construct(int j)",
            "			i = j",
            "		construct(int j)",
            "			i = j",
            "endpackage"
        );
    }

    @Test
    public void thisHandling() { // see bug #145
        test().executeProg(true)
            .testLua(false)
            .lines(
            "package test",
            "	native testSuccess()",
            "	class A",
            "		int i = this castTo int",
            "	init",
            "		let a = new A",
            "		if a.i == 1",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void dotdotOperator() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		int i",
            "		function plus(int x)",
            "			i += x",
            "		function minus(int x)",
            "			i -= x",
            "	init",
            "		A a = new A",
            "			..plus(3)",
            "			..minus(4)",
            "			..plus(5)",
            "		if a.i == 4",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void dotdotOperatorFail() {
        testAssertErrorsLines(false, "cannot be used with the cascade operator",
            "package test",
            "	native testSuccess()",
            "	class A",
            "		static function foo(int x)",
            "	init",
            "		A a = A",
            "			..foo(1)",
            "			..foo(2)",
            "endpackage"
        );
    }

    @Test
    public void arrayAttributeTest1() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	native println(string msg)",
            "	native testFail(string msg)",
            "	class A",
            "		int array[4] ints",
            "	init",
            "		A a = new A()",
            "		a.ints[0] = 1234",
            "		if a.ints[0] == 1234",
            "			testSuccess()",
            "		else",
            "			testFail(\"wrong value\")",
            "		",
            "endpackage"
        );
    }

    @Test
    public void arrayAttributeTest2() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	native println(string msg)",
            "	native testFail(string msg)",
            "	class A",
            "		string array[4] s",
            "	init",
            "		A a = new A()",
            "		a.s[0] = \"a\"",
            "		a.s[1] = \"b\"",
            "		a.s[2] = \"c\"",
            "		a.s[3] = \"d\"",
            "		if a.s[0] == \"a\" and a.s[1] == \"b\" and a.s[2] == \"c\" and a.s[3] == \"d\"",
            "			testSuccess()",
            "		else",
            "			testFail(\"wrong value\")",
            "		",
            "endpackage"
        );
    }

    @Test
    public void arrayAttributeFail1() {
        testAssertErrorsLines(true, "require a fixed size",
            "package test",
            "	class A",
            "		int array s",
            "endpackage"
        );
    }

    @Test
    public void arrayAttributeFail2() {
        testAssertErrorsLines(true, "is not a constant expression",
            "package test",
            "	function foo() returns int",
            "		return 4",
            "	class A",
            "		int array[foo()] s",
            "endpackage"
        );
    }

    @Test
    public void testInnerClass_static1() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		static class B",
            "			function foo() returns int",
            "				return 4",
            "		function bar() returns int",
            "			let b = new B",
            "			return b.foo()*10+2",
            "	init",
            "		let i = new A.bar()",
            "		if i == 42",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void testInnerClass_module() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	module M",
            "		static class B",
            "			function foo() returns int",
            "				return 4",
            "		function bar() returns int",
            "			let b = new B",
            "			return b.foo()*10+2",
            "	class A",
            "		use M",
            "	init",
            "		let i = new A.bar()",
            "		if i == 42",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void testInnerClass_module_thistype() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	module M",
            "		static thistype x",
            "		static class B",
            "			function foo() returns M.thistype",
            "				return x",
            "		function bar() returns thistype",
            "			let b = new B",
            "			return b.foo()",
            "	class A",
            "		use M",
            "	init",
            "		let a = new A",
            "		A.x = a",
            "		if a.bar() == a",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testInnerClass_module_thistype2() {
        testAssertErrorsLines(true, "Cannot access dynamic variable x",
            "package test",
            "	native testSuccess()",
            "	module M",
            "		thistype x",
            "		static class B",
            "			function foo() returns M.thistype",
            "				return x",
            "		function bar() returns thistype",
            "			let b = new B",
            "			return b.foo()",
            "	class A",
            "		use M",
            "	init",
            "		let a = new A",
            "		a.x = a",
            "		if a.bar() == a",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testInnerClass_module_thistype3() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	module M",
            "		thistype x",
            "		static class B",
            "			M.thistype parent",
            "			construct(M.thistype parent)",
            "				this.parent = parent",
            "			function foo() returns M.thistype",
            "				return parent.x",
            "		function bar() returns thistype",
            "			let b = new B(this)",
            "			return b.foo()",
            "	class A",
            "		use M",
            "	init",
            "		let a = new A",
            "		a.x = a",
            "		if a.bar() == a",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testInnerClass_static_from_outside() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		static class B",
            "			function foo() returns int",
            "				return 4",
            "		function getB() returns B",
            "			return new B",
            "	init",
            "		let b = new A.getB()",
            "		if b.foo() == 4",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testNoOverride() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "		function foo() returns int",
            "			return 42",
            "	class B extends A",
            "	init",
            "		let b = new B()",
            "		if b.foo() == 42",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void testArraySize() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	constant x = 4",
            "	constant y = 5",
            "	constant z = x*(10+(-y))",
            "	class A",
            "		int array[z] foo",
            "	init",
            "		let a = new A()",
            "		a.foo[13] = 42",
            "		if a.foo[13] == 42",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void testArraySize2() {
        testAssertErrorsLines(true, "must be at least 0",
            "package test",
            "	native testSuccess()",
            "	constant x = 4",
            "	constant y = 5",
            "	constant z = x*(4+(-y))",
            "	class A",
            "		int array[z] foo",
            "	init",
            "		let a = new A()",
            "		a.foo[13] = 42",
            "		if a.foo[13] == 42",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void initConstant() {
        testAssertOkLines(false,
            "package test",
            "	class C",
            "		constant int x",
            "		construct()",
            "			x = 5",
            "endpackage"
        );
    }

    @Test
    public void staticThisAccess() {
        testAssertErrorsLines(false, "cannot access",
            "package test",
            "   class A",
            "       static int array b",
            "       construct()",
            "           this.b[1] = 1",
            "endpackage"
        );
    }

    @Test
    public void tupleArrayMember() { // See #572
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "tuple t(int i)",
            "class A",
            "    t array[2] b",
            "init",
            "    let a = new A()",
            "    a.b[0] = t(4)",
            "    a.b[1] = t(5)",
            "    if a.b[0] == t(4) and a.b[1] == t(5)",
            "        testSuccess()"
        );
    }

    @Test
    public void testOver9000() {
        testAssertOkLines(true,
            "package Vegeta",
            "   native testSuccess()",
            "   class PowerLevel",
            "       static int amount = 0",
            "       construct()",
            "           amount++",
            "",
            "   init",
            "       for i = 0 to 20000",
            "           new PowerLevel()",
            "       if PowerLevel.amount > 9000 and PowerLevel.amount == 20001",
            "	        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void protectedInOtherPackage() {
        testAssertOkLines(true,
            "package A",
            "    native testSuccess()",
            "    public class A",
            "        protected function show()",
            "            testSuccess()",
            "endpackage",
            "package B",
            "    import A",
            "    class B extends A",
            "        override protected function show()",
            "            super.show()",
            "    init",
            "        new B().show()",
            "endpackage"
        );
    }

    @Test
    public void protectedInOtherPackage2() {
        testAssertOkLines(true,
            "package A",
            "    native testSuccess()",
            "    public class A",
            "        protected function show()",
            "            testSuccess()",
            "endpackage",
            "package B",
            "    import A",
            "    class B extends A",
            "        function blub()",
            "            show()",
            "    init",
            "        new B().blub()",
            "endpackage"
        );
    }

    @Test
    public void callStaticFunctionFromInit() {
        testAssertErrorsLines(true, "Cannot call static method f on an object.",
            "package Test",
            "public class A",
            "    static function f()",
            "init",
            "    new A().f()",
            "endpackage"
        );
    }

    @Test
    public void instanceof_null() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A",
            "	class B extends A",
            "	init",
            "		A a = null",
            "		if not a instanceof B",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void static_class_qualified_type() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class A",
            "    static function b() returns B",
            "        return new B",
            "    static class B",
            "        int x = 5",
            "",
            "init",
            "    A.B a = A.b()",
            "    if a.x == 5",
            "        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void static_class_qualified_field() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class A",
            "    static class B",
            "        static int x = 5",
            "",
            "init",
            "    if A.B.x == 5",
            "        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void static_class_qualified_function() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class A",
            "    static class B",
            "        static function f() returns int",
            "            return 5",
            "init",
            "    if A.B.f() == 5",
            "        testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void initialization_static_inner1() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "@extern native S2I(string i) returns int",
            "class A",
            "    B b = new B()",
            "    function get() returns int",
            "        return S2I(b.get())",
            "    static class B",
            "        function get() returns string",
            "            return \"1\"",
            "init",
            "    let a = new A()",
            "    if a.get() == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void initialization_static_inner2() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class A",
            "    B b = new B()",
            "    function get() returns int",
            "        return b.get()",
            "    static class B",
            "        function get() returns int",
            "            return 1",
            "init",
            "    let a = new A()",
            "    if a.get() == 1",
            "        testSuccess()"
        );
    }




}
