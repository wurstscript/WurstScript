package de.peeeq.wurstscript.translation.imtranslation;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImSet;
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
	private ArrayList<ImSet> initStatements = Lists.newArrayList();

	public ClassTranslator(ClassDef classDef, ImTranslator translator) {
		this.classDef = classDef;
		this.translator = translator;
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		new ClassTranslator(classDef, translator).translate();
		
	}
	
	private void translate() {
		for (ClassSlot s : classDef.getMethods()) {
			s.translateClassSlot(this);
		}
		for (ClassSlot s : classDef.getVars()) {
			s.translateClassSlot(this);
		}
		// TODO module instantiations
		
		
		nextFree = JassIm.ImVar(JassIm.ImArrayType("integer"), classDef.getName() + "_nextFree");
		translator.addGlobal(nextFree);
		firstFree = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_firstFree");
		translator.addGlobal(firstFree);
		maxIndex = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_maxIndex");
		translator.addGlobal(maxIndex);
	}

	public void translateClassSlot(FuncDef s) {
		ImFunction f = translator.getFuncFor(s);
		f.setReturnType(s.getReturnTyp().attrTyp().imTranslateType());
		// add implicit parameter
		f.getParameters().add(ImVar(TypesHelper.imInt(), "this"));
		// translate other parameters:
		ImHelper.translateParameters(s.getParameters(), f.getParameters());
		
		f.getBody().addAll(translator.translateStatements(f, s.getBody()));
	}

	public void translateClassSlot(GlobalVarDef s) {
		ImVar v = translator.getVarFor(s);
		if (s.attrIsDynamicClassMember()) {
			// for dynamic class members create an array
			ImType t = s.attrTyp().imTranslateType();
			v.setType(ImHelper.toArray(t));
		} else { // static class member
			translator.addGlobalInitalizer(v, s.getInitialExpr());
		}
		translator.addGlobal(v);
	}

	public void translateClassSlot(ConstructorDef s) {
		ImFunction f = translator.getFuncFor(s);
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
						ImSetArray(nextFree, ImVarAccess(thisVar), ImIntVal(0))
					), 
			// else
					ImStmts(
						// maxindex = maxindex + 1
						ImSet(maxIndex, ImOperatorCall(Ast.OpPlus(), ImExprs(ImVarAccess(maxIndex), ImIntVal(1)))),
						// this = maxindex
						ImSet(thisVar, ImVarAccess(maxIndex))
					))
			// endif
		);
		// TODO initialize vars
		
		
		// return this
		f.getBody().add(ImReturn(ImVarAccess(thisVar)));
	}

	public void translateClassSlot(ModuleInstanciation s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public void translateClassSlot(ModuleUse s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public void translateClassSlot(OnDestroyDef s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
