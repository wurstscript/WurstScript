package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;

public class TLDTranslation {

	public static void translate(FuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		// return type:
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		// parameters
		translateParameters(funcDef, f.getParameters());
		// body
		List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
		f.getBody().addAll(stmts);
		
		translator.addFunction(f);
		
	}

	private static void translateParameters(FuncDef funcDef, ImVars parameters) {
		for (WParameter p : funcDef.getParameters()) {
			parameters.add(translateParam(p));
		}
	}

	private static ImVar translateParam(WParameter p) {
		return JassIm.ImVar(p.attrTyp().imTranslateType(), p.getName());
	}

	public static void translate(JassGlobalBlock jassGlobalBlock, ImTranslator translator) {
		for (GlobalVarDef g : jassGlobalBlock) {
			ImVar v = translateVar(g);
			translator.addGlobal(v);
		}
	}

	private static ImVar translateVar(GlobalVarDef g) {
		return JassIm.ImVar(g.attrTyp().imTranslateType(), g.getName());
	}

	public static void translate(NativeFunc nativeFunc, ImTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(NativeType nativeType, ImTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(TupleDef tupleDef, ImTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(WPackage wPackage, ImTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

}
