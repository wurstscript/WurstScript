package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class TupleTests extends WurstScriptTest {

    @Test
    public void simple() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	init",
                "		vec v = vec(1,2,3)",
                "		v.x = 4",
                "		vec u = v",
                "		u.y = 5",
                "		if v.x == 4 and v.y == 2 and u.y == 5",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void swap() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple pair(real x, real y)",
                "	init",
                "		pair p = pair(1,2)",
                "		p = pair(p.y, p.x)",
                "		if p.x == 2 and p.y == 1",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void parameter() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function sum(vec v) returns real",
                "		return v.x + v.y + v.z",
                "	init",
                "		let v = vec(4,5,6)",
                "		if sum(v) == 15",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void returnValue() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function blub(real x, real y) returns vec",
                "		return vec(x, y, 0)",
                "	init",
                "		let v = blub(4,5)",
                "		if v.x == 4 and v.y == 5 and v.z == 0",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void vecs() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function vec.plus(vec other) returns vec",
                "		return vec(this.x + other.x, this.y + other.y, this.z + other.z)",
                "	init",
                "		let v = vec(1,2,3).plus(vec(4,5,6))",
                "		if v.x == 5 and v.y == 7 and v.z == 9",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void vecs2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function vec.plus(vec other) returns vec",
                "		if this.x > 0",
                "			return vec(this.x + other.x, this.y + other.y, this.z + other.z)",
                "		else",
                "			return vec(this.x + other.x, this.y + other.y, this.z + other.z)",
                "	init",
                "		if vec(1,2,3).plus(vec(4,5,6)).y == 7",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void bugtest() {
        testAssertOkLines(false,
                "package test",
                "	tuple tup(real x)",
                "	function foo(tup mytup)",
                "		real y = mytup.x",
                "endpackage"
        );
    }

    @Test
    public void vecs4() {
        testAssertOkLines(false,
                "package test",
                "	native testSuccess()",
                "	native Sin(real x) returns real",
                "	native Cos(real x) returns real",
                "	tuple vec3(real x, real y, real z)",
                "	public function polarProjection3d( vec3 pos, real distance, real angleGround, real angleAir ) returns vec3",
                "		real x = pos.x + distance * Cos(angleGround) * Sin(angleAir)",
                "		real y = pos.y + distance * Sin(angleGround) * Sin(angleAir)",
                "		real z = pos.z + distance * Cos(angleAir) ",
                "		return vec3(x,y,z)",
                "endpackage"
        );
    }

    @Test
    public void vecsTrim() {
        testAssertOkLines(false,
                "package test",
                "	native testSuccess()",
                "	tuple vec3(real x, real y, real z)",
                "	public function vec3.trim( real value ) returns vec3",
                "		vec3 result = this",
                "		if result.x > -value and result.x < value",
                "			result.x = 0.",
                "		if result.y > -value and result.y < value",
                "			result.y = 0.",
                "		if result.z > -value and result.z < value",
                "			result.z = 0.",
                "		return result",
                "	vec3 array vs",
                "	init",
                "		vs[0] = vec3(3,15,4)",
                "		if 3 > 2",
                "			vs[0].trim(5)",
                "endpackage"
        );
    }

    @Test
    public void vecsTrim2() {
        testAssertOkLines(false,
                "package test",
                "	native testSuccess()",
                "	tuple vec3(real x, real y, real z)",
                "	public function vec3.trim( real value ) returns vec3",
                "		vec3 result = this",
                "		if result.x > -value and result.x < value",
                "			result.x = 0.",
                "		if result.y > -value and result.y < value",
                "			result.y = 0.",
                "		if result.z > -value and result.z < value",
                "			result.z = 0.",
                "		return result",
                "	vec3 array vs",
                "	function foo(int x) returns int",
                "		if x > 0",
                "			return x",
                "		else",
                "			return -x",
                "	init",
                "		vs[0] = vec3(3,15,4)",
                "		vs[foo(3)].trim(foo(4)*1.)",
                "endpackage"
        );
    }


    @Test
    public void inClass() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	class A",
                "		vec pos",
                "		construct()",
                "			pos = vec(1,1,1)",
                "		function foo()",
                "			pos.x = 3",
                "	init",
                "		A a = new A()",
                "		a.foo()",
                "		if a.pos.x == 3",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void tupleInTuple() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y)",
                "	tuple line(vec start, vec end_)",
                "	init",
                "		let l = line(vec(1,2), vec(3,4))",
                "		if l.end_.x == 3",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void tupleInTuple2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y)",
                "	tuple line(vec start, vec end_)",
                "	function bla(vec v) returns real",
                "		return v.x + v.y",
                "	init",
                "		let l = line(vec(1,2), vec(3,4))",
                "		if bla(l.end_) == 7",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testEquals() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple tup(int a, int b, int c)",
                "	init",
                "		let a = tup(1,2,3)",
                "		let b = tup(1,2,3)",
                "		let c = tup(1,1,3)",
                "		if a == b and (not b == c)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testUnequals() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple tup(int a, int b, int c)",
                "	init",
                "		let a = tup(1,2,3)",
                "		let b = tup(1,2,3)",
                "		let c = tup(1,1,3)",
                "		if a != c and (not a != b)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testSingletuple() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple tup(int a)",
                "	function foo(boolean b) returns tup",
                "		if b",
                "			return tup(2)",
                "		return tup(3)",
                "	init",
                "		let a = foo(true)",
                "		if a.a == 2",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testSingletuple2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple tup(int a)",
                "	function foo(tup t) returns tup",
                "		if t.a > 5",
                "			return tup(2)",
                "		return tup(3)",
                "	tup array x",
                "	init",
                "		x[0] = tup(8)",
                "		let a = foo(x[0])",
                "		if a.a == 2",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void tupleInTuple3() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	native println(string s)",
                "	@extern native I2S(int i) returns string",
                "	tuple vec(int x, int y)",
                "	tuple line(vec a, vec b)",
                "	line array l",
                "	init",
                "		l[2] = line(vec(1,2),vec(3,4))",
                "		string s = \"abc\"",
                "		s += I2S(l[2].a.x)",
                "		s += I2S(l[2].a.y)",
                "		s += I2S(l[2].b.x)",
                "		s += I2S(l[2].b.y)",
                "		if s == \"abc1234\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void tupleInTuple4() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "tuple vec2(int x, int y)",
                "tuple rectangle(vec2 min, vec2 max)",
                "init",
                "    rectangle r = rectangle(vec2(1,2), vec2(3,4))",
                "    r.min.x = 5",
                "    if r.min.x == 5",
                "        testSuccess()"
        );
    }

    @Test
    public void tupleInTuple5() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "tuple vec2(int x, int y)",
                "tuple rectangle(vec2 min, vec2 max)",
                "init",
                "    rectangle r = rectangle(vec2(1,2), vec2(3,4))",
                "    r.min = vec2(5,2)",
                "    if r.min.x == 5",
                "        testSuccess()"
        );
    }

    @Test
    public void tupleReturn() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native GetRandomReal(real x, real y) returns real",
                "tuple vec2(real x, real y)",
                "function randomPoint() returns vec2",
                "    return vec2(GetRandomReal(0, 1), GetRandomReal(3, 4))",
                "function a() returns vec2",
                "    return randomPoint()",
                "init",
                "    let v = a()",
                "    testSuccess()"
        );
    }

    @Test
    public void tupleReturn2() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native GetRandomReal(real x, real y) returns real",
                "tuple vec2(real x, real y)",
                "tuple t(vec2 v, real r)",
                "function randomPoint() returns vec2",
                "    return vec2(GetRandomReal(0, 1), GetRandomReal(3, 4))",
                "function a() returns t",
                "    return t(randomPoint(), 1)",
                "init",
                "    let v = a()",
                "    testSuccess()"
        );
    }


    @Test
    public void nestedTuple() { // #713
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "native println(string s)",
                "@extern native I2S(int x) returns string",
                "function print(int x)",
                "    println(I2S(x))",
                "@extern native GetRandomInt(int x, int y) returns int",
                "tuple parent(child a, int index)",
                "function newParent(int i) returns parent",
                "    return parent(child(0, 0, 0, 0), i)",
                "tuple child(int a, int b, int c, int d)",
                "var putCount = 0",
                "function child.put(int i, int num) returns child",
                "    putCount += 1",
                "    if i == 0",
                "        return child(num, this.b, this.c, this.d)",
                "    else if i == 1",
                "        return child(this.a, num, this.b, this.d)",
                "    else if i == 2",
                "        return child(this.a, this.b, num, this.d)",
                "    else",
                "        return child(this.a, this.b, this.b, num)",
                "function randomOperations(parent t, int val) returns parent",
                "    var some = t",
                "    some.a = some.a.put(t.index, val)",
                "    return some",
                "init",
                "    var t = randomOperations(newParent(GetRandomInt(0, 3)), 100)",
                "    print(t.a.a)",
                "    print(t.a.b)",
                "    print(t.a.c)",
                "    print(t.a.d)",
                "    print(putCount)",
                "    if putCount == 1",
                "        testSuccess()"
        );
    }


}
