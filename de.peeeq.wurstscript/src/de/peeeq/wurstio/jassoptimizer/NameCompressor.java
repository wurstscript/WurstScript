package de.peeeq.wurstio.jassoptimizer;

import java.util.HashMap;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassSimpleVars;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.translation.imoptimizer.NameGenerator;
import de.peeeq.wurstscript.translation.imoptimizer.RestrictedStandardNames;
import de.peeeq.wurstscript.utils.Debug;

public class NameCompressor {
		
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
		final HashMap<String, String> replacements = Maps.newHashMap();
		// The Namegenerator used for creating the shortened names
		final NameGenerator ng = new NameGenerator();
						
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
					replacements.put(name, ng.getUniqueToken());
				}
				
				
				// Create small replacements for parameters and locals
				final HashMap<String, String> localReplacements = Maps.newHashMap();
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
					
					@Override
					public void visit(JassStmtCall funcCall ) {
						String name = funcCall.getFuncName();
						if ( replacements.containsKey(name)){
							funcCall.setFuncName(replacements.get(name));
						}
					}
					
				});					
					
			}
			
		});
		
		
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
			public void visit(JassExprFunctionCall funcCall ) {
				String name = funcCall.getFuncName();
				if ( replacements.containsKey(name)){
					funcCall.setFuncName(replacements.get(name));
				}
			}	
			
			@Override
			public void visit(JassExprFuncRef funcRef ) {
				String name = funcRef.getFuncName();
				if ( replacements.containsKey(name)){
					funcRef.setFuncName(replacements.get(name));
				}
			}
		});
	}
}
