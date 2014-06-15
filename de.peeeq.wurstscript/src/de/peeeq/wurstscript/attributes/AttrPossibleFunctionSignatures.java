package de.peeeq.wurstscript.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;

public class AttrPossibleFunctionSignatures {

	public static Collection<FunctionSignature> calculate(FunctionCall fc) {
		Collection<FunctionDefinition> fs = fc.attrPossibleFuncDefs();
		Collection<FunctionSignature> result = new ArrayList<>();
		for (FunctionDefinition f : fs) {
			FunctionSignature sig = FunctionSignature.forFunctionDefinition(f);
			
			if (fc.attrImplicitParameter() instanceof Expr) {
				Expr expr = (Expr) fc.attrImplicitParameter();
				sig = sig.setTypeArgs(expr.attrTyp().getTypeArgBinding());
			}
			sig = sig.setTypeArgs(fc.attrTypeParameterBindings());
			if (!paramTypesCanMatch(sig.getParamTypes(), partialArgTypes(fc), fc)) {
				continue;
			}
			
			result.add(sig);
		}
		return result;
	}

	private static boolean paramTypesCanMatch(List<WurstType> paramTypes, List<WurstType> argTypes, AstElement location) {
		if (argTypes.size() > paramTypes.size()) {
			return false;
		}
		for (int i=0; i<argTypes.size(); i++) {
			if (!argTypes.get(i).isSubtypeOf(paramTypes.get(i), location)) {
				if (!(argTypes.get(i) instanceof WurstTypeUnknown)) {
					return false;
				}
			}
		}
		return true;
	}

	private static List<WurstType> partialArgTypes(FunctionCall fc) {
		List<WurstType> result = new ArrayList<>();
		for (Expr arg : fc.getArgs()) {
			result.add(arg.attrTyp());
		}
		return result;
	}

	public static Collection<FunctionSignature> calculate(ExprNewObject fc) {
		ConstructorDef f = fc.attrConstructorDef();
		if (f == null) {
			return Collections.emptyList();
		}
		StructureDef struct = f.attrNearestStructureDef();
		assert struct != null; // because constructors can only appear inside a StructureDef
		
		WurstType returnType = struct.attrTyp().dynamic();
		Map<TypeParamDef, WurstType> binding2 = fc.attrTypeParameterBindings();
		List<WurstType> paramTypes = Lists.newArrayList();
		for (WParameter p : f.getParameters()) {
			paramTypes.add(p.attrTyp().setTypeArgs(binding2));
		}
		returnType = returnType.setTypeArgs(binding2);
		List<String> pNames = FunctionSignature.getParamNames(f.getParameters());
		return Collections.singleton(new FunctionSignature(null, paramTypes, pNames, returnType));
	}

}
