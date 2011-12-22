package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import com.google.common.base.Charsets;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
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
	
	
	public void testAssertOkLines(boolean executeProg, String ... input) {
		String prog = Utils.join(input, "\n") + "\n";
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}
	
	public void testAssertErrorsLines(boolean executeProg, String errorMessage, String ... input) {
		String prog = Utils.join(input, "\n") + "\n";
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, errorMessage);
	}
	
	public void testAssertOk(String name, boolean executeProg, String prog) {
		if (name.length() == 0) {
			name = Utils.getMethodName(1);
		}
		String errors = testScript(name, new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg, false);
		Assert.assertEquals("", errors);
	}

	public void testAssertOkFile(File file, boolean executeProg) throws IOException {
		Reader reader= new FileReader(file);
		String errors = testScript(Collections.singleton(file), null, file.getName(), executeProg, false);
		reader.close();
		Assert.assertEquals("", errors);
	}
	
	public void testAssertOkFileWithStdLib(File file, boolean executeProg) throws IOException {
		Reader reader= new FileReader(file);
		String errors = testScript(file.getAbsolutePath(), reader, file.getName(), executeProg, true);
		reader.close();
		Assert.assertEquals("", errors);
	}

	public void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
		name = Utils.getMethodName(2);
		String errors = testScript(name, new StringReader(prog), this.getClass().getSimpleName() + "_" + name, executeProg, false);
		Assert.assertTrue("No errors were discovered", errors.length() > 0);
		Assert.assertTrue(errors, errors.contains(errorMessage));
	}

	protected String testScript(String inputName, Reader input, String name, boolean executeProg, boolean withStdLib) {
		Map<String, Reader> inputs = Maps.newHashMap();
		inputs.put(inputName, input);
		return testScript(null, inputs, name, executeProg, withStdLib);
	}
	
	protected String testScript(Iterable<File> inputFiles, Map<String, Reader> inputs, String name, boolean executeProg, boolean withStdLib) {
		if (inputFiles == null) {
			inputFiles = Collections.emptyList();
		}
		if (inputs == null) {
			inputs = Collections.emptyMap();
		}
		
		boolean success = false;
		WurstGui gui = new WurstGuiCliImpl();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
		if (withStdLib) {
			compiler.loadFiles(new File("./lib/common.j"), new File("./lib/blizzard.j"));
		}
		for (File input : inputFiles) {
			compiler.loadFiles(input);
		}
		for (Entry<String, Reader> input : inputs.entrySet()) {
			compiler.loadReader(input.getKey(), input.getValue());
		}
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
		if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
			return pJassResult.getMessage();
		}

		if (executeProg) {
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
			if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
				return "Errors in optimized version: " + pJassResult.getMessage();
			}
		
			if (executeProg) {
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
		}
		
		if (executeProg && !success) {
			return "Succeed function not called";
		}
		return "";
	}


	



}
