package de.peeeq.wurstscript.jassoptimizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.utils.Debug;


public class JassOptimizerImpl implements JassOptimizer {
	String[] standards;
	int standardsAmount = 0;
	
	public static void main(String ... args) throws IOException {

		File fe = new File("./testscripts/valid/optimizer/test.wurst");
		
		BufferedWriter output = new BufferedWriter(new FileWriter(fe));
		NameGenerator ng = new NameGenerator();

		for( int i = 0; i < 40000; i++) { // Got also tested with 132651
			output.write("    int " + String.valueOf(ng.getUniqueToken()) );
			output.newLine();
		}
			
		output.write("endpackage");
		output.close();
		Debug.println("jka");
			
	}	
	
	
	
	
	
	@Override
	public void optimize(final JassProg prog) throws FileNotFoundException {
		System.out.println("Optimizer Start");
		// Cleanup the Globals and Functions
		prog.getGlobals().removeAll(prog.attrIgnoredVariables());
		prog.getFunctions().removeAll(prog.attrIgnoredFunctions());

		// Remove useless Stuff
		System.out.println("Garbage");
		GarbageRemover.removeGarbage(prog);
		
		// Compress Names
		NameCompressor.compressNames(prog);
		
		
			
		
		
	}

}
