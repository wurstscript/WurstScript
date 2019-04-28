package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.AstElementWithArgs;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.translation.imtranslation.NormalizeNames;
import de.peeeq.wurstscript.types.TypesHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LuaTranslator {

    final ImProg prog;
    final LuaCompilationUnit luaModel;
    private final Set<String> usedNames = new HashSet<>();
    List<ExprTranslation.TupleEqualsFunc> tupleEqualsFuncs = new ArrayList<>();

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
            return LuaAst.LuaMethod(receiver, a.getName(), LuaAst.LuaParams(), LuaAst.LuaStatements());
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
            return LuaAst.LuaMethod(receiver, uniqueName("create"), LuaAst.LuaParams(), LuaAst.LuaStatements());
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
        usedNames.add(rname);
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
            } else if (s instanceof LuaExpr) {
                LuaExpr e = (LuaExpr) s;
                if (!(e instanceof LuaCallExpr || e instanceof LuaLiteral)) {
                    e.setParent(null);
                    LuaVariable exprTemp = LuaAst.LuaVariable("wurstExpr", e);
                    it.set(exprTemp);
                }
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
            lf.getParams().add(pv);
        }
        // translate local variables
        for (ImVar local : f.getLocals()) {
            LuaVariable luaLocal = luaVar.getFor(local);
            lf.getBody().add(luaLocal);
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

    private Set<ImClass> translated = new HashSet<>();

    private void translateClass(ImClass c) {
        if (translated.contains(c)) {
            return;
        }
        translated.add(c);
        // first translate super-classes
        for (ImClassType sc : c.getSuperClasses()) {
            translateClass(sc.getClassDef());
        }

        // following the code at http://lua-users.org/wiki/InheritanceTutorial
        LuaVariable classVar = luaClassVar.getFor(c);
        LuaMethod initMethod = luaClassInitMethod.getFor(c);

        luaModel.add(classVar);
        luaModel.add(initMethod);

        classVar.setInitialValue(emptyTable());

        // translate functions
        for (ImFunction f : c.getFunctions()) {
            translateFunc(f);
            luaFunc.getFor(f).setName(uniqueName(c.getName() + "_" + f.getName()));
        }

        // create methods:
        Set<String> methods = new HashSet<>();
        createMethods(c, classVar, methods);

        // set supertype metadata:
        LuaTableFields superClasses = LuaAst.LuaTableFields();
        collectSuperClasses(superClasses, c, new HashSet<>());
        luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
            LuaAst.LuaExprVarAccess(classVar),
            ExprTranslation.WURST_SUPERTYPES),
            LuaAst.LuaTableConstructor(superClasses)
        ));


        // create init function:
        LuaStatements body = initMethod.getBody();
        // local new_inst = {}
        LuaTableFields initialFieldValues = LuaAst.LuaTableFields();
        LuaVariable newInst = LuaAst.LuaVariable("new_inst", LuaAst.LuaTableConstructor(initialFieldValues));
        for (ImVar field : c.getFields()) {
            initialFieldValues.add(
                LuaAst.LuaTableNamedField(field.getName(), defaultValue(field.getType()))
            );
        }

        body.add(newInst);
        // setmetatable(new_inst, {__index = classVar})
        body.add(LuaAst.LuaExprFunctionCallByName("setmetatable", LuaAst.LuaExprlist(
            LuaAst.LuaExprVarAccess(newInst),
            LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
                LuaAst.LuaTableNamedField("__index", LuaAst.LuaExprVarAccess(classVar))
            ))
        )));
        body.add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(newInst)));

    }

    private void createMethods(ImClass c, LuaVariable classVar, Set<String> methods) {
        for (ImMethod method : c.getMethods()) {
            if (methods.contains(method.getName())) {
                continue;
            }
            methods.add(method.getName());
            if (method.getIsAbstract()) {
                continue;
            }
            luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                method.getName()),
                LuaAst.LuaExprFuncRef(luaFunc.getFor(method.getImplementation()))
            ));
        }
        // also create links for inherited methods
        for (ImClassType sc : c.getSuperClasses()) {
            createMethods(sc.getClassDef(), classVar, methods);
        }
    }

    @NotNull
    private LuaTableConstructor emptyTable() {
        return LuaAst.LuaTableConstructor(LuaAst.LuaTableFields());
    }

    private void collectSuperClasses(LuaTableFields superClasses, ImClass c, Set<ImClass> visited) {
        if (visited.contains(c)) {
            return;
        }
        superClasses.add(LuaAst.LuaTableExprField(LuaAst.LuaExprVarAccess(luaClassVar.getFor(c)), LuaAst.LuaExprBoolVal(true)));
        visited.add(c);
        for (ImClassType sc : c.getSuperClasses()) {
            collectSuperClasses(superClasses, sc.getClassDef(), visited);
        }
    }


    private void translateGlobal(ImVar v) {
        LuaVariable lv = luaVar.getFor(v);
        lv.setInitialValue(defaultValue(v.getType()));
        luaModel.add(lv);
    }

    private LuaExpr defaultValue(ImType type) {
        return type.match(new ImType.Matcher<LuaExpr>() {
            @Override
            public LuaExpr case_ImTupleType(ImTupleType tt) {
                LuaTableFields tableFields = LuaAst.LuaTableFields();
                for (int i = 0; i < tt.getNames().size(); i++) {
                    tableFields.add(LuaAst.LuaTableNamedField(tt.getNames().get(i), defaultValue(tt.getTypes().get(i))));
                }
                return LuaAst.LuaTableConstructor(
                    tableFields
                );
            }

            @Override
            public LuaExpr case_ImVoid(ImVoid imVoid) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImClassType(ImClassType imClassType) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                return emptyTable();
            }

            @Override
            public LuaExpr case_ImSimpleType(ImSimpleType st) {
                if (TypesHelper.isIntType(st)) {
                    return LuaAst.LuaExprIntVal("0");
                } else if (TypesHelper.isBoolType(st)) {
                    return LuaAst.LuaExprBoolVal(false);
                }
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImArrayType(ImArrayType imArrayType) {
                return emptyTable();
            }

            @Override
            public LuaExpr case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                return LuaAst.LuaExprNull();
            }
        });
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
