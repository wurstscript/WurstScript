package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.datastructures.UnionFind;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Lazy;
import de.peeeq.wurstscript.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_SUPERTYPES;

public class LuaTranslator {

    final ImProg prog;
    final LuaCompilationUnit luaModel;
    private final Set<String> usedNames = new HashSet<>(Arrays.asList(
        // reserved function names
        "print", "tostring", "error",
        // keywords:
        "and",
        "break",
        "do",
        "else",
        "elseif",
        "end",
        "false",
        "for",
        "function",
        "if",
        "in",
        "local",
        "nil",
        "not",
        "or",
        "repeat",
        "return",
        "then",
        "true",
        "until",
        "while"
    ));

    private ImProg getProg() {
        return prog;
    }

    List<ExprTranslation.TupleFunc> tupleEqualsFuncs = new ArrayList<>();
    List<ExprTranslation.TupleFunc> tupleCopyFuncs = new ArrayList<>();

    GetAForB<ImVar, LuaVariable> luaVar = new GetAForB<ImVar, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImVar a) {
            String name = a.getName();
            if (!a.getIsBJ()) {
                name = uniqueName(name);
            }
            return LuaAst.LuaVariable(name, LuaAst.LuaNoExpr());
        }
    };

    GetAForB<ImFunction, LuaFunction> luaFunc = new GetAForB<ImFunction, LuaFunction>() {

        @Override
        public LuaFunction initFor(ImFunction a) {
            String name = a.getName();
            if (!a.isExtern() && !a.isBj() && !a.isNative()) {
                name = uniqueName(name);
            }

            LuaFunction lf = LuaAst.LuaFunction(name, LuaAst.LuaParams(), LuaAst.LuaStatements());
            // translate parameters
            for (ImVar p : a.getParameters()) {
                LuaVariable pv = luaVar.getFor(p);
                lf.getParams().add(pv);
            }
            return lf;
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

    LuaFunction arrayInitFunction = LuaAst.LuaFunction(uniqueName("defaultArray"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction stringConcatFunction = LuaAst.LuaFunction(uniqueName("stringConcat"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction toIndexFunction = LuaAst.LuaFunction(uniqueName("objectToIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction fromIndexFunction = LuaAst.LuaFunction(uniqueName("objectFromIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction instanceOfFunction = LuaAst.LuaFunction(uniqueName("isInstanceOf"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    private final Lazy<LuaFunction> errorFunc = Lazy.create(() ->
        this.getProg().getFunctions().stream()
            .flatMap(f -> {
                Element trace = f.attrTrace();
                if (trace instanceof FuncDef) {
                    FuncDef fd = (FuncDef) trace;
                    if (fd.getName().equals("error")
                        && fd.attrNearestPackage() instanceof WPackage) {
                        WPackage p = (WPackage) fd.attrNearestPackage();
                        if (p.getName().equals("ErrorHandling")) {
                            return Stream.of(luaFunc.getFor(f));
                        }
                    }
                }
                return Stream.empty();
            })
            .findFirst().orElse(null));

    private final ImTranslator imTr;

    public LuaTranslator(ImProg prog, ImTranslator imTr) {
        this.prog = prog;
        this.imTr = imTr;
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
        collectPredefinedNames();

        RemoveGarbage.removeGarbage(prog);
        prog.flatten(imTr);


        normalizeMethodNames();

//        NormalizeNames.normalizeNames(prog);

        createArrayInitFunction();
        createStringConcatFunction();
        createInstanceOfFunction();
        createObjectIndexFunctions();

        for (ImVar v : prog.getGlobals()) {
            translateGlobal(v);
        }

        // first add class variables
        for (ImClass c : prog.getClasses()) {
            LuaVariable classVar = luaClassVar.getFor(c);
            luaModel.add(classVar);
        }

        for (ImClass c : prog.getClasses()) {
            translateClass(c);
        }

        for (ImFunction f : prog.getFunctions()) {
            translateFunc(f);
        }

        for (ImClass c : prog.getClasses()) {
            initClassTables(c);
        }

        cleanStatements();

        return luaModel;
    }

    private void collectPredefinedNames() {
        for (ImFunction function : prog.getFunctions()) {
            if (function.isBj() || function.isExtern() || function.isNative()) {
                setNameFromTrace(function);
                usedNames.add(function.getName());
            }
        }

        for (ImVar global : prog.getGlobals()) {
            if (global.getIsBJ()) {
                setNameFromTrace(global);
                usedNames.add(global.getName());
            }
        }
    }

    private void setNameFromTrace(JassImElementWithName named) {
        Element trace = named.attrTrace();
        if (trace instanceof NameDef) {
            named.setName(((NameDef) trace).getName());
        }
    }

    private void normalizeMethodNames() {
        // group related methods
        UnionFind<ImMethod> methodUnions = new UnionFind<>();
        for (ImClass c : prog.getClasses()) {
            for (ImMethod m : c.getMethods()) {
                methodUnions.find(m);
                for (ImMethod subMethod : m.getSubMethods()) {
                    methodUnions.union(m, subMethod);
                }
            }
        }

        // give all related methods the same name
        for (Map.Entry<ImMethod, Set<ImMethod>> entry : methodUnions.groups().entrySet()) {
            String name = uniqueName(entry.getKey().getName());
            for (ImMethod method : entry.getValue()) {
                method.setName(name);
            }
        }
    }

    private void createStringConcatFunction() {
        String[] code = {
            "if x then",
            "    if y then return x .. y else return x end",
            "else",
            "    return y",
            "end"
        };

        stringConcatFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        stringConcatFunction.getParams().add(LuaAst.LuaVariable("y", LuaAst.LuaNoExpr()));
        for (String c : code) {
            stringConcatFunction.getBody().add(LuaAst.LuaLiteral(c));
        }
        luaModel.add(stringConcatFunction);
    }

    private void createInstanceOfFunction() {
        // x instanceof A

        // ==> x ~= nil and x.__wurst_supertypes[A]

        String[] code = {
            "return x ~= nil and x." + WURST_SUPERTYPES + "[A]"
        };

        instanceOfFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        instanceOfFunction.getParams().add(LuaAst.LuaVariable("A", LuaAst.LuaNoExpr()));
        for (String c : code) {
            instanceOfFunction.getBody().add(LuaAst.LuaLiteral(c));
        }
        luaModel.add(instanceOfFunction);
    }

    private void createObjectIndexFunctions() {
        String vName = "__wurst_objectIndexMap";
        LuaVariable v = LuaAst.LuaVariable(vName, LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0"))
        )));
        luaModel.add(v);

        LuaVariable im = LuaAst.LuaVariable("__wurst_number_wrapper_map", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0"))
        )));
        luaModel.add(im);

        {
            String[] code = {
                "if x == nil then",
                "    return 0",
                "end",
                // wrap numbers in special number-objects:
                "if type(x) == \"number\" then",
                "    if __wurst_number_wrapper_map[x] then",
                "        x = __wurst_number_wrapper_map[x]",
                "    else",
                "        local obj = {__wurst_boxed_number = x}",
                "        __wurst_number_wrapper_map[x] = obj",
                "        x = obj",
                "    end",
                "end",
                "if __wurst_objectIndexMap[x] then",
                "    return __wurst_objectIndexMap[x]",
                "else",
                "   local r = __wurst_objectIndexMap.counter + 1",
                "   __wurst_objectIndexMap.counter = r",
                "   __wurst_objectIndexMap[r] = x",
                "   __wurst_objectIndexMap[x] = r",
                "   return r",
                "end"
            };

            toIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                toIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            luaModel.add(toIndexFunction);
        }

        {
            String[] code = {
                "if type(x) == \"number\" then",
                "    x = __wurst_objectIndexMap[x]",
                "end",
                "if type(x) == \"table\" and x.__wurst_boxed_number then",
                "    return x.__wurst_boxed_number",
                "end",
                "return x"
            };

            fromIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                fromIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            luaModel.add(fromIndexFunction);
        }
    }

    private void createArrayInitFunction() {
        /*
        function defaultArray(d)
            local t = {}
            local mt = {__index = function (table, key)
                local v = d()
                table[key] = v
                return v
            end}
            setmetatable(t, mt)
            return t
        end
         */
        String[] code = {
            "local t = {}",
            "local mt = {__index = function (table, key)",
            "    local v = d()",
            "    table[key] = v",
            "    return v",
            "end}",
            "setmetatable(t, mt)",
            "return t"
        };

        arrayInitFunction.getParams().add(LuaAst.LuaVariable("d", LuaAst.LuaNoExpr()));
        for (String c : code) {
            arrayInitFunction.getBody().add(LuaAst.LuaLiteral(c));
        }
        luaModel.add(arrayInitFunction);
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
                if (!(e instanceof LuaCallExpr || e instanceof LuaLiteral) || e instanceof LuaExprFunctionCallE) {
                    e.setParent(null);
                    LuaVariable exprTemp = LuaAst.LuaVariable("wurstExpr", e);
                    it.set(exprTemp);
                }
            }
        }
    }

    private void translateFunc(ImFunction f) {
        if (f.isBj()) {
            // do not translate blizzard functions
            return;
        }
        LuaFunction lf = luaFunc.getFor(f);
        if (f.isNative()) {
            LuaNatives.get(lf);
        } else {


            if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
                LuaVariable lastParam = luaVar.getFor(Utils.getLast(f.getParameters()));
                lastParam.setName("...");
            }

            // translate local variables
            for (ImVar local : f.getLocals()) {
                LuaVariable luaLocal = luaVar.getFor(local);
                luaLocal.setInitialValue(defaultValue(local.getType()));
                lf.getBody().add(luaLocal);
            }

            // translate body:
            translateStatements(lf.getBody(), f.getBody());
        }

        if (f.isExtern() || f.isNative()) {
            // only add the function if it is not yet defined:
            String name = lf.getName();
            luaModel.add(LuaAst.LuaIf(
                LuaAst.LuaExprFuncRef(lf),
                LuaAst.LuaStatements(),
                LuaAst.LuaStatements(
                    LuaAst.LuaAssignment(LuaAst.LuaLiteral(name), LuaAst.LuaExprFunctionAbstraction(
                        lf.getParams().copy(),
                        lf.getBody().copy()
                    ))
                )
            ));
        } else {
            luaModel.add(lf);
        }
    }

    void translateStatements(List<LuaStatement> res, ImStmts stmts) {
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

        luaModel.add(initMethod);

        classVar.setInitialValue(emptyTable());

        // translate functions
        for (ImFunction f : c.getFunctions()) {
            translateFunc(f);
            luaFunc.getFor(f).setName(uniqueName(c.getName() + "_" + f.getName()));
        }

        createClassInitFunction(c, classVar, initMethod);
    }

    private void createClassInitFunction(ImClass c, LuaVariable classVar, LuaMethod initMethod) {
        // create init function:
        LuaStatements body = initMethod.getBody();
        // local new_inst = { ... }
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

    private void initClassTables(ImClass c) {
        LuaVariable classVar = luaClassVar.getFor(c);
        // create methods:
        Set<String> methods = new HashSet<>();
        createMethods(c, classVar, methods);

        // set supertype metadata:
        LuaTableFields superClasses = LuaAst.LuaTableFields();
        collectSuperClasses(superClasses, c, new HashSet<>());
        luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
            LuaAst.LuaExprVarAccess(classVar),
            WURST_SUPERTYPES),
            LuaAst.LuaTableConstructor(superClasses)
        ));

        // set typeid metadata:
        luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
            LuaAst.LuaExprVarAccess(classVar),
            ExprTranslation.TYPE_ID),
            LuaAst.LuaExprIntVal("" + prog.attrTypeId().get(c))
        ));


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
        if (v.getIsBJ()) {
            // do not translate blizzard variables
            return;
        }
        LuaVariable lv = luaVar.getFor(v);
        lv.setInitialValue(defaultValue(v.getType()));
        luaModel.add(lv);
    }

    private LuaExpr defaultValue(ImType type) {
        return type.match(new ImType.Matcher<LuaExpr>() {
            @Override
            public LuaExpr case_ImAnyType(ImAnyType imAnyType) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImTupleType(ImTupleType tt) {
                LuaTableFields tableFields = LuaAst.LuaTableFields();
                for (int i = 0; i < tt.getNames().size(); i++) {
                    tableFields.add(LuaAst.LuaTableSingleField(defaultValue(tt.getTypes().get(i))));
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
            public LuaExpr case_ImArrayTypeMulti(ImArrayTypeMulti at) {
                ImType baseType;
                if (at.getArraySize().size() <= 1) {
                    baseType = at.getEntryType();
                } else {
                    List<Integer> arraySizes = new ArrayList<>(at.getArraySize());
                    arraySizes.remove(0);
                    baseType = JassIm.ImArrayTypeMulti(at.getEntryType(), arraySizes);
                }
                return LuaAst.LuaExprFunctionCall(arrayInitFunction,
                    LuaAst.LuaExprlist(
                        LuaAst.LuaExprFunctionAbstraction(LuaAst.LuaParams(),
                            LuaAst.LuaStatements(
                                LuaAst.LuaReturn(defaultValue(baseType))
                            )
                        )
                    ));
            }

            @Override
            public LuaExpr case_ImSimpleType(ImSimpleType st) {
                if (TypesHelper.isIntType(st)) {
                    return LuaAst.LuaExprIntVal("0");
                } else if (TypesHelper.isBoolType(st)) {
                    return LuaAst.LuaExprBoolVal(false);
                } else if (TypesHelper.isRealType(st)) {
                    return LuaAst.LuaExprRealVal("0.");
                }
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImArrayType(ImArrayType imArrayType) {
                ImType baseType = imArrayType.getEntryType();
                return LuaAst.LuaExprFunctionCall(arrayInitFunction,
                    LuaAst.LuaExprlist(
                        LuaAst.LuaExprFunctionAbstraction(LuaAst.LuaParams(),
                            LuaAst.LuaStatements(
                                LuaAst.LuaReturn(defaultValue(baseType))
                            )
                        )
                    ));
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


    public LuaFunction getErrorFunc() {
        return errorFunc.get();
    }
}
