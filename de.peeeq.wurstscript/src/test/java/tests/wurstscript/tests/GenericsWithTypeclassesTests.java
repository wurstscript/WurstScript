package tests.wurstscript.tests;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class GenericsWithTypeclassesTests extends WurstScriptTest {


    @Test
    public void identity() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity<A:>(A a) returns A",
                "		return a",
                "	init",
                "		int x = identity(3)",
                "		string s = identity(\"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void identityTrans() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity1<A:>(A a) returns A",
                "		return a",
                "	function identity2<B:>(B a) returns B",
                "		return identity1(a)",
                "	function identity3<C:>(C a) returns C",
                "		return identity2(a)",
                "	init",
                "		int x = identity3(3)",
                "		string s = identity3(\"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }


}
