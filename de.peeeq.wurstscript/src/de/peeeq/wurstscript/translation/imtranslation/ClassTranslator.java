package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import static de.peeeq.wurstscript.jassIm.JassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassTranslator {

	private ClassDef classDef;
	private ImTranslator translator;

	public ClassTranslator(ClassDef classDef, ImTranslator translator) {
		this.classDef = classDef;
		this.translator = translator;
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		new ClassTranslator(classDef, translator).translate();
		
	}
	
	private void translate() {
		for (ClassSlot s : classDef.getSlots()) {
			s.translateClassSlot(this);
		}
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
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public void translateClassSlot(ConstructorDef s) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
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
