package de.peeeq.wurstscript.lua.translation;

import de.peeeq.wurstscript.ast.AstElementWithArgs;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;

import java.util.ListIterator;

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

        for (ImVar v : prog.getGlobals()) {
            translateGlobal(v);
        }

        for (ImClass c : prog.getClasses()) {
            translateClass(c);
        }

        for (ImFunction f : prog.getFunctions()) {
            translateFunc(f);
        }

        cleanStatements();

        return luaModel;
    }

    private void cleanStatements() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaStatements stmts) {
                super.visit(stmts);
                cleanStatements(stmts);
            }

        });
    }

    private void cleanStatements(LuaStatements stmts) {
        ListIterator<LuaStatement> it = stmts.listIterator();
        while (it.hasNext()) {
            LuaStatement s = it.next();
            if (s instanceof LuaExprNull) {
                it.remove();
            } else if (s instanceof LuaExpr && !(s instanceof AstElementWithArgs)) {
                LuaExpr e = (LuaExpr) s;
                e.setParent(null);
                LuaVariable exprTemp = LuaAst.LuaVariable("wurstExpr", LuaAst.LuaNoExpr());
                it.set(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(exprTemp), e));
            }

        }
    }

    private void translateFunc(ImFunction f) {
        if (f.isExtern()) {
            return;
        }
        if (f.isNative()) {
            LuaFunction nf = luaFunc.getFor(f);
            LuaNatives.get(nf);
        }

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
        throw new RuntimeException("TODO translate class " + c.getName());
    }

    private void translateGlobal(ImVar v) {
        LuaVariable lv = luaVar.getFor(v);
        if (v.getType() instanceof ImArrayType
                || v.getType() instanceof ImArrayTypeMulti) {
            lv.setInitialValue(LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
        }
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
