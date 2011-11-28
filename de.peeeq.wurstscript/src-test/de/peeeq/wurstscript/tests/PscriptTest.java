package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Assert;

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

	public void testAssertOk(String name, boolean executeProg, String prog) {
		String errors = testScript(new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg);
		Assert.assertEquals("", errors);
	}
	
	public void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
		String errors = testScript(new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg);
		Assert.assertTrue(errors.length() > 0);
		Assert.assertTrue(errors, errors.contains(errorMessage));
	}

	protected String testScript(Reader input, String name, boolean executeProg) {
		boolean success = false;
		try {
			WurstGuiLogger gui = new WurstGuiLogger();
			WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
			compiler.loadStreams(input);
			compiler.parseFiles();
			JassProg prog = compiler.getProg();

			if (prog == null || gui.getErrorCount() > 0) {
				return gui.getErrors();
			}

			File outputFile = new File(TEST_OUTPUT_PATH + name + ".j");
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			try {
				FileWriter writer = new FileWriter(outputFile, false);
				writer.append(sb.toString());
				writer.close();
			} catch (IOException e) {
				return gui.getErrors();
			}

			// run pjass:
			Result pJassResult = Pjass.runPjass(outputFile);
			System.out.println(pJassResult.getMessage());
			if (!pJassResult.isOk()) {
				return pJassResult.getMessage();
			}

			// run the interpreter
			JassInterpreter interpreter = new JassInterpreter();
			interpreter.trace(true);
			interpreter.LoadProgram(prog);
			interpreter.executeFunction("main");
		} catch (TestFailException e) {
			return e.getVal();
		} catch (TestSuccessException e)  {
			success = true;
		}
		if (executeProg && !success) {
			return "Succeed function not called";
		}
		return "";
	}
	
}
