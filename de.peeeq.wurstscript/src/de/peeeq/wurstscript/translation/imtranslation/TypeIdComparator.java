package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Comparator;

import de.peeeq.wurstscript.ast.ClassDef;

final class TypeIdComparator implements Comparator<ClassDef> {
	
	private ImTranslator translator;

	public TypeIdComparator(ImTranslator translator) {
		this.translator = translator;
	}
	
	@Override
	public int compare(ClassDef o1, ClassDef o2) {
		int i1 = translator.getTypeId(o1);
		int i2 = translator.getTypeId(o2);
		if (i1 > i2) { 
			return 1;
		} else if (i1 < i2) { 
			return -1;
		}
		return 0;
	}
}