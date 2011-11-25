package de.peeeq.wurstscript.tests;


import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WurstCompilerImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.utils.NotNullList;

public class TestInvalidNG {

	private final static String PSCRIPT_ENDING = ".pscript";

	@DataProvider
	public Object[][] getTestfiles() {
		File dir = new File("./testscripts/invalid");

		boolean exists = dir.exists();
		if (exists) {
			System.out.println("Directory " + dir + " exists!");
		} else {
			System.out.println("Directory " + dir + " could not be found!");
		}

		File[] fileList = dir.listFiles();
		List<File> pscriptFiles = new NotNullList<File>();

		if (fileList != null) {
			for (File f : fileList) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(PSCRIPT_ENDING)) {
					pscriptFiles.add(f);
				}

			}
		}
		Object[][] result = new Object[pscriptFiles.size()][];
		int i = 0;
		for (File f : pscriptFiles) {
			Object[] fa = {f};
			result[i] = fa;
			i++;
		}
		return result;
	}

	@Test(dataProvider = "getTestfiles")
	public void testScript(File file) throws IOException, InterruptedException {
		boolean success = false;
		System.out.println("file b = " + file);
		String filename = file.getAbsolutePath();
		System.out.println("parsing script ...");
		WurstGuiLogger gui = new WurstGuiLogger();
		WurstCompilerImpl compiler = new WurstCompilerImpl(gui);
		compiler.loadFiles(file);
		compiler.parseFiles();

		JassProg prog = compiler.getILprog();

		File logfile = new File(file.getAbsoluteFile() + ".log");
		if (prog == null) {
			// sucess
			Files.write("Prog was null.\n" + gui.getErrors(), logfile , Charsets.UTF_8);
		} else if (gui.getErrorCount() > 0) {
			// sucess
			Files.write("Errors in validation phase:\n" + gui.getErrors(), logfile, Charsets.UTF_8);
			
		} else {
			// failure
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
			if (gui.getErrorCount() > 0) { 
				// sucess
				Files.write("Errors in compile phase:\n" + gui.getErrors(), logfile, Charsets.UTF_8);
				assertTrue("Errors in comile phase\n", false);
			} else {
				assertTrue("Program compiled (but should not)...", false);
			}
		}
	}

}
