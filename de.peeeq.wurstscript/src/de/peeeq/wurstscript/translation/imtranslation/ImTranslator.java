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
import static de.peeeq.wurstscript.jassIm.JassIm.ImTupleExpr;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVar;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVars;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVoid;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlag.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprThis;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TranslatedToImFunction;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.types.PScriptTypeString;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

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
	private Set<ClassDef>  translatedClasses = Sets.newHashSet();

	private int typeIdCounter = 0;
	
	Map<ClassDef, Integer> typeIdMap = Maps.newHashMap();
	Map<ClassDef, Integer> typeIdMapMax = Maps.newHashMap();

	private Map<VarDef, ImVar> varMap = Maps.newHashMap();
	
	private WurstModel wurstProg;

	private ImFunction mainFunc = null;

	private ImFunction configFunc = null;
	
	// trace from im elements to ast elements
	private Map<JassImElement, AstElement> trace = Maps.newHashMap();
	
	public ImTranslator(WurstModel wurstProg) {
		this.wurstProg = wurstProg;
	}
	
	/**
	 * translates a program 
	 */
	public ImProg translateProg() {
		imProg = ImProg(ImVars(), ImFunctions());
		addSource(imProg, wurstProg);
		
		globalInitFunc = ImFunction("initGlobals", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
		addFunction(globalInitFunc);
		debugPrintFunction = ImFunction($DEBUG_PRINT, ImVars(ImVar(PScriptTypeString.instance().imTranslateType(), "msg", false)), ImVoid(), ImVars(), ImStmts(), flags(IS_NATIVE));
		
		
	
		for (CompilationUnit cu : wurstProg) {
			translateCompilationUnit(cu);
		}
		
		if (mainFunc == null) {
			mainFunc = ImFunction("main", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
			addFunction(mainFunc);
		}
		if (configFunc == null) {
			configFunc = ImFunction("config", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
			addFunction(configFunc);
		}
		finishInitFunctions();
		
		return imProg;
	}

	private ArrayList<FunctionFlag> flags(FunctionFlag ... flags) {
		return Lists.newArrayList(flags);
	}
	
	
	private void translateCompilationUnit(CompilationUnit cu) {
		for (WPackage p : cu.getPackages()) {
			p.imTranslateTLD(this);
		}
		for (JassToplevelDeclaration tld : cu.getJassDecls()) {
			tld.imTranslateTLD(this);
		}
	}

	public void addSource(JassImElement elem, AstElement source) {
		trace.put(elem, source);
	}
	
	
	/**
	 *	in the case of type parameters it may be the case that an int is passed
	 * to a function which takes two ints. The missing int is added here 
	 */
	public void adjustImArgs(ImExprs imArgs, ImFunction calledImFunc) {
		if (calledImFunc.isNative()) {
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
		// init globals, at beginning of main func:
		mainFunc.getBody().add(0, ImFunctionCall(globalInitFunc, ImExprs()));
		addCallRelation(mainFunc, globalInitFunc);
		
		
		for (ImFunction initFunc : initFuncMap.values()) {
			addFunction(initFunc);
		}
		Set<WPackage> calledInitializers = Sets.newHashSet();
		for (WPackage p : Utils.sortByName(initFuncMap.keySet())) {
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
			f = JassIm.ImFunction("destroy" + classDef.getName(), ImVars(), TypesHelper.imVoid(), ImVars(), ImStmts(), flags());
			destroyFuncMap.put(classDef, f);
			addSource(f, classDef.getOnDestroy());
			addFunction(f);
		}
		return f ;
	}


	public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
		if (functionMap.containsKey(funcDef)) {
			return functionMap.get(funcDef);
		}
		String name = getNameFor(funcDef);;
		List<FunctionFlag> flags = flags();
		if (funcDef instanceof NativeFunc) {
			flags.add(IS_NATIVE);
		}
		if (isBJ(funcDef.getSource())) {
			flags.add(IS_BJ);
		}
		if (funcDef instanceof FuncDef) {
			FuncDef funcDef2 = (FuncDef) funcDef;
			if (funcDef2.attrIsCompiletime()) {
				flags.add(IS_COMPILETIME);
			}
		}
		ImFunction f = JassIm.ImFunction(name, ImVars(), ImVoid(), ImVars(), ImStmts(), flags);
		funcDef.imCreateFuncSkeleton(this, f);
		addFunction(f);
		functionMap.put(funcDef, f);
		addSource(f, funcDef);
		return f;
	}
	private boolean isBJ(WPos source) {
		String f = source.getFile().toLowerCase();
		return f.endsWith("blizzard.j") || f.endsWith("common.j");
	}

	public ImFunction getInitFuncFor(WPackage p) {
		ImFunction f = initFuncMap.get(p); 
		if (f == null) {
			f = JassIm.ImFunction("init_" + p.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
			initFuncMap.put(p, f);
			addSource(f, p); // XXX more precise source
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
		}
		
			
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
			makeTypeIdsForHiearchy(c);
			r = typeIdMap.get(c); 
		}
//			typeIdCounter++;
//			int typeId = typeIdCounter;
//			typeIdMap.put(c, typeId);
//			
//			// also assign types to subclasses:
//			
//			return typeId;
			return r;
	}

	private void makeTypeIdsForHiearchy(ClassDef c) {
		if (c.getExtendedClass() instanceof TypeExpr) {
			TypeExpr sc = (TypeExpr) c.getExtendedClass();
			if (sc.attrTyp() instanceof PscriptTypeClass) {
				PscriptTypeClass ct = (PscriptTypeClass) sc.attrTyp();
				makeTypeIdsForHiearchy(ct.getClassDef());
				return;
			}
		}
		makeTypeIdsForClass(c);
	}

	private void makeTypeIdsForClass(ClassDef c) {
		typeIdCounter++;
		typeIdMap.put(c, typeIdCounter);
		for (ClassDef sc : getDirectSubClasses(c)) {
			makeTypeIdsForClass(sc);
		}
		typeIdMapMax.put(c, typeIdCounter);
	}

	public ImVar getVarFor(VarDef varDef) {
		ImVar v = varMap.get(varDef);
		if (v == null) {
			ImType type = varDef.attrTyp().imTranslateType();
			String name = varDef.getName();
			boolean isBj = isBJ(varDef.getSource());
			v = JassIm.ImVar(type, name, isBj);
			varMap.put(varDef, v);
			addSource(v, varDef);
		}
		return v;
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
		if (configFunc != null) {
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

	private Partitions<StructureDef> classPartitions = new Partitions<StructureDef>();
	private Map<StructureDef, ClassManagementVars> classManagementVars = Maps.newHashMap();
	
	public ClassManagementVars getClassManagementVarsFor(StructureDef classDef) {
		buildClassParition(classDef);
		
		
		// class representing this partition
		StructureDef repClass = classPartitions.getRep(classDef);
		
		ClassManagementVars m = classManagementVars.get(repClass);
		if (m == null) {
			m = new ClassManagementVars(repClass, this);
			classManagementVars.put(repClass, m);
		}
		return m;
	}

	

	private void buildClassParition(StructureDef s) {
		if (!classPartitions.contains(s)) {
			classPartitions.add(s);
			if (s instanceof ClassDef) {
				ClassDef c = (ClassDef) s;
				
				if (c.attrExtendedClass() != null) {
					// union with extended class
					buildClassParition(c.attrExtendedClass());
					classPartitions.union(c, c.attrExtendedClass());
				}
				for (PscriptTypeInterface i : c.attrImplementedInterfaces()) {
					// union with implemented interfaces
					buildClassParition(i.getInterfaceDef());
					classPartitions.union(c, i.getInterfaceDef());
				}
			} else if (s instanceof InterfaceDef)  {
				InterfaceDef i = (InterfaceDef) s;
				for (PscriptTypeInterface t : i.attrExtendedInterfaces()) {
					// union with extended interfaces
					buildClassParition(t.getInterfaceDef());
					classPartitions.union(i, t.getInterfaceDef());
				}
				
			} else {
				throw new Error("invalid type: " + s.getClass());
			}
		}
	}

	/**
	 * returns a list of classes and functions implementing funcDef
	 */
	public Map<ClassDef, FuncDef> getClassedWithImplementation(Collection<ClassDef> instances, FuncDef func) {
		if (func.attrIsPrivate()) {
			// private functions cannot be overridden
			return Collections.emptyMap();
		}
		Map<ClassDef, FuncDef> result = Maps.newHashMap();
		for (ClassDef c : instances) {
			for (NameDef nameDef : c.attrVisibleNamesPrivate().get(func.getName())) {
				if (nameDef instanceof FuncDef) {
					FuncDef f = (FuncDef) nameDef;
					result.put(c, f);
				}
			}
		}
		return result;
	}
	
	public List<ImStmt> createDispatch(Map<ClassDef, FuncDef> instances, FuncDef funcDef, ImFunction f, int maxTypeId, TypeIdGetter typeId) {
		List<Pair<IntRange, FuncDef>> instances2 = transformInstances(instances);
		IntRange knownRange;
		if (instances2.size() == 0) {
			// does not matter
			knownRange = new IntRange(0, 0);
		} else {
			knownRange = new IntRange(0, maxTypeId);
		}
		return createDispatchHelper(instances2, 0, instances2.size()-1, funcDef, f, typeId, knownRange);
	}
		
	/**
	 * returns a mapping from classdefs to functions into a mapping 
	 * from typeid ranges to functions 	
	 * 
	 * the resulting list is sorted by the intrange and the intervals are disjunct
	 */
	private List<Pair<IntRange, FuncDef>> transformInstances(Map<ClassDef, FuncDef> instances) {
		Vector<FuncDef> funcs = new Vector<>();
		List<ClassDef> classes = Lists.newArrayList(instances.keySet());
		Collections.sort(classes, new TypeIdComparator(this));
		for (ClassDef c : classes) {
			getTypeId(c);
		}
		funcs.setSize(typeIdCounter+2);
		for (ClassDef c : classes) {
			FuncDef f = instances.get(c);
			funcs.set(getTypeId(c), f);
			for (ClassDef sc : getSubClasses(c)) {
				funcs.set(getTypeId(sc), f);
			}
		}
		
		List<Pair<IntRange, FuncDef>> result = Lists.newArrayList();
		
		for (int i=0; i<=typeIdCounter; i++) {
			FuncDef f = funcs.get(i);
			if (f == null) {
				continue;
			}
			int j = i;
			while (funcs.get(j) == f) {
				j++;
			}
			result.add(Pair.create(new IntRange(i, j-1), f));
			i = j-1;
		}
		return result;
	}

	private List<ImStmt> createDispatchHelper(List<Pair<IntRange, FuncDef>> instances2, int start, int end
				, FuncDef funcDef, ImFunction f, TypeIdGetter typeId, IntRange knownRange) {
		
		
		List<ImStmt> result = Lists.newArrayList();
		ImVar thisVar = f.getParameters().get(0);
		boolean returnsVoid = funcDef.attrTyp() instanceof PScriptTypeVoid;
		if (start > end) {
			// there seem to be no instances
			assert instances2.size() == 0;
			// just create an dummy return
			if (returnsVoid) {
				// empty function
			} else {
				ImType type = f.getReturnType();
				ImExpr def = getDefaultValueForJassType(type);
				result.add(JassIm.ImReturn(def));
			}
			return result;
		} else if (start == end) {
			FuncDef calledFunc = instances2.get(start).getB();
			ImFunction calledJassFunc = getFuncFor(calledFunc);
			addCallRelation(f, calledJassFunc);
			ImExprs arguments = JassIm.ImExprs();
			for (int i=0; i<f.getParameters().size(); i++) {
				ImVar p = f.getParameters().get(i);
				arguments.add(JassIm.ImVarAccess(p));
			}
			ImCall call = JassIm.ImFunctionCall(calledJassFunc, arguments);
			if (returnsVoid) {
				result.add(call);
				result.add(JassIm.ImReturn(JassIm.ImNoExpr()));
			} else {
				result.add(JassIm.ImReturn(call));
			}
			// check for equality
			IntRange range = instances2.get(start).getA();
			List<ImExpr> conditions = Lists.newArrayList();
			if (knownRange.start < knownRange.end) {
				if (range.start == range.end) {
					conditions.add(JassIm.ImOperatorCall(Ast.OpEquals(), 
							JassIm.ImExprs(
									typeId.get(thisVar),
									JassIm.ImIntVal(range.start))));
				} else {
					// start condition
					if (range.start > knownRange.start) {
						conditions.add(JassIm.ImOperatorCall(Ast.OpGreaterEq(), 
								JassIm.ImExprs(
										typeId.get(thisVar),
										JassIm.ImIntVal(range.start))));
					}
					// end condition
					if (range.end < knownRange.end) {
						conditions.add(JassIm.ImOperatorCall(Ast.OpLessEq(),
								JassIm.ImExprs(
										typeId.get(thisVar),
										JassIm.ImIntVal(range.end))));
					}
				}
			}
			if (conditions.size() == 0) {
				return result;
			} else if (conditions.size() == 1) {
				return Collections.<ImStmt>singletonList(
						JassIm.ImIf(conditions.get(0), ImStmts(result), ImStmts())
					);
			} else {
				return Collections.<ImStmt>singletonList(
						JassIm.ImIf(JassIm.ImOperatorCall(Ast.OpAnd(), 
								ImExprs(conditions.get(0), conditions.get(1))), 
								ImStmts(result), ImStmts())
					);
			}
			
			
		} else {
			int splitAt = start + (end-start) / 2;
			
			
			int typeIdSplitPoint = instances2.get(splitAt).getA().end;
			
			List<ImStmt> case1 = createDispatchHelper(instances2, start, splitAt, funcDef, f, typeId, new IntRange(knownRange.start, typeIdSplitPoint));
			List<ImStmt> case2 = createDispatchHelper(instances2, splitAt+1, end, funcDef, f, typeId, new IntRange(typeIdSplitPoint+1, knownRange.end));

			// if (thistype <= typeIdSplitPoint)
			ImExpr cond = JassIm.ImOperatorCall(Ast.OpLessEq(), 
							JassIm.ImExprs(
									typeId.get(thisVar),
									JassIm.ImIntVal(typeIdSplitPoint)));
			ImStmts thenBlock = JassIm.ImStmts(case1);
			ImStmts elseBlock = JassIm.ImStmts(case2);
			result.add(JassIm.ImIf(cond, thenBlock, elseBlock));
			return result;
		}
	}

	private Map<ClassDef, List<Pair<ImVar, OptExpr>>> classDynamicInitMap = Maps.newHashMap();
	private Map<ClassDef, List<WStatement>> classInitStatements = Maps.newHashMap();
	
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

	Map<ConstructorDef, ImFunction> constructorFuncs = Maps.newHashMap();
	
	public ImFunction getConstructFunc(ConstructorDef constr) {
		ImFunction f = constructorFuncs.get(constr);
		if (f == null) {
			String name = "construct_" + constr.attrNearestClassDef().getName();
			ImVars params = ImVars(getThisVar(constr));
			for (WParameter p : constr.getParameters()) {
				params.add(getVarFor(p));
			}
			f = JassIm.ImFunction(name, params, ImVoid(), ImVars(), ImStmts(), flags());
			addFunction(f);
			addSource(f, constr);
			constructorFuncs.put(constr, f);
		}
		return f;
	}

	
	Map<ConstructorDef, ImFunction> constrNewFuncs = Maps.newHashMap();
	
	public ImFunction getConstructNewFunc(ConstructorDef constr) {
		ImFunction f = constrNewFuncs.get(constr);
		if (f == null) {
			String name = "new_" + constr.attrNearestClassDef().getName();
			f = JassIm.ImFunction(name, ImVars(), TypesHelper.imInt(), ImVars(), ImStmts(), flags());
			addFunction(f);
			addSource(f, constr);
			constrNewFuncs.put(constr, f);
		}
		return f;
	}

	public ImProg getImProg() {
		return imProg;
	}

	public void removeCallRelation(ImFunction f, ImFunction called) {
		callRelations.remove(f, called);
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
				for (PscriptTypeInterface i : c.attrImplementedInterfaces()) {
					interfaceInstances.put(i.getInterfaceDef(), c);
				}
			}
		}
	}

	
	private Multimap<ClassDef, ClassDef> subclasses = null;
	private Multimap<ClassDef, ClassDef> directSubclasses = null;

	private List<ImFunction> compiletimeFuncs = Lists.newArrayList();
	
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
	public Collection<ClassDef> getConcreteSubtypes(PscriptType t) {
		if (t instanceof PscriptTypeInterface) {
			PscriptTypeInterface ti = (PscriptTypeInterface) t;
			return getInterfaceInstances(ti.getInterfaceDef());
		}
		if (t instanceof PscriptTypeClass) {
			PscriptTypeClass tc = (PscriptTypeClass) t;
			ArrayList<ClassDef> result = Lists.newArrayList(getSubClasses(tc.getClassDef()));
			result.add(tc.getClassDef());
			return result;
		}
		throw new Error("not implemented");
	}

	public int getMaxTypeId(List<ClassDef> cs) {
		int max = 0;
		for (ClassDef c : cs) {
			max = Math.max(max, getTypeId(c));
		}
		return max;
	}

}
