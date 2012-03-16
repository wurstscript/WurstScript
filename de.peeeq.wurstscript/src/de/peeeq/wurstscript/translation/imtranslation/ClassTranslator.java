package de.peeeq.wurstscript.translation.imtranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import static de.peeeq.wurstscript.jassIm.JassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassTranslator {

	private ClassDef classDef;
	private ImTranslator translator;
	private ImVar nextFree;
	private ImVar firstFree;
	private ImVar maxIndex;
	/** list of statements to initialize a new object **/
	private ArrayList<WStatement> initStatements = Lists.newArrayList();
	private Map<ImVar, OptExpr> dynamicInits = Maps.newHashMap();

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
		// order is important here
		createClassManagementVars();
		translateMethods(classDef);
		translateVars(classDef);
		translateConstructors();
		createDestroyMethod();

	}

	private void createDestroyMethod() {
		ImFunction f = translator.getDestroyFuncFor(classDef);
		addOnDestroyActions(f, classDef);
	}

	private void addOnDestroyActions(ImFunction f, ClassOrModuleInstanciation c) { 
		f.getBody().addAll(translator.translateStatements(f, c.getOnDestroy()));
		
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
		for (ModuleInstanciation mi2 : classDef.getModuleInstanciations()) {
			collectModuleInitializers(mi2);
		}
		for (ConstructorDef c : mi.getConstructors()) {
			initStatements.addAll(c.getBody());
		}
	}

	private void createClassManagementVars() {
		nextFree = JassIm.ImVar(JassIm.ImArrayType("integer"), classDef.getName() + "_nextFree");
		translator.addGlobal(nextFree);
		firstFree = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_firstFree");
		translator.addGlobal(firstFree);
		maxIndex = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_maxIndex");
		translator.addGlobal(maxIndex);
	}

	private void translateVars(ClassOrModuleInstanciation c) {
		for (GlobalVarDef v : classDef.getVars()) {
			translateVar(v);
		}
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			translateVars(mi);
		}
	}

	private void translateMethods(ClassOrModuleInstanciation c) {
		for (FuncDef f : c.getMethods()) {
			translateMethod(f);
		}
		for (ModuleInstanciation mi : c.getModuleInstanciations()) {
			translateMethods(mi);
		}
	}

	public void translateMethod(FuncDef s) {
		ImFunction f = translator.getFuncFor(s);
		f.setReturnType(s.getReturnTyp().attrTyp().imTranslateType());
		// add implicit parameter
		f.getParameters().add(ImVar(TypesHelper.imInt(), "this"));
		// translate other parameters:
		ImHelper.translateParameters(s.getParameters(), f.getParameters());

		f.getBody().addAll(translator.translateStatements(f, s.getBody()));
	}

	public void translateVar(GlobalVarDef s) {
		ImVar v = translator.getVarFor(s);
		if (s.attrIsDynamicClassMember()) {
			// for dynamic class members create an array
			ImType t = s.attrTyp().imTranslateType();
			v.setType(ImHelper.toArray(t));
			dynamicInits.put(v, s.getInitialExpr());
		} else { // static class member
			translator.addGlobalInitalizer(v, s.getInitialExpr());
		}
		translator.addGlobal(v);
	}

	public void translateConstructor(ConstructorDef constr) {
		ImFunction f = translator.getFuncFor(constr);
		// TODO params & return type
		ImVar thisVar = ImVar(TypesHelper.imInt(), "this");
		f.getLocals().add(thisVar);
		f.getBody().add(
		// if firstFree > 0
				ImIf(ImOperatorCall(Ast.OpGreater(), ImExprs(ImVarAccess(firstFree), ImIntVal(0))),
				// then
						ImStmts(
						// this = firstFree
						ImSet(thisVar, ImVarAccess(firstFree)),
						// firstFree = nextFree[thisVar]
								ImSet(firstFree, ImVarArrayAccess(nextFree, ImVarAccess(thisVar))),
								// nextFree[thisVar] = 0
								ImSetArray(nextFree, ImVarAccess(thisVar), ImIntVal(0))),
						// else
						ImStmts(
						// maxindex = maxindex + 1
						ImSet(maxIndex, ImOperatorCall(Ast.OpPlus(), ImExprs(ImVarAccess(maxIndex), ImIntVal(1)))),
						// this = maxindex
								ImSet(thisVar, ImVarAccess(maxIndex))))
		// endif
				);
		// initialize vars
		for (Entry<ImVar, OptExpr> i : dynamicInits.entrySet()) {
			ImVar v = i.getKey();
			if (i.getValue() instanceof Expr) {
				Expr e = (Expr) i.getValue();
				ImStmt s = ImSetArray(i.getKey(), ImVarAccess(thisVar), e.imTranslateExpr(translator, f));
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

	public void translateModuleInit(ModuleInstanciation s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public void translateDestroyMethod(OnDestroyDef s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
