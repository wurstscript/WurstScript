package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class SimpleStatementTests extends PscriptTest {

	@Test
	public void testIf1() {
		assertOk(true, 
				"if 2 == 2 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf2() {
		assertOk(true, 
				"if 10 > 5 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf3() {
		assertOk(true, 
				"if not 10 > 5 or not 5 > 10 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf4() {
		assertOk(true, 
				"if 10 >= 10 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf5() {
		assertOk(true, 
				"if -4 <= -4 ",
				"	testSuccess()\n" +	
				"");
	}
	
	@Test
	public void testIf6() {
		assertOk(true, 
				"if 3 != 2 ",
				"	testSuccess()",	
				"");
	}
	
	@Test
	public void testIf7() {
		assertOk(true, 
				"if (10 == 10 and 5 == 5)",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testWhile1() {
		assertOk(true,
				"int x = 10",
				"while x > 2",
				"	x = x - 1",
				"if x == 2",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testWhileBreak() {
		assertOk(true,
				"int x = 10",
				"while true",
				"	x = x - 1",
				"	if x <= 2",
				"		break",
				"if x == 2",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testFor1() {
		assertOk(true,
				"int x = 0",
				"for int i = 1 to 10",
				"	x = x + 1",
				"if x == 10",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testFor2() {
		assertOk(true,
				"int x = 0",
				"for int i = 1 to 10",
				"	x = x + i",
				"if x == 55",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testForStep() {
		assertOk(true,
				"int x = 0",
				"for int i = 0 to 10 step 3",
				"	x = x + i",
				"if x == 18",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testForDownStep() {
		assertOk(true,
				"int x = 0",
				"for int i = 10 downto 0 step 3",
				"	x = x + i",
				"if x == 22",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void testForIn() {
		testAssertOkLines(true, 
			"package test",
			"	class IntList",
			"		static int array elements",
			"		int size = 0",
			
			"		private function getOffset() returns int",
			"			return 64*((this castTo int)-1)",
			
			"		function add(int x) returns IntList",
			"			elements[getOffset() + size] = x",
			"			size++",
			"			return this",
			
			"		function get(int i) returns int",
			"			return elements[getOffset() + i]",
			
			"		function iterator() returns IntListIterator",
			"			return new IntListIterator(this)",
			
			
			"	class IntListIterator",
			"		IntList list",
			"		int pos = 0",
			
			"		construct(IntList list)",
			"			this.list = list",
			
			"		function hasNext() returns boolean",
			"			return pos < list.size",
			
			"		function next() returns int",
			"			pos++",
			"			return list.get(pos-1)",
			
			"		function close()",
			"			destroy this",
			
			
			"	init",
			"		IntList list = new IntList().add(7).add(3).add(5)",
			"		int sum = 0",
			"		for int i in list",
			"			sum += i",
			"		if sum == 15",
			"			testSuccess()",
			
			
			"	native testSuccess()",
			"endpackage"
		);
	}
	
	
	@Test
	public void test_inc() {
		assertOk(true,
				"int x = 5",
				"x++",
				"if x == 6",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void test_dec() {
		assertOk(true,
				"int x = 5",
				"x--",
				"if x == 4",
				"	testSuccess()",
				"");
	}

	@Test
	public void test_pluseq() {
		assertOk(true,
				"int x = 5",
				"x += 2",
				"if x == 7",
				"	testSuccess()",
				"");
	}

	@Test
	public void test_multeq() {
		assertOk(true,
				"int x = 5",
				"x *= 2",
				"if x == 10",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void test_minuseq() {
		assertOk(true,
				"int x = 5",
				"x -= 2",
				"if x == 3",
				"	testSuccess()",
				"");
	}
	
	@Test
	public void test_unitialized() {
		assertError(false, "not initialized",
				"int x",
				"int y = 2",
				"if y == 3",
				"	x = 2",
				"x++");
	}
	
	@Test
	public void test_unitialized2() {
		assertOk(false,
				"int x",
				"int y = 2",
				"if y == 3",
				"	x = 2",
				"else",
				"	x = 4",
				"x++");
	}
	
	@Test
	public void test_arrayUpdate() {
		assertOk(false,
				"int array x",
				"x[5] = 2",
				"x[5] += 3"
				);
	}
	
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				"	init \n" +
				"		" + Utils.join(body, "\n		") +
				"\n" +
				"endpackage\n";
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				"	init \n" +
				"		" + Utils.join(body, "\n		") +
				"\n" +
				"endpackage\n";
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, expected);
	}

}
