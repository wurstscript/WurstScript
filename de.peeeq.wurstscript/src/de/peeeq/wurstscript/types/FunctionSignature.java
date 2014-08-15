package de.peeeq.wurstscript.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElementWithParameters;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.attributes.names.NameLink;

public class FunctionSignature {
	public static final FunctionSignature empty = new FunctionSignature(null, Collections.<WurstType>emptyList(), Collections.<String>emptyList(), WurstTypeUnknown.instance());
	private final @Nullable WurstType receiverType;
	private final List<WurstType> paramTypes;
	private final List<String> paramNames; // optional list of parameter names
	private final WurstType returnType;
	
	
	public FunctionSignature(@Nullable WurstType receiverType, List<WurstType> paramTypes, List<String> paramNames, WurstType returnType) {
		Preconditions.checkNotNull(paramTypes);
		Preconditions.checkNotNull(returnType);
		this.receiverType = receiverType;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
		this.paramNames = paramNames;
	}
	
	
	public List<WurstType> getParamTypes() {
		return paramTypes;
	}
	public WurstType getReturnType() {
		return returnType;
	}
	public @Nullable WurstType getReceiverType() {
		return receiverType;
	}

	public FunctionSignature setTypeArgs(Map<TypeParamDef, WurstType> typeArgBinding) {
		WurstType r2 = returnType.setTypeArgs(typeArgBinding);
		List<WurstType> pt2 = Lists.newArrayList();
		for (WurstType p : paramTypes) {
			pt2.add(p.setTypeArgs(typeArgBinding));
		}
		return new FunctionSignature(receiverType, pt2, paramNames, r2);
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


	public static FunctionSignature forFunctionDefinition(@Nullable FunctionDefinition f) {
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
		List<String> paramNames = getParamNames(f.getParameters());
		return new FunctionSignature(f.attrReceiverType(), paramTypes, paramNames, returnType);
	}


	public static List<String> getParamNames(WParameters parameters) {
		return parameters.stream()
				.map(WParameter::getName)
				.collect(Collectors.toList());
	}


	public FunctionSignature withReturnType(WurstTypeInt r) {
		return new FunctionSignature(receiverType, paramTypes, paramNames, r);
	}


	public static FunctionSignature fromNameLink(NameLink f) {
		List<String> pNames = Collections.emptyList();
		if (f.getNameDef() instanceof AstElementWithParameters) {
			AstElementWithParameters n = (AstElementWithParameters) f.getNameDef();
			pNames = getParamNames(n.getParameters());
		}
		return new FunctionSignature(f.getReceiverType(), f.getParameterTypes(), pNames, f.getReturnType());
	}


	public boolean isEmpty() {
		return receiverType == null && paramTypes.isEmpty() && returnType instanceof WurstTypeUnknown;
	}


	public String getParameterDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<paramTypes.size(); i++) {
			if (i>0) {
				sb.append(", ");
			}
			sb.append(paramTypes.get(i).toString());
			if (i < paramNames.size()) {
				sb.append(" ");
				sb.append(paramNames.get(i));
			}
		}
		return sb.toString();
	}


	public String getParamName(int i) {
		if (i>=0 && i<paramNames.size()) {
			return paramNames.get(i);
		}
		return "";
	}
	
}
