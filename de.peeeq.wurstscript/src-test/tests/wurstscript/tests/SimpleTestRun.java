package tests.wurstscript.tests;



import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class SimpleTestRun {


	private static final String WURSTSCRIPT_ENDING = ".wurst";


	public static void main(String ... args) throws IOException, InterruptedException {
		String testFile = "./testscripts/realbugs/test.wurst";
		if (args.length == 1) {
			testFile = args[0];
		}
		testScript(new File(testFile));
	}


	public static void testScript(File file) throws IOException, InterruptedException {
		boolean success = false;
		WurstGui gui = null;
		try{
			System.out.println("file b = " + file);
			String filename = file.getAbsolutePath();
			System.out.println("parsing script ...");
//			gui = new WurstGuiImpl();
			gui = new WurstGuiCliImpl();
			WurstConfig config = new WurstConfig();
			WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(config, gui, RunArgs.defaults());
			config.setSetting("lib", "../Wurstpack/wurstscript/lib/");
			compiler.loadFiles("../Wurstpack/wurstscript/common.j");
			compiler.loadFiles("../Wurstpack/wurstscript/Blizzard.j");
			compiler.loadFiles(file);
			compiler.parseFiles();
			JassProg prog = compiler.getProg();

			if (prog == null) {
				throw new TestFailException("Compiler errors:\n" + gui.getErrors());
			}

			if (gui.getErrorCount() > 0) {
				throw new TestFailException("Compiler Errors:\n" + gui.getErrors());
			}

			File outputFile = new File(filename.replaceAll("\\"+WURSTSCRIPT_ENDING+"$", ".j"));
			if (!outputFile.getName().endsWith(".j")) {
				outputFile = new File(outputFile.getAbsolutePath()+".j");
			}
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);

			// run pjass:
			Result pJassResult = Pjass.runPjass(outputFile);
			System.out.println(pJassResult.getMessage());
			if (!pJassResult.isOk()) {
				throw new TestFailException(pJassResult.getMessage());
			}

			// run the interpreter

			JassInterpreter interpreter = new JassInterpreter();
			interpreter.trace(true);
			interpreter.loadProgram(prog);
			interpreter.executeFunction("main");


		} catch (TestFailException e) {
			assertTrue("Failed: " + e.getVal(), false);
		} catch (TestSuccessException e)  {
			success = true;
		} finally {
			if (gui != null) {
				gui.sendFinished();
			}
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
