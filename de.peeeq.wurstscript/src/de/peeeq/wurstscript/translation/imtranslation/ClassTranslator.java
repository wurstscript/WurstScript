package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImFunctionCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIf;
import static de.peeeq.wurstscript.jassIm.JassIm.ImIntVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImOperatorCall;
import static de.peeeq.wurstscript.jassIm.JassIm.ImReturn;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSet;
import static de.peeeq.wurstscript.jassIm.JassIm.ImSetArray;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStmts;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStringVal;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVar;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarArrayAccess;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmt.DefaultVisitor;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.tests.WurstScriptTest;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeVoid;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

public class ClassTranslator {

	private static final AstElement emptyTrace = Ast.NoExpr();
	private ClassDef classDef;
	private ImTranslator translator;
//	/** list of statements to initialize a new object **/
//	final private List<WStatement> initStatements;
	final private List<Pair<ImVar, OptExpr>> dynamicInits;
	private ClassManagementVars m;

	public ClassTranslator(ClassDef classDef, ImTranslator translator) {
		this.classDef = classDef;
		this.translator = translator;
//		initStatements = translator.getInitStatement(classDef);
		dynamicInits = translator.getDynamicInits(classDef);
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		new ClassTranslator(classDef, translator).translate();

	}

	/**
	 * translates the given classDef
	 */
	private void translate() {
		List<ClassDef> subClasses = Lists.newArrayList(translator.getSubClasses(classDef));
		// sort subclasses by typeid
		Collections.sort(subClasses, new TypeIdComparator(translator));
		
		// order is important here
		createClassManagementVars();
		translateMethods(classDef, subClasses);
		translateVars(classDef);
		translateConstructors();
		createOnDestroyMethod();
		createDestroyMethod();

	}


	private void createDestroyMethod() {
		ImFunction f = translator.getDestroyFuncFor(classDef);
		OnDestroyDef trace = classDef.getOnDestroy();
		ImVar thisVar = ImVar(JassIm.ImSimpleType("integer"), "this", false);
		f.getParameters().add(thisVar);
		
		ImStmts addTo = f.getBody();
		for (ClassDef sc :  translator.getSubClasses(classDef)) {
			if (!hasOwnDestroy(sc, classDef)) {
				
			}
			ImStmts thenBlock = ImStmts();
			ImStmts elseBlock = ImStmts();
			addTo.add(ImIf(trace, ImOperatorCall(Ast.OpEquals(), 
						ImExprs(ImVarArrayAccess(m.typeId, ImVarAccess(thisVar)), 
								ImIntVal(translator.getTypeId(sc)))), 
					thenBlock, elseBlock));
			
			ImFunction scOnDestroy = translator.getFuncFor(sc.getOnDestroy());
			translator.addCallRelation(f, scOnDestroy);
			thenBlock.add(ImFunctionCall(trace, 
					scOnDestroy, 
					ImExprs(ImVarAccess(thisVar))));
			addTo = elseBlock;
		}
		ImFunction onDestroy = translator.getFuncFor(classDef.getOnDestroy());
		translator.addCallRelation(f, onDestroy);
		addTo.add(ImFunctionCall(trace, 
				onDestroy, 
				ImExprs(ImVarAccess(thisVar))));
		addDeallocateCode(f, thisVar);	
	}

	/**
	 * 
	 */
	private boolean hasOwnDestroy(ClassDef sc, ClassDef classDef2) {
		if (sc == classDef2) {
			return false;
		}
		if (sc.getOnDestroy().attrHasEmptyBody()) {
			WurstTypeClass superClass = (WurstTypeClass) sc.getExtendedClass().attrTyp();
			return hasOwnDestroy(superClass.getClassDef(), classDef2);
		} else {
			return true;
		}
	}

	private void addDeallocateCode(ImFunction f, ImVar thisVar) {
		f.getBody().add(		
		// if nextFree[this] < 0 then
			ImIf(emptyTrace, ImOperatorCall(Ast.OpLess(), ImExprs(ImVarArrayAccess(m.nextFree, ImVarAccess(thisVar)), ImIntVal(0))), 
				// then
				ImStmts(
						// nextFree[this] = firstFree
						ImSetArray(emptyTrace, m.nextFree, ImVarAccess(thisVar), ImVarAccess(m.firstFree)),
						// firstFree = this				
						ImSet(emptyTrace, m.firstFree, ImVarAccess(thisVar))
						), 
				// else
				ImStmts(
						// print error message: double free
						ImFunctionCall(emptyTrace, translator.getDebugPrintFunc(), 
								ImExprs(ImStringVal("Double Free of " + classDef.getName())))
						)));
		
	}

	private void createOnDestroyMethod() {
		OnDestroyDef onDestroy = classDef.getOnDestroy();
		ImFunction f = translator.getFuncFor(onDestroy);
		addOnDestroyActions(f, f.getBody(), classDef, translator.getThisVar(onDestroy));
	}
	
	private void addOnDestroyActions(ImFunction f, List<ImStmt> addTo, ClassOrModuleInstanciation c, ImVar thisVar) { 
		// translate ondestroy statements
		List<ImStmt> stmts = translator.translateStatements(f, c.getOnDestroy().getBody());
		replaceThisExpr(stmts, translator.getThisVar(c.getOnDestroy()), thisVar);
		addTo.addAll(stmts);
		
		// add onDestroy actions from modules
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			addOnDestroyActions(f, addTo, mi, thisVar);
		}
		
		if (c instanceof ClassDef) {
			ClassDef cd = (ClassDef) c;
			if (cd.attrExtendedClass() != null) {
				// call onDestroy of super class
				ImFunction onDestroy = translator.getFuncFor(cd.attrExtendedClass().getOnDestroy());
				translator.addCallRelation(f, onDestroy);
				addTo.add(ImFunctionCall(c, 
						onDestroy, 
						ImExprs(ImVarAccess(thisVar))));
			}
		}
	}

	private void replaceThisExpr(List<ImStmt> stmts, final ImVar oldThis, final ImVar newThis) {
		if (oldThis == newThis) {
			return;
		}
		DefaultVisitor replacer = new ImStmt.DefaultVisitor() {
			public void visit(ImVarAccess v) {
				if (v.getVar() == oldThis) {
					v.setVar(newThis);
				}
			}
			
			public void visit(ImSet v) {
				if (v.getLeft() == oldThis) {
					v.setLeft(newThis);
				}
			}
			
			public void visit(ImSetArray v) {
				if (v.getLeft() == oldThis) {
					v.setLeft(newThis);
				}
			}
			
			public void visit(ImSetTuple v) {
				if (v.getLeft() == oldThis) {
					v.setLeft(newThis);
				}
			}
			
			public void visit(ImSetArrayTuple v) {
				if (v.getLeft() == oldThis) {
					v.setLeft(newThis);
				}
			}
		};
		for (ImStmt s : stmts) {
			s.accept(replacer);
		}
		
	}

	private void translateConstructors() {
//		// collect init statements from module instantiations:
//		for (ModuleInstanciation mi : classDef.getModuleInstanciations()) {
//			collectModuleInitializers(mi);
//		}

		for (ConstructorDef c : classDef.getConstructors()) {
			translateConstructor(c);
		}

	}

//	private void collectModuleInitializers(ModuleInstanciation mi) {
//		for (ModuleInstanciation mi2 : mi.getModuleInstanciations()) {
//			collectModuleInitializers(mi2);
//		}
//		for (ConstructorDef c : mi.getConstructors()) {
//			initStatements.addAll(c.getBody());
//		}
//	}

	private void createClassManagementVars() {
		m = translator.getClassManagementVarsFor(classDef);
	}

	private void translateVars(ClassOrModuleInstanciation c) {
		for (GlobalVarDef v : c.getVars()) {
			translateVar(v);
		}
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			translateVars(mi);
		}
	}
	
	public void translateVar(GlobalVarDef s) {
		ImVar v = translator.getVarFor(s);
		if (s.attrIsDynamicClassMember()) {
			// for dynamic class members create an array
			ImType t = s.attrTyp().imTranslateType();
			v.setType(ImHelper.toArray(t));
			dynamicInits.add(Pair.create(v, s.getInitialExpr()));
		} else { // static class member
			translator.addGlobalInitalizer(v, classDef.attrNearestPackage(), s.getInitialExpr());
		}
		translator.addGlobal(v);
	}

	private void translateMethods(ClassOrModuleInstanciation c, List<ClassDef> subClasses) {
		for (FuncDef f : c.getMethods()) {
			translateMethod(f, subClasses);
		}
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			translateMethods(mi, subClasses);
		}
	}

	public void translateMethod(FuncDef s, List<ClassDef> subClasses) {
		if (!s.attrIsStatic()) {
			createDynamicDispatchMethod(s, subClasses);
		}
		createStaticCallFunc(s);
	}

	private ImFunction createDynamicDispatchMethod(FuncDef funcDef, List<ClassDef> subClasses) {
		ImFunction f = translator.getDynamicDispatchFuncFor(funcDef);
		ImFunction staticF = translator.getFuncFor(funcDef);
		
		
		if (f.getParameters().isEmpty()) {
			funcDef.addError("meh");
		}
		if (translator.debugLevel.methodDispatchChecks) {
			AstElement trace = funcDef;
			f.getBody().add(JassIm.ImIf(trace, JassIm.ImOperatorCall(Ast.OpLessEq(), 
					JassIm.ImExprs(ImVarAccess(f.getParameters().get(0)), ImIntVal(0))), 
						ImStmts(ImFunctionCall(trace, translator.getDebugPrintFunc(), 
								ImExprs(ImStringVal("Nullpointer dereference when calling " + Utils.printElementWithSource(funcDef))))), 
						ImStmts(
								JassIm.ImIf(trace, JassIm.ImOperatorCall(Ast.OpGreaterEq(), 
										JassIm.ImExprs(ImVarArrayAccess(m.nextFree, ImVarAccess(f.getParameters().get(0))), ImIntVal(0))), 
											ImStmts(ImFunctionCall(trace, translator.getDebugPrintFunc(), 
													ImExprs(ImStringVal("Calling " + Utils.printElementWithSource(funcDef) + " on a destroyed object.")))), 
													ImStmts()))
								
								));
		}
		
		Map<ClassDef, FuncDef> subClasses2 = translator.getClassesWithImplementation(subClasses, funcDef);
		if (subClasses2.size() > 0) {
			int maxTypeId = translator.getMaxTypeId(subClasses);
			f.getBody().addAll(translator.createDispatch(subClasses2, funcDef, f, maxTypeId, new TypeIdGetterImpl()));
		}
		ImExprs arguments = ImExprs();
		for (int i=0; i<f.getParameters().size(); i++) {
			arguments.add(ImVarAccess(f.getParameters().get(i)));
		}
		translator.addCallRelation(f, staticF);
		
		if (f.getReturnType() instanceof ImVoid) {
			f.getBody().add(JassIm.ImFunctionCall(funcDef, staticF, arguments));
		} else {
			f.getBody().add(JassIm.ImReturn(funcDef, JassIm.ImFunctionCall(funcDef, staticF, arguments)));
		}
		
		
		return f;
	}

	private void createStaticCallFunc(FuncDef funcDef) {
		ImFunction f = translator.getFuncFor(funcDef);
		f.getBody().addAll(translator.translateStatements(f, funcDef.getBody()));
		// TODO add return for abstract function
		if (funcDef.attrIsAbstract() && !(funcDef.attrReturnType() instanceof WurstTypeVoid)) {
			f.getBody().add(ImReturn(funcDef, funcDef.attrReturnType().getDefaultValue()));
		}
	}


	private class TypeIdGetterImpl implements TypeIdGetter {
		@Override
		public ImExpr get(ImVar thisVar) {
			return JassIm.ImVarArrayAccess(m.typeId, JassIm.ImVarAccess(thisVar));
		}
	}

	public void translateConstructor(ConstructorDef constr) {
		createNewFunc(constr);
		createConstructFunc(constr);
	}

	

	private void createNewFunc(ConstructorDef constr) {
		ConstructorDef trace = constr;
		ImFunction f = translator.getConstructNewFunc(constr);
		Map<ImVar, ImVar> varReplacements = Maps.newHashMap();
		
		for (WParameter p : constr.getParameters()) {
			ImVar imP = ImVar(p.attrTyp().imTranslateType(), p.getName(), false);
			varReplacements.put(translator.getVarFor(p), imP);
			f.getParameters().add(imP);
		}
		
		
		ImVar thisVar = JassIm.ImVar(TypesHelper.imInt(), "this", false);
		varReplacements.put(translator.getThisVar(constr), thisVar);
		f.getLocals().add(thisVar);
		f.getBody().add(
		// if firstFree > 0
				ImIf(trace, ImOperatorCall(Ast.OpGreater(), ImExprs(ImVarAccess(m.firstFree), ImIntVal(0))),
				// then
					ImStmts(
						// this = firstFree
						ImSet(trace, thisVar, ImVarAccess(m.firstFree)),
						// firstFree = nextFree[thisVar]
								ImSet(trace, m.firstFree, ImVarArrayAccess(m.nextFree, ImVarAccess(thisVar)))),
				// else
					ImStmts(
						// maxindex = maxindex + 1
						ImSet(emptyTrace, m.maxIndex, ImOperatorCall(Ast.OpPlus(), ImExprs(ImVarAccess(m.maxIndex), ImIntVal(1)))),
						// this = maxindex
								ImSet(trace, thisVar, ImVarAccess(m.maxIndex))))
		// endif
				);
		// nextFree[thisVar] = -1
		f.getBody().add(ImSetArray(trace, m.nextFree, ImVarAccess(thisVar), ImIntVal(-1)));
		
		// set typeId:
		f.getBody().add(ImSetArray(trace, m.typeId, ImVarAccess(thisVar), ImIntVal(translator.getTypeId(classDef))));
		
		// call user defined constructor code:
		ImFunction constrFunc = translator.getConstructFunc(constr);
		ImExprs arguments = ImExprs(ImVarAccess(thisVar));
		for (ImVar a : f.getParameters()) {
			arguments.add(ImVarAccess(a));
		}
		f.getBody().add(ImFunctionCall(trace, constrFunc, arguments));
		translator.addCallRelation(f, constrFunc);
		
		// return this
		f.getBody().add(ImReturn(trace, ImVarAccess(thisVar)));
		
	}

	

	private void createConstructFunc(ConstructorDef constr) {
		ConstructorDef trace = constr;
		ImFunction f = translator.getConstructFunc(constr);
		ImVar thisVar = translator.getThisVar(constr);
		ConstructorDef superConstr = constr.attrSuperConstructor();
		if (superConstr != null) {
			// call super constructor
			ImFunction superConstrFunc = translator.getConstructFunc(superConstr);
			ImExprs arguments = ImExprs(ImVarAccess(thisVar));
			for (Expr a : constr.getSuperArgs()) {
				arguments.add(a.imTranslateExpr(translator, f));
			}
			f.getBody().add(ImFunctionCall(trace, superConstrFunc, arguments));
			translator.addCallRelation(f, superConstrFunc);
		}
		// initialize vars
		for (Pair<ImVar, OptExpr> i : translator.getDynamicInits(classDef)) {
			ImVar v = i.getA();
			if (i.getB() instanceof Expr) {
				Expr e = (Expr) i.getB();
				ImStmt s = ImSetArray(trace, v, ImVarAccess(thisVar), e.imTranslateExpr(translator, f));
				f.getBody().add(s);
			}
		}
		// add initializers from modules
		for (ModuleInstanciation mi : classDef.getModuleInstanciations()) {
			addModuleInits(f, mi, thisVar);
		}
		// constructor user code
		f.getBody().addAll(translator.translateStatements(f, constr.getBody()));
	}

	private void addModuleInits(ImFunction f, ModuleInstanciation mi,	ImVar thisVar) {
		// add initializers from modules
		for (ModuleInstanciation mi2 : mi.getModuleInstanciations()) {
			addModuleInits(f, mi2, thisVar);
		}
		
		for (ConstructorDef c : mi.getConstructors()) {
			List<ImStmt> stmts = translator.translateStatements(f, c.getBody());
			ImHelper.replaceVar(stmts, translator.getThisVar(c), thisVar);
			f.getBody().addAll(stmts);
		}
	}

	
	

}
