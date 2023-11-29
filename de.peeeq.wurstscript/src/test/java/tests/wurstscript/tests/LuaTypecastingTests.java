package tests.wurstscript.tests;


import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;


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
            "int array zeroArray",
            "@compiletime function initArray()",
            "	nullArray[0] = null",
            "	nullArray[1] = null",
            "	nullArray[2] = null",
            "	zeroArray[0] = 0",
            "	zeroArray[1] = 0",
            "	zeroArray[2] = 0",
            "A nullA1 = compiletime(null)",
            "A nullA2 = compiletime(getNullA())",
            "A nullA3 = compiletime(nullA0)",
            "A nullA4 = compiletime(nullArray[1])",
            "int zeroInt0 = 0",
            "int zeroInt1 = compiletime(0)",
            "int zeroInt2 = compiletime(S2I(\"0\"))",
            "int zeroInt3 = compiletime(zeroInt0)",
            "int zeroInt4 = compiletime(zeroArray[1])",
            "",
            "init",
            "	nullA1.foo()",
            "	nullA2.foo()",
            "	nullA3.foo()",
            "	nullA4.foo()",
            "	print(I2S(zeroInt0 + zeroInt1 + zeroInt2 + zeroInt3 + zeroInt4))"
        );
    }

    @Test
    public void compiletimeNull3() {
        test().testLua(true).withStdLib().lines(
            "package Test",
            "import NoWurst",
            "import public _Handles",
            "import public MagicFunctions",
            "import LinkedList",
            "import TypeCasting",
            "LinkedList<int> list1",
            "function print(string msg)",
            "	DisplayTimedTextToPlayer(GetLocalPlayer(), 0., 0., 45., msg)",
            "function getList() returns LinkedList<int>",
            "	return new LinkedList<int>()",
            "LinkedList<int> array listArray",
            "@compiletime function initArray()",
            "	list1 = new LinkedList<int>()",
            "	listArray[0] = new LinkedList<int>()",
            "	listArray[1] = new LinkedList<int>()",
            "	listArray[2] = new LinkedList<int>()",
            "",
            "LinkedList<int> list2 = new LinkedList<int>()",
            "LinkedList<int> list3 = compiletime(new LinkedList<int>)",
            "LinkedList<int> list4 = compiletime(getList())",
            "LinkedList<int> list5 = compiletime(listArray[1])",
            "init",
            "	print(I2S(list1.size()))",
            "	print(I2S(list2.size()))",
            "	print(I2S(list3.size()))",
            "	print(I2S(list4.size()))",
            "	print(I2S(list5.size()))"
        );
    }

}

