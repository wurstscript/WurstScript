package de.peeeq.wurstscript.translation.imtojass;

import java.util.List;

import static de.peeeq.wurstscript.jassAst.JassAst.*;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImLoop;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;

public class StatementTranslation {


	public static void translate(ImExitwhen imExitwhen, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		stmts.add(JassStmtExitwhen(imExitwhen.getCondition().translateSingle(translator)));
	}

	public static void translate(ImIf imIf, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		JassExpr cond = imIf.getCondition().translateSingle(translator);
		JassStatements thenBlock = JassStatements(); 
		imIf.getThenBlock().translate(thenBlock, f, translator);
		JassStatements elseBlock = JassStatements();
		imIf.getElseBlock().translate(elseBlock, f, translator);
		stmts.add(JassStmtIf(cond, thenBlock, elseBlock));
	}


	public static void translate(ImLoop imLoop, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		JassStatements body = JassStatements();
		imLoop.getBody().translate(body, f, translator);
		stmts.add(JassStmtLoop(body));
	}

	public static void translate(ImReturn imReturn, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		if (imReturn.getReturnValue() instanceof ImNoExpr) {
			stmts.add(JassStmtReturnVoid());
		} else {
			ImExpr e = (ImExpr) imReturn.getReturnValue();
			List<JassExpr> returnValues = e.translate(translator);
			
			
			List<String> retTypes = imReturn.getNearestFunc().getReturnType().translateType();
		
			// assign to temp return values
			for (int i=1; i<returnValues.size(); i++) {
				
				JassVar tempVar = translator.getTempReturnVar(retTypes.get(i), i);
				stmts.add(JassStmtSet(tempVar.getName(), returnValues.get(i)));
				
			}
			// XXX remark: this will evaluate the first expression of the tuple at the end
			JassExpr returnValue = returnValues.get(0);
			stmts.add(JassStmtReturn(returnValue));
		}
	}

	public static void translate(ImSet imSet, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		List<JassVar> vars = translator.getJassVarsFor(imSet.getLeft());
		List<JassExpr> exprs = imSet.getRight().translate(translator);
		for (int i=0; i < vars.size(); i++) {
			stmts.add(JassStmtSet(vars.get(i).getName(), exprs.get(i)));
			// TODO use temporary variables for this
		}
		
	}

	public static void translate(ImExpr imSetArray, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(ImSetArrayTuple imSetArrayTuple, List<JassStatement> stmts, JassFunction f,
			ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(ImSetArray imSetArray, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(ImSetTuple imSetTuple, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(JassStatements JassStatements, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void translate(ImStmts imStmts, List<JassStatement> stmts, JassFunction f, ImToJassTranslator translator) {
		for (ImStmt s : imStmts) {
			s.translate(stmts, f, translator);
		}
	}

}
