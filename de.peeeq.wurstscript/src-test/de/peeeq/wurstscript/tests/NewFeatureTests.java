package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class NewFeatureTests extends PscriptTest {
	private static final String TEST_DIR = "./testscripts/concept/";
	
	@Test
	public void testEnums() {
		try {
			testAssertOkFileWithStdLib(new File(TEST_DIR + "enums.wurst"), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMinusOne() {
		try {
			testAssertOkFileWithStdLib(new File(TEST_DIR + "MinusOne.wurst"), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	/*
	 * 
	 * case A
	 * 	foo()
	 * case B
	 * 	foo()
	 * 
	 * 
	 * i =1 
	 * lastIf = (if b == Blub.A
	 * 	skip
	 * else
	 * 	LEER)
	 * 
	 * i = 2
	 * lastIf.setElse()
	 * 
	 * if b == Blub.A
	 *   skip
	 * else 
	 * 		if b == Blub.B
	 *   		testSuccess()
	 * 1,2,3,4,5
	 * b <= 3
	 * switch foo()
	 * 	case Blub.A
	 * 
	 */
	

}
