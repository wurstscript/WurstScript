package de.peeeq.wurstscript.jassoptimizer;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
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
import de.peeeq.wurstscript.utils.Debug;

public class NameCompressor {

	/**
	 * Sets predefined replacements like Filter instead of Condition
	 * @param map
	 */
	private static void setHashmapPredefines(HashMap<String, String> map) {
		//map.put("Condition", "Filter");
	}
			
	
	public static Set<StringExpressionPattern> getEverythingInEForTRVE(JassProg prog) {
		final Set<StringExpressionPattern> usedFunctions = Sets.newHashSet(); 
		
		prog.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassStmtCall jassCall) {
				String funcname = jassCall.getFuncName();
				Debug.println("name: " + funcname);
				if ( funcname.equals("ExecuteFunction") ) {
					Debug.println("equals");
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(0);
					usedFunctions.add(new StringExpressionPattern(argument));
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getValS();						
						Debug.println(stringName + " used in EF");
					}					
				}else if ( funcname.equals( "TriggerRegisterVariableEvent")){
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(1);
					usedFunctions.add(new StringExpressionPattern(argument));
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getValS();						
						Debug.println(stringName + " is USED IN TRVE <<<<<<<<<<<<<<<<");
					}
				}
			}
			
		});
		return usedFunctions;
	}
	
	public static void replaceEFTRVE(JassProg prog, final HashMap<String, String> replacements) {
		prog.accept(new JassProg.DefaultVisitor() {			
			@Override
			public void visit(JassStmtCall jassCall) {
				String funcname = jassCall.getFuncName();
				if ( funcname.equals("ExecuteFunction") ) {
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(0);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getValS();
						((JassExprStringVal) argument).setValS(replacements.get(stringName));
					}					
				}else if ( funcname.equals( "TriggerRegisterVariableEvent")){
					JassExprlist list = jassCall.getArguments();
					JassExpr argument = list.get(1);
					if ( argument instanceof JassExprStringVal ){
						String stringName = ((JassExprStringVal) argument).getValS();
						
						((JassExprStringVal) argument).setValS(replacements.get(stringName));
					}
				}
			}
			
		});
	}
	
	public static void generateGlobalReplacements(JassProg prg, final HashMap<String, String> replacements, final NameGenerator ng) {
		prg.accept(new JassProg.DefaultVisitor() {			
			@Override
			public void visit(JassProg prog) {
				JassVars globals = prog.getGlobals();
				
				for (JassVar global : globals ) {
					String name = global.getName();
					Debug.println(name);
					replacements.put(name, ng.getUniqueToken());
					global.setName(replacements.get(name));
				}
			}
			
		});
	}
	
	public static void compressNames(JassProg prog) {
		// The replacement map
				final HashMap<String, String> replacements = new HashMap<String, String>();
				// The Namegenerator used for creating the shortened names
				final NameGenerator ng = new NameGenerator();
				
				// Some settings
				setHashmapPredefines(replacements);
				
				// Create a set of functions that are used in EF or TRVE
				final Set<StringExpressionPattern> usedInEFTRVE = getEverythingInEForTRVE(prog);
				
				generateGlobalReplacements(prog, replacements, ng);
				
				// visit all function declarations and create a fitting replacement and put that
				// into the Hashmap
				prog.accept(new JassProg.DefaultVisitor() {
					
					
					@Override
					public void visit(JassFunction jassFunction) {

						// Visit and generate short function names,
						// checking for EF/TRVE appearance
						String name = jassFunction.getName();
						Debug.println("Function:" + name);
						if ( !RestrictedStandardNames.contains(name) ){
							Debug.println("Function:" + name + " is not restricted.");
							boolean used = false;
							for( StringExpressionPattern sex : usedInEFTRVE) {
								if( sex.check(name)) {
									Debug.println(name + " is used in EF or TRVE");
									if(sex.isConst()) {
										Debug.println("Pattern is a constant String");
										replacements.put(name, ng.getTEToken());	
									}
									used = true;
									break;
								}
							}
			
							if (!used) {replacements.put(name, ng.getUniqueToken());}
						}
						
						
						// Create small replacements for parameters and locals
						final HashMap<String, String> localReplacements = new HashMap<String, String>();
						JassSimpleVars params = jassFunction.getParams();
						Debug.println("Parameters:");
						// params
						for (JassSimpleVar param : params ) {
							String name1 = param.getName();
							Debug.println(name1);
							localReplacements.put(name1, ng.getUniqueToken());
							param.setName(localReplacements.get(name1));
						}
						JassVars locals = jassFunction.getLocals();
						Debug.println("Locals:");
						// locals
						for (JassVar local : locals ) {
							String name1 = local.getName();
							Debug.println(name1);
							localReplacements.put(name1, ng.getUniqueToken());
							local.setName(localReplacements.get(name1));
							Debug.println(name1 + " replacement: " + localReplacements.get(name1));
						}
						
						Debug.println("Body: " + jassFunction.getBody());
						
						// Replace everything in the function body
						jassFunction.accept(new JassFunction.DefaultVisitor() {
							
							@Override
							public void visit(JassStmtSet setStmt ) {
								String name = setStmt.getLeft();
								//Debug.println("Left Statement: " + name );
								if ( localReplacements.containsKey(name)){
									Debug.println("Replaced with " + localReplacements.get(name));
									setStmt.setLeft(localReplacements.get(name));
								}else if ( replacements.containsKey(name)){
									setStmt.setLeft(replacements.get(name));
								}
							}												
							
							@Override
							public void visit(JassStmtSetArray setArrayStmt ) {
								String name = setArrayStmt.getLeft();
								if ( localReplacements.containsKey(name)){
									Debug.println("Replaced with " + localReplacements.get(name));
									setArrayStmt.setLeft(localReplacements.get(name));
								}else if ( replacements.containsKey(name)){
									setArrayStmt.setLeft(replacements.get(name));
								}
							}	
							
							@Override
							public void visit(JassExprVarAccess setExpr ) {
								String name = setExpr.getVarName();
								Debug.println("Var  " + name + " has local replacement " + localReplacements.get(name));
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
					
					// Replace all function statement calls names with the shorter ones
						
					@Override
					public void visit(JassStmtCall funcCall ) {
						String name = funcCall.getFuncName();
						if ( replacements.containsKey(name)){
							funcCall.setFuncName(replacements.get(name));
						}
					}			

					
					// Replace all function expression names with the shorter ones
						
					@Override
					public void visit(JassExprFuncRef funcExpr ) {
						String name = funcExpr.getFuncName();
						if ( replacements.containsKey(name)){
							funcExpr.setFuncName(replacements.get(name));
						}
					}				
					
					
				});
				
				prog.getGlobals().accept(new JassProg.DefaultVisitor() {
					
					@Override
					public void visit(JassExprVarAccess setExpr ) {
						String name = setExpr.getVarName();
						if ( replacements.containsKey(name)){
							setExpr.setVarName(replacements.get(name));
						}else if (NullableConstants.contains(name)) {
							setExpr.setVarName("null");
						}
					}	
					
					@Override
					public void visit(JassExprVarArrayAccess setArrayExpr ) {
						String name = setArrayExpr.getVarName();
						if ( replacements.containsKey(name)){
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
}
