package de.peeeq.wurstscript.jassoptimizer;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class JassOptimizerImpl implements JassOptimizer {

	/**
	 * a main method to simply test the optimizer 
	 * @throws IOException 
	 */
	public static void main(String ... args) throws IOException {
		String testFile = "./testscripts/valid/If_1.pscript";
		if (args.length == 1) {
			testFile = args[0];
		}
		
		// compile the testFile to jass (note that this is not complete yet, but might be enough for testing the optimizer a bit)
		WurstGuiLogger gui = new WurstGuiLogger();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
		compiler.loadFiles(new File(testFile));
		compiler.parseFiles();

		JassProg prog = compiler.getProg();

		// no we have a jass program
		// print original
		Files.write(new JassPrinter().printProg(prog), new File(testFile+".original.j"), Charsets.UTF_8);
		
		// optimize it:
		new JassOptimizerImpl().optimize(prog);
		
		// print the optimized version
		Files.write(new JassPrinter().printProg(prog), new File(testFile+".optimized.j"), Charsets.UTF_8);
	}
	
	@Override
	public void optimize(JassProg prog) {
		
		// here is a small example in which we rename every function to frotty:
		
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassFunction jassFunction) {
				jassFunction.setName("frotty");
			}
			
			// look at the JassProg.DefaultVisitor to find other elements which you can visit
			
		});
		
	}

}
