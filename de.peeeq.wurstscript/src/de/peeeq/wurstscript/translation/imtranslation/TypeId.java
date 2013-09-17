package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClasses;
import de.peeeq.wurstscript.jassIm.ImProg;

public class TypeId {

	public static Map<ImClass, Integer> calculate(ImProg prog) {
		int[] count = new int[1];
		Map<ImClass, Integer> result = Maps.newHashMap();
		assignIds(count, result, prog.getClasses());
		return result;
	}

	private static void assignIds(int[] count, Map<ImClass, Integer> result,
			List<ImClass> classes) {
		for (ImClass c : classes) {
			assignId(count, result, c);
		}
	}

	private static void assignId(int[] count, Map<ImClass, Integer> result,
			ImClass c) {
		if (!result.containsKey(c)) {
			count[0]++;
			result.put(c, count[0]);
			assignIds(count, result, c.getSuperClasses());
		}
	}

	public static int get(ImClass c) {
		return c.attrProg().attrTypeId().get(c);
	}

	public static boolean isSubclass(ImClass c, ImClass other) {
		if (c == other) {
			return true;
		}
		for (ImClass sc : c.getSuperClasses()) {
			if (sc.isSubclassOf(other)) {
				return true;
			}
		}
		return false;
	}

}
