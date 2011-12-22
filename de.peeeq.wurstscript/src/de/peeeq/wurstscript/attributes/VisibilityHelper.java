package de.peeeq.wurstscript.attributes;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.HasModifier;

public class VisibilityHelper {

	/**
	 * return only the elements which are not private 
	 * those are the elements visible from the same package
	 * @param map
	 * @return
	 */
	public static <T extends HasModifier> Map<String, T> filterScopePackageLevel(Map<String, T> map) {
		Map<String, T> result = Maps.newHashMap();
		for (Entry<String, T> e : map.entrySet()) {
			if (!e.getValue().attrIsPrivate()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}
	
	/**
	 * return all the elements which are not private and not protected
	 * those are the elements visible everywhere
	 * @param map
	 * @return
	 */
	public static <T extends HasModifier> Map<String, T> filterScopePublic(Map<String, T> map) {
		Map<String, T> result = Maps.newHashMap();
		for (Entry<String, T> e : map.entrySet()) {
			if (!e.getValue().attrIsPrivate()
					&& !e.getValue().attrIsProtected()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

}
