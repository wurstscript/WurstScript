package de.peeeq.wurstscript.tests;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.peeeq.wurstscript.WurstCompiler;
import de.peeeq.wurstscript.WurstCompilerImpl;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreter;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILInterpreterImpl;
import de.peeeq.wurstscript.intermediateLang.interpreter.TestFailException;
import de.peeeq.wurstscript.intermediateLang.interpreter.TestSuccessException;
import de.peeeq.wurstscript.utils.NotNullList;

@RunWith(Parameterized.class)
public class TestScriptsTest {

	private final static String PSCRIPT_ENDING = ".pscript";

	private File file;

	public TestScriptsTest(File file) {
		if (file == null) {
			throw new IllegalArgumentException();
		}
		System.out.println("file a = " + file);
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		File dir = new File("./testscripts/valid");

		boolean exists = dir.exists();
		if (exists) {
			System.out.println("Directory " + dir + " exists!");
		} else {
			System.out.println("Directory " + dir + " could not be found!");
		}

		File[] fileList = dir.listFiles();
		List<Object[]> pscriptFiles = new NotNullList<Object[]>();

		if (fileList != null) {
			for (File f : fileList) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(PSCRIPT_ENDING)) {
					Object[] f_asArray = new Object[1];
					f_asArray[0] = f;
					pscriptFiles.add(f_asArray);
				}

			}
		}
		return pscriptFiles;
	}

	@Test
	public void runTest() {
		try {
			System.out.println("file b = " + file);
			String filename = file.getAbsolutePath();
			System.out.println("parsing script ...");
			WurstCompiler compiler = new WurstCompilerImpl(new WurstGuiCliImpl());
			compiler.loadFiles(file);
			compiler.parseFiles();

			ILprog prog = compiler.getILprog();

			if (prog == null) {
				throw new TestFailException("Compiler errors ...");
			}

			File outputFile = new File(filename.replaceAll("\\"+PSCRIPT_ENDING, ".j"));
			StringBuilder sb = new StringBuilder();
			prog.printJass(sb, 0);
			try {
				FileWriter writer = new FileWriter(outputFile, false);
				writer.append(sb.toString());
				writer.close();
			} catch (IOException e) {
				throw new Error(e);
			}

			ILInterpreter interpreter = new ILInterpreterImpl();
			interpreter.LoadProgram(prog);
			interpreter.executeFunction("test_runTest");
		} catch (TestFailException e) {
			assertFalse("Failed: " + e.getVal(), true);
		} catch (TestSuccessException e)  {
			
		}
	}
}
