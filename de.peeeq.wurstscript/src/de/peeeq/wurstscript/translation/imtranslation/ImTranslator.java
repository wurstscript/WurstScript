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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
		
		globalInitFunc = ImFunction("initGlobals", ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
		addFunction(globalInitFunc);
		debugPrintFunction = ImFunction($DEBUG_PRINT, ImVars(ImVar(PScriptTypeString.instance().imTranslateType(), "msg", false)), ImVoid(), ImVars(), ImStmts(), true, false);
		
		
	
		for (CompilationUnit cu : wurstProg) {
			translateCompilationUnit(cu);
		}
		
		if (mainFunc == null) {
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
			f = JassIm.ImFunction("destroy" + classDef.getName(), ImVars(), TypesHelper.imVoid(), ImVars(), ImStmts(), false, false);
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
		boolean isNative = funcDef instanceof NativeFunc;
		boolean isBJ = isBJ(funcDef.getSource());
		ImFunction f = JassIm.ImFunction(name, ImVars(), ImVoid(), ImVars(), ImStmts(), isNative, isBJ);
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
			f = JassIm.ImFunction("init_" + p.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), false, false);
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
	public List<Pair<ClassDef, FuncDef>> getClassedWithImplementation(Collection<ClassDef> instances, FuncDef func) {
		if (func.attrIsPrivate()) {
			// private functions cannot be overridden
			return Collections.emptyList();
		}
		List<Pair<ClassDef, FuncDef>> result = Lists.newArrayListWithCapacity(instances.size());
		for (ClassDef c : instances) {
			for (NameDef nameDef : c.attrVisibleNamesPrivate().get(func.getName())) {
				if (nameDef instanceof FuncDef) {
					FuncDef f = (FuncDef) nameDef;
					result.add(Pair.create(c, f));
				}
			}
		}
		return result;
	}
	
	public List<ImStmt> createDispatch(List<Pair<ClassDef, FuncDef>> instances, int start, int end
			, FuncDef funcDef, ImFunction f, boolean equalityKnown, TypeIdGetter typeId) {
		
		List<ImStmt> result = Lists.newArrayList();
		ImVar thisVar = f.getParameters().get(0);
		boolean returnsVoid = funcDef.attrTyp() instanceof PScriptTypeVoid;
		if (start > end) {
			// there seem to be no instances
			assert instances.size() == 0;
			// just create an dummy return
			if (funcDef.getReturnTyp().attrTyp() instanceof PScriptTypeVoid) {
				// empty function
			} else {
				ImType type = f.getReturnType();
				ImExpr def = getDefaultValueForJassType(type);
				result.add(JassIm.ImReturn(def));
			}
			return result;
		} else if (start == end) {
			FuncDef calledFunc = instances.get(start).getB();
			ImFunction calledJassFunc = getFuncFor(calledFunc);
			addCallRelation(f, calledJassFunc);
			ImExprs arguments = JassIm.ImExprs();
			for (int i=0; i<f.getParameters().size(); i++) {
				ImExpr arg;
				ImVar p = f.getParameters().get(i);
				ImVar expected = calledJassFunc.getParameters().get(i);
				if (expected.getType() instanceof ImSimpleType
						&& p.getType() instanceof ImTupleType) {
					// class type expected but got interface type 
					// ==> select only first part 
					arg = JassIm.ImTupleSelection(JassIm.ImVarAccess(p), 0);
				} else {
					arg = JassIm.ImVarAccess(p);
					// TODO subtyping differences interface vs class?
				}
				arguments.add(arg);
			}
			ImCall call = JassIm.ImFunctionCall(calledJassFunc, arguments);
			if (returnsVoid) {
				result.add(call);
			} else {
				result.add(JassIm.ImReturn(call));
			}
			if (!equalityKnown) {
				// check for equality
				ImExpr condition = JassIm.ImOperatorCall(Ast.OpEquals(), 
						JassIm.ImExprs(
								typeId.get(thisVar),
								JassIm.ImIntVal(getTypeId(instances.get(start).getA()))));;
				
				return Collections.<ImStmt>singletonList(
						JassIm.ImIf(condition, ImStmts(result), ImStmts())
					);
			} else {
				return result;
			}
		} else {
			int splitAt = start + (end-start) / 2;
			
			boolean eq = false;
			if (splitAt - start == 0) {
				// if we only have one element at the left side, we can already check for equality
				eq = true;
			}
			
			List<ImStmt> case1 = createDispatch(instances, start, splitAt, funcDef, f, eq, typeId);
			List<ImStmt> case2 = createDispatch(instances, splitAt+1, end, funcDef, f, false, typeId);

			// if (thistype <= instances[splitAt].typeId)
			ImExpr cond = 
					JassIm.ImOperatorCall(eq ? Ast.OpEquals() : Ast.OpLessEq(), 
							JassIm.ImExprs(
									typeId.get(thisVar),
									JassIm.ImIntVal(getTypeId(instances.get(splitAt).getA()))));
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
			f = JassIm.ImFunction(name, params, ImVoid(), ImVars(), ImStmts(), false, false);
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
			f = JassIm.ImFunction(name, ImVars(), TypesHelper.imInt(), ImVars(), ImStmts(), false, false);
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

	private List<ImFunction> compiletimeFuncs = Lists.newArrayList();
	
	public Collection<ClassDef> getSubClasses(ClassDef classDef) {
		if (subclasses == null) {
			calculateSubclasses();
		}
		return subclasses.get(classDef);
	}

	private void calculateSubclasses() {
		subclasses = HashMultimap.create();
		for (CompilationUnit cu : wurstProg) {
			for (ClassDef c : cu.attrGetByType().classes) {
				if (c.attrExtendedClass() != null) {
					subclasses.put(c.attrExtendedClass(), c);
				}
			}
		}
		subclasses = Utils.transientClosure(subclasses);
	}

	public void addCompiletimeFunc(ImFunction f) {
		compiletimeFuncs.add(f);
	}

}
