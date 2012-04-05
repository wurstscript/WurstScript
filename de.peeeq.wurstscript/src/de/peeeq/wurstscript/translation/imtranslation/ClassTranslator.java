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
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarAccess;
import static de.peeeq.wurstscript.jassIm.JassIm.ImVarArrayAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.Pair;

public class ClassTranslator {

	private ClassDef classDef;
	private ImTranslator translator;
	/** list of statements to initialize a new object **/
	private ArrayList<WStatement> initStatements = Lists.newArrayList();
	private Map<ImVar, OptExpr> dynamicInits = Maps.newHashMap();
	private ClassManagementVars m;

	public ClassTranslator(ClassDef classDef, ImTranslator translator) {
		this.classDef = classDef;
		this.translator = translator;
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		new ClassTranslator(classDef, translator).translate();

	}

	/**
	 * translates the given classDef
	 */
	private void translate() {
		List<ClassDef> subClasses = Lists.newArrayList(classDef.attrSubClasses());
		// sort subclasses by typeid
		Collections.sort(subClasses, new TypeIdComparator(translator));
		
		// order is important here
		createClassManagementVars();
		translateMethods(classDef, subClasses);
		translateVars(classDef);
		translateConstructors();
		createDestroyMethod();

	}

	private void createDestroyMethod() {
		ImFunction f = translator.getDestroyFuncFor(classDef);
		ImVar thisVar = translator.getThisVar(classDef.getOnDestroy());
		f.getParameters().add(thisVar);
		addOnDestroyActions(f, classDef);
		addDeallocateCode(f, thisVar);	
	}

	private void addDeallocateCode(ImFunction f, ImVar thisVar) {
		f.getBody().add(		
		// if nextFree[this] < 0 then
			ImIf(ImOperatorCall(Ast.OpLess(), ImExprs(ImVarArrayAccess(m.nextFree, ImVarAccess(thisVar)), ImIntVal(0))), 
				// then
				ImStmts(
						// nextFree[this] = firstFree
						ImSetArray(m.nextFree, ImVarAccess(thisVar), ImVarAccess(m.firstFree)),
						// firstFree = this				
						ImSet(m.firstFree, ImVarAccess(m.firstFree))
						), 
				// else
				ImStmts(
						// print error message: double free
						ImFunctionCall(translator.getDebugPrintFunc(), 
								ImExprs(ImStringVal("Double Free of " + classDef.getName())))
						)));
		
	}

	private void addOnDestroyActions(ImFunction f, ClassOrModuleInstanciation c) { 
		f.getBody().addAll(translator.translateStatements(f, c.getOnDestroy().getBody()));
		
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			addOnDestroyActions(f, mi);
		}
	}

	private void translateConstructors() {
		// collect init statements from module instantiations:
		for (ModuleInstanciation mi : classDef.getModuleInstanciations()) {
			collectModuleInitializers(mi);
		}

		for (ConstructorDef c : classDef.getConstructors()) {
			translateConstructor(c);
		}

	}

	private void collectModuleInitializers(ModuleInstanciation mi) {
		for (ModuleInstanciation mi2 : mi.getModuleInstanciations()) {
			collectModuleInitializers(mi2);
		}
		for (ConstructorDef c : mi.getConstructors()) {
			initStatements.addAll(c.getBody());
		}
	}

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
			dynamicInits.put(v, s.getInitialExpr());
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
		ImFunction f = translator.getFuncFor(s);
		
		
		List<Pair<ClassDef, FuncDef>> subClasses2 = translator.getClassedWithImplementation(subClasses, s);
		
		System.out.println(classDef.getName() + ".subClasses =  ");
		for (ClassDef i  : subClasses) {
			System.out.println("	" + i.getName());
		}
		System.out.println(classDef.getName() + "." + s.getName() + ".instances 2 =  ");
		for (Pair<ClassDef, FuncDef> i  : subClasses2) {
			System.out.println("	" + i.getA().getName());
		}
		
		
		if (subClasses2.size() > 0) {
			f.getBody().addAll(translator.createDispatch(subClasses2, 0, subClasses2.size()-1, s, f, false, new TypeIdGetterImpl()));
		}
		
		
		f.getBody().addAll(translator.translateStatements(f, s.getBody()));
	}


	private class TypeIdGetterImpl implements TypeIdGetter {
		@Override
		public ImExpr get(ImVar thisVar) {
			return JassIm.ImVarArrayAccess(m.typeId, JassIm.ImVarAccess(thisVar));
		}
	}

	public void translateConstructor(ConstructorDef constr) {
		ImFunction f = translator.getFuncFor(constr);
		ImVar thisVar = translator.getThisVar(constr);
		f.getLocals().add(thisVar);
		f.getBody().add(
		// if firstFree > 0
				ImIf(ImOperatorCall(Ast.OpGreater(), ImExprs(ImVarAccess(m.firstFree), ImIntVal(0))),
				// then
					ImStmts(
						// this = firstFree
						ImSet(thisVar, ImVarAccess(m.firstFree)),
						// firstFree = nextFree[thisVar]
								ImSet(m.firstFree, ImVarArrayAccess(m.nextFree, ImVarAccess(thisVar)))),
				// else
					ImStmts(
						// maxindex = maxindex + 1
						ImSet(m.maxIndex, ImOperatorCall(Ast.OpPlus(), ImExprs(ImVarAccess(m.maxIndex), ImIntVal(1)))),
						// this = maxindex
								ImSet(thisVar, ImVarAccess(m.maxIndex))))
		// endif
				);
		// nextFree[thisVar] = -1
		f.getBody().add(ImSetArray(m.nextFree, ImVarAccess(thisVar), ImIntVal(-1)));
		
		if (classDef.attrSubClasses().size() > 0 || classDef.attrExtendedClass() != null) {
			// set typeId:
			f.getBody().add(ImSetArray(m.typeId, ImVarAccess(thisVar), ImIntVal(translator.getTypeId(classDef))));
		}
		
		// initialize vars
		for (Entry<ImVar, OptExpr> i : dynamicInits.entrySet()) {
			ImVar v = i.getKey();
			if (i.getValue() instanceof Expr) {
				Expr e = (Expr) i.getValue();
				ImStmt s = ImSetArray(v, ImVarAccess(thisVar), e.imTranslateExpr(translator, f));
				f.getBody().add(s);
			}
		}
		// initializers from modules
		f.getBody().addAll(translator.translateStatements(f, initStatements));
		// constructor
		f.getBody().addAll(translator.translateStatements(f, constr.getBody()));
		// return this
		f.getBody().add(ImReturn(ImVarAccess(thisVar)));
	}


}
