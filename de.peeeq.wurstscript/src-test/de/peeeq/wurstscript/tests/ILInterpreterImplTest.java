package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class ILInterpreterImplTest {
	
	private final static String PSCRIPT_ENDING = ".pscript"; 
	
	static public void main(String ... args) throws IOException {
		
		File dir = new File("./testscripts/valid/other");
		
		boolean exists = dir.exists();
		if (exists) {
			System.out.println("Directory " + dir + " exists!");
		} else {
			System.out.println("Directory " + dir + " could not be found!");	
		}
		
		
		File[] fileList = dir.listFiles();
		List<File> pscriptFiles = new LinkedList<File>();
		
		int files = 0;
		if ( fileList != null ) {
			for(File f : fileList) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(PSCRIPT_ENDING)) {
					pscriptFiles.add(f);
					files++;
				}
	
			}
		}
		
		int testsFailed = 0;
		int testCount = 0;
		System.out.println( "Found Files: " + files );
		for ( File file : pscriptFiles) {
			
			System.out.println( "----------------------------------------------");
			System.out.println( "Testing file: " + file );
			try {
				testCount++;
				runTest(file.getPath());
				System.out.println("Test did not succeed");
				testsFailed++;
			} catch (TestFailException e) {
				testsFailed++;
				System.out.println("Failed: " + e.getVal());
			} catch (TestSuccessException e) {
				System.out.println("Ok");
			} catch (Throwable e) {
				testsFailed++;
				System.err.println(file + " failed with exception.");
				e.printStackTrace();
			}
			System.out.println();
			Process p = Runtime.getRuntime().exec("lib/pjass.exe common.j blizzard.j " + file.getPath());
		}
		
		System.out.println(testsFailed + " of " + testCount + " Tests failed.");
		if (testsFailed == 0) {
			System.out.println("Everything went better than expected :)");
		}
		
	}
	
	
	private static void runTest(String filename) {
		System.out.println("parsing script ...");
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(new WurstGuiCliImpl());
		compiler.loadFiles(filename);
		compiler.parseFiles();		
		
		JassProg prog = compiler.getProg();
		
		if (prog == null) {
			throw new TestFailException("Compiler errors ...");
		}
		
		File outputFile = new File(filename.replaceAll(PSCRIPT_ENDING, ".j"));
		StringBuilder sb = new StringBuilder();
		new JassPrinter(true).printProg(sb, prog);
		try {
			FileWriter writer = new FileWriter(outputFile, false);
			writer.append(sb.toString());
			writer.close();
		}
		catch (IOException e) {
			throw new Error(e);
		}
		
		
		
		JassInterpreter interpreter = new JassInterpreter();
		interpreter.LoadProgram(prog);
		interpreter.executeFunction("test_runTest");
	}

	
}