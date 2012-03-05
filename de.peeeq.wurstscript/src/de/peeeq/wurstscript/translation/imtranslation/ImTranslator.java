package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class ImTranslator {

	
	private Map<Class<? extends OpBinary>, ImFunction> binaryOperatorFunctions = Maps.newHashMap();
	private CompilationUnit wurstProg;
	
	
	
	
	public ImTranslator(CompilationUnit wurstProg) {
		this.wurstProg = wurstProg;
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
	

	// TODO select function based on parameter types

	public ImFunction getFuncFor(OpBinary op) {
		ImFunction r = binaryOperatorFunctions.get(op);
		if (r == null) throw new Error("invalid op " + op);
		return r;
	}

	private ImFunction opUnaryFunc_not = ImFunction("not", ImVars(ImVar(ImSimpleType("boolean"), "right")), ImSimpleType("boolean"), ImVars(), ImStmts(), true);
	private ImFunction opUnaryFunc_minus = ImFunction("minus", ImVars(ImVar(ImSimpleType("*"), "right")), ImSimpleType("*"), ImVars(), ImStmts(), true);
	
	public ImFunction getFuncFor(OpUnary op) {
		if (op instanceof OpNot) {
			return opUnaryFunc_not;
		} else if (op instanceof OpMinus) {
			return opUnaryFunc_minus;
		} else {
			throw new IllegalArgumentException("invalid op " + op);
		}
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
		ImVar v = varMap.get(varDef);
		if (v == null) {
			ImType type = varDef.attrTyp().imTranslateType();
			String name = varDef.getName();
			v = JassIm.ImVar(type, name);
			varMap.put(varDef, v);
			
		}
		return v;
	}

	public int getTupleIndex(TupleDef tupleDef, VarDef parameter) {
		int i = 0;
		for (WParameter p : tupleDef.getParameters()) {
			if (p == parameter) {
				return i;
			}
			i++;
		}
		throw new Error("");
	}
	
	private Map<ConstructorDef, ImFunction> constructorFuncMap = Maps.newHashMap();

	public ImFunction getConstructorFuncFor(ConstructorDef c) {
		ImFunction f = constructorFuncMap.get(c);
		if (f == null) {
			f = JassIm.ImFunction("new" + c.attrNearestClassDef().getName(), ImVars(), TypesHelper.imInt(), ImVars(), ImStmts(), false);
			constructorFuncMap.put(c, f);
		}
		return f ;
	}

	Map<ClassDef, ImFunction> destroyFuncMap = Maps.newHashMap();
	private ImProg imProg;

	public ImFunction getDestroyFuncFor(ClassDef classDef) {
		ImFunction f = destroyFuncMap.get(classDef); 
		if (f == null) {
			f = JassIm.ImFunction("destroy" + classDef.getName(), ImVars(ImVar(TypesHelper.imInt(), "this")), TypesHelper.imInt(), ImVars(), ImStmts(), false);
			destroyFuncMap.put(classDef, f);
		}
		return f ;
	}


	public List<ImStmt> translateStatements(ImFunction f, WStatements statements) {
		List<ImStmt> result = Lists.newArrayList();
		for (WStatement s : statements) {
			result.add(s.imTranslateStmt(this, f));
		}
		return result ;
	}

	public ImProg translateProg() {
		imProg = ImProg(ImVars(), ImFunctions());
		for (TopLevelDeclaration tld : wurstProg) {
			tld.imTranslateTLD(this);
		}
		return imProg;
	}

	public ImProg imProg() {
		return imProg;
	}

	public void addFunction(ImFunction f) {
		imProg.getFunctions().add(f);
	}

	public void addGlobal(ImVar v) {
		imProg.getGlobals().add(v);
	}

	


}
