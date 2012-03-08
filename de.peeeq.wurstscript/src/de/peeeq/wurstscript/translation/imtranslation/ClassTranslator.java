package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;

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
			// TODO translate class slots
		}
	}

}
