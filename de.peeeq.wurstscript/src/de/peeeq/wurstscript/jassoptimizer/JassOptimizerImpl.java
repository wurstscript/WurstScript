package de.peeeq.wurstscript.jassoptimizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiImpl;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassSimpleVars;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

// The fabulous Froptimizer!
public class JassOptimizerImpl implements JassOptimizer {
	String[] standards;
	int standardsAmount = 0;
	private final static String PSCRIPT_ENDING = ".pscript";
	/**
	 * a main method to simply test the optimizer 
	 * @throws IOException 
	 */
	public static void main(String ... args) throws IOException {

		// compile the testFile to jass (note that this is not complete yet, but might be enough for testing the optimizer a bit)
		WurstGui gui = new WurstGuiImpl();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui);
		
		File dir = new File("./testscripts/valid/optimizer/");
		
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
		
		for ( File file : pscriptFiles) {
			
			System.out.println( "----------------------------------------------");
			System.out.println( "Optimizing file: " + file );
			
			compiler.loadFiles(file);
			compiler.parseFiles();

			JassProg prog = compiler.getProg();

			// no we have a jass program
			// print original
			Files.write(new JassPrinter(true).printProg(prog), new File(file+".original.j"), Charsets.UTF_8);
			
			// optimize it:
			new JassOptimizerImpl().optimize(prog);
			
			// print the optimized version
			Files.write(new JassPrinter(false).printProg(prog), new File(file+".optimized.j"), Charsets.UTF_8);
			
		}
		
		
		
		// Close the gui
		gui.sendFinished();
		
		
	}
	
	/**
	 * Sets predefined replacements like Filter instead of Condition
	 * @param map
	 */
	private void setHashmapPredefines(HashMap<String, String> map) {
		map.put("Condition", "Filter");
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
	
	public void generateGlobalReplacements(JassProg prg, final HashMap<String, String> replacements, final NameGenerator ng) {
		prg.accept(new JassProg.DefaultVisitor() {			
			@Override
			public void visit(JassProg prog) {
				JassVars globals = prog.getGlobals();
				
				for (JassVar global : globals ) {
					String name = global.getName();
					System.out.println(name);
					replacements.put(name, ng.getUniqueToken());
					global.setName(replacements.get(name));
				}
			}
			
		});
	}
	
	@Override
	public void optimize(final JassProg prog) throws FileNotFoundException {
		
		// Visit all functions and variables and save their name and their 
		// replacement into a hashmap
		
		// The replacement map
		final HashMap<String, String> replacements = new HashMap<String, String>();
		// The Namegenerator used for creating the shortened names
		final NameGenerator ng = new NameGenerator();
		
		// Some settings
		setHashmapPredefines(replacements);
		
		// Create a set of functions that are used in EF or TRVE
		final Set<String> usedInEFTRVE = getEverythingInEForTRVE(prog);
		
		generateGlobalReplacements(prog, replacements, ng);
		
		// visit all function declarations and create a fitting replacement and put that
		// into the Hashmap
		prog.accept(new JassProg.DefaultVisitor() {
			
			
			@Override
			public void visit(JassFunction jassFunction) {

				// Visit and generate short function names,
				// checking for EF/TRVE appearance
				String name = jassFunction.getName();
				if ( !RestrictedStandardNames.contains(name)){
					System.out.println(name);
					if ( usedInEFTRVE.contains(name)) {	
						System.out.println("used");
						replacements.put(name, ng.getTEToken());							
					}else {		
						System.out.println("not");
						replacements.put(name, ng.getUniqueToken());
					}
				}
				
				// Create small replacements for parameters and locals
				final HashMap<String, String> localReplacements = new HashMap<String, String>();
				JassSimpleVars params = jassFunction.getParams();
				System.out.println("params");
				// params
				for (JassSimpleVar param : params ) {
					String name1 = param.getName();
					System.out.println(name1);
					localReplacements.put(name1, ng.getUniqueToken());
					param.setName(localReplacements.get(name1));
				}
				JassVars locals = jassFunction.getLocals();
				System.out.println("locals");
				// locals
				for (JassVar local : locals ) {
					String name1 = local.getName();
					System.out.println(name1);
					localReplacements.put(name1, ng.getUniqueToken());
					local.setName(localReplacements.get(name1));
				}
				
				System.out.println("nachLocals");
				System.out.println("body = " + jassFunction.getBody());
				
				// Replace everything in the function body
				jassFunction.getBody().accept(new JassFunction.DefaultVisitor() {
					
					@Override
					public void visit(JassStmtSet setStmt ) {
						System.out.println("2.visitor");
						String name = setStmt.getLeft();
						System.out.println("Left Statement: " + name );
						if ( localReplacements.containsKey(name)){
							System.out.println("Replaced with " + localReplacements.get(name));
							setStmt.setLeft(localReplacements.get(name));
						}else if ( replacements.containsKey(name)){
							setStmt.setLeft(replacements.get(name));
						}
					}												
					
					@Override
					public void visit(JassStmtSetArray setArrayStmt ) {
						String name = setArrayStmt.getLeft();
						if ( localReplacements.containsKey(name)){
							setArrayStmt.setLeft(localReplacements.get(name));
						}else if ( replacements.containsKey(name)){
							setArrayStmt.setLeft(replacements.get(name));
						}
					}	
					
					@Override
					public void visit(JassExprVarAccess setExpr ) {
						String name = setExpr.getVarName();
						if ( localReplacements.containsKey(name)){
							setExpr.setVarName(localReplacements.get(name));
						}else if ( replacements.containsKey(name)){
							setExpr.setVarName(replacements.get(name));
						}else if (NullableConstants.contains(name)) {
							setExpr.setVarName("null");
						}
					}	
					
					@Override
					public void visit(JassExprVarArrayAccess setArrayExpr ) {
						String name = setArrayExpr.getVarName();
						if ( localReplacements.containsKey(name)){
							setArrayExpr.setVarName(localReplacements.get(name));
						}else if ( replacements.containsKey(name)){
							setArrayExpr.setVarName(replacements.get(name));
						}
					}	
					
					@Override
					public void visit(JassExprFuncRef funcRef ) {
						String name = funcRef.getFuncName();
						if ( replacements.containsKey(name)){
							funcRef.setFuncName(replacements.get(name));
						}
					}	
					
					@Override
					public void visit(JassExprFunctionCall funcCall ) {
						String name = funcCall.getFuncName();
						if ( replacements.containsKey(name)){
							funcCall.setFuncName(replacements.get(name));
						}
					}	
					
				});					
					
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
		
		// Replace all function statement calls names with the shorter ones
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassStmtCall funcCall ) {
				String name = funcCall.getFunctionName();
				if ( replacements.containsKey(name)){
					funcCall.setFunctionName(replacements.get(name));
				}
			}			
			
		});
		
		// Replace all function expression names with the shorter ones
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassExprFuncRef funcExpr ) {
				String name = funcExpr.getFuncName();
				if ( replacements.containsKey(name)){
					funcExpr.setFuncName(replacements.get(name));
				}
			}			
			
		});
		
		
		
	}

}
