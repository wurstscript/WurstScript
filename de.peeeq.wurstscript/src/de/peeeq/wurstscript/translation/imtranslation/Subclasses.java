package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;

public class Subclasses {

	public static Multimap<ImClass, ImClass> calculate(ImProg prog) {
		Multimap<ImClass, ImClass> result = ArrayListMultimap.create();
		
		for (ImClass c : prog.getClasses()) {
			for (ImClass sc : c.getSuperClasses()) {
				result.put(sc, c);
			}
		}
		
		return result;
	}

	public static List<ImClass> get(ImClass c) {
		return Lists.newArrayList(c.attrProg().attrSubclasses().get(c));
	}

}
