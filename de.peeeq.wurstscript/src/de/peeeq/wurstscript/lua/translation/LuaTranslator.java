package de.peeeq.wurstscript.lua.translation;

import java.util.Collections;
import java.util.Iterator;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.luaAst.LuaExpr;
import de.peeeq.wurstscript.luaAst.LuaExprOpt;
import de.peeeq.wurstscript.luaAst.LuaExprlist;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaModel;
import de.peeeq.wurstscript.luaAst.LuaParams;
import de.peeeq.wurstscript.luaAst.LuaStatements;
import de.peeeq.wurstscript.luaAst.LuaVariable;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.types.TypesHelper;

public class LuaTranslator {
	
	private final ImProg prog;
	private final LuaCompilationUnit luaModel;

	GetAForB<ImVar, LuaVariable> luaVar = new GetAForB<ImVar, LuaVariable>() {
		@Override
		public LuaVariable initFor(ImVar a) {
			return LuaAst.LuaVariable(uniqueName(a.getName()), LuaAst.LuaNoExpr());
		}
	};
	
	GetAForB<ImFunction, LuaFunction> luaFunc = new GetAForB<ImFunction, LuaFunction>() {

		@Override
		public LuaFunction initFor(ImFunction a) {
			return LuaAst.LuaFunction(uniqueName(a.getName()), LuaAst.LuaParams(), LuaAst.LuaStatements());
		}
	};
	public GetAForB<ImMethod, LuaFunction> luaMethod = new GetAForB<ImMethod, LuaFunction>() {

		@Override
		public LuaFunction initFor(ImMethod a) {
			return LuaAst.LuaFunction(uniqueName(a.getName()), LuaAst.LuaParams(), LuaAst.LuaStatements());
		}
	};
	public GetAForB<ImClass, Integer> typeId = new GetAForB<ImClass, Integer>() {
		int idCounter = 0;
		
		@Override
		public Integer initFor(ImClass a) {
			return ++idCounter;
		}
	};
	
	public LuaTranslator(ImProg prog) {
		this.prog = prog;
		luaModel = LuaAst.LuaCompilationUnit();
	}
	
	protected String uniqueName(String name) {
		return name;
	}

	public LuaCompilationUnit translate() {
		
		for (ImVar v  :prog.getGlobals()) {
			translateGlobal(v);
		}
		
		for (ImClass c : prog.getClasses()) {
			translateClass(c);
		}
		
		for (ImFunction f : prog.getFunctions()) {
			translateFunc(f);
		}
		
		return luaModel;
	}

	private void translateFunc(ImFunction f) {
		LuaFunction lf = luaFunc.getFor(f);
		luaModel.add(lf);
		// translate parameters
		for (ImVar p : f.getParameters()) {
			lf.getParams().add(luaVar.getFor(p));
		}
		// translate body:
		translateStatements(lf.getBody(), f.getBody());		
	}

	void translateStatements(LuaStatements res, ImStmts stmts) {
		for (ImStmt s : stmts) {
			s.translateStmtToLua(res, this);
		}
	}
	
	public LuaStatements translateStatements(ImStmts stmts) {
		LuaStatements r = LuaAst.LuaStatements();
		translateStatements(r, stmts);
		return r;
	}

	private void translateClass(ImClass c) {
		// TODO
		System.out.println("TODO translate class " + c.getName());
	}

	private void translateGlobal(ImVar v) {
		LuaVariable lv = luaVar.getFor(v);
		luaModel.add(lv);
	}

	public LuaExprOpt translateOptional(ImExprOpt e) {
		if (e instanceof ImExpr) {
			ImExpr imExpr = (ImExpr) e;
			return imExpr.translateToLua(this);
		}
		return LuaAst.LuaNoExpr();
	}

	public LuaExprlist translateExprList(ImExprs exprs) {
		LuaExprlist r = LuaAst.LuaExprlist();
		for (ImExpr e : exprs) {
			r.add(e.translateToLua(this));
		}
		return r;
	}

	
	

}
