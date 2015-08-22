package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;

public class TypeId {

	public static Map<ImClass, Integer> calculate(ImProg prog) {
		AtomicInteger count = new AtomicInteger();
		Map<ImClass, Integer> result = Maps.newLinkedHashMap();
		assignIds(count, result, prog.getClasses());
		return result;
	}

	private static void assignIds(AtomicInteger count, Map<ImClass, Integer> result,
			List<ImClass> classes) {
		Multimap<ImClass, ImClass> subClasses = calculateSubclasses(classes);
		
		// start with the classes that do not have any super classes (preorder traversal)
		for (ImClass c : classes) {
			if (c.getSuperClasses().isEmpty()) {
				assignId(count, result, c, subClasses);
			}
		}
	}

	private static Multimap<ImClass, ImClass> calculateSubclasses(List<ImClass> classes) {
		Multimap<ImClass, ImClass> subClasses = HashMultimap.create();
		for (ImClass c : classes) {
			for (ImClass superClass : c.getSuperClasses()) {
				subClasses.put(superClass, c);
			}
		}
		return subClasses;
	}

	/** preorder traversal and assign ids */
	private static void assignId(AtomicInteger count, Map<ImClass, Integer> result,
			ImClass c, Multimap<ImClass,ImClass> subClasses) {
		if (!result.containsKey(c)) {
			result.put(c, count.incrementAndGet());
			// assign ids to subclasses:
			for (ImClass sub : subClasses.get(c)) {
				assignId(count, result, sub, subClasses);
			}
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
