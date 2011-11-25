package de.peeeq.wurstscript.tests;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class SimpleTestRun {


	private static final String PSCRIPT_ENDING = ".pscript";

	
	public static void main(String ... args) throws IOException, InterruptedException {
		//String testFile = "./testscripts/valid/modules_1.pscript";
		String testFile = "./testscripts/valid/Classes_1.pscript";
		if (args.length == 1) {
			testFile = args[0];
		}
		testScript(new File(testFile));
	}
	
	
	public static void testScript(File file) throws IOException, InterruptedException {
		boolean success = false;
		try {
			System.out.println("file b = " + file);
			String filename = file.getAbsolutePath();
			System.out.println("parsing script ...");
			WurstGuiLogger gui = new WurstGuiLogger();
			WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
			compiler.loadFiles(file);
			compiler.parseFiles();
			JassProg prog = compiler.getProg();

			if (prog == null) {
				throw new TestFailException("Compiler errors:\n" + gui.getErrors());
			}

			if (gui.getErrorCount() > 0) {
				throw new TestFailException("Compiler Errors:\n" + gui.getErrors());
			}

			File outputFile = new File(filename.replaceAll("\\"+PSCRIPT_ENDING, ".j"));
			StringBuilder sb = new StringBuilder();
			JassPrinter.printProg(sb, prog, false);
			try {
				FileWriter writer = new FileWriter(outputFile, false);
				writer.append(sb.toString());
				writer.close();
			} catch (IOException e) {
				throw new Error(e);
			}

			// run pjass:
			Result pJassResult = Pjass.runPjass(outputFile);
			System.out.println(pJassResult.getMessage());
			if (!pJassResult.isOk()) {
				throw new TestFailException(pJassResult.getMessage());
			}

			// run the interpreter
			JassInterpreter interpreter = new JassInterpreter();
			interpreter.trace(true);
			interpreter.LoadProgram(prog);
			interpreter.executeFunction("main");
		} catch (TestFailException e) {
			assertTrue("Failed: " + e.getVal(), false);
		} catch (TestSuccessException e)  {
			success = true;
		}
		if (!success) {
			assertTrue("Succeed function not called", false);
		}
	}

	private static void assertTrue(String string, boolean b) {
		if (!b) {
			throw new Error(string);
		}
	}

}
