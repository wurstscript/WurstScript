package de.peeeq.wurstscript.attributes;

import java.io.ObjectInputStream.GetField;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.types.PscriptType;

public class AttrParameterTypes {

	public static List<PscriptType> calculate(ExtensionFuncDef f) {
		List<PscriptType> result = Lists.newLinkedList();
		result.add(f.getExtendedType().attrTyp());
		addParameterTypes(result, f.getParameters());
		return result;
	}

	private static void addParameterTypes(List<PscriptType> result, WParameters parameters) {
		for (WParameter p : parameters) {
			result.add(p.attrTyp());
		}
		
	}

	public static List<PscriptType> calculate(FuncDef f) {
		List<PscriptType> result = Lists.newLinkedList();
		if (f.attrIsDynamicClassMember()) {
			NameDef n = (NameDef) f.attrNearestClassOrModule();
			result.add(n.attrTyp());
		}
		addParameterTypes(result, f.getParameters());
		return result;
	}

	public static List<PscriptType> calculate(NativeFunc f) {
		List<PscriptType> result = Lists.newLinkedList();
		addParameterTypes(result, f.getParameters());
		return result;
	}

}
