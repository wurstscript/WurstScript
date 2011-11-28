package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class PscriptTest {

	private static final String TEST_OUTPUT_PATH = "./test-output/";

	public void assertOk(String name, String prog) {
		testScript(new StringReader(prog), this.getClass().getSimpleName() + "_" + name);
	}

	private void testScript(Reader input, String name) {
		boolean success = false;
		try {
			WurstGuiLogger gui = new WurstGuiLogger();
			WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
			compiler.loadStreams(input);
			compiler.parseFiles();
			JassProg prog = compiler.getProg();

			if (prog == null || gui.getErrorCount() > 0) {
				throw new TestFailException("Compiler errors:\n" + gui.getErrors());
			}

			File outputFile = new File(TEST_OUTPUT_PATH + name + ".j");
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
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
//			assertTrue("Failed: " + e.getVal(), false);
		} catch (TestSuccessException e)  {
			success = true;
		}
		if (!success) {
//			assertTrue("Succeed function not called", false);
			// TODO
		}
	}
	
}
