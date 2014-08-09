package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImBoolVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFunction;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFunctionCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFunctions;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIntVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImNull;
import static de.peeeq.wurstscript.jassIm.JassIm.ImProg;
import static de.peeeq.wurstscript.jassIm.JassIm.ImRealVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSet;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSimpleType;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVars;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVoid;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlag.IS_BJ;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlag.IS_COMPILETIME;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlag.IS_NATIVE;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlag.IS_TEST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.datastructures.ImmutableTree;
import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.EnumMembers;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprSuper;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TranslatedToImFunction;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeExprArray;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeExprThis;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.jassIm.JassImElementWithLeft;
import de.peeeq.wurstscript.jassIm.JassImElementWithVar;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;

public class ImTranslator {


	public static final String $DEBUG_PRINT = "$debugPrint";

	private static final AstElement emptyTrace = Ast.NoExpr();

	private @Nullable Multimap<ImFunction, ImFunction> callRelations = null;
	private @Nullable Set<ImVar> usedVariables = null;
	private @Nullable Set<ImVar> readVariables = null;
	private @Nullable Set<ImFunction> usedFunctions = null;

	private @Nullable ImFunction debugPrintFunction;

	private final Map<TranslatedToImFunction, ImFunction> functionMap = new LinkedHashMap<>();
	private @Nullable ImFunction globalInitFunc;

	private final ImProg imProg;

	final Map<WPackage, ImFunction> initFuncMap = new LinkedHashMap<>();

	private final Map<TranslatedToImFunction, ImVar> thisVarMap = new LinkedHashMap<>();

	private final Set<WPackage> translatedPackages = new LinkedHashSet<>();
	private final Set<ClassDef>  translatedClasses = new LinkedHashSet<>();


	final Map<ClassDef, Integer> typeIdMap = new LinkedHashMap<>();
	final Map<ClassDef, Integer> typeIdMapMax = new LinkedHashMap<>();

	private final Map<VarDef, ImVar> varMap = new LinkedHashMap<>();

	private final WurstModel wurstProg;

	private @Nullable ImFunction mainFunc = null;

	private @Nullable ImFunction configFunc = null;

	private final Map<ImVar, ImmutableTree<ImVar>> varsForTupleVar = new LinkedHashMap<>();

	private boolean isUnitTestMode;

	private ImVar lastInitFunc = JassIm.ImVar(emptyTrace, WurstTypeString.instance().imTranslateType(), "lastInitFunc", false);

	private boolean addInitChecks = false;

	AstElement lasttranslatedThing;  
	
	public ImTranslator(WurstModel wurstProg, boolean isUnitTestMode) {
		this.wurstProg = wurstProg;
		this.lasttranslatedThing = wurstProg;
		this.isUnitTestMode = isUnitTestMode;
		imProg = ImProg(ImVars(), ImFunctions(), JassIm.ImClasses(), new LinkedHashMap<ImVar,ImExpr>());
	}


	/**
	 * translates a program 
	 */
	public ImProg translateProg() {
		try {
			globalInitFunc = ImFunction(emptyTrace, "initGlobals", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
			addFunction(getGlobalInitFunc());
			debugPrintFunction = ImFunction(emptyTrace, $DEBUG_PRINT, ImVars(JassIm.ImVar(wurstProg, WurstTypeString.instance().imTranslateType(), "msg", false)), ImVoid(), ImVars(), ImStmts(), flags(IS_NATIVE,IS_BJ));

			if (addInitChecks) {
				imProg.getGlobals().add(lastInitFunc);
			}

			for (CompilationUnit cu : wurstProg) {
				translateCompilationUnit(cu);
			}

			if (mainFunc == null) {
				mainFunc = ImFunction(emptyTrace, "main", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
				addFunction(mainFunc);
			}
			if (configFunc == null) {
				configFunc = ImFunction(emptyTrace, "config", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
				addFunction(configFunc);
			}
			finishInitFunctions();

			return imProg;
		} catch (CompileError t) {
			throw t;
		} catch (Throwable t) {
			WLogger.severe(t);
			throw new RuntimeException("There was a Wurst bug in the translation of " + Utils.printElementWithSource(lasttranslatedThing) + ": " + t.getMessage() + 
					"\nPlease open a ticket with source code and the error log.", t);
		}
	}

	private ArrayList<FunctionFlag> flags(FunctionFlag ... flags) {
		return Lists.newArrayList(flags);
	}


	private void translateCompilationUnit(CompilationUnit cu) {
		lasttranslatedThing = cu;
		// TODO can we make this smarter? Only translate functions which are actually called...
		for (WPackage p : cu.getPackages()) {
			lasttranslatedThing = p;
			p.imTranslateTLD(this);
		}
		for (JassToplevelDeclaration tld : cu.getJassDecls()) {
			lasttranslatedThing = tld;
			tld.imTranslateTLD(this);
		}	
	}




	private void finishInitFunctions() {
		// init globals, at beginning of main func:
		getMainFunc().getBody().add(0, ImFunctionCall(emptyTrace, globalInitFunc, ImExprs(), false, CallType.NORMAL));
		if (addInitChecks) {
			getMainFunc().getBody().add(0, JassIm.ImSet(emptyTrace, lastInitFunc, JassIm.ImStringVal("init globals")));
		}
		
		addInitSuccessCheck();
		
		
		for (ImFunction initFunc : initFuncMap.values()) {
			addFunction(initFunc);
		}
		Set<WPackage> calledInitializers = Sets.newLinkedHashSet();
		for (WPackage p : Utils.sortByName(initFuncMap.keySet())) {
			callInitFunc(calledInitializers, p);
		}
		
		if (addInitChecks) {
			getMainFunc().getBody().add(JassIm.ImSet(emptyTrace, lastInitFunc, JassIm.ImStringVal("")));
		}
		
		
	}


	private void addInitSuccessCheck() {
		if (!addInitChecks) {
			return;
		}
		ImFunction timerStartFunc = getNativeFunc("TimerStart");
		ImFunction createTimerFunc = getNativeFunc("CreateTimer");
		ImFunction print = getNativeFunc("BJDebugMsg");
		ImExpr whichTimer = JassIm.ImFunctionCall(emptyTrace, createTimerFunc, JassIm.ImExprs(), false, CallType.NORMAL);
		ImExpr timeout = JassIm.ImRealVal("1.");
		ImExpr periodic = JassIm.ImBoolVal(false);
		ImStmts thenStatements = JassIm.ImStmts(JassIm.ImError(JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImStringVal("Initialization thread crashed in "), JassIm.ImVarAccess(lastInitFunc)))));
		ImStmts body = JassIm.ImStmts(
				JassIm.ImIf(emptyTrace, JassIm.ImOperatorCall(WurstOperator.NOTEQ, JassIm.ImExprs(JassIm.ImVarAccess(lastInitFunc), JassIm.ImStringVal(""))), 
						thenStatements, 
						JassIm.ImStmts()
				));
		ImFunction initCheckFunc = JassIm.ImFunction(emptyTrace, "initCheckFunc", JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), body, Lists.<FunctionFlag>newArrayList());
		ImExpr handlerFunc = JassIm.ImFuncRef(initCheckFunc);
		
		getMainFunc().getBody().add(2, JassIm.ImFunctionCall(emptyTrace, print, JassIm.ImExprs(JassIm.ImStringVal("BLUB")), false, CallType.NORMAL));
		getMainFunc().getBody().add(3, JassIm.ImFunctionCall(emptyTrace, timerStartFunc, JassIm.ImExprs(whichTimer, timeout, periodic, handlerFunc), false, CallType.NORMAL));
	}


	private ImFunction getNativeFunc(String funcName) {
		return getFuncFor((TranslatedToImFunction) Utils.getFirst(wurstProg.lookupFuncs(funcName)).getNameDef());
	}

	private void callInitFunc(Set<WPackage> calledInitializers, WPackage p) {
		Preconditions.checkNotNull(p);
		if (calledInitializers.contains(p)) {
			return;
		}
		calledInitializers.add(p);
		// first initialize all packages imported by this package:
		for (WPackage dep : p.attrInitDependencies()) {
			callInitFunc(calledInitializers, dep);
		}
		ImFunction initFunc = initFuncMap.get(p);
		if (initFunc == null) {
			return;
		}
		if (addInitChecks) {
			getMainFunc().getBody().add(JassIm.ImSet(emptyTrace, lastInitFunc, JassIm.ImStringVal(p.getName())));
		}
		getMainFunc().getBody().add(ImFunctionCall(emptyTrace, initFunc, ImExprs(), false, CallType.NORMAL));
	}

	private void addFunction(ImFunction f) {
		imProg.getFunctions().add(f);
	}

	public void addGlobal(ImVar v) {
		imProg.getGlobals().add(v);
	}


	public void addGlobalInitalizer(ImVar v, PackageOrGlobal packageOrGlobal, OptExpr initialExpr) {
		if (initialExpr instanceof Expr) {
			Expr expr = (Expr) initialExpr;

			ImFunction f;
			if (packageOrGlobal instanceof WPackage) {
				WPackage p = (WPackage) packageOrGlobal;
				f = getInitFuncFor(p);
			} else {
				f = globalInitFunc;
			}
			AstElement trace = packageOrGlobal == null ? emptyTrace : packageOrGlobal;
			ImExpr translated = expr.imTranslateExpr(this, f);
			if (!v.getIsBJ()) {
				// add init statement for non-bj vars
				// bj-vars are already initalized by blizzard
				f.getBody().add(ImSet(trace, v, translated));
			}
			imProg.getGlobalInits().put(v, translated);
		}
	}
	
	public void addGlobalWithInitalizer(ImVar g, ImExpr initial) {
		imProg.getGlobals().add(g);
		getGlobalInitFunc().getBody().add(ImSet(g.getTrace(), g, initial));
		imProg.getGlobalInits().put(g, (ImExpr) initial.copy());
	}

	public ImFunction getDebugPrintFunc() {
		return debugPrintFunction;
	}

	public ImExpr getDefaultValueForJassType(ImType type) {
		if (type instanceof ImSimpleType) {
			ImSimpleType imSimpleType = (ImSimpleType) type;
			String typeName = imSimpleType.getTypename();
			return getDefaultValueForJassTypeName(typeName);
		} else if (type instanceof ImTupleType) {
			ImTupleType imTupleType = (ImTupleType) type;
			return getDefaultValueForJassType(imTupleType.getTypes().get(0));
		} else {
			throw new IllegalArgumentException("could not get default value for type " + type);
		}
	}

	private ImExpr getDefaultValueForJassTypeName(String typeName) {
		if (typeName.equals("integer")) {
			return ImIntVal(0);
		} else if (typeName.equals("real")) {
			return ImRealVal("0.");
		} else if (typeName.equals("boolean")) {
			return ImBoolVal(false);
		} else {
			return ImNull();
		}
	}
	
	public GetAForB<StructureDef, ImFunction> destroyFunc = new GetAForB<StructureDef, ImFunction>() {
		
		@Override
		public ImFunction initFor(StructureDef classDef) {
			ImVars params = ImVars(JassIm.ImVar(classDef, TypesHelper.imInt(), "this", false));
			ImFunction f = JassIm.ImFunction(classDef.getOnDestroy(), "destroy" + classDef.getName(), params, TypesHelper.imVoid(), ImVars(), ImStmts(), flags());
			addFunction(f);
			return f;
		}
	};
	
	public GetAForB<StructureDef, ImMethod> destroyMethod = new GetAForB<StructureDef, ImMethod>() {
		
		@Override
		public ImMethod initFor(StructureDef classDef) {
			ImFunction impl = destroyFunc.getFor(classDef);
			boolean abstr;
			if (classDef instanceof ClassDef) {
				abstr = false; // all classes can have ondestroy, so this is not abstract 
			} else {
				// interface destroy methods are abstract
				abstr = true;
			}
			ImMethod m = JassIm.ImMethod(classDef, "destroy" + classDef.getName(), 
					impl, Lists.<ImMethod>newArrayList(), abstr);
			return m;
		}
	};
	
	public GetAForB<ImClass, ImFunction> allocFunc = new GetAForB<ImClass, ImFunction>() {

		@Override
		public ImFunction initFor(ImClass c) {
			return JassIm.ImFunction(c.getTrace(), "alloc_" + c.getName(), JassIm.ImVars(), TypesHelper.imInt(), 
					JassIm.ImVars(), JassIm.ImStmts(), Collections.<FunctionFlag>emptyList());
		}
		
	};
	
	public GetAForB<ImClass, ImFunction> deallocFunc = new GetAForB<ImClass, ImFunction>() {

		@Override
		public ImFunction initFor(ImClass c) {
			return JassIm.ImFunction(c.getTrace(), "dealloc_" + c.getName(), JassIm.ImVars(JassIm.ImVar(c.getTrace(), TypesHelper.imInt(), "obj", false)), TypesHelper.imVoid(), 
					JassIm.ImVars(), JassIm.ImStmts(), Collections.<FunctionFlag>emptyList());
		}
		
	};
	

	public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
		if (functionMap.containsKey(funcDef)) {
			return functionMap.get(funcDef);
		}
		String name = getNameFor(funcDef);
		List<FunctionFlag> flags = flags();
		if (funcDef instanceof NativeFunc) {
			flags.add(IS_NATIVE);
		}
		if (isBJ(funcDef.getSource())) {
			flags.add(IS_BJ);
		}
		if (isExtern(funcDef)) {
			flags.add(FunctionFlag.IS_EXTERN);
		}
		if (funcDef instanceof FuncDef) {
			FuncDef funcDef2 = (FuncDef) funcDef;
			if (funcDef2.attrIsCompiletime()) {
				flags.add(IS_COMPILETIME);
			}
			if (funcDef2.attrHasAnnotation("compiletimenative")) {
				flags.add(FunctionFlag.IS_COMPILETIME_NATIVE);
			}
			if (funcDef2.attrHasAnnotation("test")) {
				flags.add(IS_TEST);
			}
		}
		ImFunction f = JassIm.ImFunction(funcDef, name, ImVars(), ImVoid(), ImVars(), ImStmts(), flags);
		funcDef.imCreateFuncSkeleton(this, f);
		addFunction(f);
		functionMap.put(funcDef, f);
		return f;
	}


	private boolean isExtern(TranslatedToImFunction funcDef) {
		if (funcDef instanceof AstElementWithModifiers) {
			AstElementWithModifiers f = (AstElementWithModifiers) funcDef;
			for (Modifier m: f.getModifiers()) {
				if (m instanceof Annotation) {
					Annotation a = (Annotation) m;
					if (a.getAnnotationType().equals("@extern")) {
						return true;
					}
				}
			}
		}
		return false;
	}


	private boolean isBJ(WPos source) {
		String f = source.getFile().toLowerCase();
		return f.endsWith("blizzard.j") || f.endsWith("common.j");
	}

	public ImFunction getInitFuncFor(WPackage p) {
		ImFunction f = initFuncMap.get(p); 
		if (f == null) {
			f = JassIm.ImFunction(p, "init_" + p.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
			initFuncMap.put(p, f);
			// TODO more precise trace
		}
		return f ;
	}

	/**
	 * returns a suitable name for the given element
	 * the returned name is a valid jass identifier 
	 */
	public String getNameFor(AstElement e) {
		if (e instanceof FuncDef) {
			FuncDef f = (FuncDef) e;
			if (f.attrNearestStructureDef() != null) {
				return f.attrNearestStructureDef().getName() + "_" + f.getName();
			}
		} else if (e instanceof ExtensionFuncDef) {
			ExtensionFuncDef f = (ExtensionFuncDef) e;
			return getNameFor(f.getExtendedType()) + "_" + f.getName();
		} else if (e instanceof TypeExprSimple) {
			TypeExprSimple t = (TypeExprSimple) e;
			return t.getTypeName();
		} else if (e instanceof TypeExprSimple) {
			TypeExprSimple t = (TypeExprSimple) e;
			return t.getTypeName();
		} else if (e instanceof TypeExprThis) {
			return "thistype";
		} else if (e instanceof TypeExprArray) {
			TypeExprArray t = (TypeExprArray) e;
			return getNameFor(t.getBase()) + "Array";
		}



		if (e instanceof AstElementWithName) {
			AstElementWithName wn = (AstElementWithName) e;
			return wn.getName();
		} else if (e instanceof ConstructorDef) {
			return "new_" + e.attrNearestClassDef().getName();
		}
		String r = e.getClass().getName();
		while (e != null) {
			if (e instanceof AstElementWithName) {
				AstElementWithName wn = (AstElementWithName) e;
				r = wn + "_" + r;
			}
			e = e.getParent();
		}
		return r;
	}

	public ImVar getThisVar(TranslatedToImFunction f) {
		if (f instanceof OnDestroyDef) {
			// special case for onDestroy defs
			// TODO also special case for constructors needed?
			OnDestroyDef od = (OnDestroyDef) f;
			if (od.getParent() instanceof ModuleInstanciation) {
				ModuleInstanciation mi = (ModuleInstanciation) od.getParent();
				ClassDef c = mi.attrNearestClassDef();
				f = c.getOnDestroy();  
			}
		}
		if (thisVarMap.containsKey(f)) {
			return thisVarMap.get(f);
		}
		ImVar v = JassIm.ImVar(f, ImSimpleType("integer"), "this", false);
		thisVarMap.put(f, v);
		return v ;
	}

	public ImVar getThisVar(ImFunction f, ExprThis e) {
		return getThisVarForNode(f, e);
	}

	public ImVar getThisVar(ImFunction f, ExprSuper e) {
		return getThisVarForNode(f, e);
	}

	private ImVar getThisVarForNode(ImFunction f, AstElement node1) {
		AstElement node = node1;
		while (node != null ) {
			if (node instanceof TranslatedToImFunction && !(node instanceof ExprClosure)) {
				return getThisVar((TranslatedToImFunction) node);
			}
			node = node.getParent();
		}
		if (f.getParameters().isEmpty()) {
			throw new CompileError(node1.attrSource(), "Could not get 'this'.");
		}
		return f.getParameters().get(0);
	}


	public int getTupleIndex(TupleDef tupleDef, VarDef parameter) {
		int i = 0;
		for (WParameter p : tupleDef.getParameters()) {
			if (p == parameter) {
				return i;
			}
			i++;
		}
		throw new Error("");
	}



	public ImVar getVarFor(VarDef varDef) {
		ImVar v = varMap.get(varDef);
		if (v == null) {
			ImType type = varDef.attrTyp().imTranslateType();
			String name = varDef.getName();
			if (isNamedScopeVar(varDef)) {
				name = varDef.attrNearestNamedScope().getName() + "_" + name;
			}
			boolean isBj = isBJ(varDef.getSource());
			v = JassIm.ImVar(varDef, type, name, isBj);
			varMap.put(varDef, v);
		}
		return v;
	}

	private boolean isNamedScopeVar(VarDef varDef) {
		if (varDef.getParent() == null) {
			return false;
		}
		return varDef.getParent().getParent() instanceof NamedScope;
	}

	public WurstModel getWurstProg() {
		return wurstProg;
	}

	public ImProg imProg() {
		return imProg;
	}

	public boolean isTranslated(WPackage pack) {
		return translatedPackages.contains(pack);
	}

	public void setTranslated(WPackage pack) {
		translatedPackages.add(pack);
	}

	public boolean isTranslated(ClassDef c) {
		return translatedClasses.contains(c);
	}

	public void setTranslated(ClassDef c) {
		translatedClasses.add(c);
	}

	public List<ImStmt> translateStatements(ImFunction f, List<WStatement> statements) {
		List<ImStmt> result = Lists.newArrayList();
		for (WStatement s : statements) {
			lasttranslatedThing = s;
			ImStmt translated = s.imTranslateStmt(this, f);
			result.add(translated);
		}
		return result ;
	}

	public void setMainFunc(ImFunction f) {
		if (mainFunc != null) {
			throw new Error("mainFunction already set");
		}
		mainFunc = f;
	}

	public void setConfigFunc(ImFunction f) {
		if (configFunc != null) {
			throw new Error("configFunction already set");
		}
		configFunc = f;
	}

	public Multimap<ImFunction, ImFunction> getCalledFunctions() {
		if (callRelations == null) {
			calculateCallRelationsAndUsedVariables();
		}
		return callRelations;
	}

	public void calculateCallRelationsAndUsedVariables() {
		callRelations = HashMultimap.create();
		usedVariables = Sets.newLinkedHashSet();
		readVariables = Sets.newLinkedHashSet();
		usedFunctions = Sets.newLinkedHashSet();
		calculateCallRelations(getMainFunc());
		calculateCallRelations(getConfFunc());
		
//		WLogger.info("USED FUNCS:");
//		for (ImFunction f : usedFunctions) {
//			WLogger.info("	" + f.getName());
//		}
	}

	private void calculateCallRelations(ImFunction f) {
		if (getUsedFunctions().contains(f)) {
			return;
		}
		getUsedFunctions().add(f);

		getUsedVariables().addAll(f.calcUsedVariables());
		getReadVariables().addAll(f.calcReadVariables());
		
		Set<ImFunction> calledFuncs = f.calcUsedFunctions();
		for (ImFunction called : calledFuncs) {
			if (f != called) { // ignore reflexive call relations
				getCallRelations().put(f, called);
			}
			calculateCallRelations(called);
		}
		
	}

	private Multimap<ImFunction, ImFunction> getCallRelations() {
		return callRelations;
	}


	public ImFunction getMainFunc() {
		return mainFunc;
	}

	public ImFunction getConfFunc() {
		return configFunc;
	}



	/**
	 * returns a list of classes and functions implementing funcDef
	 */
	public Map<ClassDef, FuncDef> getClassesWithImplementation(Collection<ClassDef> instances, FuncDef func) {
		if (func.attrIsPrivate()) {
			// private functions cannot be overridden
			return Collections.emptyMap();
		}
		Map<ClassDef, FuncDef> result = Maps.newLinkedHashMap();
		for (ClassDef c : instances) {
			NameLink funcNameLink = null;
			for (NameLink nameLink : c.attrNameLinks().get(func.getName())) {
				if (nameLink.getNameDef() == func) {
					funcNameLink = nameLink;
				}
			}
			if (funcNameLink == null) {
				throw new Error("must not happen");
			}
			for (NameLink nameLink : c.attrNameLinks().get(func.getName())) {
				NameDef nameDef = nameLink.getNameDef();
				if (nameLink.getDefinedIn() == c) {
					if (nameDef instanceof FuncDef) {
						FuncDef f = (FuncDef) nameDef;
						// check if function f overrides func 
						if (WurstValidator.canOverride(nameLink, funcNameLink)) {
							result.put(c, f);
						}
					}
				}
			}
		}
		return result;
	}


	private Map<ClassDef, List<Pair<ImVar, OptExpr>>> classDynamicInitMap = Maps.newLinkedHashMap();
	private Map<ClassDef, List<WStatement>> classInitStatements = Maps.newLinkedHashMap();

	public List<Pair<ImVar, OptExpr>> getDynamicInits(ClassDef c) {
		List<Pair<ImVar, OptExpr>> r = classDynamicInitMap.get(c);
		if (r == null) {
			r = Lists.newArrayList();
			classDynamicInitMap.put(c, r);
		}
		return r;
	}

	public List<WStatement> getInitStatement(ClassDef c) {
		List<WStatement> r = classInitStatements.get(c);
		if (r == null) {
			r = Lists.newArrayList();
			classInitStatements.put(c, r);
		}
		return r;
	}

	Map<ConstructorDef, ImFunction> constructorFuncs = Maps.newLinkedHashMap();

	public ImFunction getConstructFunc(ConstructorDef constr) {
		ImFunction f = constructorFuncs.get(constr);
		if (f == null) {
			String name = "construct_" + constr.attrNearestClassDef().getName();
			ImVars params = ImVars(getThisVar(constr));
			for (WParameter p : constr.getParameters()) {
				params.add(getVarFor(p));
			}
			f = JassIm.ImFunction(constr, name, params, ImVoid(), ImVars(), ImStmts(), flags());
			addFunction(f);
			constructorFuncs.put(constr, f);
		}
		return f;
	}


	Map<ConstructorDef, ImFunction> constrNewFuncs = Maps.newLinkedHashMap();

	public ImFunction getConstructNewFunc(ConstructorDef constr) {
		ImFunction f = constrNewFuncs.get(constr);
		if (f == null) {
			String name = "new_" + constr.attrNearestClassDef().getName();
			f = JassIm.ImFunction(constr, name, ImVars(), TypesHelper.imInt(), ImVars(), ImStmts(), flags());
			addFunction(f);
			constrNewFuncs.put(constr, f);
		}
		return f;
	}

	public ImProg getImProg() {
		return imProg;
	}



	private Multimap<InterfaceDef, ClassDef> interfaceInstances = null;

	public Collection<ClassDef> getInterfaceInstances(InterfaceDef interfaceDef) {
		if (interfaceInstances == null) {
			calculateInterfaceInstances();
		}
		return interfaceInstances.get(interfaceDef);
	}

	private void calculateInterfaceInstances() {
		interfaceInstances = HashMultimap.create();
		for (CompilationUnit cu : wurstProg) {
			for (ClassDef c : cu.attrGetByType().classes) {
				for (WurstTypeInterface i : c.attrImplementedInterfaces()) {
					interfaceInstances.put(i.getInterfaceDef(), c);
				}
			}
		}
	}


	private Multimap<ClassDef, ClassDef> subclasses = null;
	private Multimap<ClassDef, ClassDef> directSubclasses = null;

	private List<ImFunction> compiletimeFuncs = Lists.newArrayList();

	public DebugLevel debugLevel = DebugLevel.DEFAULT;

	private boolean isEclipseMode = false;

	public Collection<ClassDef> getSubClasses(ClassDef classDef) {
		calculateSubclasses();
		return subclasses.get(classDef);
	}

	private void calculateSubclasses() {
		if (subclasses != null) {
			return;
		}
		calculateDirectSubclasses();
		subclasses = Utils.transientClosure(directSubclasses);
	}

	public Collection<ClassDef> getDirectSubClasses(ClassDef classDef) {
		calculateDirectSubclasses();
		return directSubclasses.get(classDef);
	}

	private void calculateDirectSubclasses() {
		if (directSubclasses != null) {
			return;
		}
		directSubclasses = HashMultimap.create();
		for (CompilationUnit cu : wurstProg) {
			for (ClassDef c : cu.attrGetByType().classes) {
				if (c.attrExtendedClass() != null) {
					directSubclasses.put(c.attrExtendedClass(), c);
				}
			}
		}
	}

	public void addCompiletimeFunc(ImFunction f) {
		compiletimeFuncs.add(f);
	}

	/**
	 * returns all classes which are subtypes or equal to the given type 
	 */
	public Collection<ClassDef> getConcreteSubtypes(WurstType t) {
		if (t instanceof WurstTypeInterface) {
			WurstTypeInterface ti = (WurstTypeInterface) t;
			return getInterfaceInstances(ti.getInterfaceDef());
		}
		if (t instanceof WurstTypeClass) {
			WurstTypeClass tc = (WurstTypeClass) t;
			ArrayList<ClassDef> result = Lists.newArrayList(getSubClasses(tc.getClassDef()));
			result.add(tc.getClassDef());
			return result;
		}
		throw new Error("not implemented");
	}



	public int getEnumMemberId(EnumMember enumMember) {
		return ((EnumMembers) enumMember.getParent()).indexOf(enumMember);
	}

	public ImFunction getDebugPrintFunction() {
		return debugPrintFunction;
	}

	public boolean isEclipseMode() {
		return isEclipseMode;
	}

	public void setEclipseMode(boolean enabled) {
		isEclipseMode = enabled;
	}

	public ImmutableTree<ImVar> getVarsForTuple(ImVar v) {

		ImmutableTree<ImVar> result = varsForTupleVar.get(v);
		if (result == null) {
			if (v.getType() instanceof ImArrayType || v.getType() instanceof ImSimpleType) {
				result = ImmutableTree.leaf(v);
			} else {
				result = createVarsForType(v.getName(), v.getType(), false, v.getTrace());
			}
			
//			if (v.getType() instanceof ImArrayType || v.getType() instanceof ImSimpleType) {
//				result = ImmutableTree.leaf(v);
//			} else {
//				result = Lists.newArrayList();
//				addVarsForType(result, v.getName(), v.getType(), false, v.getTrace());
//			}
			varsForTupleVar.put(v, result);
		}
		return result;
	}

	private ImmutableTree<ImVar> createVarsForType(String name, ImType type, boolean array, AstElement tr) {
		if (type instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) type;
			int i=0;
			Builder<ImmutableTree<ImVar>> ts = ImmutableList.builder();			
			for (ImType t : tt.getTypes()) {
				ts.add(createVarsForType(name+ "_" + tt.getNames().get(i) , t, array, tr));
				i++;
			}
			return ImmutableTree.node(ts.build());
		} else if (type instanceof ImTupleArrayType) {
			ImTupleArrayType tt = (ImTupleArrayType) type;
			Builder<ImmutableTree<ImVar>> ts = ImmutableList.builder();	
			for (ImType t : tt.getTypes()) {
				ts.add(createVarsForType(name, t, true, tr));
			}
			return ImmutableTree.node(ts.build());
		} else if (type instanceof ImVoid) {
			return ImmutableTree.empty();
		} else {
			if (array && type instanceof ImSimpleType){
				ImSimpleType st = (ImSimpleType) type;
				type = JassIm.ImArrayType(st.getTypename());
			}
			return ImmutableTree.leaf(JassIm.ImVar(tr, type, name, false));
		}
	}


	private void addVarsForType(List<ImVar> result, String name, ImType type, boolean array, AstElement tr) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(result);
		// TODO handle names
		if (type instanceof ImTupleType) {
			ImTupleType tt = (ImTupleType) type;
			int i=0;
			for (ImType t : tt.getTypes()) {
				addVarsForType(result, name + "_" + tt.getNames().get(i), t, false, tr);
				i++;
			}
		} else if (type instanceof ImTupleArrayType) {
			ImTupleArrayType tt = (ImTupleArrayType) type;
			for (ImType t : tt.getTypes()) {
				addVarsForType(result, name, t, true, tr);
			}
		} else if (type instanceof ImVoid) {

		} else {
			if (array && type instanceof ImSimpleType){
				ImSimpleType st = (ImSimpleType) type;
				type = JassIm.ImArrayType(st.getTypename());
			}
			result.add(JassIm.ImVar(tr, type, name, false));
		}

	}

	private Map<ImFunction, List<ImVar>> tempReturnVars = Maps.newLinkedHashMap();
	public List<ImVar> getTupleTempReturnVarsFor(ImFunction f) {
		List<ImVar> result = tempReturnVars.get(f);
		if (result == null) {
			result = Lists.newArrayList();
			addVarsForType(result, f.getName() +  "_return", getOriginalReturnValue(f), false, f.getTrace());
			if (result.size() > 1) {
				imProg.getGlobals().addAll(result);
				// if we only have one return var it will never get used
			}
			tempReturnVars.put(f, result);
		}
		return result ;
	}

	private Map<ImFunction, ImType> originalReturnValues = Maps.newLinkedHashMap();


	public void setOriginalReturnValue(ImFunction f, ImType t) {
		originalReturnValues.put(f, t);
	}

	public ImType getOriginalReturnValue(ImFunction f) {
		ImType result = originalReturnValues.get(f);
		if (result == null) {
			result = f.getReturnType();
			originalReturnValues.put(f, result);
		}
		return result;
	}

	public void assertProperties(AssertProperty ... properties1) {
		final Set<AssertProperty> properties = Sets.newEnumSet(Lists.newArrayList(properties1), AssertProperty.class);
		assertProperties(properties, imProg);
	}

	private void assertProperties(Set<AssertProperty> properties, JassImElement e) {
		if (e instanceof JassImElementWithLeft) {
			checkVar(((JassImElementWithLeft) e).getLeft(), properties);
		}
		if (e instanceof JassImElementWithVar) {
			checkVar(((JassImElementWithVar) e).getVar(), properties);
		}
		if (properties.contains(AssertProperty.NOTUPLES)) {
			if (e instanceof ImTupleExpr
					|| e instanceof ImTupleSelection
					) {
				throw new Error("contains tuple expr " + e);
			}
			if (e instanceof ImVar) {
				ImVar v = (ImVar) e;
				if (v.getType() instanceof ImTupleType
						|| v.getType() instanceof ImTupleArrayType) {
					throw new Error("contains tuple var: " + v + " in\n" + v.getParent().getParent());
				}
			}
		}
		if (properties.contains(AssertProperty.FLAT)) {
			if (e instanceof ImStatementExpr) {
				throw new Error("contains statementExpr " + e);
			}
		}
		for (int i=0; i<e.size(); i++) {
			assertProperties(properties, e.get(i));
		}
	}
	
	private void checkVar(ImVar left, Set<AssertProperty> properties) {
		if (left.getParent() == null) {
			throw new Error("var not attached: " + left);
		}			
		if (properties.contains(AssertProperty.NOTUPLES)) {
			if (left.getType() instanceof ImTupleType
					|| left.getType() instanceof ImTupleArrayType) {
				throw new Error("program contains tuple var " + left);
			}
		}
	}

	public Set<ImVar> getUsedVariables() {
		if (usedVariables == null) {
			calculateCallRelationsAndUsedVariables();
		}
		return usedVariables;
	}
	
	public Set<ImVar> getReadVariables() {
		if (readVariables == null) {
			calculateCallRelationsAndUsedVariables();
		}
		return readVariables;
	}

	public Set<ImFunction> getUsedFunctions() {
		if (usedFunctions == null) {
			calculateCallRelationsAndUsedVariables();
		}
		return usedFunctions;
	}

	public boolean isUnitTestMode() {
		return isUnitTestMode;
	}


	Map<StructureDef, ImClass> classForStructureDef = Maps.newLinkedHashMap();
	public ImClass getClassFor(StructureDef s) {
		ImClass c = classForStructureDef.get(s);
		if (c == null) {
			c = JassIm.ImClass(s, s.getName(), JassIm.ImVars(), JassIm.ImMethods(), Lists.<ImClass>newArrayList());
			classForStructureDef.put(s, c);
		}
		return c;
	}


	Map<FuncDef, ImMethod> methodForFuncDef = Maps.newLinkedHashMap();
	public ImMethod getMethodFor(FuncDef f) {
		ImMethod m = methodForFuncDef.get(f);
		if (m == null) {
			ImFunction imFunc = getFuncFor(f);
			m = JassIm.ImMethod(f, f.getName(), imFunc, Lists.<ImMethod>newArrayList(), false);
			methodForFuncDef.put(f, m);
		}
		return m;
	}
	
	public ClassManagementVars getClassManagementVarsFor(ImClass c) {
		return getClassManagementVars().get(c);
	}

	
	private Map<ImClass, ClassManagementVars> classManagementVars = null;
	
	public Map<ImClass, ClassManagementVars> getClassManagementVars() {
		if (classManagementVars != null) {
			return classManagementVars;
		}
		// create partitions, such that each sub-class and super-class are in 
		// the same partition
		Partitions<ImClass> p = new Partitions<>();
		for (ImClass c : imProg.getClasses()) {
			p.add(c);
			for (ImClass sc : c.getSuperClasses()) {
				p.union(c, sc);
			}
		}
		// generate typeId variables
		classManagementVars = Maps.newLinkedHashMap();
		for (ImClass c : imProg.getClasses()) {
			ImClass rep = p.getRep(c);
			ClassManagementVars v = classManagementVars.get(rep);
			if (v == null) {
				v = new ClassManagementVars(rep, this);
				classManagementVars.put(rep, v);
			}
			classManagementVars.put(c, v);
		}
		return classManagementVars;
	}


	public ImFunction getGlobalInitFunc() {
		return globalInitFunc;
	}
	
	
}
