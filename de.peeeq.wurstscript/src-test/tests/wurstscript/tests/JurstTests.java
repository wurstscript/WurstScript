package tests.wurstscript.tests;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.peeeq.wurstscript.utils.Utils;

public class JurstTests extends WurstScriptTest {
	
	
	@Test
	public void thisAsVarNameInJass() { // #498
		String jassCode = Utils.string(
				"function foo takes integer this returns integer",
				"	return this",
				"endfunction",
				"function bar takes integer x returns integer",
				"	local integer this = x",
				"	return this",
				"endfunction");
		
		
		String jurstCode = Utils.string(
				"package test",
				"	native testSuccess()",
				"	init",
				"		if foo(5) == 5",
				"			testSuccess()",
				"		end",
				"	end",
				"endpackage");
		
		boolean withStdLib = false;
		boolean executeTests = false;
		boolean executeProg = true;
		String name = "Blub";
		Map<String, String> inputs = ImmutableMap.of(
				"example.j", jassCode,
				"test.jurst", jurstCode
				);
		Iterable<File> inputFiles = Collections.emptyList();
		testScript(inputFiles, inputs, name, executeProg, withStdLib, executeTests);
	}
	
}
