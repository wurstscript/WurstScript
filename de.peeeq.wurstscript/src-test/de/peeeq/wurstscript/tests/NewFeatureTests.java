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

}
