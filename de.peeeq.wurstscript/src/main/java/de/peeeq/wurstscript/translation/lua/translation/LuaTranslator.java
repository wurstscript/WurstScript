package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.datastructures.UnionFind;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WParameter;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_SUPERTYPES;

public class LuaTranslator {
    private static final int LUA_LOCALS_LIMIT = 200;
    private static final List<String> HASHTABLE_HANDLE_SAVE_NAMES = Arrays.asList(
        "SavePlayerHandle", "SaveWidgetHandle", "SaveDestructableHandle", "SaveItemHandle", "SaveUnitHandle",
        "SaveAbilityHandle", "SaveTimerHandle", "SaveTriggerHandle", "SaveTriggerConditionHandle",
        "SaveTriggerActionHandle", "SaveTriggerEventHandle", "SaveForceHandle", "SaveGroupHandle",
        "SaveLocationHandle", "SaveRectHandle", "SaveBooleanExprHandle", "SaveSoundHandle", "SaveEffectHandle",
        "SaveUnitPoolHandle", "SaveItemPoolHandle", "SaveQuestHandle", "SaveQuestItemHandle",
        "SaveDefeatConditionHandle", "SaveTimerDialogHandle", "SaveLeaderboardHandle", "SaveMultiboardHandle",
        "SaveMultiboardItemHandle", "SaveTrackableHandle", "SaveDialogHandle", "SaveButtonHandle",
        "SaveTextTagHandle", "SaveLightningHandle", "SaveImageHandle", "SaveUbersplatHandle", "SaveRegionHandle",
        "SaveFogStateHandle", "SaveFogModifierHandle", "SaveAgentHandle", "SaveHashtableHandle", "SaveFrameHandle"
    );
    private static final List<String> HASHTABLE_HANDLE_LOAD_NAMES = Arrays.asList(
        "LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle",
        "LoadAbilityHandle", "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle",
        "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle", "LoadGroupHandle",
        "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle",
        "LoadUnitPoolHandle", "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle",
        "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle", "LoadMultiboardHandle",
        "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle",
        "LoadTextTagHandle", "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle",
        "LoadFogStateHandle", "LoadFogModifierHandle", "LoadHashtableHandle", "LoadFrameHandle"
    );
    private static final List<String> HASHTABLE_NATIVE_NAMES_RAW = Arrays.asList(
        "InitHashtable",
        "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
        "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr",
        "HaveSavedInteger", "HaveSavedBoolean", "HaveSavedReal", "HaveSavedString", "HaveSavedHandle",
        "FlushChildHashtable", "FlushParentHashtable",
        "RemoveSavedInteger", "RemoveSavedBoolean", "RemoveSavedReal", "RemoveSavedString", "RemoveSavedHandle"
    );
    private static final List<String> REQUIRED_WURST_HASHTABLE_HELPERS = Arrays.asList(
        "__wurst_InitHashtable",
        "__wurst_SaveInteger", "__wurst_SaveBoolean", "__wurst_SaveReal", "__wurst_SaveStr",
        "__wurst_LoadInteger", "__wurst_LoadBoolean", "__wurst_LoadReal", "__wurst_LoadStr",
        "__wurst_HaveSavedInteger", "__wurst_HaveSavedBoolean", "__wurst_HaveSavedReal", "__wurst_HaveSavedString", "__wurst_HaveSavedHandle",
        "__wurst_FlushChildHashtable", "__wurst_FlushParentHashtable",
        "__wurst_RemoveSavedInteger", "__wurst_RemoveSavedBoolean", "__wurst_RemoveSavedReal", "__wurst_RemoveSavedString", "__wurst_RemoveSavedHandle"
    );
    private static final List<String> REQUIRED_WURST_CONTEXT_CALLBACK_HELPERS = Arrays.asList(
        "__wurst_ForForce",
        "__wurst_GetEnumPlayer",
        "__wurst_ForGroup",
        "__wurst_GetEnumUnit",
        "__wurst_EnumItemsInRect",
        "__wurst_GetEnumItem",
        "__wurst_EnumDestructablesInRect",
        "__wurst_GetEnumDestructable"
    );
    private static final Set<String> HASHTABLE_NATIVE_NAMES = new HashSet<>(allHashtableNativeNames());
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
    private static final boolean DEBUG_LUA_DISPATCH = "1".equals(System.getenv("WURST_DEBUG_LUA_DISPATCH"))
        || Boolean.getBoolean("wurst.debug.lua.dispatch");
    private static final boolean DEBUG_LUA_LOCALS = "1".equals(System.getenv("WURST_DEBUG_LUA_LOCALS"))
        || Boolean.getBoolean("wurst.debug.lua.locals");

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
        if ("ForForce".equals(name)) {
            return "__wurst_ForForce";
        }
        if ("GetEnumPlayer".equals(name)) {
            return "__wurst_GetEnumPlayer";
        }
        if ("ForGroup".equals(name)) {
            return "__wurst_ForGroup";
        }
        if ("GetEnumUnit".equals(name)) {
            return "__wurst_GetEnumUnit";
        }
        if ("EnumItemsInRect".equals(name)) {
            return "__wurst_EnumItemsInRect";
        }
        if ("GetEnumItem".equals(name)) {
            return "__wurst_GetEnumItem";
        }
        if ("EnumDestructablesInRect".equals(name)) {
            return "__wurst_EnumDestructablesInRect";
        }
        if ("GetEnumDestructable".equals(name)) {
            return "__wurst_GetEnumDestructable";
        }
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
        ensureWurstContextCallbackHelpers();

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
        enforceLuaLocalLimits();
        emitExperimentalHashtableLeakGuards();

        return luaModel;
    }

    /**
     * Always emit internal hashtable helper functions used by Lua lowering.
     * This keeps compiletime migration data loading robust even if the
     * corresponding Warcraft natives are unavailable or filtered out.
     */
    private void ensureWurstHashtableHelpers() {
        Set<String> requiredHelpers = new LinkedHashSet<>(REQUIRED_WURST_HASHTABLE_HELPERS);
        requiredHelpers.addAll(prefixed(HASHTABLE_HANDLE_SAVE_NAMES));
        requiredHelpers.addAll(prefixed(HASHTABLE_HANDLE_LOAD_NAMES));
        for (String helper : requiredHelpers) {
            LuaFunction f = LuaAst.LuaFunction(helper, LuaAst.LuaParams(), LuaAst.LuaStatements());
            LuaNatives.get(f);
            luaModel.add(f);
        }
    }

    private void ensureWurstContextCallbackHelpers() {
        for (String helper : REQUIRED_WURST_CONTEXT_CALLBACK_HELPERS) {
            LuaFunction f = LuaAst.LuaFunction(helper, LuaAst.LuaParams(), LuaAst.LuaStatements());
            LuaNatives.get(f);
            luaModel.add(f);
        }
    }

    private void emitExperimentalHashtableLeakGuards() {
        luaModel.add(LuaAst.LuaLiteral("-- Wurst experimental Lua assertion guards: raw WC3 hashtable natives must not be called."));
        for (String nativeName : allHashtableNativeNames()) {
            luaModel.add(LuaAst.LuaLiteral("if " + nativeName + " ~= nil then " + nativeName
                + " = function(...) error(\"Wurst Lua assertion failed: unexpected call to native " + nativeName
                + ". Expected __wurst_" + nativeName + ".\") end end"));
        }
    }

    public static void assertNoLeakedHashtableNativeCalls(String luaCode) {
        List<String> leaked = new ArrayList<>();
        List<String> missingHelpers = new ArrayList<>();
        for (String nativeName : allHashtableNativeNames()) {
            if (containsRegex(luaCode, "\\b" + nativeName + "\\s*\\(")) {
                leaked.add(nativeName);
            }
            String helperName = "__wurst_" + nativeName;
            boolean helperCalled = containsRegex(luaCode, "\\b" + helperName + "\\s*\\(");
            boolean helperDefined = containsRegex(luaCode, "\\bfunction\\s+" + helperName + "\\s*\\(");
            if (helperCalled && !helperDefined) {
                missingHelpers.add(helperName);
            }
        }
        if (!leaked.isEmpty()) {
            throw new RuntimeException("Wurst Lua backend assertion failed: leaked raw hashtable native calls in generated Lua: "
                + String.join(", ", leaked));
        }
        if (!missingHelpers.isEmpty()) {
            throw new RuntimeException("Wurst Lua backend assertion failed: missing __wurst hashtable helper definitions in generated Lua: "
                + String.join(", ", missingHelpers));
        }
    }

    private static List<String> allHashtableNativeNames() {
        List<String> result = new ArrayList<>(HASHTABLE_NATIVE_NAMES_RAW);
        result.addAll(HASHTABLE_HANDLE_SAVE_NAMES);
        result.addAll(HASHTABLE_HANDLE_LOAD_NAMES);
        return result;
    }

    private static List<String> prefixed(List<String> names) {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            result.add("__wurst_" + name);
        }
        return result;
    }

    private static boolean containsRegex(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
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

        // give all related methods the same name in deterministic order
        List<List<ImMethod>> groups = new ArrayList<>();
        for (Set<ImMethod> group : methodUnions.groups().values()) {
            List<ImMethod> sortedGroup = new ArrayList<>(group);
            sortedGroup.sort(Comparator.comparing(this::methodSortKey));
            groups.add(sortedGroup);
        }
        groups.sort(Comparator.comparing(g -> g.isEmpty() ? "" : methodSortKey(g.get(0))));
        for (List<ImMethod> group : groups) {
            if (group.isEmpty()) {
                continue;
            }
            String name = uniqueName(group.get(0).getName());
            for (ImMethod method : group) {
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
            // local-limit enforcement is done after final statement cleanup,
            // because cleanup and later rewrites can still introduce locals.
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
        String tcFunc = getTypeCastingFunctionName(f);
        if (tcFunc == null) {
            return false;
        }
        ImVar firstParam = f.getParameters().get(0);
        LuaExpr arg = LuaAst.LuaExprVarAccess(luaVar.getFor(firstParam));

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
        return false;
    }

    private void enforceLuaLocalLimits() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaFunction f) {
                super.visit(f);
                spillLocalsIntoTableIfNeeded(f.getName(), f.getParams(), f.getBody());
            }

            @Override
            public void visit(LuaMethod m) {
                super.visit(m);
                spillLocalsIntoTableIfNeeded(m.getName(), m.getParams(), m.getBody());
            }
        });
    }

    private void spillLocalsIntoTableIfNeeded(String functionName, LuaParams params, LuaStatements body) {
        List<LuaVariable> scopeLocals = collectFunctionScopeLocals(body);
        int localCount = params.size() + scopeLocals.size();
        if (DEBUG_LUA_LOCALS) {
            WLogger.info("[LUA_LOCALS] function=" + functionName + " params=" + params.size()
                + " locals=" + scopeLocals.size() + " total=" + localCount);
        }
        if (localCount < LUA_LOCALS_LIMIT || scopeLocals.isEmpty()) {
            return;
        }

        LuaVariable localsTable = findTopLevelLocalsTable(body);
        if (localsTable == null) {
            localsTable = LuaAst.LuaVariable(uniqueName("__wurst_locals"),
                LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
        }
        // Must be declared before any rewritten uses; otherwise accesses become global lookups.
        if (!body.isEmpty() && body.get(0) != localsTable) {
            body.remove(localsTable);
            body.add(0, localsTable);
        }

        Set<LuaVariable> localSet = new LinkedHashSet<>();
        for (LuaVariable v : scopeLocals) {
            if (v != localsTable) {
                localSet.add(v);
            }
        }
        if (localSet.isEmpty()) {
            return;
        }

        if (DEBUG_LUA_LOCALS) {
            WLogger.info("[LUA_LOCALS] spill function=" + functionName + " total=" + localCount
                + " spilledLocals=" + localSet.size());
        }

        final LuaVariable tableVar = localsTable;
        final Map<LuaVariable, Integer> localSlots = createLocalSlots(localSet);

        // Rewrite accesses first, then replace declarations with table init assignments.
        forEachElementRec(body, e -> {
            if (e instanceof LuaExprVarAccess) {
                LuaExprVarAccess va = (LuaExprVarAccess) e;
                LuaVariable var = va.getVar();
                Integer slot = localSlots.get(var);
                if (slot != null) {
                    LuaExpr tableRef = LuaAst.LuaExprVarAccess(tableVar);
                    LuaExpr key = LuaAst.LuaExprIntVal("" + slot);
                    va.replaceBy(LuaAst.LuaExprArrayAccess(tableRef, LuaAst.LuaExprlist(key)));
                }
            }
        });

        rewriteLocalDeclarationsToTableAssignments(body, localSet, localSlots, tableVar);
    }

    private Map<LuaVariable, Integer> createLocalSlots(Set<LuaVariable> localSet) {
        Map<LuaVariable, Integer> r = new LinkedHashMap<>();
        int i = 1;
        for (LuaVariable v : localSet) {
            r.put(v, i++);
        }
        return r;
    }

    private LuaVariable findTopLevelLocalsTable(LuaStatements body) {
        for (LuaStatement stmt : body) {
            if (stmt instanceof LuaVariable) {
                LuaVariable v = (LuaVariable) stmt;
                if (v.getName().startsWith("__wurst_locals") && v.getInitialValue() instanceof LuaTableConstructor) {
                    return v;
                }
            }
        }
        return null;
    }

    private List<LuaVariable> collectFunctionScopeLocals(LuaStatements body) {
        List<LuaVariable> result = new ArrayList<>();
        collectFunctionScopeLocalsRec(body, result);
        return result;
    }

    private void rewriteLocalDeclarationsToTableAssignments(LuaStatements stmts, Set<LuaVariable> localSet, Map<LuaVariable, Integer> localSlots, LuaVariable tableVar) {
        ListIterator<LuaStatement> it = stmts.listIterator();
        while (it.hasNext()) {
            LuaStatement stmt = it.next();
            if (stmt instanceof LuaVariable && localSet.contains(stmt)) {
                LuaVariable localDecl = (LuaVariable) stmt;
                Integer slot = localSlots.get(localDecl);
                if (slot == null) {
                    continue;
                }
                LuaExpr key = LuaAst.LuaExprIntVal("" + slot);
                LuaExpr left = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tableVar), LuaAst.LuaExprlist(key));
                LuaExprOpt initVal = localDecl.getInitialValue();
                LuaExpr right = initVal instanceof LuaExpr ? (LuaExpr) initVal.copy() : LuaAst.LuaExprNull();
                it.set(LuaAst.LuaAssignment(left, right));
            } else if (stmt instanceof LuaIf) {
                LuaIf luaIf = (LuaIf) stmt;
                rewriteLocalDeclarationsToTableAssignments(luaIf.getThenStmts(), localSet, localSlots, tableVar);
                rewriteLocalDeclarationsToTableAssignments(luaIf.getElseStmts(), localSet, localSlots, tableVar);
            } else if (stmt instanceof LuaWhile) {
                LuaWhile luaWhile = (LuaWhile) stmt;
                rewriteLocalDeclarationsToTableAssignments(luaWhile.getBody(), localSet, localSlots, tableVar);
            }
        }
    }

    private void collectFunctionScopeLocalsRec(de.peeeq.wurstscript.luaAst.Element e, List<LuaVariable> out) {
        if (e instanceof LuaExprFunctionAbstraction || e instanceof LuaFunction || e instanceof LuaMethod) {
            return;
        }
        if (e instanceof LuaVariable) {
            out.add((LuaVariable) e);
        }
        e.forEachElement(child -> collectFunctionScopeLocalsRec(child, out));
    }

    private void forEachElementRec(de.peeeq.wurstscript.luaAst.Element root, java.util.function.Consumer<de.peeeq.wurstscript.luaAst.Element> action) {
        action.accept(root);
        root.forEachElement(child -> forEachElementRec(child, action));
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
        createMethods(c, classVar);

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

    private void createMethods(ImClass c, LuaVariable classVar) {
        List<ImMethod> allMethods = collectMethodsInHierarchy(c);
        Set<ImMethod> inHierarchy = new HashSet<>(allMethods);
        UnionFind<ImMethod> unions = new UnionFind<>();
        for (ImMethod method : allMethods) {
            unions.find(method);
            for (ImMethod subMethod : method.getSubMethods()) {
                if (inHierarchy.contains(subMethod)) {
                    unions.union(method, subMethod);
                }
            }
        }

        Map<ImMethod, List<ImMethod>> groupedMethods = new HashMap<>();
        for (ImMethod method : allMethods) {
            ImMethod root = unions.find(method);
            groupedMethods.computeIfAbsent(root, k -> new ArrayList<>()).add(method);
        }

        List<List<ImMethod>> groups = new ArrayList<>(groupedMethods.values());
        groups.sort(Comparator.comparing(group -> group.isEmpty() ? "" : methodSortKey(group.get(0))));
        Map<String, ImMethod> slotToImpl = new TreeMap<>();
        for (List<ImMethod> groupMethods : groups) {
            if (groupMethods == null || groupMethods.isEmpty()) {
                continue;
            }
            groupMethods.sort(Comparator.comparing(this::methodSortKey));
            ImMethod chosen = chooseBestImplementationForClass(c, groupMethods);
            if (chosen == null || chosen.getIsAbstract() || chosen.getImplementation() == null) {
                continue;
            }
            Set<String> slotNames = collectDispatchSlotNames(c, groupMethods);
            for (String slotName : slotNames) {
                ImMethod current = slotToImpl.get(slotName);
                if (current == null || compareDispatchCandidates(c, chosen, current) < 0) {
                    slotToImpl.put(slotName, chosen);
                }
            }
            String debugKey = groupMethods.get(0).getName();
            debugDispatchGroup(c, debugKey, slotNames, groupMethods, chosen);
        }
        for (Map.Entry<String, ImMethod> e : slotToImpl.entrySet()) {
            ImMethod impl = e.getValue();
            if (impl == null || impl.getImplementation() == null) {
                continue;
            }
            luaModel.add(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                e.getKey()),
                LuaAst.LuaExprFuncRef(luaFunc.getFor(impl.getImplementation()))
            ));
        }
    }

    private Set<String> collectDispatchSlotNames(ImClass receiverClass, List<ImMethod> groupMethods) {
        Set<String> slotNames = new TreeSet<>();
        Set<String> semanticNames = new TreeSet<>();
        for (ImMethod m : groupMethods) {
            if (m == null) {
                continue;
            }
            String methodName = m.getName();
            if (!methodName.isEmpty()) {
                slotNames.add(methodName);
            }
            ImClass owner = m.attrClass();
            String semanticName = semanticNameFromMethodName(methodName);
            if (!semanticName.isEmpty()) {
                semanticNames.add(semanticName);
            }
            if (owner != null && !semanticName.isEmpty()) {
                slotNames.add(owner.getName() + "_" + semanticName);
            }
        }
        if (receiverClass != null && !semanticNames.isEmpty()) {
            Set<String> classNames = new TreeSet<>();
            collectClassNamesInHierarchy(receiverClass, classNames, new HashSet<>());
            for (String className : classNames) {
                for (String semanticName : semanticNames) {
                    slotNames.add(className + "_" + semanticName);
                }
            }
        }
        return slotNames;
    }

    private void collectClassNamesInHierarchy(ImClass c, Set<String> out, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        out.add(c.getName());
        for (ImClassType sc : c.getSuperClasses()) {
            collectClassNamesInHierarchy(sc.getClassDef(), out, visited);
        }
    }

    private List<ImMethod> collectMethodsInHierarchy(ImClass c) {
        List<ImMethod> result = new ArrayList<>();
        collectMethodsInHierarchy(c, result, new HashSet<>());
        result.sort(Comparator.comparing(this::methodSortKey));
        return result;
    }

    private void collectMethodsInHierarchy(ImClass c, List<ImMethod> out, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        out.addAll(c.getMethods());
        List<ImClassType> superClasses = new ArrayList<>(c.getSuperClasses());
        superClasses.sort(Comparator.comparing(t -> classSortKey(t.getClassDef())));
        for (ImClassType sc : superClasses) {
            collectMethodsInHierarchy(sc.getClassDef(), out, visited);
        }
    }

    private ImMethod chooseBestImplementationForClass(ImClass receiverClass, List<ImMethod> candidates) {
        List<ImMethod> concrete = new ArrayList<>();
        for (ImMethod m : candidates) {
            if (!m.getIsAbstract() && m.getImplementation() != null) {
                concrete.add(m);
            }
        }
        if (concrete.isEmpty()) {
            return null;
        }
        concrete.sort((a, b) -> compareDispatchCandidates(receiverClass, a, b));
        return concrete.get(0);
    }

    private int compareDispatchCandidates(ImClass receiverClass, ImMethod a, ImMethod b) {
        boolean aLocal = isImplementationFromClass(a, receiverClass);
        boolean bLocal = isImplementationFromClass(b, receiverClass);
        if (aLocal != bLocal) {
            return aLocal ? -1 : 1;
        }
        int aDist = classDistance(receiverClass, a.attrClass());
        int bDist = classDistance(receiverClass, b.attrClass());
        if (aDist != bDist) {
            return Integer.compare(aDist, bDist);
        }
        boolean aNoOp = isNoOpImplementation(a);
        boolean bNoOp = isNoOpImplementation(b);
        if (aNoOp != bNoOp) {
            return aNoOp ? 1 : -1;
        }
        return methodSortKey(a).compareTo(methodSortKey(b));
    }

    private boolean isImplementationFromClass(ImMethod method, ImClass ownerClass) {
        if (method == null || ownerClass == null || method.getImplementation() == null) {
            return false;
        }
        return method.getImplementation().getName().startsWith(ownerClass.getName() + "_");
    }

    private boolean isNoOpImplementation(ImMethod method) {
        return method != null
            && method.getImplementation() != null
            && method.getImplementation().getName().contains("NoOpState_");
    }

    private int classDistance(ImClass from, ImClass to) {
        if (from == null || to == null) {
            return Integer.MAX_VALUE;
        }
        if (from == to) {
            return 0;
        }
        ArrayDeque<ImClass> queue = new ArrayDeque<>();
        Map<ImClass, Integer> dist = new HashMap<>();
        queue.add(from);
        dist.put(from, 0);
        while (!queue.isEmpty()) {
            ImClass current = queue.removeFirst();
            int currentDist = dist.get(current);
            List<ImClassType> superClasses = new ArrayList<>(current.getSuperClasses());
            superClasses.sort(Comparator.comparing(t -> classSortKey(t.getClassDef())));
            for (ImClassType sc : superClasses) {
                ImClass next = sc.getClassDef();
                if (next == null || dist.containsKey(next)) {
                    continue;
                }
                int nextDist = currentDist + 1;
                if (next == to) {
                    return nextDist;
                }
                dist.put(next, nextDist);
                queue.add(next);
            }
        }
        return Integer.MAX_VALUE;
    }

    private void debugDispatchGroup(ImClass receiverClass, String key, Set<String> slotNames, List<ImMethod> groupMethods, ImMethod chosen) {
        if (!DEBUG_LUA_DISPATCH && !isSuspiciousGroup(slotNames, groupMethods, chosen)) {
            return;
        }
        String chosenImpl = chosen != null && chosen.getImplementation() != null ? chosen.getImplementation().getName() : "null";
        StringBuilder candidates = new StringBuilder();
        List<ImMethod> sorted = new ArrayList<>(groupMethods);
        sorted.sort(Comparator.comparing(this::methodSortKey));
        for (ImMethod m : sorted) {
            String impl = m.getImplementation() != null ? m.getImplementation().getName() : "null";
            if (candidates.length() > 0) {
                candidates.append("; ");
            }
            candidates.append(m.getName()).append("->").append(impl).append("@").append(classSortKey(m.attrClass()));
        }
        System.err.println("[LuaDispatch] class=" + classSortKey(receiverClass)
            + " key=" + key
            + " slots=" + slotNames
            + " chosen=" + chosenImpl
            + " candidates=[" + candidates + "]");
        if (DEBUG_LUA_DISPATCH) {
            String line = "[LuaDispatch] class=" + classSortKey(receiverClass)
                + " key=" + key
                + " slots=" + slotNames
                + " chosen=" + chosenImpl
                + " candidates=[" + candidates + "]"
                + System.lineSeparator();
            try {
                Files.writeString(Path.of("C:/Users/Frotty/Documents/GitHub/WurstScript/lua-dispatch-debug.log"),
                    line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isSuspiciousGroup(Set<String> slotNames, List<ImMethod> groupMethods, ImMethod chosen) {
        if (slotNames.size() > 1) {
            return true;
        }
        boolean hasNonNoOp = false;
        for (ImMethod m : groupMethods) {
            if (!isNoOpImplementation(m) && m.getImplementation() != null) {
                hasNonNoOp = true;
                break;
            }
        }
        return hasNonNoOp && isNoOpImplementation(chosen);
    }

    private String methodSortKey(ImMethod m) {
        String owner = classSortKey(m.attrClass());
        String impl = m.getImplementation() != null ? m.getImplementation().getName() : "";
        return owner + "|" + m.getName() + "|" + impl;
    }

    private String semanticNameFromMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return "";
        }
        int lastUnderscore = methodName.lastIndexOf('_');
        if (lastUnderscore >= 0 && lastUnderscore + 1 < methodName.length()) {
            return methodName.substring(lastUnderscore + 1);
        }
        return methodName;
    }

    private String classSortKey(ImClass c) {
        if (c == null) {
            return "";
        }
        return c.getName();
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
        return null;
    }
}
