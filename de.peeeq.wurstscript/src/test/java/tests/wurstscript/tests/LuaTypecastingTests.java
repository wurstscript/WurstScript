package tests.wurstscript.tests;


import org.testng.annotations.Test;


import static org.testng.AssertJUnit.*;


public class LuaTypecastingTests extends WurstScriptTest {

    @Test
    public void hashMap1() {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "",
            "import HashMap",
            "import ClosureTimers",
            "",
            "class A",
            "",
            "constant intMap = new HashMap<int,int>()",
            "constant realMap = new HashMap<real,real>()",
            "constant boolMap = new HashMap<bool,bool>()",
            "constant strMap = new HashMap<string,string>()",
            "constant unitMap = new HashMap<unit,unit>()",
            "constant classMap = new HashMap<A,A>()",
            "",
            "init",
            "	doAfter(0.0) -> ",
            "		foo()",
            "",
            "@compiletime",
            "function foo()",
            "	strMap.put(\"a\", \"some string\")",
            "	print(strMap.get(\"a\"))",
            "	let aString = strMap.get(\"a\")",
            "	var s = \"hjgf\" + strMap.get(\"a\")		// str + any",
            "	print(s)",
            "	s = \"hjgf\" + aString				// str + str",
            "	print(s)",
            "	s = strMap.get(\"a\") + strMap.get(\"a\")	// any + any",
            "	print(s)",
            "",
            "	if not compiletime",
            "	    let u = unitMap.get(null)",
            "	    print(u.getName())",
            "	var i = intMap.get(0)",
            "	print(i)",
            "	var r = realMap.get(0.0)",
            "	print(r)",
            "	var b = boolMap.get(true)",
            "	print(b)",
            "",
            "	if not compiletime",
            "		unitMap.put(null, createUnit(players[0], 'hfoo', vec2(0,0), angle(0)))",
            "		let u = unitMap.get(null)",
            "		print(u.getName())",
            "	intMap.put(4,8)",
            "	i = intMap.get(4)",
            "	print(i)",
            "	realMap.put(3.7754, 591.53)",
            "	r = realMap.get(3.7754)",
            "	print(r)",
            "	boolMap.put(true, true)",
            "	b = boolMap.get(true)",
            "	print(b)"
        );
        assertTrue(true);
    }

    @Test
    public void compiletimeNull1() {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import NoWurst",
            "import public _Handles",
            "import public MagicFunctions",
            "",
            "",
            "function print(string msg)",
            "	DisplayTimedTextToPlayer(GetLocalPlayer(), 0., 0., 45., msg)",
            "",
            "class A",
            "	function foo()",
            "		print(\"I am A\")",
            "",
            "class B",
            "	A a = null",
            "",
            "function createB() returns B",
            "	let b = new B()",
            "	return b",
            "",
            "function getNullA() returns A",
            "	return null",
            "constant b = compiletime(createB())",
            "A nullA = compiletime(null)",
            "A nullA2 = compiletime(getNullA())",
            "A newA = compiletime(new A)",
            "",
            "init",
            "	if b.a == null",
            "		b.a = new A()",
            "	b.a.foo()",
            "	nullA.foo()",
            "	nullA2.foo()",
            "	newA.foo()"
        );
    }

    @Test
    public void compiletimeNull2() {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import NoWurst",
            "import public _Handles",
            "import public MagicFunctions",
            "",
            "function print(string msg)",
            "	DisplayTimedTextToPlayer(GetLocalPlayer(), 0., 0., 45., msg)",
            "",
            "class A",
            "	function foo()",
            "		print(\"I am A\")",
            "",
            "function getNullA() returns A",
            "	return null",
            "A nullA0 = null",
            "A array nullArray",
            "@compiletime function initArray()",
            "	nullArray[0] = null",
            "	nullArray[1] = null",
            "	nullArray[2] = null",
            "A nullA1 = compiletime(null)",
            "A nullA2 = compiletime(getNullA())",
            "A nullA3 = compiletime(nullA0)",
            "A nullA4 = compiletime(nullArray[1])",
            "",
            "init",
            "	nullA1.foo()",
            "	nullA2.foo()",
            "	nullA3.foo()",
            "	nullA4.foo()"
        );
    }

}

