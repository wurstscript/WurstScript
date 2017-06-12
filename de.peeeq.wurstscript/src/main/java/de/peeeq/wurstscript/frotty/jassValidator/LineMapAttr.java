package de.peeeq.wurstscript.frotty.jassValidator;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.jassAst.Element;
import de.peeeq.wurstscript.jassAst.JassProg;

public class LineMapAttr {

	public static Map<Element, Integer> get(JassProg jassProgImpl) {
		return Maps.newLinkedHashMap();
	}

	public static int getLine(Element e) {
		Integer integer = e.getProg().attrLineMap().get(e);
		if (integer != null) {
			return integer;
		}
		return e.getParent().getLine();
	}

	public static JassProg getProg(Element e) {
		if (e instanceof JassProg) {
			return (JassProg) e;
		}
		return e.getParent().getProg();
	}

}
