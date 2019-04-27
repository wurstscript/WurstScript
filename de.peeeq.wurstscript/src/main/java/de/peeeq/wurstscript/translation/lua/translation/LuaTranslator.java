package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.AstElementWithArgs;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.translation.imtranslation.NormalizeNames;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

public class LuaTranslator {

    private final ImProg prog;
    private final LuaCompilationUnit luaModel;
    private final Set<String> usedNames = new HashSet<>();

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
    public GetAForB<ImMethod, LuaMethod> luaMethod = new GetAForB<ImMethod, LuaMethod>() {

        @Override
        public LuaMethod initFor(ImMethod a) {
            LuaExpr receiver = LuaAst.LuaExprVarAccess(luaClassVar.getFor(a.attrClass()));
            return LuaAst.LuaMethod(receiver, uniqueName(a.getName()), LuaAst.LuaParams(), LuaAst.LuaStatements());
        }
    };


    GetAForB<ImClass, LuaVariable> luaClassVar = new GetAForB<ImClass, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImClass a) {
            return LuaAst.LuaVariable(uniqueName(a.getName()), LuaAst.LuaNoExpr());
        }
    };

    GetAForB<ImClass, LuaVariable> luaClassMetaTableVar = new GetAForB<ImClass, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImClass a) {
            return LuaAst.LuaVariable(uniqueName(a.getName() + "_mt"), LuaAst.LuaNoExpr());
        }
    };

    GetAForB<ImClass, LuaMethod> luaClassInitMethod = new GetAForB<ImClass, LuaMethod>() {
        @Override
        public LuaMethod initFor(ImClass a) {
            LuaExprVarAccess receiver = LuaAst.LuaExprVarAccess(luaClassVar.getFor(a));
            return LuaAst.LuaMethod(receiver,  uniqueName("create"), LuaAst.LuaParams(), LuaAst.LuaStatements());
        }
    };



    public LuaTranslator(ImProg prog) {
        this.prog = prog;
        luaModel = LuaAst.LuaCompilationUnit();
    }

    protected String uniqueName(String name) {
        int i = 0;
        String rname = name;
        while (usedNames.contains(rname)) {
            rname = name + ++i;
        }
        return rname;
    }

    public LuaCompilationUnit translate() {
        NormalizeNames.normalizeNames(prog);

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
            LuaVariable pv = luaVar.getFor(p);
            if (pv.getParent() != null) {
                System.out.println(pv.getParent());
                System.out.println(pv.getParent().getParent());
                System.out.println(pv.getParent().getParent().getParent());
            }
            lf.getParams().add(pv);
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
        // following the code at http://lua-users.org/wiki/InheritanceTutorial
        LuaVariable classVar = luaClassVar.getFor(c);
        LuaMethod initMethod = luaClassInitMethod.getFor(c);

        luaModel.add(classVar);
        luaModel.add(initMethod);

        classVar.setInitialValue(LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));

        // translate functions
        for (ImFunction f : c.getFunctions()) {
            translateFunc(f);
            luaFunc.getFor(f).setName(uniqueName(c.getName() + "_" + f.getName()));
        }

        // create methods:
        for (ImMethod method : c.getMethods()) {
            if (method.getIsAbstract()) {
                continue;
            }
            luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                method.getName()),
                LuaAst.LuaExprFuncRef(luaFunc.getFor(method.getImplementation()))
            ));
        }

        // create init function:
        LuaStatements body = initMethod.getBody();
        // local new_inst = {}
        LuaVariable newInst = LuaAst.LuaVariable("new_inst", LuaAst.LuaNoExpr());
        body.add(LuaAst.LuaLocal(newInst, LuaAst.LuaTableConstructor(LuaAst.LuaTableFields())));
        // setmetatable(new_inst, {__index = classVar})
        body.add(LuaAst.LuaExprFunctionCallByName("setmetatable", LuaAst.LuaExprlist(
            LuaAst.LuaExprVarAccess(newInst),
            LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
                LuaAst.LuaTableNamedField("__index", LuaAst.LuaExprVarAccess(classVar))
            ))
        )));
        body.add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(newInst)));

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


    public int getTypeId(ImClass classDef) {
        return prog.attrTypeId().get(classDef);
    }
}
