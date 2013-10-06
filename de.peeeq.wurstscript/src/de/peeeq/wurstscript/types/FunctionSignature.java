package de.peeeq.wurstscript.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.utils.Utils;

public class FunctionSignature {
	public static final FunctionSignature empty = new FunctionSignature(null, Collections.<WurstType>emptyList(), WurstTypeUnknown.instance());
	private final WurstType receiverType;
	private final List<WurstType> paramTypes;
	private final WurstType returnType;
	
	
	public FunctionSignature(WurstType receiverType, List<WurstType> paramTypes, WurstType returnType) {
		Preconditions.checkNotNull(paramTypes);
		Preconditions.checkNotNull(returnType);
		this.receiverType = receiverType;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}
	
	
	public List<WurstType> getParamTypes() {
		return paramTypes;
	}
	public WurstType getReturnType() {
		return returnType;
	}
	public WurstType getReceiverType() {
		return receiverType;
	}

	public FunctionSignature setTypeArgs(Map<TypeParamDef, WurstType> typeArgBinding) {
		WurstType r2 = returnType.setTypeArgs(typeArgBinding);
		List<WurstType> pt2 = Lists.newArrayList();
		for (WurstType p : paramTypes) {
			pt2.add(p.setTypeArgs(typeArgBinding));
		}
		return new FunctionSignature(receiverType, pt2, r2);
	}
	
	
	public WurstTypeClosure asClosureType() {
		if (receiverType == null) {
			return new WurstTypeClosure(paramTypes, returnType);
		} else {
			ArrayList<WurstType> params = Lists.newArrayListWithCapacity(paramTypes.size() + 1);
			params.add(receiverType);
			params.addAll(paramTypes);
			return new WurstTypeClosure(params, returnType);
		}
		
		
	}


	public static FunctionSignature forFunctionDefinition(FunctionDefinition f) {
//		return new FunctionSignature(def.attrReceiverType(), def.attrParameterTypes(), def.getReturnTyp().attrTyp());
		if (f == null) {
			return FunctionSignature.empty;
		}
		WurstType returnType = f.getReturnTyp().attrTyp().dynamic();
		if (f instanceof TupleDef) {
			TupleDef tupleDef = (TupleDef) f;
			returnType = tupleDef.attrTyp().dynamic();
		}
		
		
		
		List<WurstType> paramTypes = f.attrParameterTypes(); 
		return new FunctionSignature(f.attrReceiverType(), paramTypes, returnType);
	}


	public FunctionSignature withReturnType(WurstTypeInt r) {
		return new FunctionSignature(receiverType, paramTypes, r);
	}


	public static FunctionSignature fromNameLink(NameLink f) {
		return new FunctionSignature(f.getReceiverType(), f.getParameterTypes(), f.getReturnType());
	}
	
}
