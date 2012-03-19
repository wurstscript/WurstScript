package de.peeeq.wurstscript.translation.imtojass;

import java.util.List;

import de.peeeq.wurstscript.jassAst.JassExpr;
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

public class ExprTranslation {

	public static List<JassExpr> translate(ImBoolVal imBoolVal, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImFuncRef imFuncRef, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImFunctionCall imFunctionCall, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImIntVal imIntVal, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImNull imNull, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImOperatorCall imOperatorCall, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImRealVal imRealVal, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImStatementExpr imStatementExpr, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImStringVal imStringVal, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImTupleExpr imTupleExpr, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImTupleSelection imTupleSelection, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImVarAccess imVarAccess, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static List<JassExpr> translate(ImVarArrayAccess imVarArrayAccess, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static JassExpr translateSingle(ImExpr e, ImToJassTranslator translator) {
		List<JassExpr> translated = e.translate(translator);
		if (translated.size() != 1){
			throw new Error("expression has size " + translated.size());
		}
		return translated.get(0);
	}

}
