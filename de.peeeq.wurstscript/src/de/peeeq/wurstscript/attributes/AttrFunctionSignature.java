package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.FunctionCall;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeJassInt;
import de.peeeq.wurstscript.utils.Utils;

public class AttrFunctionSignature {

	public static FunctionSignature calculate(FunctionCall fc) {
		FunctionDefinition f = fc.attrFuncDef();
		if (f == null) {
			return FunctionSignature.empty;
		}
		WurstType returnType = f.getReturnTyp().attrTyp().dynamic();
		if (f instanceof TupleDef) {
			TupleDef tupleDef = (TupleDef) f;
			returnType = tupleDef.attrTyp().dynamic();
		}
		
		if (returnType instanceof WurstTypeJassInt && !Utils.isJassCode(fc)) {
			returnType = WurstTypeInt.instance();
		}
		
		List<WurstType> paramTypes = f.attrParameterTypes(); 
		FunctionSignature sig = new FunctionSignature(f.attrReceiverType(), paramTypes, returnType);
		if (fc.attrImplicitParameter() instanceof Expr) {
			Expr expr = (Expr) fc.attrImplicitParameter();
			sig = sig.setTypeArgs(expr.attrTyp().getTypeArgBinding());
		}
		sig = sig.setTypeArgs(fc.attrTypeParameterBindings());
		return sig;
	}

	public static FunctionSignature calculate(ExprNewObject fc) {
		ConstructorDef f = fc.attrConstructorDef();
		if (f == null) {
			return FunctionSignature.empty;
		}
		WurstType returnType = f.attrNearestStructureDef().attrTyp().dynamic();
		Map<TypeParamDef, WurstType> binding2 = fc.attrTypeParameterBindings();
		List<WurstType> paramTypes = Lists.newArrayList();
		for (WParameter p : f.getParameters()) {
			paramTypes.add(p.attrTyp().setTypeArgs(binding2));
		}
		returnType = returnType.setTypeArgs(binding2);
		return new FunctionSignature(null, paramTypes, returnType);
	}

}
