package de.peeeq.wurstscript.translation.imtojass;


import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprFunctionCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprNull;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprRealVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprStringVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprUnary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarArrayAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprlist;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
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

	public static JassExpr translate(ImBoolVal e, ImToJassTranslator translator) {
		return JassAst.JassExprBoolVal(e.getValB());
	}

	public static JassExpr translate(ImFuncRef e, ImToJassTranslator translator) {
		JassFunction f = translator.getJassFuncFor(e.getFunc());
		return JassAst.JassExprFuncRef(f.getName());
	}

	public static JassExpr translate(ImFunctionCall e, ImToJassTranslator translator) {
		
		JassFunction f = translator.getJassFuncFor(e.getFunc());
		JassExprlist arguments = JassExprlist();
		for (ImExpr arg : e.getArguments()) {
			arguments.add(arg.translate(translator));
		}
		String funcName = f.getName();
		if (funcName.equals(ImTranslator.$DEBUG_PRINT)) {
			funcName = "BJDebugMsg";
		}
		JassExprFunctionCall call = JassAst.JassExprFunctionCall(funcName, arguments);
		return call;
	}

	public static JassExpr translate(ImIntVal e, ImToJassTranslator translator) {
		return JassExprIntVal(String.valueOf(e.getValI()));
	}

	public static JassExpr translate(ImNull e, ImToJassTranslator translator) {
		return JassExprNull();
	}

	public static JassExpr translate(ImOperatorCall e, ImToJassTranslator translator) {
		if (e.getOp() instanceof OpBinary && e.getArguments().size() == 2) {
			OpBinary op = (OpBinary) e.getOp();
			JassExpr left  = e.getArguments().get(0).translate(translator);
			JassExpr right = e.getArguments().get(1).translate(translator);
			
			if (op instanceof OpModReal) {
				return JassExprFunctionCall("ModuloReal", JassExprlist(left, right));
			} else if (op instanceof OpModInt) {
				return JassExprFunctionCall("ModuloInteger", JassExprlist(left, right));
			}
			
			return JassExprBinary(left, op.jassTranslateBinary(), right);
		} else if (e.getOp() instanceof OpUnary && e.getArguments().size() == 1) {
			OpUnary op = (OpUnary) e.getOp();
			return JassExprUnary(op.jassTranslateUnary(), e.getArguments().get(0).translate(translator));
		} else {
			throw new Error("not implemented: " + e);
		}
	}


	public static JassExpr translate(ImRealVal e, ImToJassTranslator translator) {
		return JassExprRealVal(e.getValR());
	}

	public static JassExpr translate(ImStatementExpr e, ImToJassTranslator translator) {
		throw new Error("this expr should have been flattened: " + e);
	}

	public static JassExpr translate(ImStringVal e, ImToJassTranslator translator) {
		return JassExprStringVal(e.getValS());
	}

	public static JassExpr translate(ImTupleExpr e, ImToJassTranslator translator) {
		throw new Error("tuples should be eliminated in this phase");
	}

	public static JassExpr translate(ImTupleSelection e, ImToJassTranslator translator) {
		throw new Error("tuples should be eliminated in this phase");
	}

	public static JassExprVarAccess translate(ImVarAccess e, ImToJassTranslator translator) {
		JassVar v = translator.getJassVarFor(e.getVar());
		return JassExprVarAccess(v.getName());
	}

	public static JassExprVarArrayAccess translate(ImVarArrayAccess e, ImToJassTranslator translator) {
		JassVar v = translator.getJassVarFor(e.getVar());
		return JassExprVarArrayAccess(v.getName(), e.getIndex().translate(translator));
	}

}
