package de.peeeq.wurstscript.jassoptimizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.utils.Utils;

public class JassOptimizerImpl implements JassOptimizer {
	String[] standards;
	int standardsAmount = 0;
	/**
	 * a main method to simply test the optimizer 
	 * @throws IOException 
	 */
	public static void main(String ... args) throws IOException {
		String testFile = "./testscripts/valid/optimizertest_1.pscript";
		if (args.length == 1) {
			testFile = args[0];
		}
		
		// compile the testFile to jass (note that this is not complete yet, but might be enough for testing the optimizer a bit)
		WurstGui gui = new WurstGuiImpl();
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
	
	/**
	 * Sets predefined replacements like Filter instead of Condition
	 * @param map
	 */
	private void setHashmapPredefines(HashMap<String, String> map) {
		map.put("Condition", "Filter");
	}
	
	private void setStandards() {
		standards  = new String[20];
		try {
			Scanner sc = new Scanner(new File("lib/restrictedStandardFunctions.txt"));
			int i = 0;
			while(sc.hasNext()){
				String s = sc.next();
				standards[i] = s;
				i++;
			}
			standardsAmount = i;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public boolean isStandard(String name) {
		for( int i = 0; i <= standardsAmount; i++){
			if ( name == standards[i] ){
				return true;
			}
		}
		return false;
	}
	
	
	public Set<String> getEverythingInEForTRVE(JassProg prg) {
		final Set<String> usedFunctions = Sets.newHashSet(); 
		prg.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassStmtCall jassCall) {
				String funcname = jassCall.getFunctionName();
				System.out.println("name: " + funcname);
				if ( funcname.equals("test_ExecuteFunc") ) {
					System.out.println("equals");
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(0);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getVal();
						usedFunctions.add(stringName);
						System.out.println(stringName);
					}					
				}else if ( funcname.equals( "TriggerRegisterVariableEvent")){
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(1);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getVal();
						usedFunctions.add(stringName);
					}
				}
			}
			
		});
		return usedFunctions;
	}
	
	public void replaceEFTRVE(JassProg prg, final HashMap<String, String> replacements) {
		prg.accept(new JassProg.DefaultVisitor() {			
			@Override
			public void visit(JassStmtCall jassCall) {
				String funcname = jassCall.getFunctionName();
				if ( funcname.equals("test_ExecuteFunc") ) {
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(0);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getVal();
						((JassExprStringVal) argument).setVal(replacements.get(stringName));
					}					
				}else if ( funcname.equals( "TriggerRegisterVariableEvent")){
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(1);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getVal();
						((JassExprStringVal) argument).setVal(replacements.get(stringName));
					}
				}
			}
			
		});
	}
	
	@Override
	public void optimize(final JassProg prog) throws FileNotFoundException {
		
		// here is a small example in which we rename every function to frotty:
		
//		prog.accept(new JassProg.DefaultVisitor() {
//			
//			@Override
//			public void visit(JassFunction jassFunction) {
//				jassFunction.setName("frotty");
//			}
//			
//			// look at the JassProg.DefaultVisitor to find other elements which you can visit
//			
//		});
		
		// Visit all functions and variables and save their name and their 
		// replacement into a hashmap
		
		// The replacement map
		final HashMap<String, String> replacements = new HashMap<String, String>();
		// The Namegenerator used for creating the shortened names
		final NameGenerator ng = new NameGenerator(new File("lib/restrictedCompressedNames.txt"));
		
		// Some settings
		setStandards();
		setHashmapPredefines(replacements);
		
		// Create a set of functions that are used in EF or TRVE
		final Set<String> usedInEFTRVE = getEverythingInEForTRVE(prog);
		
		
		
		// visit all function declarations and create a fitting replacement and put that
		// into the Hashmap
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassFunction jassFunction) {
				try {
					String name = jassFunction.getName();
					if ( !isStandard(name)){
						System.out.println(name);
						if ( usedInEFTRVE.contains(name)) {	
							System.out.println("used");
							replacements.put(name, ng.getTEToken());							
						}else {		
							System.out.println("not");
							replacements.put(name, ng.getUniqueToken());
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		replaceEFTRVE( prog, replacements);
		
		// Replace all function declaration names with the shorter ones
		// from the replacements Hashmap
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassFunction jassFunction) {
				String name = jassFunction.getName();
				if ( replacements.containsKey(name)){
					jassFunction.setName(replacements.get(name));
				}
			}			
			
		});
		
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassStmtCall funcCall ) {
				String name = funcCall.getFunctionName();
				if ( replacements.containsKey(name)){
					funcCall.setFunctionName(replacements.get(name));
				}
			}			
			
		});
		
	}

}
