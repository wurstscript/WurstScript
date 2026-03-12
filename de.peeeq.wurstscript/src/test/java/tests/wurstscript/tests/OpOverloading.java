package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class OpOverloading extends WurstScriptTest {


    @Test
    public void testOverloading1() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class A ",
                "		int i = 2",
                "		",
                "		function op_plus(A a) returns int",
                "			return this.i + a.i",
                "	init",
                "		A a1 = new A()",
                "		A a2 = new A()",
                "		int result = a1 + a2",
                "		if result == 4",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading1_2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class A ",
                "		int i = 2",
                "		",
                "		function op_minus(A a) returns int",
                "			return this.i - a.i",
                "	init",
                "		A a1 = new A()",
                "		A a2 = new A()",
                "		int result = a1 - a2",
                "		if result == 0",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading1_3() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class A ",
                "		int i = 2",
                "		",
                "		function op_mult(A a) returns int",
                "			return this.i * a.i",
                "	init",
                "		A a1 = new A()",
                "		A a2 = new A()",
                "		int result = a1 * a2",
                "		if result == 4",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading1_4() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class A ",
                "		int i = 2",
                "		",
                "		function op_divReal(A a) returns real",
                "			return this.i / a.i",
                "	init",
                "		A a1 = new A()",
                "		A a2 = new A()",
                "		real result = a1 / a2",
                "		if result == 1",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec3( real x, real y, real z )",
                "",
                "	public function vec3.op_plus( vec3 v )	returns vec3",
                "			return vec3(this.x + v.x, this.y + v.y, this.z + v.z)",
                "",
                "",
                "	init",
                "		vec3 v1 = vec3(1.,1.,1.)",
                "		vec3 v2 = vec3(1.,1.,1.)",
                "		vec3 v3 = v1 + v2",
                "		if v3.x == 2",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading2Abbreviation() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec3( real x, real y, real z )",
                "",
                "	public function vec3.op_plus( vec3 v )	returns vec3",
                "			return vec3(this.x + v.x, this.y + v.y, this.z + v.z)",
                "",
                "",
                "	init",
                "		vec3 v1 = vec3(1.,1.,1.)",
                "		vec3 v2 = vec3(1.,1.,1.)",
                "		v1 += v2",
                "		if v1.x == 2",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading3() {
        testAssertErrorsLines(true, "No operator overloading function",
                "package test",
                "	native testSuccess()",
                "	tuple vec3( real x, real y, real z )",
                "",
                "",
                "",
                "	init",
                "		vec3 v1 = vec3(1.,1.,1.)",
                "		vec3 v2 = vec3(1.,1.,1.)",
                "		vec3 v3 = v1 + v2",
                "endpackage");
    }

    @Test
    public void testOverloading4() {
        testAssertErrorsLines(true, "No operator overloading function",
                "package test",
                "	native testSuccess()",
                "	nativetype unit",
                "	init",
                "		unit u = null",
                "		unit u2 = null",
                "		unit u3 = u + u2",
                "endpackage");
    }

    @Test
    public void testOverloading_shortForm1() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function vec.op_mult(real r) returns vec",
                "			return vec(this.x*r, this.y*r, this.z*r)",
                "	init",
                "		vec v = vec(1,2,3)",
                "		v *= 2.",
                "		if v == vec(2,4,6)",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading_shortForm2() {
        testAssertErrorsLines(true, "expected real",
                "package test",
                "	native testSuccess()",
                "	tuple vec(real x, real y, real z)",
                "	function vec.op_mult(real r) returns vec",
                "			return vec(this.x*r, this.y*r, this.z*r)",
                "	init",
                "		vec v = vec(1,2,3)",
                "		v *= v",
                "		if v == vec(2,4,6)",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading_indexRead() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class ListLike",
                "		function op_index(int i) returns int",
                "			return i + 5",
                "	init",
                "		ListLike l = new ListLike()",
                "		int x = l[3]",
                "		if x == 8",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading_indexReadMissing() {
        testAssertErrorsLines(true, "op_index",
                "package test",
                "	class ListLike",
                "	init",
                "		ListLike l = new ListLike()",
                "		int x = l[3]",
                "endpackage");
    }

    @Test
    public void testOverloading_indexWrite() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class ListLike",
                "		int x = 0",
                "		function op_index(int i) returns int",
                "			return x + i",
                "		function op_indexAssign(int i, int v)",
                "			x = v + i",
                "	init",
                "		ListLike l = new ListLike()",
                "		l[1] = 2",
                "		if l[1] == 4",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testOverloading_indexWriteMissingSetter() {
        testAssertErrorsLines(true, "op_indexAssign",
                "package test",
                "	class ListLike",
                "		function op_index(int i) returns int",
                "			return i",
                "	init",
                "		ListLike l = new ListLike()",
                "		l[1] = 2",
                "endpackage");
    }

    @Test
    public void testOverloading_indexReadWrongIndexType() {
        testAssertErrorsLines(true, "op_index",
                "package test",
                "	class ListLike",
                "		function op_index(int i) returns int",
                "			return i",
                "	init",
                "		ListLike l = new ListLike()",
                "		int x = l[\"x\"]",
                "endpackage");
    }

    @Test
    public void testOverloading_indexWriteWrongIndexType() {
        testAssertErrorsLines(true, "op_indexAssign",
                "package test",
                "	class ListLike",
                "		function op_index(int i) returns int",
                "			return i",
                "		function op_indexAssign(int i, int v)",
                "	init",
                "		ListLike l = new ListLike()",
                "		l[\"x\"] = 1",
                "endpackage");
    }

    @Test
    public void testOverloading_indexWriteOnlySetter() {
        testAssertOkLines(false,
                "package test",
                "	class ListLike",
                "		function op_indexAssign(int i, int v)",
                "	init",
                "		ListLike l = new ListLike()",
                "		l[1] = 2",
                "endpackage");
    }

    @Test
    public void testOverloading_memberIndexWriteOnlySetter() {
        testAssertOkLines(false,
                "package test",
                "	class ListLike",
                "		function op_indexAssign(int i, int v)",
                "	class Box",
                "		ListLike l = new ListLike()",
                "	init",
                "		Box b = new Box()",
                "		b.l[1] = 2",
                "endpackage");
    }


}
