package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.Utils;

public class AttrParameterTypes {

	public static List<PscriptType> calculate(ExtensionFuncDef f) {
		List<PscriptType> result = Lists.newArrayList();
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
		List<PscriptType> result = Lists.newArrayList();
		if (f.attrIsDynamicClassMember()) {
			NameDef n = (NameDef) f.attrNearestStructureDef();
			result.add(n.attrTyp());
		}
		addParameterTypes(result, f.getParameters());
		return result;
	}

	public static List<PscriptType> calculate(NativeFunc f) {
		List<PscriptType> result = Lists.newArrayList();
		addParameterTypes(result, f.getParameters());
		return result;
	}

	public static List<PscriptType> calculate(ExprFunctionCall call) {
		FunctionDefinition calledFunc = call.attrFuncDef();
		if (calledFunc == null) {
			return Collections.emptyList();
		}
		return calledFunc.attrParameterTypes();
	}

	public static List<PscriptType> calculate(ExprMemberMethod call) {
		FunctionDefinition calledFunc = call.attrFuncDef();
		if (calledFunc == null) {
			return Collections.emptyList();
		}
		List<PscriptType> types = calledFunc.attrParameterTypes();
		final Map<TypeParamDef, PscriptType> typeParamMapping = call.getLeft().attrTyp().getTypeArgBinding();
		return Utils.map(types, new Function<PscriptType, PscriptType>() {
			@Override
			public PscriptType apply(PscriptType input) {
				return input.setTypeArgs(typeParamMapping);
			}
		});
	}

}
