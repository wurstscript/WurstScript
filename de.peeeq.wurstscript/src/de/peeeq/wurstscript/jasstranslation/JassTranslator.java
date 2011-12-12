package de.peeeq.wurstscript.jasstranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassArrayVar;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBoolVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprFuncRef;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprFunctionCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprNull;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprRealVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprStringVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprUnary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarArrayAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprlist;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunction;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunctions;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpAnd;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpDiv;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpEquals;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreaterEq;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpLess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpLessEq;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMinus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMult;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpNot;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpOr;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpUnequals;
import static de.peeeq.wurstscript.jassAst.JassAst.JassProg;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVar;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVars;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtExitwhen;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtIf;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtLoop;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturnVoid;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSet;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSetArray;
import static de.peeeq.wurstscript.jassAst.JassAst.JassVars;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ClassSlots;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprAssignable;
import de.peeeq.wurstscript.ast.ExprBinary;
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprCast;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExprUnary;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.Indexes;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpAssign;
import de.peeeq.wurstscript.ast.OpAssignment;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpMinusAssign;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpMultAssign;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpPlusAssign;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.ast.OpUpdateAssign;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtErr;
import de.peeeq.wurstscript.ast.StmtExitwhen;
import de.peeeq.wurstscript.ast.StmtForRange;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtLoop;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.FuncDefInstance;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpUnary;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeClassDefinition;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeModuleDefinition;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeError;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;

public class JassTranslator {

	private static final boolean debug = false;
	protected static final ImmutableList<ClassOrModule> ROOT_CONTEXT = ImmutableList.<ClassOrModule>emptyList();
	private JassManager manager;
	private CompilationUnit wurstProg;
	private JassProg prog;
	private JassVars globals;
	private JassFunctions functions;
	private List<GlobalInit> globalInitializers = Lists.newLinkedList();
	private BiMap<WPackage, JassFunction> initFunctions = HashBiMap.create();
	private Multimap<JassFunction, JassFunction> calledFunctions = HashMultimap.create();
	private Multimap<WPackage, WPackage> importedPackages = HashMultimap.create();
	private JassFunction initGlobalsFunc;
	private Collection<WPackage> packages = Lists.newLinkedList();
	
	
	public JassTranslator(CompilationUnit wurstProgram) {
		this.manager = new JassManager(this);
		this.wurstProg = wurstProgram;
	}
	
	public JassProg translate() {
		trace("translate " + wurstProg);
		globals = JassVars();
		functions = JassFunctions();
		prog = JassProg(globals, functions);
		
		for (TopLevelDeclaration t : wurstProg) {
			trace("translate tld " + t);
			translateTopLevelDeclaration(t);
		}
		
		
		initGlobals();
		
		createMainMethod();
		
		sortFunctions();
		
		return prog;
	}

	/**
	 * sort the functions so that each functions only calls functions above it
	 */
	private void sortFunctions() {
		try {
			List<JassFunction> sortedFunctions = Utils.topSort(prog.getFunctions(), calledFunctions);
			
			// the list of sorted functions now also contains natives, remove them
			ListIterator<JassFunction> it = sortedFunctions.listIterator();
			while (it.hasNext()) {
				JassFunction f = it.next();
				AstElement source = manager.getFunctionSource(f);
				if (source instanceof NativeFunc) {
					it.remove();
				}
			}
			
			// remove old functions and add sorted ones:
			prog.getFunctions().removeAll();
			prog.getFunctions().addAll(sortedFunctions);
			
		} catch (TopsortCycleException e) {
			@SuppressWarnings("unchecked")
			List<JassFunction> items = (List<JassFunction>) e.activeItems;
			String msg = Utils.join(Utils.map(items, new Function<JassFunction, String>() {

				@Override
				public String apply(JassFunction input) {
					return input.getName();
				}
				
			}), ", ");
			attr.addError(Ast.WPos("", 0, 0), "Cannot generate code because of a cyclic dependency between the following functions: \n" + msg);
		}
	}

	/**
	 * generate the method which initializes global variables
	 */
	private void initGlobals() {
		String name = manager.getUniqueName("wurst__init_globals");
		JassStatements body = JassStatements();
		initGlobalsFunc = JassFunction(name, JassSimpleVars(), "nothing", JassVars(), body);
		prog.getFunctions().add(initGlobalsFunc);
		
		// create a mapping from jassvar to GlobalInit
		final Map<JassVar, GlobalInit> varToInit = Maps.newHashMap();
		for (final GlobalInit gi : globalInitializers) {
			varToInit.put(gi.v, gi);
		}
		
		
		// collect dependencies between global initializers into a multimap:
		final Multimap<GlobalInit, GlobalInit> initDependsOn = HashMultimap.create();
		for (final GlobalInit gi : globalInitializers) {
			gi.initialExpr.accept(new Expr.DefaultVisitor() {
				@Override
				public void visit(ExprVarAccess exprVarAccess) {
					VarDef varDef = (VarDef) exprVarAccess.attrNameDef();
					JassVar jassVar = manager.getJassVarForTranslatedVar(gi.context, varDef);
					GlobalInit v = varToInit.get(jassVar);
					if (v != null) {
						initDependsOn.put(gi, v);
					}
				}
			});
		}
		
		// sort globals according to dependency between globals
		try {
			globalInitializers = Utils.topSort(globalInitializers, initDependsOn);
		} catch (TopsortCycleException e) {
			@SuppressWarnings("unchecked")
			List<GlobalInit> items = (List<GlobalInit>) e.activeItems;
			String msg = Utils.join(Utils.map(items, new Function<GlobalInit, String>() {

				@Override
				public String apply(GlobalInit input) {
					return input.v.getName();
				}
				
			}), ", ");
			attr.addError(Ast.WPos("", 0, 0), "Cannot generate code because of a cyclic dependency between the following variables: \n" + msg);
		}
		
		for (GlobalInit gi : globalInitializers) {
			if (! prog.attrIgnoredVariables().contains(gi.v)) {
				ExprTranslationResult e = translateExpr(gi.context, initGlobalsFunc, gi.initialExpr);
				body.addAll(e.getStatements());
				body.add(JassStmtSet(gi.v.getName(), e.getExpr()));
			}
		}
		
	}

	/**
	 * create the main function if it does not exist already
	 * and executes all initializers from the main function  
	 */
	private void createMainMethod() {
		Preconditions.checkNotNull(initGlobalsFunc, "The initGlobals function has to be executed first.");
		
		
		JassFunction mainFunction = null;
		for (JassFunction f : prog.getFunctions()) {
			if (f.getName().equals("main")) {
				mainFunction = f;
			}
		}
		if (mainFunction == null) {
			// create a new main function
			mainFunction = JassFunction("main", JassSimpleVars(), "nothing", JassVars(), JassStatements());
			prog.getFunctions().add(mainFunction);
		}
		
		// finish main function:
		
		JassStatements body = mainFunction.getBody();
		
		// call the initGlobals function
		body.add(0, JassStmtCall(initGlobalsFunc.getName(), JassExprlist()));
		calledFunctions.put(mainFunction, initGlobalsFunc);
		

		// sort init functions according to import hierarchy
		List<WPackage> packagesSorted = Utils.topSortIgnoreCycles(packages , importedPackages);
		
		// call all initializers:
		for (WPackage p : packagesSorted) {
			JassFunction f = initFunctions.get(p);
			if (f != null) {
				body.add(JassStmtCall(f.getName(), JassExprlist()));
			}
		}
		
	
		
	}

	private void translateTopLevelDeclaration(TopLevelDeclaration t) {
		trace("translate " + t);
		t.match(new TopLevelDeclaration.MatcherVoid() {
			
			@Override
			public void case_WPackage(WPackage wPackage) {
				translatePackage(wPackage);
			}
			
			@Override
			public void case_NativeType(NativeType nativeType) {
				translateNativeType(nativeType);
			}
			
			@Override
			public void case_NativeFunc(NativeFunc nativeFunc) {
				translateNativeFunc(nativeFunc);
			}
			
			@Override
			public void case_JassGlobalBlock(JassGlobalBlock jassGlobalBlock) {
				translateJassGlobalsBlock(jassGlobalBlock);
			}
			
			@Override
			public void case_FuncDef(FuncDef funcDef) {
				translateFuncDef(ROOT_CONTEXT, funcDef, false);
			}

			@Override
			public void case_ExtensionFuncDef(ExtensionFuncDef funcDef) {
				translateExtensionFuncDef(ROOT_CONTEXT, funcDef);
			}
		});
	}

	protected void translateExtensionFuncDef(ImmutableList<ClassOrModule> context, ExtensionFuncDef funcDef) {
		JassFunction f = manager.getJassFunctionFor(context, funcDef);
		f.setReturnType(translateType(funcDef.getSignature().getTyp()));
		
		// add implicit parameter 'this'
		f.getParams().add(JassAst.JassSimpleVar(translateType(funcDef.getExtendedType().attrTyp()), "this"));
		
		for (WParameter param : funcDef.getSignature().getParameters()) {
			f.getParams().add(translateParam(context, param));
		}
		f.getBody().addAll(translateStatements(context, f, funcDef.getBody()));
		prog.getFunctions().add(f);
	}
	
	protected void translateFuncDef(ImmutableList<ClassOrModule> context, FuncDef funcDef, boolean isMethod) {
		if (funcDef.attrIsAbstract()) {
			// do not translate abstract methods, they no hav body, u no?
			return;
		}
		trace("translateFuncDef " + Utils.printContext(context) + " -> " + funcDef.getSignature().getName());
		JassFunction f = manager.getJassFunctionFor(context, funcDef);
		
		if (isCommonOrBlizzard(funcDef.getSource())) {
			prog.attrIgnoredFunctions().add(f);
		}
		
		f.setReturnType(translateType(funcDef.getSignature().getTyp()));
		if (isMethod && !funcDef.attrIsStatic()) {
			// methods have an additional implicit parameter
			f.getParams().add(jassThisVar());
		}
		for (WParameter param : funcDef.getSignature().getParameters()) {
			f.getParams().add(translateParam(context, param));
		}
		f.getBody().addAll(translateStatements(context, f, funcDef.getBody()));
		prog.getFunctions().add(f);
	}

	private JassSimpleVar jassThisVar() {
		return JassAst.JassSimpleVar("integer", "this");
	}

	private List<JassStatement> translateStatements(ImmutableList<ClassOrModule> context, JassFunction f, WStatements statements) {
		List<JassStatement> result = Lists.newLinkedList();
		for (WStatement s : statements) {
			result.addAll(translateStatement(context, f, s));
		}
		return result;
	}

	private List<JassStatement> translateStatement(final ImmutableList<ClassOrModule> context, final JassFunction f, final WStatement s) {
		Preconditions.checkNotNull(context);
		final List<JassStatement> result = Lists.newLinkedList();
		s.match(new WStatement.MatcherVoid() {

			@Override
			public void case_ExprMemberMethod(ExprMemberMethod exprMemberMethod) {
				case_Expr(exprMemberMethod);
				
			}


			@Override
			public void case_StmtLoop(StmtLoop stmtLoop) {
				List<JassStatement> body = translateStatements(context, f, stmtLoop.getBody());
				result.add(JassStmtLoop(JassStatements(body)));
			}

			@Override
			public void case_StmtSet(StmtSet stmtSet) {
				final ExprTranslationResult right = translateExpr(context, f, stmtSet.getRight());
				final JassOpBinary binaryOp = stmtSet.getOp().match(new OpAssignment.Matcher<JassOpBinary>() {

					@Override
					public JassOpBinary case_OpMinusAssign(OpMinusAssign opMinusAssign) {
						return JassOpMinus();
					}

					@Override
					public JassOpBinary case_OpPlusAssign(OpPlusAssign opPlusAssign) {
						return JassOpPlus();
					}

					@Override
					public JassOpBinary case_OpMultAssign(OpMultAssign opMultAssign) {
						return JassOpMult();
					}

					@Override
					public JassOpBinary case_OpAssign(OpAssign opAssign) {
						return null;
					}
				});
				
				
				stmtSet.getLeft().match(new ExprAssignable.MatcherVoid() {
					
					@Override
					public void case_ExprMemberVar(ExprMemberVar exprMemberVar) {
						VarDef leftVar = (VarDef) exprMemberVar.attrNameDef();
						String leftJassVar = manager.getJassVarNameFor(getVarContext(context, exprMemberVar.getLeft().attrTyp()), leftVar);
						ExprTranslationResult index = translateExpr(context, f, exprMemberVar.getLeft());
						
						withIndex(leftJassVar, index);
					}

					private void withIndex(String leftJassVar, ExprTranslationResult index) {
						result.addAll(index.getStatements());
						result.addAll(right.getStatements());
						JassExpr indexExpr;
						
						if (binaryOp == null) {
							result.add(JassStmtSetArray(leftJassVar, index.getExpr(), right.getExpr())); 
						} else {
							
							if (index.getExpr() instanceof ExprIntVal 
									|| index.getExpr() instanceof ExprVarAccess) {
								indexExpr = index.getExpr();
							} else {
								JassVar tempIndex = getNewTempVar(f, "integer");
								result.add(JassStmtSet(tempIndex.getName(), index.getExpr()));
								indexExpr = JassExprVarAccess(tempIndex.getName());
							}
							
							result.add(JassStmtSetArray(leftJassVar, indexExpr, 
								JassExprBinary(JassExprVarArrayAccess(leftJassVar, indexExpr), binaryOp, right.getExpr())));
						}
					}
					
					private void withoutIndex(String leftJassVar) {
						result.addAll(right.getStatements());
						if (binaryOp == null) {
							result.add(JassStmtSet(leftJassVar, right.getExpr())); 
						} else {
							result.add(JassStmtSet(leftJassVar, 
									JassExprBinary(JassExprVarAccess(leftJassVar), binaryOp, right.getExpr())));
						}
					}

					@Override
					public void case_ExprMemberArrayVar(ExprMemberArrayVar exprMemberArrayVar) {
						throw new Error("Array member vars not supported yet.");
					}

					@Override
					public void case_ExprVarAccess(ExprVarAccess exprVarAccess) {
						VarDef leftVar = (VarDef) exprVarAccess.attrNameDef();
						String leftJassVar = getJassVarNameFor(context, leftVar);
						result.addAll(right.getStatements());
						if (leftVar.attrIsDynamicClassMember()) {
							withIndex(leftJassVar, new ExprTranslationResult(JassExprVarAccess("this")));
						} else {
							withoutIndex(leftJassVar);
						}
					}

					

					@Override
					public void case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
						VarDef leftVar = (VarDef) exprVarArrayAccess.attrNameDef(); // TODO cast not always possible
						String leftJassVar = manager.getJassVarNameFor(context, leftVar);
						ExprTranslationResult index;
						if (exprVarArrayAccess.getIndexes().size() == 1) {
							index = translateExpr(context, f, exprVarArrayAccess.getIndexes().get(0));
						} else {
							throw new Error("multiple indexes not supported yet");
						}
						withIndex(leftJassVar, index);
					}
				});
				
				
				
			}

			@Override
			public void case_StmtIf(StmtIf stmtIf) {
				ExprTranslationResult cond = translateExpr(context, f, stmtIf.getCond());
				List<JassStatement> thenBlock = translateStatements(context, f, stmtIf.getThenBlock());
				List<JassStatement> elseBlock = translateStatements(context, f, stmtIf.getElseBlock());
				result.addAll(cond.getStatements());
				result.add(JassStmtIf(cond.getExpr(), JassStatements(thenBlock), JassStatements(elseBlock)));
			}

			@Override
			public void case_StmtDestroy(StmtDestroy stmtDestroy) {
				PscriptType typ = stmtDestroy.getObj().attrTyp();
				if (typ instanceof PscriptTypeClass) {
					PscriptTypeClass classType = (PscriptTypeClass) typ;
					ClassDef classDef = classType.getClassDef();
					callDestroyFunc(classDef, stmtDestroy.getObj());
				} else if (typ instanceof PscriptTypeModule) {
					ClassDef classDef = (ClassDef) context.head();
					callDestroyFunc(classDef, stmtDestroy.getObj());
				} else {
					throw new Error("cannot destroy object of type " + typ);
				}
			}

			private void callDestroyFunc(ClassDef classDef, Expr e) {
				JassFunction destroyMethod = manager.getJassDestroyFunctionFor(classDef); 
				ExprTranslationResult toDestroy = translateExpr(context, f, e);
				result.addAll(toDestroy.getStatements());
				result.add(JassStmtCall(destroyMethod.getName(), JassExprlist(toDestroy.getExpr())));
			}


			@Override
			public void case_StmtWhile(StmtWhile stmtWhile) {
				ExprTranslationResult cond = translateExpr(context, f, stmtWhile.getCond());
				
				JassStatements body = JassStatements();
				// ==> exitwhen not cond
				body.addAll(cond.getStatements());
				body.add(JassStmtExitwhen(JassExprUnary(JassOpNot(), cond.getExpr())));
				
				body.addAll(translateStatements(context, f, stmtWhile.getBody()));
				
				
				
				result.add(JassStmtLoop(body));
			}

			@Override
			public void case_ExprNewObject(ExprNewObject exprNewObject) {
				case_Expr(exprNewObject);
			}

			private void case_Expr(Expr expr) {
				ExprTranslationResult e = translateExpr(context, f, expr);
				result.addAll(e.getStatements());
				if (e.getExpr() instanceof JassExprFunctionCall) {
					JassExprFunctionCall call = (JassExprFunctionCall) e.getExpr();
					result.add(JassStmtCall(call.getFuncName(), call.getArguments().copy()));
				} else {
					// we can ignore any other case because we will not need the result of the expression
				}
			}


			@Override
			public void case_StmtReturn(StmtReturn stmtReturn) {
				if (stmtReturn.getObj() instanceof Expr) {
					Expr expr = (Expr) stmtReturn.getObj();
					ExprTranslationResult e = translateExpr(context, f, expr);
					result.addAll(e.getStatements());
					result.add(JassStmtReturn(e.getExpr()));
				} else {
					result.add(JassStmtReturnVoid());
				}
			}


			@Override
			public void case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				case_Expr(exprFunctionCall);
			}

			@Override
			public void case_StmtExitwhen(StmtExitwhen stmtExitwhen) {
				ExprTranslationResult e = translateExpr(context, f, stmtExitwhen.getCond());
				result.addAll(e.getStatements());
				result.add(JassStmtExitwhen(e.getExpr()));
			}

			@Override
			public void case_StmtErr(StmtErr stmtErr) {
				throw new Error("Source contains errors : " + stmtErr);
			}

			@Override
			public void case_LocalVarDef(LocalVarDef localVarDef) {
				String type = translateType(localVarDef.attrTyp());
				JassVar v = manager.getJassVarFor(context, localVarDef, type , isArray(localVarDef), true); 
						//getLocalVar(f, localVarDef.getName(), localVarDef.attrTyp());
				f.getLocals().add(v);
				if (localVarDef.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) localVarDef.getInitialExpr();
					ExprTranslationResult e = translateExpr(context, f, expr);
					result.addAll(e.getStatements());
					result.add(JassStmtSet(v.getName(), e.getExpr()));
				}
			}


			@Override
			public void case_StmtForRange(StmtForRange stmtForRange) {
				JassVar loopVar = manager.getJassVarFor(context, stmtForRange.getLoopVar(), translateType(stmtForRange.getLoopVar().getTyp()), false);
				f.getLocals().add(loopVar);
				
				ExprTranslationResult fromExpr = translateExpr(context, f, stmtForRange.getFrom());
				ExprTranslationResult toExpr = translateExpr(context, f, stmtForRange.getTo());
				result.addAll(fromExpr.getStatements());
				JassExpr toExpr2;
				if (toExpr.getStatements().size() == 0 && toExpr.getExpr() instanceof JassExprIntVal) {
					toExpr2 = toExpr.getExpr(); 
				} else {
					JassVar loopEndVar = getNewTempVar(f, "integer");
					result.addAll(toExpr.getStatements());
					result.add(JassStmtSet(loopEndVar.getName(), toExpr.getExpr()));
					toExpr2 = JassExprVarAccess(loopEndVar.getName());
				}
				result.add(JassStmtSet(loopVar.getName(), fromExpr.getExpr()));
				JassStatements body = JassStatements();
				body.add(JassStmtExitwhen(JassExprBinary(JassExprVarAccess(loopVar.getName()), JassOpGreater(), toExpr2)));
				body.addAll(translateStatements(context, f, stmtForRange.getBody()));
				body.add(JassStmtSet(loopVar.getName(), JassExprBinary(JassExprVarAccess(loopVar.getName()), JassOpPlus(), JassExprIntVal(1))));
				result.add(JassStmtLoop(body));
				
			}

		
		});
		return result;
	}

	/**
	 * get the jass var name for the variable v, when accessed from a given context
	 * @param context
	 * @param v
	 * @return
	 */
	private String getJassVarNameFor(final ImmutableList<ClassOrModule> context, VarDef v) {
		if (v.attrNearestClassOrModule() == null) {
			return manager.getJassVarNameFor(ROOT_CONTEXT, v);
		} else {
			return manager.getJassVarNameFor(context, v);
		}
	}
	
	private ImmutableList<ClassOrModule> getVarContext(	ImmutableList<ClassOrModule> currentContext, PscriptType leftType) {
		ClassDef c = null;
		if (leftType instanceof PscriptTypeClass) {
			c = ((PscriptTypeClass) leftType).getClassDef();
		} else if (leftType instanceof PScriptTypeClassDefinition) {
			c = ((PscriptTypeClass) leftType).getClassDef();
		}
		if (c != null) {
			// class variables are always in the class-context: 
			return ROOT_CONTEXT.appFront(c);
		} 
		// all other variables are always private and can only be accessed from the current context.
		// so we can just return the current context:
		return currentContext;
	
	}
	
//	/**
//	 * gets the local var for a given name in the source function
//	 * the name is created if it does not exist yet 
//	 */
//	protected JassVar getLocalVar(JassFunction f, String name, PscriptType attrTyp) {
//		for (JassVar v :  f.getLocals()) {
//			if (v.getName().equals(name)) {
//				return v;
//			}
//		}
//		// var not found -> create it
//		JassVar v = JassSimpleVar(translateType(attrTyp), name);
//		f.getLocals().add(v);
//		return v;
//	}

	protected ExprTranslationResult translateExpr(final ImmutableList<ClassOrModule> context, final JassFunction f, Expr expr) {
		Preconditions.checkNotNull(context);
		return expr.match(new Expr.Matcher<ExprTranslationResult>() {

			@Override
			public ExprTranslationResult case_ExprNewObject(ExprNewObject exprNewObject) {
				ConstructorDef constructorFunc = exprNewObject.attrConstructorDef();
				JassFunction constructorJassFunc = manager.getJassConstructorFor(constructorFunc);
				
				JassExprlist arguments = JassExprlist(); 
				ExprListTranslationResult args = translateArguments(context, f, exprNewObject.getArgs(), getParameterTypes(constructorFunc.getParams()));
				List<JassStatement> statements = Lists.newLinkedList();
				statements.addAll(args.getStatements());
				arguments.addAll(args.getExprs());
				JassExpr e = JassExprFunctionCall(constructorJassFunc.getName(), arguments);
				return new ExprTranslationResult(statements, e);
			}

			@Override
			public ExprTranslationResult case_ExprRealVal(ExprRealVal exprRealVal) {
				return new ExprTranslationResult(JassExprRealVal(exprRealVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprUnary(ExprUnary exprUnary) {
				ExprTranslationResult right = translateExpr(context, f, exprUnary.getRight());
				return new ExprTranslationResult(
						right.getStatements(), 
						JassExprUnary(translateOpUnary(exprUnary.getOp()), right.getExpr()));
			}

			@Override
			public ExprTranslationResult case_ExprThis(ExprThis exprThis) {
				return new ExprTranslationResult(JassExprVarAccess("this"));
			}

			@Override
			public ExprTranslationResult case_ExprCast(ExprCast exprCast) {
				// a cast has no effect:
				return translateExpr(context, f, exprCast.getExpr());
			}

			@Override
			public ExprTranslationResult case_ExprFunctionCall(ExprFunctionCall exprFunctionCall) {
				FuncDefInstance calledFunc = getRealCalledFunction(context, exprFunctionCall.attrFuncDef());
				if (calledFunc == null) {
					return new ExprTranslationResult(JassExprFunctionCall("DoNothing", JassExprlist()));
				}
				JassFunction calledJassFunc = manager.getJassFunctionFor(calledFunc);
				String functionName = calledJassFunc.getName();
				
				calledFunctions.put(f, calledJassFunc);
				
				JassExprlist arguments = JassExprlist();

				if (isDynamicFunction(calledFunc.getDef())) {
					arguments.add(JassExprVarAccess("this"));
				}
				
				ExprListTranslationResult args = translateArguments(context, f, exprFunctionCall.getArgs(), getParameterTypes(calledFunc.getDef().getSignature().getParameters()));
				arguments.addAll(args.getExprs());
				return new ExprTranslationResult(
						args.getStatements(),
						JassExprFunctionCall(functionName, arguments )
						);
			}

			@Override
			public ExprTranslationResult case_ExprMemberMethod(ExprMemberMethod exprMemberMethod) {
				PscriptType leftType = exprMemberMethod.getLeft().attrTyp();
				if (leftType instanceof PscriptTypeClass) {
					return translateDynamicFunctionCall(context, f, exprMemberMethod);
				} else if (leftType instanceof PScriptTypeClassDefinition) {
					return translateStaticFunctionCall(context, f, exprMemberMethod);
				} else if (leftType instanceof PscriptTypeModule) {
					return translateModuleFunctionCall(context, f, exprMemberMethod);
				} else if (leftType instanceof PScriptTypeModuleDefinition) {
					return translateModuleFunctionCall(context, f, exprMemberMethod);
				} else {
					return translateDynamicFunctionCall(context, f, exprMemberMethod);
				}
			}

			

			@Override
			public ExprTranslationResult case_ExprMemberArrayVar(ExprMemberArrayVar exprMemberArrayVar) {
				VarDef varDef = (VarDef) exprMemberArrayVar.attrNameDef();
				String varName = getJassVarNameFor(context, varDef);
				
				ExprTranslationResult left = translateExpr(context, f, exprMemberArrayVar.getLeft());
				return new ExprTranslationResult(left.getStatements(), JassExprVarArrayAccess(varName, left.getExpr()));
			}

			@Override
			public ExprTranslationResult case_ExprStringVal(ExprStringVal exprStringVal) {
				return new ExprTranslationResult(JassExprStringVal(exprStringVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprVarAccess(ExprVarAccess exprVarAccess) {
				VarDef varDef = (VarDef) exprVarAccess.attrNameDef();
				String varName = getJassVarNameFor(context, varDef);
				if (varDef.attrIsDynamicClassMember()) {
					// access to a field
					JassExpr index = JassExprVarAccess("this");
					return new ExprTranslationResult(JassExprVarArrayAccess(varName, index));
				} else {
					// access to a normal variable
					return new ExprTranslationResult(JassExprVarAccess(varName));
				}
			}

			@Override
			public ExprTranslationResult case_ExprBinary(ExprBinary exprBinary) {
				ExprTranslationResult left = translateExpr(context, f, exprBinary.getLeft());
				ExprTranslationResult right = translateExpr(context, f, exprBinary.getRight());
				
				JassExpr leftExpr;
				JassExpr rightExpr;
				
				List<JassStatement> statements = Lists.newLinkedList();
				statements.addAll(left.getStatements());
				
				
				
				// if the right hand side of the expression uses statements we have to make sure that
				// the left hand side is executed first:
				if (right.getStatements().size() > 0) {
					String type = translateType(exprBinary.getLeft().attrTyp());
					JassVar tempVar = getNewTempVar(f, type);
					statements.add(JassStmtSet(tempVar.getName(), left.getExpr()));
					leftExpr = JassExprVarAccess(tempVar.getName());
					
					
					// boolean operators (and, or) have to be treated differently because the evalutation
					// of the right hand side depends on the result of the left hand side.
					if (exprBinary.getOp() instanceof OpAnd) {
						JassStatements thenBlock = JassStatements();
						JassStatements elseBlock  = JassStatements();;
						elseBlock.addAll(right.getStatements());
						elseBlock.add(JassStmtSet(tempVar.getName(), right.getExpr()));
						statements.add(
								JassStmtIf(
										JassExprVarAccess(tempVar.getName()), 
										thenBlock, elseBlock));
						return new ExprTranslationResult(statements, JassExprVarAccess(tempVar.getName()));
					} else if (exprBinary.getOp() instanceof OpAnd) {
						JassStatements thenBlock = JassStatements();
						JassStatements elseBlock  = JassStatements();;
						thenBlock.addAll(right.getStatements());
						thenBlock.add(JassStmtSet(tempVar.getName(), right.getExpr()));
						statements.add(
								JassStmtIf(
										JassExprVarAccess(tempVar.getName()), 
										thenBlock, elseBlock));
						return new ExprTranslationResult(statements, JassExprVarAccess(tempVar.getName()));
					} else {
						rightExpr = right.getExpr();
					}
				} else {
					leftExpr = left.getExpr();
					rightExpr = right.getExpr();
				}
				
				JassExpr e;
				
				// modulo operators nead special treatment ...
				if (exprBinary.getOp() instanceof OpModInt) {
					e = JassExprFunctionCall("ModuloInteger", JassExprlist(leftExpr, rightExpr));
				} else if (exprBinary.getOp() instanceof OpModReal) {
					e = JassExprFunctionCall("ModuloReal", JassExprlist(leftExpr, rightExpr));
				} else if (exprBinary.getOp() instanceof OpDivReal 
						&& exprBinary.getLeft().attrTyp() instanceof PScriptTypeInt
						&& exprBinary.getRight().attrTyp() instanceof PScriptTypeInt) {
					// multiply the left expression by 1.0 to convert it to real
					e = JassExprBinary(JassExprBinary(leftExpr, JassOpMult(), JassExprRealVal(1.0)), JassOpDiv(), rightExpr);
				} else {
					e = JassExprBinary(leftExpr, translateOp(exprBinary.getOp()), rightExpr);
				}
				return new ExprTranslationResult(statements, e);
			}

			@Override
			public ExprTranslationResult case_ExprBoolVal(ExprBoolVal exprBoolVal) {
				return new ExprTranslationResult(JassExprBoolVal(exprBoolVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprMemberVar(ExprMemberVar exprMemberVar) {
				VarDef varDef = (VarDef) exprMemberVar.attrNameDef(); // TODO cast not always possible
				String varName = manager.getJassVarNameFor(getVarContext(context, exprMemberVar.getLeft().attrTyp()), varDef);

				ExprTranslationResult left = translateExpr(context, f, exprMemberVar.getLeft());
				
				JassExpr e = JassExprVarArrayAccess(varName, left.getExpr());
				return new ExprTranslationResult(left.getStatements(), e);
			}

			@Override
			public ExprTranslationResult case_ExprNull(ExprNull exprNull) {
				return new ExprTranslationResult(JassExprNull());
			}

			@Override
			public ExprTranslationResult case_ExprIntVal(ExprIntVal exprIntVal) {
				return new ExprTranslationResult(JassExprIntVal(exprIntVal.getVal()));
			}

			@Override
			public ExprTranslationResult case_ExprFuncRef(ExprFuncRef exprFuncRef) {
				FuncDefInstance funcDef = exprFuncRef.attrFuncDef();
				JassFunction jassFunc = manager.getJassFunctionFor(context, funcDef);
				
				// f calls jassfunc
				calledFunctions.put(f, jassFunc);
				
				String funcName = jassFunc.getName();
				return new ExprTranslationResult(JassExprFuncRef(funcName));
			}

			@Override
			public ExprTranslationResult case_ExprVarArrayAccess(ExprVarArrayAccess exprVarArrayAccess) {
				VarDef varDef = (VarDef) exprVarArrayAccess.attrNameDef();
				String varName = manager.getJassVarNameFor(context, varDef);

				ExprTranslationResult left;
				if (exprVarArrayAccess.getIndexes().size() == 1) {
					left = translateExpr(context, f, exprVarArrayAccess.getIndexes().get(0)); 
				} else {
					throw new Error("only one array index is supported currently");
				}
				JassExpr e = JassExprVarArrayAccess(varName, left.getExpr());
				return new ExprTranslationResult(left.getStatements(), e);
			}
		});
	}


protected List<String> getParameterTypes(WParameters params) {
	List<String> result = Lists.newArrayListWithCapacity(params.size());
	for (WParameter p : params) {
		result.add(translateType(p.getTyp()));
	}
	return result;
}

//	/**
//	 * get the real func def instance, where the context is measured from the root
//	 * @param context the context of the call
//	 * @param relative the relative func def instance
//	 * @return
//	 */
//	protected FuncDefInstance getRealCalledFunction(ImmutableList<ClassOrModule> context, FuncDefInstance relative) {
//		ImmutableList<ClassOrModule> overallContext = mergeContexts(context, relative.getContext());
//		FunctionDefinition funcDef = relative.getDef();
//		String funcName = funcDef.getSignature().getName();
//		
//		for (ClassOrModule cm : overallContext) {
//			Collection<FuncDefInstance> funcs = cm.attrScopeFunctions().get(funcName);
//			for (FuncDefInstance fi : funcs) {
//				for (FuncDefInstance overriddenFunction : fi.getDef().attrOverriddenFunctions()) {
//					if (overriddenFunction.getDef() == funcDef) {
//						
//						
//						return overriddenFunction.inContext();
//					}
//				}
//			}
//		}
//		
//		return FuncDefInstance.create(funcDef, overallContext);
//	}


	protected boolean isDynamicFunction(FunctionDefinition def) {
		return def.getParent() instanceof ClassSlots 
				&& !def.attrIsStatic();
	}

	/**
	 * get the real called function, where the funcDefInstance is measured from the root context
	 * @param context the context of the call
	 * @param relative the funcDefInstance with relative context
	 * @return funcDefInstance with absolute context
	 */
	protected FuncDefInstance getRealCalledFunction(ImmutableList<ClassOrModule> context, FuncDefInstance relative) {
		if (context.isEmpty()) {
			// call from global scope
			return relative;
		}
		if (relative.getContext().isEmpty()) {
			// call of function in global scope
			return relative;
		}
		
		ImmutableList<ClassOrModule> overallContext = mergeContexts(context, relative.getContext());
		FunctionDefinition funcDef = relative.getDef();
		String funcName = funcDef.getSignature().getName();
		Collection<FuncDefInstance> possibleFunctions = context.head().attrAllFunctions().get(funcName);
		// search the function with the longest common prefix
		int longestCommonPrefixLength = -1;
		FuncDefInstance result = null; 
		for (FuncDefInstance funcInstance : possibleFunctions) {
			int commonPrefixLength = Utils.getCommonPrefixLength(overallContext, funcInstance.getContext());
			if (commonPrefixLength > longestCommonPrefixLength) {
				result = funcInstance;
				longestCommonPrefixLength = commonPrefixLength;
			}
		}
		if (result == null) throw new Error("could not find real function for " + relative + " in context " + Utils.printContext(context));
		WLogger.info("getRealCalledFunction  = " + result);
		return result;
	}

	private ImmutableList<ClassOrModule> mergeContexts(ImmutableList<ClassOrModule> c1, ImmutableList<ClassOrModule> c2) {
		WLogger.info("merging " + Utils.printContext(c1) + " AND " + Utils.printContext(c2));
		// merge the two contexts in such a way that each element occurs only once		
		ImmutableList<ClassOrModule> part2 = c2;
		while (!part2.isEmpty() && c1.contains(part2.head())) {
			part2 = part2.tail();
		}
		ImmutableList<ClassOrModule> result = c1.cons(part2);
		WLogger.info("	result = " + Utils.printContext(result));
		return result;
	}
	
	protected ExprTranslationResult translateModuleFunctionCall(ImmutableList<ClassOrModule> context, JassFunction f,
								ExprMemberMethod exprMemberMethod) {
		FuncDefInstance calledFunc = exprMemberMethod.attrFuncDef();
		
		JassFunction calledJassFunc = manager.getJassFunctionFor(context, calledFunc); // FIXME use right context
		
		calledFunctions.put(f, calledJassFunc);
		
		String functionName = calledJassFunc.getName();
		JassExprlist arguments = JassExprlist();
		
		if (!calledFunc.getDef().attrIsStatic()) {
			// not static
			// translate this:
			arguments.addFront(JassAst.JassExprVarAccess("this"));
		}
		
		// translate arguments:			
		List<JassStatement> statements = Lists.newLinkedList();
		ExprListTranslationResult args = translateArguments(context, f, exprMemberMethod.getArgs(), getParameterTypes(calledFunc.getDef().getSignature().getParameters()));
		statements.addAll(args.getStatements());
		arguments.addAll(args.getExprs());
		
		
		JassExprFunctionCall ex = JassExprFunctionCall(functionName, arguments);
		return new ExprTranslationResult(statements, ex);
	}

	/**
	 * a call like Math.sqrt(5)
	 * @param f
	 * @param exprMemberMethod
	 * @return
	 */
	private ExprTranslationResult translateStaticFunctionCall(ImmutableList<ClassOrModule> context, JassFunction f, ExprMemberMethod exprMemberMethod) {
		FuncDefInstance calledFunc = exprMemberMethod.attrFuncDef();
		JassFunction calledJassFunc = manager.getJassFunctionFor(context, calledFunc);
		
		calledFunctions.put(f, calledJassFunc);
		
		String functionName = calledJassFunc.getName();
		JassExprlist arguments = JassExprlist();
		List<JassStatement> statements = Lists.newLinkedList();
		
		// translate arguments:			
		ExprListTranslationResult args = translateArguments(context, f, exprMemberMethod.getArgs(), getParameterTypes(calledFunc.getDef().getSignature().getParameters()));
		statements.addAll(args.getStatements());
		arguments.addAll(args.getExprs());
		
		
		JassExprFunctionCall ex = JassExprFunctionCall(functionName, arguments);
		return new ExprTranslationResult(statements, ex);
	}

	/**
	 * c call like p.moveTo(x,y);
	 * @param f
	 * @param exprMemberMethod
	 * @return
	 */
	private ExprTranslationResult translateDynamicFunctionCall(ImmutableList<ClassOrModule> context, final JassFunction f, ExprMemberMethod exprMemberMethod) {
		Preconditions.checkNotNull(context);
		FuncDefInstance calledFunc_relative = exprMemberMethod.attrFuncDef();
		FuncDefInstance calledFunc = getRealCalledFunction(context, calledFunc_relative);
		JassFunction calledJassFunc = manager.getJassFunctionFor(calledFunc);
		
		calledFunctions.put(f, calledJassFunc);
		
		String functionName = calledJassFunc.getName();
		JassExprlist arguments = JassExprlist();
		// translate right:
		ExprTranslationResult e = translateExpr(context, f, exprMemberMethod.getLeft());
		List<JassStatement> statements = Lists.newLinkedList();
		statements .addAll(e.getStatements());
		arguments.addFront(e.getExpr());
		
		// translate arguments:			
		ExprListTranslationResult args = translateArguments(context, f, exprMemberMethod.getArgs(), getParameterTypes(calledFunc.getDef().getSignature().getParameters()));
		statements.addAll(args.getStatements());
		arguments.addAll(args.getExprs());
		
		
		JassExprFunctionCall ex = JassExprFunctionCall(functionName, arguments);
		return new ExprTranslationResult(statements, ex);
	}
	
	
	protected JassOpBinary translateOp(OpBinary op) {
		return op.match(new OpBinary.Matcher<JassOpBinary>() {

			@Override
			public JassOpBinary case_OpDivInt(OpDivInt opDivInt) {
				return JassOpDiv();
			}

			@Override
			public JassOpBinary case_OpLessEq(OpLessEq opLessEq) {
				return JassOpLessEq();
			}

			@Override
			public JassOpBinary case_OpEquals(OpEquals opEquals) {
				return JassOpEquals();
			}

			@Override
			public JassOpBinary case_OpModReal(OpModReal opModReal) {
				throw new Error("modulo operator cannot be translated");
			}

			@Override
			public JassOpBinary case_OpModInt(OpModInt opModInt) {
				throw new Error("modulo operator cannot be translated");
			}

			@Override
			public JassOpBinary case_OpUnequals(OpUnequals opUnequals) {
				return JassOpUnequals();
			}

			@Override
			public JassOpBinary case_OpDivReal(OpDivReal opDivReal) {
				return JassOpDiv();
			}

			@Override
			public JassOpBinary case_OpAnd(OpAnd opAnd) {
				return JassOpAnd();
			}

			@Override
			public JassOpBinary case_OpGreater(OpGreater opGreater) {
				return JassOpGreater();
			}

			@Override
			public JassOpBinary case_OpPlus(OpPlus opPlus) {
				return JassOpPlus();
			}

			@Override
			public JassOpBinary case_OpMult(OpMult opMult) {
				return JassOpMult();
			}

			@Override
			public JassOpBinary case_OpGreaterEq(OpGreaterEq opGreaterEq) {
				return JassOpGreaterEq();
			}

			@Override
			public JassOpBinary case_OpMinus(OpMinus opMinus) {
				return JassOpMinus();
			}

			@Override
			public JassOpBinary case_OpLess(OpLess opLess) {
				return JassOpLess();
			}

			@Override
			public JassOpBinary case_OpOr(OpOr opOr) {
				return JassOpOr();
			}
		});
	}

	protected JassVar getNewTempVar(JassFunction f, String type) {
		String name = manager.getUniqueName("temp");
		JassSimpleVar v = JassSimpleVar(type, name);
		f.getLocals().add(v);
		return v;
	}

	protected JassOpUnary translateOpUnary(OpUnary op) {
		return op.match(new OpUnary.Matcher<JassOpUnary>() {

			@Override
			public JassOpUnary case_OpNot(OpNot opNot) {
				return JassOpNot();
			}

			@Override
			public JassOpUnary case_OpMinus(OpMinus opMinus) {
				return JassOpMinus();
			}
		});
	}

	/**
	 * translate a list of expressions, makes sure that the arguments are evaluated in correct order
	 * so if we hava a list of expressions (a,b,c) and c requires additional statements then a and b
	 * will use statements too
	 * @param f 
	 */
	protected ExprListTranslationResult translateArguments(ImmutableList<ClassOrModule> context, JassFunction f, Arguments args, List<String> types) {
		List<ExprTranslationResult> translations = Lists.newLinkedList();
		int lastTranslationWithStatements = 0;
		int i = 0;
		for (Expr arg : args) {
			ExprTranslationResult translation = translateExpr(context, f, arg);
			if (translation.getStatements().size() > 0) {
				lastTranslationWithStatements = i;
			}
			translations.add(translation);
			i++;
		}
		
		
		
		List<JassStatement> statements = Lists.newLinkedList();
		List<JassExpr> exprs = Lists.newLinkedList();
		
		i = 0;
		for (ExprTranslationResult arg : translations) {
			statements.addAll(arg.getStatements());
			if (i < lastTranslationWithStatements) {
				JassVar tempVar = getNewTempVar(f, types.get(i));
				statements.add(JassStmtSet(tempVar.getName(), arg.getExpr()));
				exprs.add(JassExprVarAccess(tempVar.getName()));
			} else {
				exprs.add(arg.getExpr());
			}
		}
		return new ExprListTranslationResult(statements, exprs);
	}

	private JassSimpleVar translateParam(ImmutableList<ClassOrModule> context, WParameter param) {
		String type = translateType(param.getTyp());
		JassVar v = manager.getJassVarFor(context, param, type, false, true);
		return (JassSimpleVar) v; // can be cast, becaue its no array
	}

	private String translateType(OptTypeExpr typ) {
		return translateType(typ.attrTyp());
		
	}

	/**
	 * translates a type to the corresponding jass type
	 * in case of an array type it returns the name of the base type 
	 * @param t
	 * @return
	 */
	private String translateType(PscriptType t) {
		if (t instanceof PscriptNativeType) {
			return t.getName();
		} else if (t instanceof PScriptTypeArray) {
			return translateType(((PScriptTypeArray) t).getBaseType());
		} else if (t instanceof PScriptTypeBool) {
			return "boolean";
		} else if (t instanceof PscriptTypeClass) {
			return "integer";
		} else if (t instanceof PScriptTypeCode) {
			return "code";
		} else if (t instanceof PscriptTypeError) {
			throw new Error("Error type in program...");
		} else if (t instanceof PScriptTypeHandle) {
			PScriptTypeHandle tt = (PScriptTypeHandle) t;
			return tt.getName();
		} else if (t instanceof PScriptTypeInt) {
			return "integer";
		} else if (t instanceof PScriptTypeReal) {
			return "real";
		} else if (t instanceof PScriptTypeString) {
			return "string";
		} else if (t instanceof PScriptTypeVoid) {
			return "nothing";
		} else if (t instanceof PscriptTypeModule) {
			return "integer";
		}
		throw new Error("Cannot translate type: " + t + " // " + t.getClass());
	}

	protected void translateJassGlobalsBlock(JassGlobalBlock jassGlobalBlock) {
		for (GlobalVarDef v : jassGlobalBlock) {
			translateGlobalVariable(ROOT_CONTEXT, v);			
		}
	}

	private void translateGlobalVariable(ImmutableList<ClassOrModule> context, GlobalVarDef v) {
		JassVar jassVar = manager.getJassVarFor(context, v, translateType(v.attrTyp()), isArray(v));
		prog.getGlobals().add(jassVar);
		
		if (isCommonOrBlizzard(v.getSource())) {
			prog.attrIgnoredVariables().add(jassVar);
		}
		
		if (v.getInitialExpr() instanceof Expr) {
			Expr expr = (Expr) v.getInitialExpr();
			globalInitializers.add(new GlobalInit(context, jassVar, expr));
		}
	}

	/**
	 * returns if a position is in common.j or blizzard.j
	 */
	private boolean isCommonOrBlizzard(WPos source) {
		return source.getFile().toLowerCase().endsWith("common.j")
				|| source.getFile().toLowerCase().endsWith("blizzard.j");
	}

	private boolean isArray(VarDef v) {
		PscriptType typ = v.attrTyp();
		if (typ instanceof PScriptTypeArray) {
			return true;
		}
		return false;
	}

	protected void translateNativeFunc(NativeFunc nativeFunc) {
		// nothing to translate
	}

	protected void translateNativeType(NativeType nativeType) {
		// nothing to translate
	}

	protected void translatePackage(WPackage wPackage) {
		packages.add(wPackage);
		for (WImport imp : wPackage.getImports()) {
			WPackage importedPackage = attr.getImportedPackage(imp);
			importedPackages.put(wPackage, importedPackage);
		}
		
		for (WEntity elem : wPackage.getElements()) {
			elem.match(new WEntity.MatcherVoid() {
				
				@Override
				public void case_NativeType(NativeType nativeType) {
					translateNativeType(nativeType);
				}
				
				@Override
				public void case_NativeFunc(NativeFunc nativeFunc) {
					translateNativeFunc(nativeFunc);
				}
				
				@Override
				public void case_InitBlock(InitBlock initBlock) {
					translateInitBlock(ROOT_CONTEXT, initBlock);
				}
				
				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					translateGlobalVariable(ROOT_CONTEXT, globalVarDef);
				}
				
				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(ROOT_CONTEXT, funcDef, false);
				}
				
				@Override
				public void case_ClassDef(ClassDef classDef) {
					translateClassDef(classDef);
				}

				@Override
				public void case_ModuleDef(ModuleDef moduleDef) {
					// nothing to do, modules are translated only where they are used...
				}

				@Override
				public void case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
					translateExtensionFuncDef(ROOT_CONTEXT, extensionFuncDef);
				}
			});
		}
	}

	protected void translateInitBlock(ImmutableList<ClassOrModule> context, InitBlock initBlock) {
		trace("translate init block: " + initBlock);
		JassFunction jassFunction = manager.getJassInitFunctionFor(initBlock);
		jassFunction.setReturnType("nothing");
		jassFunction.getBody().addAll(translateStatements(context, jassFunction, initBlock.getBody()));
		
		initFunctions.put((WPackage) initBlock.attrNearestPackage(), jassFunction);
		
		prog.getFunctions().add(jassFunction);
	}

	private void trace(String string) {
		if (debug) {
			WLogger.info(string);
		}
	}

	protected void translateClassDef(final ClassDef classDef) {
		trace("translate classdef " + classDef.getName());
		String baseName = ((WPackage) classDef.attrNearestPackage()).getName() + "_" + classDef.getName() + "_";
		
		
		// create default class variables
		final JassArrayVar nextFree = JassArrayVar("integer", manager.getUniqueName(baseName + "nextFree"));
		globals.add(nextFree);
		final JassSimpleVar firstFree = JassSimpleVar("integer", manager.getUniqueName(baseName + "firstFree"));
		globals.add(firstFree);
		final JassSimpleVar maxIndex = JassSimpleVar("integer", manager.getUniqueName(baseName + "maxIndex"));
		globals.add(maxIndex);
		
		
		
		for (ClassSlot member : classDef.getSlots()) {
			trace("translate member " + member.getClass());
			final ImmutableList<ClassOrModule> context = ImmutableList.<ClassOrModule>of(classDef);
			member.match(new ClassSlot.MatcherVoid() {
				
				@Override
				public void case_OnDestroyDef(OnDestroyDef onDestroyDef) {
					JassFunction f = manager.getJassDestroyFunctionFor(classDef);
					// destroy method is added in finish method
					f.getBody().addAll(translateStatements(context, f, onDestroyDef.getBody()));
					f.getBody().addAll(translateOnDestroyForUsedModules(classDef, context, f));
				}
				
				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					trace("translate global var " + globalVarDef.getName());
					boolean isArray = !globalVarDef.attrIsStatic();
					String type = translateType(globalVarDef.attrTyp());
					JassVar v = manager.getJassVarFor(context, globalVarDef, type, isArray);
					trace("	translated to " + v);
					prog.getGlobals().add(v);
					// add initializer for static variables:
					if (globalVarDef.attrIsStatic() && globalVarDef.getInitialExpr() instanceof Expr) {
						Expr expr = (Expr) globalVarDef.getInitialExpr();
						globalInitializers.add(new GlobalInit(context, v, expr));
					}
				}
				
				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(context, funcDef, true);
				}
				
				@Override
				public void case_ConstructorDef(ConstructorDef constructorDef) {
					translateConstructorDef(classDef, constructorDef, nextFree, firstFree, maxIndex);
				}

				@Override
				public void case_ModuleUse(ModuleUse moduleUse) {
					translateModuleUse(context, moduleUse);
				}
			});
		}
		
		finishDestroyMethod(classDef, nextFree, firstFree, maxIndex);
	}

	

	protected Collection<JassStatement> translateOnDestroyForUsedModules(ClassOrModule c,
			ImmutableList<ClassOrModule> context, JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		for (ModuleDef m : c.attrUsedModules()) {
			result.addAll(translateOnDestroyForModule(m, context, f));
		}
		return result;
	}

	private Collection<JassStatement> translateOnDestroyForModule(ModuleDef m, ImmutableList<ClassOrModule> context, JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		context = context.appBack(m);
		OnDestroyDef onDestroy = null;
		for (ClassSlot s : m.getSlots()) {
			if (s instanceof OnDestroyDef) {
				onDestroy = (OnDestroyDef) s;
			}
		}
		
		if (onDestroy != null) {
			result.addAll(translateStatements(context, f, onDestroy.getBody()));
		}
		
		result.addAll(translateOnDestroyForUsedModules(m, context, f));
		return result;
	}

	protected void translateModuleUse(ImmutableList<ClassOrModule> parent_context, ModuleUse moduleUse) {
		trace("transslate module use " + Utils.printContext(parent_context) + " _ " + moduleUse.getModuleName());
		ModuleDef usedModule = moduleUse.attrModuleDef();
		ImmutableList<ClassOrModule> context =  parent_context.appBack(usedModule);
		for (ClassSlot slot : usedModule.getSlots()) {
			if (slot instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) slot;
				boolean isMethod = ! funcDef.attrIsStatic();
				translateFuncDef(context, funcDef, isMethod);
			} else if (slot instanceof GlobalVarDef) {
				GlobalVarDef globalVarDef = (GlobalVarDef) slot;
				boolean isArray = !globalVarDef.attrIsStatic();
				String type = translateType(globalVarDef.attrTyp());
				JassVar v = manager.getJassVarFor(context, globalVarDef, type, isArray);
				prog.getGlobals().add(v);
				// init var:
				if (globalVarDef.attrIsStatic() && globalVarDef.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) globalVarDef.getInitialExpr();
					globalInitializers.add(new GlobalInit(context, v, expr));
				}
			} else if (slot instanceof ModuleUse) {
				ModuleUse moduleUse2 = (ModuleUse) slot;
				translateModuleUse(context, moduleUse2);
			}
		}
		
	}

	private void finishDestroyMethod(ClassDef classDef, JassArrayVar nextFree, JassSimpleVar firstFree, JassSimpleVar maxIndex) {
		JassFunction f = manager.getJassDestroyFunctionFor(classDef);
		prog.getFunctions().add(f);

		f.getBody().add(
		//	if nextFree[this] >= 0
			JassStmtIf(
				JassExprBinary(
						JassExprVarArrayAccess(nextFree.getName(), JassExprVarAccess("this")), 
						JassOpGreaterEq(),
						JassExprIntVal(0)),
		// then
				JassStatements(
		//	show error
					JassStmtCall("BJDebugMsg", JassExprlist(JassExprStringVal("Double free of " + classDef.getName())))						
				),
		//else 
				JassStatements(
		//	nextFree[this] = firstFree
						JassStmtSetArray(nextFree.getName(), JassExprVarAccess("this"), JassExprVarAccess(firstFree.getName())),
		//	firstFree = this
						JassStmtSet(firstFree.getName(), JassExprVarAccess("this"))
			)));
		//endif
	}

	protected void translateConstructorDef(ClassDef classDef, ConstructorDef constructorDef, JassArrayVar nextFree, JassSimpleVar firstFree, JassSimpleVar maxIndex) {
		ImmutableList<ClassOrModule> context = ImmutableList.of((ClassOrModule)classDef);
		
		JassFunction f = manager.getJassConstructorFor(constructorDef);
		prog.getFunctions().add(f);
		
		f.setReturnType("integer");
		
		for (WParameter param : constructorDef.getParams()) {
			f.getParams().add(translateParam(context, param));
		}
		
		f.getLocals().add(jassThisVar());
		
		// if has free indexes (firstFree > 0)
		f.getBody().add(JassStmtIf(
				JassExprBinary(
						JassExprVarAccess(firstFree.getName()), JassOpGreater(), JassExprIntVal(0)),
		// then
				JassStatements(
		// 		this = firstFree
						JassStmtSet("this", JassExprVarAccess(firstFree.getName())),
		//		firstFree = nextFree[this]
						JassStmtSet(firstFree.getName(), JassExprVarArrayAccess(nextFree.getName(), JassExprVarAccess("this")))
				),
		// else
				JassStatements(
		// 		maxIndex = maxIndex + 1
						JassStmtSet(maxIndex.getName(), 
								JassExprBinary(JassExprVarAccess(maxIndex.getName()), JassOpPlus(), JassExprIntVal(1))),
		// 		this = maxIndex
						JassStmtSet("this", JassExprVarAccess(maxIndex.getName()))
		// endif
				)));
		// nextFree[this] = -1
		f.getBody().add(JassStmtSetArray(nextFree.getName(), JassExprVarAccess("this"), JassExprIntVal(-1)));
		
		// TODO call module constructors if feasible, compile error otherwise
		f.getBody().addAll(translateModuleUseConstructors(classDef, context, f));
		
		
		// init members:
		for (ClassSlot member : classDef.getSlots()) {
			if (member instanceof GlobalVarDef) {
				GlobalVarDef var = (GlobalVarDef) member;
				if (var.attrIsDynamicClassMember() && var.getInitialExpr() instanceof Expr) {
					Expr initial = (Expr) var.getInitialExpr();
					ExprTranslationResult e = translateExpr(context, f, initial);
					f.getBody().addAll(e.getStatements());
					String jassVar = manager.getJassVarNameFor(context, var);
					f.getBody().add(JassStmtSetArray(jassVar, JassExprVarAccess("this"), e.getExpr()));
				}
			}
		}
		
		
		
		// custom code:
		f.getBody().addAll(translateStatements(context, f, constructorDef.getBody()));
		
		// return this:
		f.getBody().add(JassStmtReturn(JassExprVarAccess("this")));
	}

	private Collection<JassStatement> translateModuleUseConstructors(ClassOrModule c,
			ImmutableList<ClassOrModule> context, JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		for (ModuleDef m : c.attrUsedModules()) {
			result.addAll(translateModuleConstructor(m, context, f));
		}
		return result;
	}

	private Collection<JassStatement> translateModuleConstructor(ModuleDef m, ImmutableList<ClassOrModule> context,	JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		context = context.appBack(m);
		// add used modules
		result.addAll(translateModuleUseConstructors(m, context, f));
		
		ConstructorDef constructor = null;
		// initialize module variables:
		for (ClassSlot s : m.getSlots()) {
			if (s instanceof GlobalVarDef) {
				GlobalVarDef v = (GlobalVarDef) s;
				if (!v.attrIsStatic() && v.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) v.getInitialExpr();
					ExprTranslationResult er = translateExpr(context, f, expr);
					result.addAll(er.getStatements());
					String varName = getJassVarNameFor(context, v);
					result.add(JassStmtSetArray(varName,JassExprVarAccess("this"), er.getExpr()));
				}
			} else if (s instanceof ConstructorDef) {
				constructor  = (ConstructorDef) s;
			}
		}
		
		// custom code
		if (constructor != null) {
			result.addAll(translateStatements(context, f, constructor.getBody()));
		}
		
		return result;
	}

	

	
//	/**
//	 * translate a vardef to a jassvar without adding the jassvariable to anything
//	 * @param f
//	 * @return
//	 */
//	public JassVar translateVarDef(VarDef v) { // TODO use context here, there might be more than one instance per variable
//		return v.match(new VarDef.Matcher<JassVar>() {
//
//			@Override
//			public JassVar case_WParameter(WParameter wParameter) {
//				return JassSimpleVar(translateType(wParameter.attrTyp()), wParameter.getName());
//			}
//
//			@Override
//			public JassVar case_GlobalVarDef(GlobalVarDef globalVarDef) {
//				PscriptType typ = globalVarDef.attrTyp();
//				String name = globalVarDef.getName();
//				if (globalVarDef.attrNearestClassDef() != null) {
//					name = globalVarDef.attrNearestClassDef().getName() + "_" + name;
//				}
//				if (globalVarDef.attrNearestPackage() instanceof WPackage) {
//					name = ((WPackage) globalVarDef.attrNearestPackage()).getName() + "_" + name;
//				}	
//				name = manager.getUniqueName(globalVarDef, name);
//				if (globalVarDef.attrIsClassMember()) {
//					return JassArrayVar(translateType(typ), name);	
//				} else {
//					if (typ instanceof PScriptTypeArray) {
//						PScriptTypeArray arrayTyp = (PScriptTypeArray) typ;
//						return JassArrayVar(translateType(arrayTyp.getBaseType()), name);					
//					} else {
//						return JassSimpleVar(translateType(typ), name);
//					}
//				}
//			}
//
//			@Override
//			public JassVar case_LocalVarDef(LocalVarDef localVarDef) {
//				PscriptType typ = localVarDef.attrTyp();
//				if (typ instanceof PScriptTypeArray) {
//					PScriptTypeArray arrayTyp = (PScriptTypeArray) typ;
//					return JassArrayVar(translateType(arrayTyp.getBaseType()), localVarDef.getName());					
//				} else {
//					return JassSimpleVar(translateType(typ), localVarDef.getName());
//				}
//			}
//		});
//	}

	
}
