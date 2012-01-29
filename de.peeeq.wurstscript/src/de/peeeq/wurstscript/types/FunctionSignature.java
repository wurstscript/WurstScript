package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.TypeParamDef;

public class FunctionSignature {
	public static FunctionSignature empty = new FunctionSignature(Collections.<PscriptType>emptyList(), PScriptTypeUnknown.instance());
	private List<PscriptType> paramTypes;
	private PscriptType returnType;
	
	
	public FunctionSignature(List<PscriptType> paramTypes, PscriptType returnType) {
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}
	
	
	public List<PscriptType> getParamTypes() {
		return paramTypes;
	}
	public PscriptType getReturnType() {
		return returnType;
	}


	public FunctionSignature setTypeArgs(Map<TypeParamDef, PscriptType> typeArgBinding) {
		PscriptType r2 = returnType.setTypeArgs(typeArgBinding);
		List<PscriptType> pt2 = Lists.newArrayList();
		for (PscriptType p : paramTypes) {
			pt2.add(p.setTypeArgs(typeArgBinding));
		}
		return new FunctionSignature(pt2, r2);
	}
	
	
}
