package de.peeeq.wurstscript.translation.imtojass;


import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import static de.peeeq.wurstscript.jassAst.JassAst.*;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class ExprTranslation {

	public static List<JassExpr> translate(ImBoolVal e, ImToJassTranslator translator) {
		return single(JassAst.JassExprBoolVal(e.getValB()));
	}

	private static List<JassExpr> single(JassExpr e) {
		return Collections.singletonList(e);
	}

	public static List<JassExpr> translate(ImFuncRef e, ImToJassTranslator translator) {
		JassFunction f = translator.getJassFuncFor(e.getFunc());
		return single(JassAst.JassExprFuncRef(f.getName()));
	}

	public static List<JassExpr> translate(ImFunctionCall e, ImToJassTranslator translator) {
		
		JassFunction f = translator.getJassFuncFor(e.getFunc());
		JassExprlist arguments = JassExprlist();
		for (ImExpr arg : e.getArguments()) {
			arguments.addAll(arg.translate(translator));
		}
		String funcName = f.getName();
		if (funcName.equals(ImTranslator.$DEBUG_PRINT)) {
			funcName = "BJDebugMsg";
		}
		return single(JassAst.JassExprFunctionCall(funcName, arguments));
	}

	public static List<JassExpr> translate(ImIntVal e, ImToJassTranslator translator) {
		return single(JassExprIntVal(e.getValI()));
	}

	public static List<JassExpr> translate(ImNull e, ImToJassTranslator translator) {
		return single(JassExprNull());
	}

	public static List<JassExpr> translate(ImOperatorCall e, ImToJassTranslator translator) {
		if (e.getOp() instanceof OpBinary && e.getArguments().size() == 2) {
			OpBinary op = (OpBinary) e.getOp();
			JassExpr left  = e.getArguments().get(0).translateSingle(translator);
			JassExpr right = e.getArguments().get(1).translateSingle(translator);
			
			if (op instanceof OpModReal) {
				return single(JassExprFunctionCall("ModuloReal", JassExprlist(left, right)));
			} else if (op instanceof OpModInt) {
				return single(JassExprFunctionCall("ModuloInteger", JassExprlist(left, right)));
			}
			
			return single(JassExprBinary(left, op.jassTranslateBinary(), right));
		} else if (e.getOp() instanceof OpUnary && e.getArguments().size() == 1) {
			OpUnary op = (OpUnary) e.getOp();
			return single(JassExprUnary(op.jassTranslateUnary(), e.getArguments().get(0).translateSingle(translator)));
		} else {
			throw new Error("not implemented: " + e);
		}
	}

	public static List<JassExpr> translate(ImRealVal e, ImToJassTranslator translator) {
		return single(JassExprRealVal(e.getValR()));
	}

	public static List<JassExpr> translate(ImStatementExpr e, ImToJassTranslator translator) {
		throw new Error("this expr should have been flattened: " + e);
	}

	public static List<JassExpr> translate(ImStringVal e, ImToJassTranslator translator) {
		return single(JassExprStringVal(e.getValS()));
	}

	public static List<JassExpr> translate(ImTupleExpr e, ImToJassTranslator translator) {
		List<JassExpr> result = Lists.newArrayList();
		for (ImExpr x : e.getExprs()) {
			result.addAll(x.translate(translator));
		}
		return result;
	}

	public static List<JassExpr> translate(ImTupleSelection e, ImToJassTranslator translator) {
		List<JassExpr> exprs = e.getTupleExpr().translate(translator);
		return single(exprs.get(e.getTupleIndex()));
	}

	public static List<JassExpr> translate(ImVarAccess e, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(e.getVar());
		List<JassExpr> result = Lists.newArrayListWithCapacity(vars.size());
		for (JassVar v : vars) {
			result.add(JassExprVarAccess(v.getName()));
		}
		return result;
	}

	public static List<JassExpr> translate(ImVarArrayAccess e, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(e.getVar());
		List<JassExpr> result = Lists.newArrayListWithCapacity(vars.size());
		for (JassVar v : vars) {
			result.add(JassExprVarArrayAccess(v.getName(), e.getIndex().translateSingle(translator)));
			// XXX index expression is evaluated n times ...
		}
		return result;
	}

	public static JassExpr translateSingle(ImExpr e, ImToJassTranslator translator) {
		List<JassExpr> translated = e.translate(translator);
		if (translated.size() != 1){
			throw new Error("expression has size " + translated.size() + " for expression " + e);
		}
		return translated.get(0);
	}

}
