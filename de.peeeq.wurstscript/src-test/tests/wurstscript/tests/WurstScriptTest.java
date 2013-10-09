package tests.wurstscript.tests;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.jassinterpreter.JassInterpreter;
import de.peeeq.wurstio.jassinterpreter.NativeFunctionsIO;
import de.peeeq.wurstio.utils.FileReading;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

public class WurstScriptTest {

	private static final String TEST_OUTPUT_PATH = "./test-output/";

	
	protected boolean testOptimizer() {
		return true;
	}
	
	static class CU {
		final public String content;
		final public String name;
		public CU(String name, String content) {
			this.name = name;
			this.content = content;
		}
	}
	
	public CU compilationUnit(String name, String ... input) {
		return new CU(name, Utils.join(input, "\n"));
	}
	
	public void testAssertOk(boolean excuteProg, boolean withStdLib, CU ... units) {
		List<File> inputFiles = Collections.emptyList();
		Map<String, String> inputs = Maps.newLinkedHashMap();
		for (CU cu : units) {
			inputs.put(cu.name, cu.content);
		}
		String name = UtilsIO.getMethodName(2);
		testScript(inputFiles, inputs, name, excuteProg, withStdLib, false);
	}
	
	public void testAssertErrors(String errorMessage, boolean excuteProg, boolean withStdLib, CU ... units) {
		List<File> inputFiles = Collections.emptyList();
		Map<String, String> inputs = Maps.newLinkedHashMap();
		for (CU cu : units) {
			inputs.put(cu.name, cu.content);
		}
		String name = UtilsIO.getMethodName(2);
		try {
			testScript(inputFiles, inputs, name, excuteProg, withStdLib, false);
			Assert.assertTrue("No errors were discovered", false);
		} catch (CompileError e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains(errorMessage));
		}
	}
	
	
	public void testAssertOkLines(boolean executeProg, String ... input) {
		String prog = Utils.join(input, "\n") + "\n";
		testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
	}
	
	public void testAssertErrorsLines(boolean executeProg, String errorMessage, String ... input) {
		String prog = Utils.join(input, "\n") + "\n";
		testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, errorMessage);
	}
	
	public void testAssertOk(String name, boolean executeProg, String prog) {
		if (name.length() == 0) {
			name = UtilsIO.getMethodName(1);
		}
		testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, false);
	}

	public void testAssertOkFile(File file, boolean executeProg) throws IOException {
		Reader reader= FileReading.getFileReader(file);
		testScript(Collections.singleton(file), null, file.getName(), executeProg, false, false);
		reader.close();
	}
	
	public void testAssertOkFileWithStdLib(File file, boolean executeProg) throws IOException {
		String input = Files.toString(file, Charsets.UTF_8);
		testScript(file.getAbsolutePath(), input, file.getName(), executeProg, true);
	}
	
	public void testAssertOkLinesWithStdLib(boolean executeProg, String ... input) {
		String prog = Utils.join(input, "\n") + "\n";
		String name = UtilsIO.getMethodName(1);
		testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, true);
	}
	
	public void testAssertErrorFileWithStdLib(File file, String errorMessage, boolean executeProg) throws IOException {
		try { 
			String input = Files.toString(file, Charsets.UTF_8);
			testScript(file.getAbsolutePath(), input, file.getName(), executeProg, true);
		} catch (CompileError e) {
			Assert.assertTrue(e.toString(), e.getMessage().contains(errorMessage));
		}
	}

	public void testAssertErrors(String name, boolean executeProg, String prog, String errorMessage) {
		name = UtilsIO.getMethodName(2);
		try {
			testScript(name, prog, this.getClass().getSimpleName() + "_" + name, executeProg, false);
			Assert.assertTrue("No errors were discovered", false);
		} catch (CompileError e) {
			if (!e.getMessage().toLowerCase().contains(errorMessage.toLowerCase())) {
				throw e;
			}
		}
		
		
	}

	protected void testScript(String inputName, String input, String name, boolean executeProg, boolean withStdLib) {
		Map<String, String> inputs = Maps.newLinkedHashMap();
		inputs.put(inputName, input);
		testScript(null, inputs, name, executeProg, withStdLib, false);
	}
	
	protected void testScript(Iterable<File> inputFiles, Map<String, String> inputs, String name, boolean executeProg, boolean withStdLib, boolean executeTests) {
		// test without inlining and optimization
		testScript(inputFiles, inputs, name, executeProg, withStdLib, executeTests, 
				new RunArgs(new String[] {}));
		
		// test with inlining		
		testScript(inputFiles, inputs, name + "_inl", executeProg, withStdLib, executeTests, 
				new RunArgs(new String[] {"-inline"	}));
		
		// test with inlining and local optimization 
		testScript(inputFiles, inputs, name + "_inlopt", executeProg, withStdLib, executeTests, 
				new RunArgs(new String[] {"-inline", "-localOptimizations"}));
		
		
	}
	
	protected void testScript(Iterable<File> inputFiles, Map<String, String> inputs, String name, boolean executeProg, boolean withStdLib, boolean executeTests, RunArgs runArgs) {
		if (inputFiles == null) {
			inputFiles = Collections.emptyList();
		}
		if (inputs == null) {
			inputs = Collections.emptyMap();
		}
		
		boolean success = false;
		WurstGui gui = new WurstGuiCliImpl();
		WurstConfig config = new WurstConfig();
		config.setSetting("lib", "../Wurstpack/wurstscript/lib/");
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(config , gui, runArgs);
		compiler.getErrorHandler().enableUnitTestMode();
		if (withStdLib) {
			compiler.loadFiles(new File("./resources/common.j"), new File("./resources/blizzard.j"));
		}
		for (File input : inputFiles) {
			compiler.loadFiles(input);
		}
		for (Entry<String, String> input : inputs.entrySet()) {
			compiler.loadReader(input.getKey(), new StringReader(input.getValue()));
		}
		WurstModel model = compiler.parseFiles();
		
		
		if (gui.getErrorCount() > 0) {
			throw gui.getErrorList().get(0);
		}
		
		
		ImProg imProg = compiler.getImProg();
		writeJassImProg(name, gui, imProg);
		if (executeTests) {
			CompiletimeFunctionRunner cfr = new CompiletimeFunctionRunner(imProg, null, gui, FunctionFlag.IS_TEST);
			cfr.run();
			WLogger.info("Successfull tests: " + cfr.getSuccessTests().size());
			int failedTestCount = cfr.getFailTests().size();
			WLogger.info("Failed tests: " + failedTestCount);
			if (failedTestCount > 0 ) {
				for (Entry<ImFunction, Pair<ImStmt, String>> e : cfr.getFailTests().entrySet()) {
					Assert.assertFalse(Utils.printElementWithSource(e.getKey().attrTrace()) + " " + e.getValue().getB()
							+ "\n" + "at " + Utils.printElementWithSource(e.getValue().getA().attrTrace()), true);
				}
			} else {
				success = true;
			}
		} if (executeProg) {
			
			try {
				// run the interpreter on the intermediate language
				success = false;
				ILInterpreter interpreter = new ILInterpreter(imProg, gui, null);
				interpreter.addNativeProvider(new NativeFunctionsIO());
//				interpreter.addNativeProvider(new CompiletimeNatives((ProgramStateIO) interpreter.getGlobalState()));
				interpreter.executeFunction("main");
			} catch (TestFailException e) {
				throw e;
			} catch (TestSuccessException e)  {
				success = true;
			}
		}
		
		
		JassProg prog = compiler.getProg();
		
		
		if (gui.getErrorCount() > 0) {
			throw gui.getErrorList().get(0);
		}
		Assert.assertNotNull(prog);

		File outputFile = writeJassProg(name, gui, prog);


		// run pjass:
		Result pJassResult = Pjass.runPjass(outputFile);
		WLogger.info(pJassResult.getMessage());
		if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
			throw new Error(pJassResult.getMessage());
		}

		if (executeProg) {
			try {
				// run the interpreter
				success = false;
				JassInterpreter interpreter = new JassInterpreter();
				interpreter.trace(true);
				interpreter.loadProgram(prog);
				interpreter.executeFunction("main");
			} catch (TestFailException e) {
				throw e;
			} catch (TestSuccessException e)  {
				success = true;
			}
		}

		// run the optimizer:
		WLogger.info("optimizer1");
		if (testOptimizer()) {

			
	
			// write optimized file:
			outputFile = writeJassProg(name+"opt", gui, prog);
	
			// test optimized file with pjass:
			pJassResult = Pjass.runPjass(outputFile);
			WLogger.info(pJassResult.getMessage());
			if (!pJassResult.isOk() && !pJassResult.getMessage().equals("IO Exception")) {
				throw new Error("Errors in optimized version: " + pJassResult.getMessage());
			}
		
			if (executeProg) {
				try {
					success = false;
					// run the interpreter with the optimized program
					JassInterpreter interpreter = new JassInterpreter();
					interpreter.trace(true);
					interpreter.loadProgram(prog);
					interpreter.executeFunction("main");
				} catch (TestFailException e) {
					throw e;
				} catch (TestSuccessException e)  {
					success = true;
				}
			}
		}
		
		if (executeProg && !success) {
			throw new Error("Succeed function not called");
		}
	}

	/**
	 * writes a jass prog to a file
	 */
	private File writeJassProg(String name, WurstGui gui, JassProg prog) throws Error {
		File outputFile = new File(TEST_OUTPUT_PATH + name + ".j");
		new File(TEST_OUTPUT_PATH).mkdirs();
		try {
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);
		} catch (IOException e) {
			throw new Error("IOException, could not write jass file "+ outputFile + "\n"  + gui.getErrors());
		}
		return outputFile;
	}

	/**
	 * writes a jass prog to a file
	 */
	private File writeJassImProg(String name, WurstGui gui, ImProg prog) throws Error {
		File outputFile = new File(TEST_OUTPUT_PATH + name + ".jim");
		new File(TEST_OUTPUT_PATH).mkdirs();
		try {
			StringBuilder sb = new StringBuilder();
			prog.print(sb, 0);
			
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);
		} catch (IOException e) {
			throw new Error("IOException, could not write jass file "+ outputFile + "\n"  + gui.getErrors());
		}
		return outputFile;
	}

	



}
