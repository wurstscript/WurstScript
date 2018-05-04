package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class NewFeatureTests extends WurstScriptTest {
    private static final String TEST_DIR = "./testscripts/concept/";

    @Test
    public void testEnums() throws IOException {
        testAssertOkFileWithStdLib(new File(TEST_DIR + "enums.wurst"), false);
    }

    @Test
    public void testGenericUnit() throws IOException {
        testAssertOkFileWithStdLib(new File(TEST_DIR + "generics.wurst"), false);
    }

    @Test
    public void testMinusOne() throws IOException {
        testAssertOkFileWithStdLib(new File(TEST_DIR + "MinusOne.wurst"), false);
    }


    @Test
    public void testEnums2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "enum Blub",
                "	A",
                "	B",
                "init",
                "	if Blub.A != Blub.B",
                "		testSuccess()"
        );
    }


    @Test
    public void testEnums_cast() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "enum Blub",
                "	A",
                "	B",
                "init",
                "	if ((Blub.A castTo int) castTo Blub) != Blub.B",
                "		testSuccess()"
        );
    }

    @Test
    public void testSwitch() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "enum Blub",
                "	A",
                "	B",
                "init",
                "	Blub b = Blub.B",
                "	switch b",
                "		case Blub.A",
                "			skip",
                "		case Blub.B",
                "			testSuccess()"
        );
    }

    @Test
    public void testSwitchDefault() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "enum Blub",
                "	A",
                "	B",
                "	C",
                "init",
                "	var i = 5",
                "	switch Blub.C",
                "		case Blub.A",
                "			i = 1",
                "		case Blub.B",
                "			i = 2",
                "		default",
                "			testSuccess()"
        );
    }

    @Test
    public void testSwitchInt() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "init",
                "	var i = 1",
                "	switch i",
                "		case 0",
                "			i = 1",
                "		case 1",
                "			testSuccess()",
                "		default",
                "			skip"
        );
    }


    @Test
    public void testSwitchString() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "init",
                "	var s = \"toll\"",
                "	switch s",
                "		case \"wurst\"",
                "			s = \"\"",
                "		case \"toll\"",
                "			testSuccess()",
                "		default",
                "			skip"
        );
    }

    @Test
    public void testSwitchWrongTypes() {
        testAssertErrorsLines(true, "does not match",
                "package Test",
                "native testSuccess()",
                "init",
                "	var s = \"toll\"",
                "	switch s",
                "		case 123",
                "			s = \"\"",
                "		case \"toll\"",
                "			testSuccess()",
                "		default",
                "			skip"
        );
    }

    @Test
    public void testSwitchReturn() {
        testAssertOkLines(false,
                "package Test",
                "native testSuccess()",
                "function foo() returns int",
                "	var s = \"toll\"",
                "	switch s",
                "		case \"bla\"",
                "			return 1",
                "		case \"toll\"",
                "			return 2",
                "		default",
                "			return 3"
        );
    }

    @Test
    public void testSwitchInit() {
        testAssertOkLines(false,
                "package Test",
                "native testSuccess()",
                "function foo()",
                "	var s = \"toll\"",
                "	int i",
                "	switch s",
                "		case \"bla\"",
                "			i = 1",
                "		case \"toll\"",
                "			i= 2",
                "		default",
                "			i = 3",
                "	i++"
        );
    }

    @Test
    public void testSwitchEnumAll() {
        testAssertErrorsLines(false, "Enum member",
                "package Test",
                "native testSuccess()",
                "enum Blub",
                "	A",
                "	B",
                "	C",
                "init",
                "	var i = 5",
                "	switch Blub.C",
                "		case Blub.A",
                "			i = 1",
                "		case Blub.B",
                "			i = 2"
        );
    }

    @Test
    public void testTypeId1() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class Blub",
                "	int i = 0",
                "class Foo",
                "	int j = 0",
                "init",
                "	Blub b = new Blub()",
                "	Foo f = new Foo()",
                "	if b.typeId != f.typeId",
                "		testSuccess()"
        );
    }


    @Test
    public void testTypeId2() {
        testAssertErrorsLines(false, "cannot use typeId",
                "package Test",
                "class Blub",
                "	int i = 0",
                "init",
                "	Blub b = new Blub()",
                "	b.typeId = 2"
        );
    }

    @Test
    public void testTypeId3() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class Blub",
                "	int i = 0",
                "class Foo extends Blub",
                "	int j = 0",
                "init",
                "	Blub b = new Blub()",
                "	Blub f = new Foo()",
                "	if b.typeId == Blub.typeId and f.typeId == Foo.typeId",
                "		testSuccess()"
        );
    }

    @Test
    public void testTypeId4() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "abstract class Blub",
                "class A extends Blub",
                "class B extends Blub",
                "init",
                "	Blub a = new A()",
                "	Blub b = new B()",
                "	if b.typeId == B.typeId and a.typeId == A.typeId",
                "		testSuccess()"
        );
    }

    @Test
    public void testTypeId5() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class Blub",
                "	function foo()",
                "		skip",
                "class A extends Blub",
                "class B extends Blub",
                "init",
                "	Blub a = new A()",
                "	Blub b = new B()",
                "	if b.typeId == B.typeId and a.typeId == A.typeId",
                "		testSuccess()"
        );
    }


    @Test
    public void cyclicFunc1() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x) returns int",
                "	return 1+bar(x)",
                "function bar(int x) returns int",
                "	if x>0",
                "		return 1+foo(x-1)",
                "	else",
                "		return 0",
                "init",
                "	if foo(5) == 11",
                "		testSuccess()"
        );
    }

    @Test
    public void cyclicFunc2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x,real y) returns real",
                "	return 1+bar(y, x)",
                "function bar(real a, int b) returns real",
                "	if a>0",
                "		return a",
                "	else",
                "		return foo(1, 2.*b)",
                "init",
                "	if foo(5,7) == 8",
                "		testSuccess()"
        );
    }

    @Test
    public void callFunctionsWithAnnotation() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function callFunctionsWithAnnotation(string _annotation)",
                "int x = 1",
                "@blub function foo()",
                "	x = x + 2",
                "@blub function bar()",
                "	x += 3",
                "init",
                "	callFunctionsWithAnnotation(\"@blub\")",
                "	if x == 6",
                "		testSuccess()"
        );
    }

    @Test
    public void testAnnotationWithMessage() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@deprecated(\"yes\") function foo()",
                "init",
                "	foo()",
                "	testSuccess()"
        );
    }



}
