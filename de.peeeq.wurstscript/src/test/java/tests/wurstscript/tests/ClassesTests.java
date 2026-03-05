package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.DebugPrintError;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
    public void dispatchNarrowingUsesStaticReceiverTypeInJass() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "",
            "    class A",
            "        function bar() returns int",
            "            return 1",
            "",
            "    class B extends A",
            "        override function bar() returns int",
            "            return 2",
            "",
            "    class C extends A",
            "",
            "    function useC(C c) returns int",
            "        return c.bar()",
            "",
            "    init",
            "        let c = new C",
            "        if useC(c) == 1",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_dispatchNarrowingUsesStaticReceiverTypeInJass_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);

        assertTrue(jass.contains("dispatch_narrow_C_A_A_bar"),
            "Expected narrowed dispatch function for receiver type C.");
        assertTrue(jass.contains("return dispatch_narrow_C_A_A_bar(c)")
                || jass.contains("call dispatch_narrow_C_A_A_bar"),
            "Expected call site to use narrowed dispatch.");
        assertFalse(jass.contains("return dispatch_A_A_bar(") && jass.contains("function useC"),
            "Call site should not use full A dispatch for static receiver type C.");
        assertFalse(jass.contains("call dispatch_A_A_bar"),
            "Call site should not use full A dispatch for static receiver type C.");
    }

    @Test
    public void dispatchNarrowingOnInterfaceSubtypeKeepsSemantics() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "",
            "    interface A",
            "        function f1() returns int",
            "",
            "    interface B extends A",
            "        function f2() returns int",
            "",
            "    class C implements B",
            "        override function f1() returns int",
            "            return 3",
            "        override function f2() returns int",
            "            return 4",
            "",
            "    function useB(B b) returns int",
            "        return b.f1()",
            "",
            "    function useA(A a) returns int",
            "        return a.f1()",
            "",
            "    init",
            "        B b = new C()",
            "        A a = b",
            "        if useB(b) == 3 and useA(a) == 3",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_dispatchNarrowingOnInterfaceSubtypeKeepsSemantics_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);

        assertTrue(jass.contains("function useB takes integer b returns integer"),
            "Expected useB helper in generated jass.");
        assertTrue(jass.contains("return dispatch_narrow_B_A_A_f1(b)"),
            "Expected B-typed call site to use narrowed dispatch.");
        assertTrue(jass.contains("function useA takes integer a returns integer"),
            "Expected useA helper in generated jass.");
        assertTrue(jass.contains("return dispatch_A_A_f1(a)"),
            "Expected A-typed call site to use regular A dispatch.");
    }

    @Test
    public void repeatedCallsOnSameReceiverAreNotDispatchCachedYet() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    @extern native I2S(int x) returns string",
            "    @extern native S2I(string x) returns int",
            "    class A",
            "        function bar() returns int",
            "            return 1",
            "",
            "    function useA(A a) returns int",
            "        int r = 0",
            "        r += a.bar()",
            "        r += S2I(I2S(0))",
            "        r += a.bar()",
            "        r += S2I(I2S(0))",
            "        r += a.bar()",
            "        r += S2I(I2S(0))",
            "        return r",
            "",
            "    init",
            "        let a = new A",
            "        let ua = useA(a)",
            "        if ua == 3",
            "            testSuccess()",
            "endpackage"
        );

        File noOptOut = new File(TEST_OUTPUT_PATH + "ClassesTests_repeatedCallsOnSameReceiverAreNotDispatchCachedYet_no_opts.j");
        File optOut = new File(TEST_OUTPUT_PATH + "ClassesTests_repeatedCallsOnSameReceiverAreNotDispatchCachedYet_opt.j");
        String noOpt = Files.readString(noOptOut.toPath(), StandardCharsets.UTF_8);
        String opt = Files.readString(optOut.toPath(), StandardCharsets.UTF_8);

        int noOptDispatchCalls = countOccurrences(noOpt, "dispatch_A_A_bar(");
        int optDispatchCalls = countOccurrences(opt, "dispatch_A_A_bar(");

        assertTrue(noOptDispatchCalls >= 3,
            "Expected at least three dispatch calls in no_opts output, found " + noOptDispatchCalls);
        assertTrue(optDispatchCalls >= 3,
            "Expected at least three dispatch calls in opt output, found " + optDispatchCalls);

        File inlOptOut = new File(TEST_OUTPUT_PATH + "ClassesTests_repeatedCallsOnSameReceiverAreNotDispatchCachedYet_inlopt.j");
        String inlOpt = Files.readString(inlOptOut.toPath(), StandardCharsets.UTF_8);
        int inlOptGuardCount = countOccurrences(inlOpt, "if A_typeId[a] == 0 then");
        assertTrue(inlOptGuardCount == 1,
            "Expected inlopt output to contain a single dispatch guard, found " + inlOptGuardCount);
    }

    @Test
    public void dispatchGuardNotDedupedAcrossRhsSideEffects() throws IOException {
        testAssertOkLines(false,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        function bar() returns int",
            "            return 1",
            "",
            "    function mutatingRhs(A a) returns int",
            "        destroy a",
            "        return 0",
            "",
            "    function useA(A a) returns int",
            "        int r = 0",
            "        r += a.bar()",
            "        r = mutatingRhs(a)",
            "        r += a.bar()",
            "        return r",
            "",
            "    init",
            "        let a = new A",
            "        useA(a)",
            "        testSuccess()",
            "endpackage"
        );

        File inlOptOut = new File(TEST_OUTPUT_PATH + "ClassesTests_dispatchGuardNotDedupedAcrossRhsSideEffects_inlopt.j");
        String inlOpt = Files.readString(inlOptOut.toPath(), StandardCharsets.UTF_8);
        int inlOptGuardCount = countOccurrences(inlOpt, "if A_typeId[a] == 0 then");
        assertTrue(inlOptGuardCount >= 2,
            "Expected inlopt output to keep separate guards across mutating RHS call, found " + inlOptGuardCount);
    }

    @Test(expectedExceptions = DebugPrintError.class)
    public void dispatchGuardAcrossRhsSideEffectsStillThrowsAtRuntime() {
        test().executeProg()
            .executeProgOnlyAfterTransforms()
            .lines(
                "package test",
                "    class A",
                "        function bar() returns int",
                "            return 1",
                "",
                "    function mutatingRhs(A a) returns int",
                "        destroy a",
                "        return 0",
                "",
                "    function useA(A a) returns int",
                "        int r = 0",
                "        r += a.bar()",
                "        r = mutatingRhs(a)",
                "        r += a.bar()",
                "        return r",
                "",
                "    init",
                "        let a = new A",
                "        useA(a)",
                "endpackage"
            );
    }

    @Test
    public void dispatchGuardNotDedupedAcrossNestedMutatingExprStatement() throws IOException {
        testAssertOkLines(false,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        function bar() returns int",
            "            return 1",
            "",
            "    function mutatingRhs(A a) returns int",
            "        destroy a",
            "        return 0",
            "",
            "    function useA(A a) returns int",
            "        int r = 0",
            "        r += a.bar()",
            "        int unused = mutatingRhs(a) + 0",
            "        r += a.bar()",
            "        return r",
            "",
            "    init",
            "        let a = new A",
            "        useA(a)",
            "        testSuccess()",
            "endpackage"
        );

        File inlOptOut = new File(TEST_OUTPUT_PATH + "ClassesTests_dispatchGuardNotDedupedAcrossNestedMutatingExprStatement_inlopt.j");
        String inlOpt = Files.readString(inlOptOut.toPath(), StandardCharsets.UTF_8);
        int inlOptGuardCount = countOccurrences(inlOpt, "if A_typeId[a] == 0 then");
        assertTrue(inlOptGuardCount >= 2,
            "Expected inlopt output to keep separate guards across nested mutating expr statement, found " + inlOptGuardCount);
    }

    @Test(expectedExceptions = DebugPrintError.class)
    public void dispatchGuardAcrossNestedMutatingExprStatementStillThrowsAtRuntime() {
        test().executeProg()
            .executeProgOnlyAfterTransforms()
            .lines(
                "package test",
                "    class A",
                "        function bar() returns int",
                "            return 1",
                "",
                "    function mutatingRhs(A a) returns int",
                "        destroy a",
                "        return 0",
                "",
                "    function useA(A a) returns int",
                "        int r = 0",
                "        r += a.bar()",
                "        int unused = mutatingRhs(a) + 0",
                "        r += a.bar()",
                "        return r",
                "",
                "    init",
                "        let a = new A",
                "        useA(a)",
                "endpackage"
            );
    }

    private static int countOccurrences(String text, String needle) {
        int c = 0;
        int i = 0;
        while ((i = text.indexOf(needle, i)) >= 0) {
            c++;
            i += needle.length();
        }
        return c;
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
    public void constructor_chaining_basic() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 0",
            "        construct()",
            "            this(3)",
            "        construct(int i)",
            "            x = i",
            "    init",
            "        let a = new A()",
            "        if a.x == 3",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_constructor_chaining_basic_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);
        assertTrue(jass.contains("function construct_A takes integer this returns nothing"),
            "Expected delegating constructor function in generated jass.");
        assertTrue(jass.contains("function construct_A2 takes integer this, integer i returns nothing"),
            "Expected target constructor overload in generated jass.");
        assertTrue(jass.contains("call construct_A2(this, 3)"),
            "Expected delegating constructor to call target constructor.");
        assertFalse(jass.contains("function construct_A takes integer this returns nothing\n\tcall A_init(this)"),
            "Delegating constructor must not emit duplicate class init.");
        assertTrue(countOccurrences(jass, "call A_init(this)") == 1,
            "Expected class init to be emitted exactly once.");
    }

    @Test
    public void constructor_chaining_vararg() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 0",
            "        construct()",
            "            this(1, \"a\", \"b\")",
            "        construct(int i, vararg string xs)",
            "            x = i",
            "            for s in xs",
            "                x++",
            "    init",
            "        let a = new A()",
            "        if a.x == 3",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_constructor_chaining_vararg_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);
        assertTrue(jass.contains("call construct_A2_2(this, 1, \"a\", \"b\")"),
            "Expected delegating constructor to pass concrete vararg arguments.");
        assertTrue(countOccurrences(jass, "call A_init(this)") == 1,
            "Expected vararg constructor chain to initialize class exactly once.");
    }

    @Test
    public void constructor_chaining_must_be_first_statement() {
        testAssertErrorsLines(false, "must be the first statement",
            "package test",
            "    class A",
            "        construct()",
            "            skip",
            "            this(1)",
            "        construct(int i)",
            "            skip",
            "endpackage"
        );
    }

    @Test
    public void constructor_chaining_self_call() {
        testAssertErrorsLines(false, "cannot call itself",
            "package test",
            "    class A",
            "        construct()",
            "            this()",
            "endpackage"
        );
    }

    @Test
    public void constructor_chaining_and_super_conflict() {
        testAssertErrorsLines(false, "Cannot call super(...) and this(...)",
            "package test",
            "    class A",
            "        construct(int i)",
            "            skip",
            "    class B extends A",
            "        construct()",
            "            super(1)",
            "            this(2)",
            "        construct(int i)",
            "            super(i)",
            "endpackage"
        );
    }

    @Test
    public void constructor_chaining_extends_without_direct_super_call() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 0",
            "        construct(int i)",
            "            x = i",
            "    class B extends A",
            "        construct()",
            "            this(7)",
            "        construct(int i)",
            "            super(i)",
            "    init",
            "        let b = new B()",
            "        if b.x == 7",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_constructor_chaining_extends_without_direct_super_call_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);
        assertTrue(jass.contains("call construct_B2(this, 7)"),
            "Expected delegating constructor in subclass to call target constructor.");
        assertTrue(jass.contains("call construct_A(this, i)"),
            "Expected target constructor to emit super constructor call.");
        assertTrue(jass.contains("call B_init(this)"),
            "Expected subclass init call in target constructor.");
        assertFalse(jass.contains("function construct_B takes integer this returns nothing\n\tcall B_init(this)"),
            "Delegating subclass constructor must not emit duplicate subclass init.");
        assertTrue(countOccurrences(jass, "call B_init(this)") == 1,
            "Expected subclass init to be emitted exactly once.");
    }

    @Test
    public void constructor_chaining_timing_and_sideeffects() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    int ticks = 0",
            "    function tick() returns int",
            "        ticks++",
            "        return ticks",
            "    class A",
            "        int p = tick()",
            "        int q = 0",
            "        construct()",
            "            this(7)",
            "            q = tick()",
            "            p += 100",
            "        construct(int i)",
            "            q = tick()",
            "            p += i",
            "    init",
            "        let a = new A()",
            "        if a.p == 108 and a.q == 3 and ticks == 3",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_constructor_chaining_timing_and_sideeffects_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);
        assertTrue(countOccurrences(jass, "call A_init(this)") == 1,
            "Expected class init to run exactly once in constructor chain.");
        int idxDelegatingCall = jass.indexOf("call construct_A2(this, 7)");
        int idxDelegatingBody = jass.indexOf("set A_q[this] = tick()", idxDelegatingCall);
        assertTrue(idxDelegatingCall >= 0 && idxDelegatingBody > idxDelegatingCall,
            "Expected delegating constructor body to run after this(...) target call.");
    }

    @Test
    public void constructor_chaining_subclass_timing_and_order() throws IOException {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "        int x = 0",
            "        construct(int i)",
            "            x = i * 10",
            "    class B extends A",
            "        int y = 0",
            "        construct()",
            "            this(2)",
            "            y += 1",
            "            x += 1",
            "        construct(int i)",
            "            super(i)",
            "            y += 10",
            "            x += 2",
            "    init",
            "        let b = new B()",
            "        if b.x == 23 and b.y == 11",
            "            testSuccess()",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "ClassesTests_constructor_chaining_subclass_timing_and_order_no_opts.j");
        String jass = Files.readString(output.toPath(), StandardCharsets.UTF_8);
        assertTrue(countOccurrences(jass, "call B_init(this)") == 1,
            "Expected subclass init to run exactly once in constructor chain.");
        int idxSuperCall = jass.indexOf("call construct_A(this, i)");
        int idxBInitCall = jass.indexOf("call B_init(this)");
        int idxYMutation = jass.indexOf("set B_y[this] = B_y[this] + 10");
        assertTrue(idxSuperCall >= 0 && idxBInitCall > idxSuperCall && idxYMutation > idxBInitCall,
            "Expected order super(...) -> B_init -> target constructor body in subclass constructor chain.");
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
