package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;

public class ClassTranslatorDispatch {

	public static void translate(FuncDef s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}

	public static void translate(GlobalVarDef s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}

	public static void translate(ConstructorDef s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}

	public static void translate(ModuleInstanciation s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}

	public static void translate(ModuleUse s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}

	public static void translate(OnDestroyDef s, ClassTranslator translator) {
		translator.translateClassSlot(s);
	}
		
}
