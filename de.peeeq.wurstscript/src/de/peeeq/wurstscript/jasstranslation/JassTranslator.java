package de.peeeq.wurstscript.jasstranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassArrayVar;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprNull;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprStringVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarArrayAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprlist;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunction;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunctions;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreaterEq;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassProg;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVar;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVars;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtIf;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSet;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtSetArray;
import static de.peeeq.wurstscript.jassAst.JassAst.JassVars;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ClassSlots;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeError;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;

public class JassTranslator {

	private static final boolean debug = false;
	private static final ImmutableList<ClassOrModule> ROOT_CONTEXT = ImmutableList.<ClassOrModule>emptyList();
	JassManager manager;
	private CompilationUnit wurstProg;
	JassProg prog;
	private JassVars globals;
	private JassFunctions functions;
	private List<GlobalInit> globalInitializers = Lists.newLinkedList();
	private BiMap<WPackage, JassFunction> initFunctions = HashBiMap.create();
	Multimap<JassFunction, JassFunction> calledFunctions = HashMultimap.create();
	private Multimap<WPackage, WPackage> importedPackages = HashMultimap.create();
	private JassFunction initGlobalsFunc;
	private Collection<WPackage> packages = Lists.newLinkedList();
	Set<String> handleSubTypes = Sets.newHashSet("handle");


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
			attr.addError(Ast.WPos("", null, 0, 0), "Cannot generate code because of a cyclic dependency between the following functions: \n" + msg);
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
					JassVar jassVar = manager.getJassVarForTranslatedVar(varDef);
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
			attr.addError(Ast.WPos("", LineOffsets.dummy, 0, 0), "Cannot generate code because of a cyclic dependency between the following variables: \n" + msg);
		}

		Set<JassVar> initializedVars = Sets.newHashSet();
		for (GlobalInit gi : globalInitializers) {
			if (! prog.attrIgnoredVariables().contains(gi.v)) {
				ExprTranslationResult e = translateExpr(initGlobalsFunc, gi.initialExpr);
				body.addAll(e.getStatements());
				body.add(JassStmtSet(gi.v.getName(), e.getExpr()));
				initializedVars.add(gi.v);
			}
		}

		// add default initialization for all vars which are not initialized yet
		for (JassVar globalVar : prog.getGlobals()) {
			if (globalVar instanceof JassSimpleVar) {
				if (!initializedVars.contains(globalVar)) {
					if (!prog.attrIgnoredVariables().contains(globalVar)) {
						body.add(JassStmtSet(globalVar.getName(), getDefaultValueForJassType(globalVar.getType())));
					}
				}
			}
		}
	}

	private JassExpr getDefaultValueForJassType(String type) {
		if (type.equals("integer")) {
			return JassExprIntVal(0);
		} else if (type.equals("real")) {
			return JassAst.JassExprRealVal(0.);
		} else if (type.equals("boolean")) {
			return JassAst.JassExprBoolVal(false);
		} else {
			return JassAst.JassExprNull();
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
				calledFunctions.put(mainFunction, f);
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
				translateFuncDef(funcDef, false);
			}

		});
	}

	private void translateExtensionFuncDef(ExtensionFuncDef funcDef) {
		JassFunction f = manager.getJassFunctionFor(funcDef);
		f.setReturnType(translateType(funcDef.getReturnTyp()));

		// add implicit parameter 'this'
		f.getParams().add(JassAst.JassSimpleVar(translateType(funcDef.getExtendedType().attrTyp()), "this"));

		for (WParameter param : funcDef.getParameters()) {
			f.getParams().add(translateParam(param));
		}
		translateFunctionBody(funcDef.getBody(), f);
		prog.getFunctions().add(f);
	}

	private void translateFuncDef(FuncDef funcDef, boolean isMethod) {
		if (funcDef.attrIsAbstract()) {
			// do not translate abstract methods, they no hav body, u no?
			return;
		}
		JassFunction f = manager.getJassFunctionFor(funcDef);

		if (isCommonOrBlizzard(funcDef.getSource())) {
			prog.attrIgnoredFunctions().add(f);
		}

		f.setReturnType(translateType(funcDef.getReturnTyp()));
		if (isMethod && !funcDef.attrIsStatic()) {
			// methods have an additional implicit parameter
			f.getParams().add(jassThisVar());
		}
		for (WParameter param : funcDef.getParameters()) {
			f.getParams().add(translateParam(param));
		}
		translateFunctionBody(funcDef.getBody(), f);
		
		prog.getFunctions().add(f);
	}

	private void translateFunctionBody(WStatements body, JassFunction f) {
		f.getBody().addAll(translateStatements(f, body));
		
		if (!body.attrDoesReturn()) {
			// set handle variables to null
			addHandleNullSetters(f);
		}
	}

	private void addHandleNullSetters(JassFunction f) {
		for (JassVar l : f.getLocals()) {
			if (handleSubTypes.contains(l.getType())) {
				f.getBody().add(JassStmtSet(l.getName(), JassExprNull()));
			}
		}
	}

	private List<JassStatement> translateStatements(JassFunction f, WStatements statements) {
		return new JassTranslatorStatements(this).translateStatements(f, statements);
	}

	private JassSimpleVar jassThisVar() {
		return JassAst.JassSimpleVar("integer", "this");
	}

	


	ClassDef getClassDef(PscriptTypeModuleInstanciation typ) {
		AstElement node = typ.getDef();
		return Utils.findParentOfType(ClassDef.class, node);
	}

	Set<LocalVarDef> getUsedLocalVarsInExpr(Expr expr) {
		final Set<LocalVarDef> result = Sets.newHashSet();
		expr.accept(new Expr.DefaultVisitor() {
			@Override
			public void visit(ExprVarAccess exprVarAccess) {
				NameDef varDef = exprVarAccess.attrNameDef();
				if (varDef instanceof LocalVarDef) {
					result.add((LocalVarDef) varDef);
				}
			}
		});
		return result;
	}



	ExprTranslationResult translateExpr(final JassFunction f, Expr expr) {
		return new JassTranslatorExpressions(this).translateExpr(f, expr);
	}

	

	JassVar getNewTempVar(JassFunction f, String type) {
		String name = manager.getUniqueName("temp");
		JassSimpleVar v = JassSimpleVar(type, name);
		f.getLocals().add(v);
		return v;
	}

	

	

	private JassSimpleVar translateParam(WParameter param) {
		String type = translateType(param.getTyp());
		JassVar v = manager.getJassVarFor(param, type, false, true);
		return (JassSimpleVar) v; // can be cast, becaue its no array
	}

	String translateType(OptTypeExpr typ) {
		return translateType(typ.attrTyp());

	}

	/**
	 * translates a type to the corresponding jass type
	 * in case of an array type it returns the name of the base type 
	 * @param t
	 * @return
	 */
	String translateType(PscriptType t) {
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
		} else if (t instanceof PscriptTypeTypeParam) {
			return "integer";
		}
		throw new Error("Cannot translate type: " + t + " // " + t.getClass());
	}

	private void translateJassGlobalsBlock(JassGlobalBlock jassGlobalBlock) {
		for (GlobalVarDef v : jassGlobalBlock) {
			translateGlobalVariable(v);			
		}
	}

	private void translateGlobalVariable(GlobalVarDef v) {
		JassVar jassVar = manager.getJassVarFor(v, translateType(v.attrTyp()), isArray(v));
		prog.getGlobals().add(jassVar);

		if (isCommonOrBlizzard(v.getSource())) {
			prog.attrIgnoredVariables().add(jassVar);
		}

		if (v.getInitialExpr() instanceof Expr) {
			Expr expr = (Expr) v.getInitialExpr();
			globalInitializers.add(new GlobalInit(jassVar, expr));
		}
	}

	/**
	 * returns if a position is in common.j or blizzard.j
	 */
	private boolean isCommonOrBlizzard(WPos source) {
		return source.getFile().toLowerCase().endsWith("common.j")
				|| source.getFile().toLowerCase().endsWith("blizzard.j");
	}

	boolean isArray(VarDef v) {
		PscriptType typ = v.attrTyp();
		if (typ instanceof PScriptTypeArray) {
			return true;
		}
		return false;
	}

	private void translateNativeFunc(NativeFunc nativeFunc) {
		// nothing to translate
	}

	private void translateNativeType(NativeType nativeType) {
		// nothing to translate
		PscriptType superTyp = nativeType.getOptTyp().attrTyp();
		if (superTyp.isSubtypeOf(PScriptTypeHandle.instance())) {
			handleSubTypes.add(nativeType.getName());
		}
	}

	private void translatePackage(WPackage wPackage) {
		packages.add(wPackage);
		for (WImport imp : wPackage.getImports()) {
			WPackage importedPackage = imp.attrImportedPackage();
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
					translateInitBlock(initBlock);
				}

				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					translateGlobalVariable(globalVarDef);
				}

				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(funcDef, false);
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
					translateExtensionFuncDef(extensionFuncDef);
				}

				@Override
				public void case_TypeParamDef(TypeParamDef typeParamDef) {
					throw new Error("not implemented");
				}
			});
		}
	}

	private void translateInitBlock(InitBlock initBlock) {
		trace("translate init block: " + initBlock);
		JassFunction jassFunction = manager.getJassInitFunctionFor(initBlock);
		jassFunction.setReturnType("nothing");
		translateFunctionBody(initBlock.getBody(), jassFunction);

		initFunctions.put((WPackage) initBlock.attrNearestPackage(), jassFunction);

		prog.getFunctions().add(jassFunction);
	}

	private void trace(String string) {
		if (debug) {
			WLogger.info(string);
		}
	}

	private void translateClassDef(final ClassDef classDef) {
		trace("translate classdef " + classDef.getName());
		String baseName = ((WPackage) classDef.attrNearestPackage()).getName() + "_" + classDef.getName() + "_";


		// create default class variables
		final JassArrayVar nextFree = JassArrayVar("integer", manager.getUniqueName(baseName + "nextFree"));
		globals.add(nextFree);
		final JassSimpleVar firstFree = JassSimpleVar("integer", manager.getUniqueName(baseName + "firstFree"));
		globals.add(firstFree);
		final JassSimpleVar maxIndex = JassSimpleVar("integer", manager.getUniqueName(baseName + "maxIndex"));
		globals.add(maxIndex);



		translateClassSlots(classDef, classDef.getSlots(), nextFree, firstFree, maxIndex, false);

		finishDestroyMethod(classDef, nextFree, firstFree, maxIndex);
	}


	private void translateClassSlots(final ClassDef classDef,	ClassSlots slots, final JassArrayVar nextFree
			, final JassSimpleVar firstFree, final JassSimpleVar maxIndex, final boolean inModule) {

		for (ClassSlot member : slots) {
			trace("translate member " + member.getClass());
			final ImmutableList<ClassOrModule> context = ImmutableList.<ClassOrModule>of(classDef);
			member.match(new ClassSlot.MatcherVoid() {

				@Override
				public void case_OnDestroyDef(OnDestroyDef onDestroyDef) {
					JassFunction f = manager.getJassDestroyFunctionFor(classDef);
					// destroy method is added in finish method
					f.getBody().addAll(translateStatements(f, onDestroyDef.getBody()));
					f.getBody().addAll(translateOnDestroyForUsedModules(classDef, f));
				}

				@Override
				public void case_GlobalVarDef(GlobalVarDef globalVarDef) {
					trace("translate global var " + globalVarDef.getName());
					boolean isArray = !globalVarDef.attrIsStatic() || (globalVarDef.attrTyp() instanceof PScriptTypeArray);
					String type = translateType(globalVarDef.attrTyp());
					JassVar v = manager.getJassVarFor(globalVarDef, type, isArray);
					trace("	translated to " + v);
					prog.getGlobals().add(v);
					// add initializer for static variables:
					if (globalVarDef.attrIsStatic() && globalVarDef.getInitialExpr() instanceof Expr) {
						Expr expr = (Expr) globalVarDef.getInitialExpr();
						globalInitializers.add(new GlobalInit(v, expr));
					}
				}

				@Override
				public void case_FuncDef(FuncDef funcDef) {
					translateFuncDef(funcDef, true);
				}

				@Override
				public void case_ConstructorDef(ConstructorDef constructorDef) {
					if (!inModule) { // only translate class constructor
						translateConstructorDef(classDef, constructorDef, nextFree, firstFree, maxIndex);
					}
				}

				@Override
				public void case_ModuleUse(ModuleUse moduleUse) {

				}

				@Override
				public void case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
					translateClassSlots(classDef, moduleInstanciation.getSlots(), nextFree, maxIndex, maxIndex, true);
				}


			});
		}

	}


	private Collection<JassStatement> translateOnDestroyForUsedModules(ClassOrModule c,
			JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		for (ModuleDef m : c.attrUsedModules()) {
			result.addAll(translateOnDestroyForModule(m, f));
		}
		return result;
	}

	private Collection<JassStatement> translateOnDestroyForModule(ModuleDef m, JassFunction f) {
		Collection<JassStatement> result = Lists.newLinkedList();
		OnDestroyDef onDestroy = null;
		for (ClassSlot s : m.getSlots()) {
			if (s instanceof OnDestroyDef) {
				onDestroy = (OnDestroyDef) s;
			}
		}

		if (onDestroy != null) {
			result.addAll(translateStatements(f, onDestroy.getBody()));
		}

		result.addAll(translateOnDestroyForUsedModules(m, f));
		return result;
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
		
		addHandleNullSetters(f);
	}

	private void translateConstructorDef(ClassDef classDef, ConstructorDef constructorDef, JassArrayVar nextFree, JassSimpleVar firstFree, JassSimpleVar maxIndex) {
		System.out.println("translating constr fro " + classDef.getName() + " -> " + constructorDef);
		JassFunction f = manager.getJassConstructorFor(constructorDef);
		prog.getFunctions().add(f);

		f.setReturnType("integer");

		for (WParameter param : constructorDef.getParameters()) {
			f.getParams().add(translateParam(param));
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

		// call module constructors if feasible, compile error otherwise
		for (ModuleInstanciation m : classDef.attrModuleInstanciations()) {
			translateModuleUseConstructors(m, f);
		}
		


		// init members:
		for (ClassSlot member : classDef.getSlots()) {
			if (member instanceof GlobalVarDef) {
				GlobalVarDef var = (GlobalVarDef) member;
				if (var.attrIsDynamicClassMember() && var.getInitialExpr() instanceof Expr) {
					Expr initial = (Expr) var.getInitialExpr();
					ExprTranslationResult e = translateExpr(f, initial);
					f.getBody().addAll(e.getStatements());
					String jassVar = manager.getJassVarNameFor(var);
					f.getBody().add(JassStmtSetArray(jassVar, JassExprVarAccess("this"), e.getExpr()));
				} // TODO default value?
			}
		}



		// custom code:
		f.getBody().addAll(translateStatements(f, constructorDef.getBody()));

		addHandleNullSetters(f);
		
		// return this:
		f.getBody().add(JassStmtReturn(JassExprVarAccess("this")));
	}

	private void translateModuleUseConstructors(ModuleInstanciation m, JassFunction f) {
		// translate child modules:
		for (ModuleInstanciation childM : m.attrModuleInstanciations()) {
			translateModuleUseConstructors(childM, f);
		}

		ConstructorDef constructor = null;
		// initialize module variables:
		for (ClassSlot s : m.getSlots()) {
			if (s instanceof GlobalVarDef) {
				GlobalVarDef v = (GlobalVarDef) s;
				if (!v.attrIsStatic() && v.getInitialExpr() instanceof Expr) {
					Expr expr = (Expr) v.getInitialExpr();
					ExprTranslationResult er = translateExpr(f, expr);
					f.getBody().addAll(er.getStatements());
					String varName = manager.getJassVarNameFor(v);
					f.getBody().add(JassStmtSetArray(varName,JassExprVarAccess("this"), er.getExpr()));
				}
			} else if (s instanceof ConstructorDef) {
				constructor  = (ConstructorDef) s;
			}
		}

		// custom code
		if (constructor != null) {
			f.getBody().addAll(translateStatements(f, constructor.getBody()));
		}
	}


}
