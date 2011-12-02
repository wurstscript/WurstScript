package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.Assert;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizer;
import de.peeeq.wurstscript.jassoptimizer.JassOptimizerImpl;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.utils.Utils;

public class PscriptTest {

	private static final String TEST_OUTPUT_PATH = "./test-output/";

	protected boolean testOptimizer() {
		return true;
	}
	
	public void testAssertOk(String name, boolean executeProg, String prog) {
		if (name.length() == 0) {
			name = Utils.getMethodName(1);
		}
		String errors = testScript(new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg);
		Assert.assertEquals("", errors);
	}

	public void testAssertOkFile(File file, boolean executeProg) throws IOException {
		Reader reader= new FileReader(file);
		String errors = testScript(reader, file.getName(), executeProg);
		reader.close();
		Assert.assertEquals("", errors);
	}

	public void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
		name = Utils.getMethodName(2);
		String errors = testScript(new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg);
		Assert.assertTrue(errors.length() > 0);
		Assert.assertTrue(errors, errors.contains(errorMessage));
	}

	protected String testScript(Reader input, String name, boolean executeProg) {
		boolean success = false;
		WurstGuiLogger gui = new WurstGuiLogger();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
		compiler.loadStreams(input);
		compiler.parseFiles();
		JassProg prog = compiler.getProg();

		if (prog == null || gui.getErrorCount() > 0) {
			return gui.getErrors();
		}

		File outputFile = new File(TEST_OUTPUT_PATH + name + ".j");
		new File(TEST_OUTPUT_PATH).mkdirs();
		try {
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);
		} catch (IOException e) {
			return "IOException, could not write jass file "+ outputFile + "\n"  + gui.getErrors();
		}


		// run pjass:
		Result pJassResult = Pjass.runPjass(outputFile);
		System.out.println(pJassResult.getMessage());
		if (!pJassResult.isOk()) {
			return pJassResult.getMessage();
		}


		try {
			// run the interpreter
			success = false;
			JassInterpreter interpreter = new JassInterpreter();
			interpreter.trace(true);
			interpreter.LoadProgram(prog);
			interpreter.executeFunction("main");
		} catch (TestFailException e) {
			return e.getVal();
		} catch (TestSuccessException e)  {
			success = true;
		}

		// run the optimizer:
		if (testOptimizer()) {
			JassOptimizer optimizer = new JassOptimizerImpl();
			try {
				optimizer.optimize(prog);
			} catch (FileNotFoundException e) {
				return "Optimizer Exception " + e;
			}
			
			
			
	
			// write optimized file:
			try {
				outputFile = new File(TEST_OUTPUT_PATH + name + "_opt.j");
				StringBuilder sb = new StringBuilder();
				new JassPrinter(false).printProg(sb, prog);
				Files.write(sb.toString(), outputFile, Charsets.UTF_8);
			} catch (IOException e) {
				return "IOException, could not write optimized file "+ outputFile + "\n"  + gui.getErrors();
			}
	
			// test optimized file with pjass:
			pJassResult = Pjass.runPjass(outputFile);
			System.out.println(pJassResult.getMessage());
			if (!pJassResult.isOk()) {
				return "Errors in optimized version: " + pJassResult.getMessage();
			}
		
		
			try {
				success = false;
				// run the interpreter with the optimized program
				JassInterpreter interpreter = new JassInterpreter();
				interpreter.trace(true);
				interpreter.LoadProgram(prog);
				interpreter.executeFunction("main");
			} catch (TestFailException e) {
				return e.getVal();
			} catch (TestSuccessException e)  {
				success = true;
			}
		}
		
		if (executeProg && !success) {
			return "Succeed function not called";
		}
		return "";
	}



}
