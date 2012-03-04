package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class Translator {

	
	private Map<Class<? extends OpBinary>, ImFunction> binaryOperatorFunctions = Maps.newHashMap();
	
	
	
	public Translator() {
		initBinaryOps();
	}
	
	private void initBinaryOps() {
		addOp(OpAnd.class, "boolean", "boolean");
		addOp(OpOr.class, "boolean", "boolean");
		
		addOp(OpDivInt.class, "integer", "integer");
		addOp(OpModInt.class, "integer", "integer");
		
		
		addOp(OpModReal.class, "real", "real");
		addOp(OpDivReal.class, "real", "real");
		
		
		addOp(OpMinus.class, "*", "*");
		addOp(OpMult.class, "*", "*");
		addOp(OpPlus.class, "*", "*");

		addOp(OpUnequals.class, "*", "boolean");
		addOp(OpEquals.class, "*", "boolean");
		addOp(OpGreater.class, "*", "boolean");
		addOp(OpGreaterEq.class, "*", "boolean");
		addOp(OpLess.class, "*", "boolean");
		addOp(OpLessEq.class, "*", "boolean");
	}

	private void addOp(Class<? extends Op> op, String parameterType, String returnType) {
		ImVars vars = ImVars(ImVar(ImSimpleType(parameterType), "left"), ImVar(ImSimpleType(parameterType), "right"));
		binaryOperatorFunctions.put(OpAnd.class, JassIm.ImFunction(op.getSimpleName(), vars, ImSimpleType(returnType), ImVars(), ImStmts(), true));
	}

	public ImFunction getFuncFor(OpBinary op) {
		ImFunction r = binaryOperatorFunctions.get(op);
		if (r == null) throw new Error("invalid op " + op);
		return r;
	}

	private Map<FunctionDefinition, ImFunction> functionMap = Maps.newHashMap();
	
	public ImFunction getFuncFor(FunctionDefinition funcDef) {
		if (functionMap.containsKey(funcDef)) {
			return functionMap.get(funcDef);
		}
		ImFunction f = JassIm.ImFunction(funcDef.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), false);
		
		functionMap.put(funcDef, f);
		return f;
	}
	
	private Map<WScope, ImVar> thisVarMap = Maps.newHashMap();

	public ImVar getThisVar(WScope scope) {
		if (thisVarMap.containsKey(scope)) {
			return thisVarMap.get(scope);
		}
		ImVar v = ImVar(ImSimpleType("integer"), "this");
		thisVarMap.put(scope, v);
		return v ;
	}

	private Map<VarDef, ImVar> varMap = Maps.newHashMap();
	
	public ImVar getVarFor(VarDef varDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public int getTupleIndex(VarDef varDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getConstructorFuncFor(ConstructorDef constructorFunc) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getFuncFor(OpUnary opU) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getDestroyFuncFor(ClassDef classDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImVar getNewTempVar() {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public List<ImStmt> translateStatements(ImFunction f, WStatements statements) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


}
