package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.datastructures.UnionFind;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Lazy;
import de.peeeq.wurstscript.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_SUPERTYPES;

public class LuaTranslator {
    private static final int LUA_LOCALS_LIMIT = 200;
    private static final List<String> REQUIRED_WURST_HASHTABLE_HELPERS = Arrays.asList(
        "__wurst_InitHashtable",
        "__wurst_SaveInteger", "__wurst_SaveBoolean", "__wurst_SaveReal", "__wurst_SaveStr",
        "__wurst_LoadInteger", "__wurst_LoadBoolean", "__wurst_LoadReal", "__wurst_LoadStr",
        "__wurst_HaveSavedInteger", "__wurst_HaveSavedBoolean", "__wurst_HaveSavedReal", "__wurst_HaveSavedString",
        "__wurst_FlushChildHashtable", "__wurst_FlushParentHashtable",
        "__wurst_RemoveSavedInteger", "__wurst_RemoveSavedBoolean", "__wurst_RemoveSavedReal", "__wurst_RemoveSavedString"
    );
    private static final Set<String> HASHTABLE_NATIVE_NAMES = new HashSet<>(Arrays.asList(
        "InitHashtable",
        "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
        "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr",
        "HaveSavedInteger", "HaveSavedBoolean", "HaveSavedReal", "HaveSavedString",
        "FlushChildHashtable", "FlushParentHashtable",
        "RemoveSavedInteger", "RemoveSavedBoolean", "RemoveSavedReal", "RemoveSavedString"
    ));
    private static final Set<String> LUA_HANDLE_TO_INDEX = Set.of(
        "widgetToIndex", "unitToIndex", "destructableToIndex", "itemToIndex", "abilityToIndex",
        "forceToIndex", "groupToIndex", "triggerToIndex", "triggeractionToIndex", "triggerconditionToIndex",
        "timerToIndex", "locationToIndex", "regionToIndex", "rectToIndex", "soundToIndex",
        "effectToIndex", "dialogToIndex", "buttonToIndex", "questToIndex", "questitemToIndex",
        "leaderboardToIndex", "multiboardToIndex", "trackableToIndex", "lightningToIndex",
        "ubersplatToIndex", "framehandleToIndex", "oskeytypeToIndex"
    );
    private static final Set<String> LUA_HANDLE_FROM_INDEX = Set.of(
        "widgetFromIndex", "unitFromIndex", "destructableFromIndex", "itemFromIndex", "abilityFromIndex",
        "forceFromIndex", "groupFromIndex", "triggerFromIndex", "triggeractionFromIndex", "triggerconditionFromIndex",
        "timerFromIndex", "locationFromIndex", "regionFromIndex", "rectFromIndex", "soundFromIndex",
        "effectFromIndex", "dialogFromIndex", "buttonFromIndex", "questFromIndex", "questitemFromIndex",
        "leaderboardFromIndex", "multiboardFromIndex", "trackableFromIndex", "lightningFromIndex",
        "ubersplatFromIndex", "framehandleFromIndex", "oskeytypeFromIndex"
    );

    final ImProg prog;
    final LuaCompilationUnit luaModel;
    private final Set<String> usedNames = new HashSet<>(Arrays.asList(
        // reserved function names
        "print", "tostring", "error",
        "main", "config",
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
            String name = remapNativeName(a.getName());
            if (!a.isExtern() && !a.isBj() && !a.isNative() && !isFixedEntryPoint(a)) {
                name = uniqueName(name);
            } else if (isFixedEntryPoint(a)) {
                usedNames.add(name);
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

    LuaFunction toIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_objectToIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction fromIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_objectFromIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction stringToIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_stringToIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction stringFromIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_stringFromIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction instanceOfFunction = LuaAst.LuaFunction(uniqueName("isInstanceOf"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction ensureIntFunction = LuaAst.LuaFunction(uniqueName("intEnsure"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction ensureStrFunction = LuaAst.LuaFunction(uniqueName("stringEnsure"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction ensureBoolFunction = LuaAst.LuaFunction(uniqueName("boolEnsure"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction ensureRealFunction = LuaAst.LuaFunction(uniqueName("realEnsure"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    private final Lazy<LuaFunction> errorFunc = Lazy.create(() ->
        Objects.requireNonNull(this.getProg().getFunctions().stream()
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
            .findFirst().orElse(null)));
    private final ImTranslator imTr;

    public LuaTranslator(ImProg prog, ImTranslator imTr) {
        this.prog = prog;
        this.imTr = imTr;
        luaModel = LuaAst.LuaCompilationUnit();
    }

    private String remapNativeName(String name) {
        if (HASHTABLE_NATIVE_NAMES.contains(name)) {
            return "__wurst_" + name;
        }
        return name;
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

        normalizeMethodNames();
        normalizeFieldNames();

//        NormalizeNames.normalizeNames(prog);

        createArrayInitFunction();
        createStringConcatFunction();
        createInstanceOfFunction();
        createObjectIndexFunctions();
        createStringIndexFunctions();
        createEnsureTypeFunctions();
        ensureWurstHashtableHelpers();

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

    /**
     * Always emit internal hashtable helper functions used by Lua lowering.
     * This keeps compiletime migration data loading robust even if the
     * corresponding Warcraft natives are unavailable or filtered out.
     */
    private void ensureWurstHashtableHelpers() {
        for (String helper : REQUIRED_WURST_HASHTABLE_HELPERS) {
            LuaFunction f = LuaAst.LuaFunction(helper, LuaAst.LuaParams(), LuaAst.LuaStatements());
            LuaNatives.get(f);
            luaModel.add(f);
        }
    }

    private boolean isFixedEntryPoint(ImFunction function) {
        return function == imTr.getMainFunc() || function == imTr.getConfFunc();
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

    private void normalizeFieldNames() {
        for (ImClass c : prog.getClasses()) {
            Set<String> methodNames = new HashSet<>();
            collectMethodNames(c, methodNames, new HashSet<>());
            if (methodNames.isEmpty()) {
                continue;
            }
            Set<String> reserved = new HashSet<>(methodNames);
            for (ImVar field : c.getFields()) {
                if (reserved.contains(field.getName())) {
                    String base = field.getName() + "_field";
                    String candidate = base;
                    int i = 1;
                    while (reserved.contains(candidate)) {
                        candidate = base + i++;
                    }
                    field.setName(candidate);
                }
                reserved.add(field.getName());
            }
        }
    }

    private void collectMethodNames(ImClass c, Set<String> methodNames, Set<ImClass> visited) {
        if (visited.contains(c)) {
            return;
        }
        visited.add(c);
        for (ImMethod method : c.getMethods()) {
            methodNames.add(method.getName());
        }
        for (ImClassType sc : c.getSuperClasses()) {
            collectMethodNames(sc.getClassDef(), methodNames, visited);
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

    private void createStringIndexFunctions() {
        LuaVariable map = LuaAst.LuaVariable("__wurst_string_index_map", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0")),
            LuaAst.LuaTableNamedField("byString", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields())),
            LuaAst.LuaTableNamedField("byIndex", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()))
        )));
        luaModel.add(map);

        {
            String[] code = {
                "if x == nil then",
                "    return 0",
                "end",
                "if type(x) ~= \"string\" then",
                "    x = tostring(x)",
                "end",
                "local id = __wurst_string_index_map.byString[x]",
                "if id ~= nil then",
                "    return id",
                "end",
                "id = __wurst_string_index_map.counter + 1",
                "__wurst_string_index_map.counter = id",
                "__wurst_string_index_map.byString[x] = id",
                "__wurst_string_index_map.byIndex[id] = x",
                "return id"
            };

            stringToIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                stringToIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            luaModel.add(stringToIndexFunction);
        }

        {
            String[] code = {
                "local id = tonumber(x)",
                "if id == nil then",
                "    return \"\"",
                "end",
                "id = math.tointeger(id)",
                "if id == nil then",
                "    return \"\"",
                "end",
                "local s = __wurst_string_index_map.byIndex[id]",
                "if s == nil then",
                "    return \"\"",
                "end",
                "return s"
            };

            stringFromIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                stringFromIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            luaModel.add(stringFromIndexFunction);
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

    private void createEnsureTypeFunctions() {
        ensureIntFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        ensureIntFunction.getBody().add(LuaAst.LuaLiteral("local n = tonumber(x)"));
        ensureIntFunction.getBody().add(LuaAst.LuaLiteral("if n == nil then return 0 end"));
        ensureIntFunction.getBody().add(LuaAst.LuaLiteral("local i = math.tointeger(n)"));
        ensureIntFunction.getBody().add(LuaAst.LuaLiteral("if i == nil then return 0 end"));
        ensureIntFunction.getBody().add(LuaAst.LuaLiteral("return i"));
        luaModel.add(ensureIntFunction);

        ensureBoolFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        ensureBoolFunction.getBody().add(LuaAst.LuaLiteral("if x == nil then return false end"));
        ensureBoolFunction.getBody().add(LuaAst.LuaLiteral("return x"));
        luaModel.add(ensureBoolFunction);

        ensureRealFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        ensureRealFunction.getBody().add(LuaAst.LuaLiteral("local n = tonumber(x)"));
        ensureRealFunction.getBody().add(LuaAst.LuaLiteral("if n == nil then return 0.0 end"));
        ensureRealFunction.getBody().add(LuaAst.LuaLiteral("return n"));
        luaModel.add(ensureRealFunction);

        ensureStrFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        ensureStrFunction.getBody().add(LuaAst.LuaLiteral("if x == nil then return \"\" end"));
        ensureStrFunction.getBody().add(LuaAst.LuaLiteral("return tostring(x)"));
        luaModel.add(ensureStrFunction);
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
            if (rewriteTypeCastingCompatFunction(f, lf)) {
                luaModel.add(lf);
                return;
            }


            if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
                LuaVariable lastParam = luaVar.getFor(Utils.getLast(f.getParameters()));
                lastParam.setName("...");
            }

            // translate local variables
            List<LuaVariable> functionLocals = new ArrayList<>();
            for (ImVar local : f.getLocals()) {
                LuaVariable luaLocal = luaVar.getFor(local);
                luaLocal.setInitialValue(defaultValue(local.getType()));
                lf.getBody().add(luaLocal);
                functionLocals.add(luaLocal);
            }

            // translate body:
            translateStatements(lf.getBody(), f.getBody());
            spillLocalsIntoTableIfNeeded(lf, functionLocals);
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

    private boolean rewriteTypeCastingCompatFunction(ImFunction f, LuaFunction lf) {
        if (f.getParameters().isEmpty()) {
            return false;
        }
        String tcFunc = f.getName();
        ImVar p = f.getParameters().get(0);
        LuaExpr arg = LuaAst.LuaExprVarAccess(luaVar.getFor(p));

        if ("stringToIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(stringToIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if ("stringFromIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(stringFromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        // Keep semantic conversions for primitive/index-domain helpers intact.
        if ("realToIndex".equals(tcFunc) || "realFromIndex".equals(tcFunc)
            || "playerToIndex".equals(tcFunc) || "playerFromIndex".equals(tcFunc)
            || "booleanToIndex".equals(tcFunc) || "booleanFromIndex".equals(tcFunc)) {
            return false;
        }
        if (LUA_HANDLE_TO_INDEX.contains(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(toIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if (LUA_HANDLE_FROM_INDEX.contains(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(fromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        // Final fallback for transformed/copied function names that may lose trace info:
        if (tcFunc.endsWith("ToIndex")) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(toIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if (tcFunc.endsWith("FromIndex")) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(fromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        return false;
    }

    private void spillLocalsIntoTableIfNeeded(LuaFunction lf, List<LuaVariable> functionLocals) {
        if (functionLocals.size() <= LUA_LOCALS_LIMIT) {
            return;
        }

        Set<LuaVariable> localSet = new HashSet<>(functionLocals);
        LuaVariable localsTable = LuaAst.LuaVariable(uniqueName("__wurst_locals"),
            LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));

        // Rewrite accesses first, then replace declarations with table init assignments.
        lf.getBody().forEachElement(e -> {
            if (e instanceof LuaExprVarAccess) {
                LuaExprVarAccess va = (LuaExprVarAccess) e;
                if (localSet.contains(va.getVar())) {
                    LuaExpr tableRef = LuaAst.LuaExprVarAccess(localsTable);
                    LuaExpr key = LuaAst.LuaExprStringVal(va.getVar().getName());
                    va.replaceBy(LuaAst.LuaExprArrayAccess(tableRef, LuaAst.LuaExprlist(key)));
                }
            }
        });

        List<LuaStatement> oldBody = new ArrayList<>(lf.getBody());
        lf.getBody().clear();
        lf.getBody().add(localsTable);

        for (LuaStatement stmt : oldBody) {
            if (stmt instanceof LuaVariable && localSet.contains(stmt)) {
                LuaVariable localDecl = (LuaVariable) stmt;
                LuaExpr key = LuaAst.LuaExprStringVal(localDecl.getName());
                LuaExpr left = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(localsTable), LuaAst.LuaExprlist(key));
                lf.getBody().add(LuaAst.LuaAssignment(left, ((LuaExpr) localDecl.getInitialValue()).copy()));
            } else {
                lf.getBody().add(stmt);
            }
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
                } else if (TypesHelper.isStringType(st)) {
                    return LuaAst.LuaExprStringVal("");
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

    public String getTypeCastingFunctionName(ImFunction f) {
        Element trace = f.attrTrace();
        if (trace instanceof FuncDef fd && fd.attrNearestPackage() instanceof WPackage p) {
            if ("TypeCasting".equals(p.getName())) {
                return fd.getName();
            }
        }
        // Fallback: transformed/copied IM functions may lose package trace metadata.
        // Keep Lua behavior stable by routing canonical *ToIndex/*FromIndex names anyway.
        String name = f.getName();
        if ("stringToIndex".equals(name) || "stringFromIndex".equals(name)
            || name.endsWith("ToIndex") || name.endsWith("FromIndex")) {
            return name;
        }
        return null;
    }
}
