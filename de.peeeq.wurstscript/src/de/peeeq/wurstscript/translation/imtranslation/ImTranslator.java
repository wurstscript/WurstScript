package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.TypesHelper;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class ImTranslator {

	
	public static final String $DEBUG_PRINT = "$debugPrint";

	private Multimap<ImFunction, ImFunction> callRelations = HashMultimap.create();
	
	private ImFunction debugPrintFunction;
	
	Map<ClassDef, ImFunction> destroyFuncMap = Maps.newHashMap();
	
	private List<ImFunction> entryPoints = Lists.newArrayList();
	
	private Map<TranslatedToImFunction, ImFunction> functionMap = Maps.newHashMap();
	

	private ImFunction globalInitFunc;
	
	private ImProg imProg;
	
	Map<WPackage, ImFunction> initFuncMap = Maps.newHashMap();

	private Map<TranslatedToImFunction, ImVar> thisVarMap = Maps.newHashMap();

	private Set<WPackage> translatedPackages = Sets.newHashSet();

	private int typeIdCounter = 0;
	
	Map<ClassDef, Integer> typeIdMap = Maps.newHashMap();

	private Map<VarDef, ImVar> varMap = Maps.newHashMap();
	
	private CompilationUnit wurstProg;

	private ImFunction mainFunc = null;

	private ImFunction configFunc = null;
	
	// trace from im elements to ast elements
	private Map<JassImElement, AstElement> trace = Maps.newHashMap();
	
	public ImTranslator(CompilationUnit wurstProg) {
		this.wurstProg = wurstProg;
	}
	
	/**
	 * translates a program 
	 */
	public ImProg translateProg() {
		imProg = ImProg(ImVars(), ImFunctions());
		addSource(imProg, wurstProg);
		
		globalInitFunc = ImFunction("initGlobals", ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
		debugPrintFunction = ImFunction($DEBUG_PRINT, ImVars(ImVar(PScriptTypeString.instance().imTranslateType(), "msg", false)), ImVoid(), ImVars(), ImStmts(), true, false);
		
		
		
		for (TopLevelDeclaration tld : wurstProg) {
			tld.imTranslateTLD(this);
		}
		
		if (mainFunc == null) {
			System.out.println("generating default main function");
			mainFunc = ImFunction("main", ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
			addFunction(mainFunc);
		}
		if (configFunc == null) {
			configFunc = ImFunction("config", ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
			addFunction(configFunc);
		}
		finishInitFunctions();
		
		return imProg;
	}
	
	
	public void addSource(JassImElement elem, AstElement source) {
		trace.put(elem, source);
	}
	
	
	/**
	 *	in the case of type parameters it may be the case that an int is passed
	 * to a function which takes two ints. The missing int is added here 
	 */
	public void adjustImArgs(ImExprs imArgs, ImFunction calledImFunc) {
		if (calledImFunc.getIsNative()) {
			return;
		}
		if (imArgs.size() != calledImFunc.getParameters().size()) {
			throw new Error();
		}
		for (int i = 0; i<imArgs.size(); i++) {
			ImType expectedType = calledImFunc.getParameters().get(i).getType();
			ImExpr actualExpr = imArgs.get(i);
			if (!(expectedType instanceof ImTupleType)) {
				continue;
			}
			ImTupleType tt = (ImTupleType) expectedType;
			if (tt.getTypes().size() != 2) {
				continue;
			}
			String t1 = tt.getTypes().get(0);
			String t2 = tt.getTypes().get(1);
			if (!t1.equals(TypesHelper.imInt().getTypename()) || !t2.equals(TypesHelper.imInt().getTypename())) {
				continue;
			}
			if (actualExpr.attrTyp() instanceof ImSimpleType) {
				imArgs.set(i, ImTupleExpr(ImExprs((ImExpr) actualExpr.copy(), ImIntVal(0))));
			}
		}
	}

	private void finishInitFunctions() {
		
		for (ImFunction initFunc : initFuncMap.values()) {
			addFunction(initFunc);
		}
		Set<WPackage> calledInitializers = Sets.newHashSet();
		for (WPackage p : initFuncMap.keySet()) {
			callInitFunc(calledInitializers, p);
		}
	}

	private void callInitFunc(Set<WPackage> calledInitializers, WPackage p) {
		if (calledInitializers.contains(p)) {
			return;
		}
		calledInitializers.add(p);
		for (WImport i : p.getImports()) {
			callInitFunc(calledInitializers, i.attrImportedPackage());
		}
		ImFunction initFunc = initFuncMap.get(p);
		if (initFunc == null) {
			return;
		}
		mainFunc.getBody().add(ImFunctionCall(initFunc, ImExprs()));
		addCallRelation(mainFunc, initFunc);
	}

	public void addCallRelation(ImFunction callingFunc, ImFunction calledFunc) {
		callRelations.put(callingFunc, calledFunc);
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
			f.getBody().add(ImSet(v, expr.imTranslateExpr(this, f)));
		}
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
			return getDefaultValueForJassTypeName(imTupleType.getTypes().get(0));
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

	public ImFunction getDestroyFuncFor(ClassDef classDef) {
		ImFunction f = destroyFuncMap.get(classDef); 
		if (f == null) {
			f = JassIm.ImFunction("destroy" + classDef.getName(), ImVars(), TypesHelper.imVoid(), ImVars(), ImStmts(), false, false);
			destroyFuncMap.put(classDef, f);
			addSource(f, classDef.getOnDestroy().getSource());
			addFunction(f);
		}
		return f ;
	}


	public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
		if (functionMap.containsKey(funcDef)) {
			return functionMap.get(funcDef);
		}
		String name = getNameFor(funcDef);;
		boolean isNative = funcDef instanceof NativeFunc;
		boolean isBJ = isBJ(funcDef.getSource());
		ImFunction f = JassIm.ImFunction(name, ImVars(), ImVoid(), ImVars(), ImStmts(), isNative, isBJ);
		funcDef.imCreateFuncSkeleton(this, f);
		addFunction(f);
		functionMap.put(funcDef, f);
		addSource(f, funcDef.getSource());
		return f;
	}
	private boolean isBJ(WPos source) {
		String f = source.getFile().toLowerCase();
		return f.endsWith("blizzard.j") || f.endsWith("common.j");
	}

	public ImFunction getInitFuncFor(WPackage p) {
		ImFunction f = initFuncMap.get(p); 
		if (f == null) {
			f = JassIm.ImFunction("init_" + p.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
			initFuncMap.put(p, f);
			addSource(f, p.getSource()); // XXX more precise source
		}
		return f ;
	}

	/**
	 * returns a suitable name for the given element
	 * the returned name is a valid jass identifier 
	 */
	public String getNameFor(AstElement e) {
		if (e instanceof AstElementWithName) {
			AstElementWithName wn = (AstElementWithName) e;
			return wn.getName();
		} else if (e instanceof ConstructorDef) {
			return "new_" + e.attrNearestClassDef().getName();
		}
		String r = e.getClass().getSimpleName();
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
		ImVar v = ImVar(ImSimpleType("integer"), "this", false);
		thisVarMap.put(f, v);
		return v ;
	}
	
	public ImVar getThisVar(ExprThis e) {
		AstElement node = e;
		while (!(node instanceof TranslatedToImFunction)) {
			node = node.getParent();
		}
		return getThisVar((TranslatedToImFunction) node);
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

	public int getTypeId(ClassDef c) {
		Integer r = typeIdMap.get(c); 
		if (r == null) {   
			typeIdCounter++;
			typeIdMap.put(c, typeIdCounter);
			return typeIdCounter;
		} else {
			return r;
		}
	}

	public ImVar getVarFor(VarDef varDef) {
		ImVar v = varMap.get(varDef);
		if (v == null) {
			ImType type = varDef.attrTyp().imTranslateType();
			String name = varDef.getName();
			boolean isBj = isBJ(varDef.getSource());
			v = JassIm.ImVar(type, name, isBj);
			varMap.put(varDef, v);
			addSource(v, varDef.getSource());
		}
		return v;
	}

	public CompilationUnit getWurstProg() {
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

	

	public List<ImStmt> translateStatements(ImFunction f, List<WStatement> statements) {
		List<ImStmt> result = Lists.newArrayList();
		for (WStatement s : statements) {
			result.add(s.imTranslateStmt(this, f));
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
		if (configFunc == null) {
			throw new Error("configFunction already set");
		}
		configFunc = f;
	}

	public Multimap<ImFunction, ImFunction> getCalledFunctions() {
		return callRelations;
	}

	public ImFunction getMainFunc() {
		return mainFunc;
	}

	public ImFunction getConfFunc() {
		return configFunc;
	}

	public Map<JassImElement, AstElement> getTrace() {
		return trace;
	}


	

	

	

	
		


}
