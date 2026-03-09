package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaVariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LuaNatives {

    private static final Map<String, Consumer<LuaFunction>> nativeCodes = new HashMap<>();
    private static final String[] HASHTABLE_HANDLE_SAVE_NAMES = {
        "SavePlayerHandle", "SaveWidgetHandle", "SaveDestructableHandle", "SaveItemHandle", "SaveUnitHandle",
        "SaveAbilityHandle", "SaveTimerHandle", "SaveTriggerHandle", "SaveTriggerConditionHandle",
        "SaveTriggerActionHandle", "SaveTriggerEventHandle", "SaveForceHandle", "SaveGroupHandle",
        "SaveLocationHandle", "SaveRectHandle", "SaveBooleanExprHandle", "SaveSoundHandle", "SaveEffectHandle",
        "SaveUnitPoolHandle", "SaveItemPoolHandle", "SaveQuestHandle", "SaveQuestItemHandle",
        "SaveDefeatConditionHandle", "SaveTimerDialogHandle", "SaveLeaderboardHandle", "SaveMultiboardHandle",
        "SaveMultiboardItemHandle", "SaveTrackableHandle", "SaveDialogHandle", "SaveButtonHandle",
        "SaveTextTagHandle", "SaveLightningHandle", "SaveImageHandle", "SaveUbersplatHandle", "SaveRegionHandle",
        "SaveFogStateHandle", "SaveFogModifierHandle", "SaveAgentHandle", "SaveHashtableHandle", "SaveFrameHandle"
    };
    private static final String[] HASHTABLE_HANDLE_LOAD_NAMES = {
        "LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle",
        "LoadAbilityHandle", "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle",
        "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle", "LoadGroupHandle",
        "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle",
        "LoadUnitPoolHandle", "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle",
        "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle", "LoadMultiboardHandle",
        "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle",
        "LoadTextTagHandle", "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle",
        "LoadFogStateHandle", "LoadFogModifierHandle", "LoadHashtableHandle", "LoadFrameHandle"
    };

    static {
        addNative("testSuccess", f -> {
            f.getBody().add(LuaAst.LuaLiteral("print(\"testSuccess\")"));
            f.getBody().add(LuaAst.LuaLiteral("os.exit()"));
        });
        addNative(Arrays.asList("print", "println"), f -> {
            LuaVariable p = LuaAst.LuaVariable("text", LuaAst.LuaNoExpr());
            f.getParams().add(p);
            f.getBody().add(LuaAst.LuaExprFunctionCallByName("print",
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(p))));
        });

        addNative("testFail", f -> {
            f.getParams().add(LuaAst.LuaVariable("msg", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("print(\"error: \" .. msg)"));
            f.getBody().add(LuaAst.LuaLiteral("error()"));
        });


        addNative("Sin", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.sin(x)"));
        });

        addNative("Cos", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.cos(x)"));
        });

        addNative(Arrays.asList("I2S", "R2S"), f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return tostring(x)"));
        });

        addNative(Collections.singletonList("S2I"), f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local m = string.match(tostring(x), \"^[%+%-]?%d+\")"));
            f.getBody().add(LuaAst.LuaLiteral("if m then return tonumber(m) else return 0 end"));
        });

        addNative("Player", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return { id = x }"));
        });

        addNative("GetPlayerId", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return x.id"));
        });

        addNative("GetRandomReal", f -> {
            f.getParams().add(LuaAst.LuaVariable("l", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return l + math.random() * (h - l)"));
        });

        addNative("GetRandomInt", f -> {
            f.getParams().add(LuaAst.LuaVariable("l", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.random(l,h)"));
        });

        addNative("SquareRoot", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.sqrt(x)"));
        });

        addNative("CreateTrigger", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return { actions = {}}"));
        });

        addNative("TriggerAddAction", f -> {
            f.getParams().add(LuaAst.LuaVariable("t", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("table.insert(t.actions, c)"));
        });

        addNative("TriggerEvaluate", f -> {
            f.getParams().add(LuaAst.LuaVariable("t", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("for i,a in ipairs(t.actions) do a() end"));
            f.getBody().add(LuaAst.LuaLiteral("return true"));
        });

        addNative("R2I", f -> {
            f.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return math.modf(x)"));
        });

        addNative("__wurst_GetEnumPlayer", f -> {
            // Prefer the native enum player when inside an active native ForForce callback.
            // This preserves Jass semantics for nested enumerations.
            f.getBody().add(LuaAst.LuaLiteral("if GetEnumPlayer ~= nil then"));
            f.getBody().add(LuaAst.LuaLiteral("    local p = GetEnumPlayer()"));
            f.getBody().add(LuaAst.LuaLiteral("    if p ~= nil then return p end"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("if __wurst_enumPlayer_override ~= nil then return __wurst_enumPlayer_override end"));
            f.getBody().add(LuaAst.LuaLiteral("return nil"));
        });

        addNative("__wurst_ForForce", f -> {
            f.getParams().add(LuaAst.LuaVariable("whichForce", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("callback", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if ForForce == nil then return end"));
            f.getBody().add(LuaAst.LuaLiteral("local players = {}"));
            f.getBody().add(LuaAst.LuaLiteral("local count = 0"));
            f.getBody().add(LuaAst.LuaLiteral("local prev = __wurst_enumPlayer_override"));
            f.getBody().add(LuaAst.LuaLiteral("ForForce(whichForce, function()"));
            f.getBody().add(LuaAst.LuaLiteral("    count = count + 1"));
            f.getBody().add(LuaAst.LuaLiteral("    players[count] = __wurst_GetEnumPlayer()"));
            f.getBody().add(LuaAst.LuaLiteral("end)"));
            f.getBody().add(LuaAst.LuaLiteral("for i = 1, count do"));
            f.getBody().add(LuaAst.LuaLiteral("    __wurst_enumPlayer_override = players[i]"));
            f.getBody().add(LuaAst.LuaLiteral("    callback()"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("__wurst_enumPlayer_override = prev"));
        });

        addNative("__wurst_GetEnumUnit", f -> {
            f.getBody().add(LuaAst.LuaLiteral("if GetEnumUnit ~= nil then"));
            f.getBody().add(LuaAst.LuaLiteral("    local u = GetEnumUnit()"));
            f.getBody().add(LuaAst.LuaLiteral("    if u ~= nil then return u end"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("if __wurst_enumUnit_override ~= nil then return __wurst_enumUnit_override end"));
            f.getBody().add(LuaAst.LuaLiteral("return nil"));
        });

        addNative("__wurst_ForGroup", f -> {
            f.getParams().add(LuaAst.LuaVariable("whichGroup", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("callback", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if ForGroup == nil then return end"));
            f.getBody().add(LuaAst.LuaLiteral("local units = {}"));
            f.getBody().add(LuaAst.LuaLiteral("local count = 0"));
            f.getBody().add(LuaAst.LuaLiteral("local prev = __wurst_enumUnit_override"));
            f.getBody().add(LuaAst.LuaLiteral("ForGroup(whichGroup, function()"));
            f.getBody().add(LuaAst.LuaLiteral("    count = count + 1"));
            f.getBody().add(LuaAst.LuaLiteral("    units[count] = __wurst_GetEnumUnit()"));
            f.getBody().add(LuaAst.LuaLiteral("end)"));
            f.getBody().add(LuaAst.LuaLiteral("for i = 1, count do"));
            f.getBody().add(LuaAst.LuaLiteral("    __wurst_enumUnit_override = units[i]"));
            f.getBody().add(LuaAst.LuaLiteral("    callback()"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("__wurst_enumUnit_override = prev"));
        });

        addNative("__wurst_GetEnumItem", f -> {
            f.getBody().add(LuaAst.LuaLiteral("if GetEnumItem ~= nil then"));
            f.getBody().add(LuaAst.LuaLiteral("    local it = GetEnumItem()"));
            f.getBody().add(LuaAst.LuaLiteral("    if it ~= nil then return it end"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("if __wurst_enumItem_override ~= nil then return __wurst_enumItem_override end"));
            f.getBody().add(LuaAst.LuaLiteral("return nil"));
        });

        addNative("__wurst_EnumItemsInRect", f -> {
            f.getParams().add(LuaAst.LuaVariable("r", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("filter", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("actionFunc", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if EnumItemsInRect == nil then return end"));
            f.getBody().add(LuaAst.LuaLiteral("local items = {}"));
            f.getBody().add(LuaAst.LuaLiteral("local count = 0"));
            f.getBody().add(LuaAst.LuaLiteral("local prev = __wurst_enumItem_override"));
            f.getBody().add(LuaAst.LuaLiteral("EnumItemsInRect(r, filter, function()"));
            f.getBody().add(LuaAst.LuaLiteral("    count = count + 1"));
            f.getBody().add(LuaAst.LuaLiteral("    items[count] = __wurst_GetEnumItem()"));
            f.getBody().add(LuaAst.LuaLiteral("end)"));
            f.getBody().add(LuaAst.LuaLiteral("for i = 1, count do"));
            f.getBody().add(LuaAst.LuaLiteral("    __wurst_enumItem_override = items[i]"));
            f.getBody().add(LuaAst.LuaLiteral("    actionFunc()"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("__wurst_enumItem_override = prev"));
        });

        addNative("__wurst_GetEnumDestructable", f -> {
            f.getBody().add(LuaAst.LuaLiteral("if GetEnumDestructable ~= nil then"));
            f.getBody().add(LuaAst.LuaLiteral("    local d = GetEnumDestructable()"));
            f.getBody().add(LuaAst.LuaLiteral("    if d ~= nil then return d end"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("if __wurst_enumDestructable_override ~= nil then return __wurst_enumDestructable_override end"));
            f.getBody().add(LuaAst.LuaLiteral("return nil"));
        });

        addNative("__wurst_EnumDestructablesInRect", f -> {
            f.getParams().add(LuaAst.LuaVariable("r", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("filter", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("actionFunc", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if EnumDestructablesInRect == nil then return end"));
            f.getBody().add(LuaAst.LuaLiteral("local dests = {}"));
            f.getBody().add(LuaAst.LuaLiteral("local count = 0"));
            f.getBody().add(LuaAst.LuaLiteral("local prev = __wurst_enumDestructable_override"));
            f.getBody().add(LuaAst.LuaLiteral("EnumDestructablesInRect(r, filter, function()"));
            f.getBody().add(LuaAst.LuaLiteral("    count = count + 1"));
            f.getBody().add(LuaAst.LuaLiteral("    dests[count] = __wurst_GetEnumDestructable()"));
            f.getBody().add(LuaAst.LuaLiteral("end)"));
            f.getBody().add(LuaAst.LuaLiteral("for i = 1, count do"));
            f.getBody().add(LuaAst.LuaLiteral("    __wurst_enumDestructable_override = dests[i]"));
            f.getBody().add(LuaAst.LuaLiteral("    actionFunc()"));
            f.getBody().add(LuaAst.LuaLiteral("end"));
            f.getBody().add(LuaAst.LuaLiteral("__wurst_enumDestructable_override = prev"));
        });

        addNative(Arrays.asList("InitHashtable", "__wurst_InitHashtable"), f ->
            f.getBody().add(LuaAst.LuaLiteral("return { __wurst_ht_int = {}, __wurst_ht_bool = {}, __wurst_ht_real = {}, __wurst_ht_str = {}, __wurst_ht_handle = {} }")));

        addNative(Arrays.asList("SaveInteger", "__wurst_SaveInteger"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_int"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil then t = {}; h.__wurst_ht_int = t end"));
            f.getBody().add(LuaAst.LuaLiteral("if not t[p] then t[p] = {} end t[p][c] = i"));
        });
        addNative(Arrays.asList("SaveBoolean", "__wurst_SaveBoolean"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_bool"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil then t = {}; h.__wurst_ht_bool = t end"));
            f.getBody().add(LuaAst.LuaLiteral("if not t[p] then t[p] = {} end t[p][c] = i"));
        });
        addNative(Arrays.asList("SaveReal", "__wurst_SaveReal"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_real"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil then t = {}; h.__wurst_ht_real = t end"));
            f.getBody().add(LuaAst.LuaLiteral("if not t[p] then t[p] = {} end t[p][c] = i"));
        });
        addNative(Arrays.asList("SaveStr", "__wurst_SaveStr"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_str"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil then t = {}; h.__wurst_ht_str = t end"));
            f.getBody().add(LuaAst.LuaLiteral("if not t[p] then t[p] = {} end t[p][c] = i"));
        });
        addNative(withWurstPrefix(HASHTABLE_HANDLE_SAVE_NAMES), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("i", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_handle"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil then t = {}; h.__wurst_ht_handle = t end"));
            f.getBody().add(LuaAst.LuaLiteral("if not t[p] then t[p] = {} end t[p][c] = i"));
        });

        addNative(Arrays.asList("LoadInteger", "__wurst_LoadInteger"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_int"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil or t[p] == nil then return 0 end"));
            f.getBody().add(LuaAst.LuaLiteral("local v = t[p][c]"));
            f.getBody().add(LuaAst.LuaLiteral("if v == nil then return 0 end"));
            f.getBody().add(LuaAst.LuaLiteral("return v"));
        });

        addNative(Arrays.asList("LoadBoolean", "__wurst_LoadBoolean"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_bool"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil or t[p] == nil then return false end"));
            f.getBody().add(LuaAst.LuaLiteral("local v = t[p][c]"));
            f.getBody().add(LuaAst.LuaLiteral("if v == nil then return false end"));
            f.getBody().add(LuaAst.LuaLiteral("return v"));
        });

        addNative(Arrays.asList("LoadReal", "__wurst_LoadReal"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_real"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil or t[p] == nil then return 0.0 end"));
            f.getBody().add(LuaAst.LuaLiteral("local v = t[p][c]"));
            f.getBody().add(LuaAst.LuaLiteral("if v == nil then return 0.0 end"));
            f.getBody().add(LuaAst.LuaLiteral("return v"));
        });

        addNative(Arrays.asList("LoadStr", "__wurst_LoadStr"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_str"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil or t[p] == nil then return nil end"));
            f.getBody().add(LuaAst.LuaLiteral("return t[p][c]"));
        });
        addNative(withWurstPrefix(HASHTABLE_HANDLE_LOAD_NAMES), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_handle"));
            f.getBody().add(LuaAst.LuaLiteral("if t == nil or t[p] == nil then return nil end"));
            f.getBody().add(LuaAst.LuaLiteral("return t[p][c]"));
        });

        addNative(Arrays.asList("HaveSavedInteger", "__wurst_HaveSavedInteger"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_int"));
            f.getBody().add(LuaAst.LuaLiteral("return t ~= nil and t[p] ~= nil and t[p][c] ~= nil"));
        });
        addNative(Arrays.asList("HaveSavedBoolean", "__wurst_HaveSavedBoolean"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_bool"));
            f.getBody().add(LuaAst.LuaLiteral("return t ~= nil and t[p] ~= nil and t[p][c] ~= nil"));
        });
        addNative(Arrays.asList("HaveSavedReal", "__wurst_HaveSavedReal"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_real"));
            f.getBody().add(LuaAst.LuaLiteral("return t ~= nil and t[p] ~= nil and t[p][c] ~= nil"));
        });
        addNative(Arrays.asList("HaveSavedString", "__wurst_HaveSavedString"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_str"));
            f.getBody().add(LuaAst.LuaLiteral("return t ~= nil and t[p] ~= nil and t[p][c] ~= nil"));
        });
        addNative(Arrays.asList("HaveSavedHandle", "__wurst_HaveSavedHandle"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_handle"));
            f.getBody().add(LuaAst.LuaLiteral("return t ~= nil and t[p] ~= nil and t[p][c] ~= nil"));
        });

        addNative(Arrays.asList("FlushChildHashtable", "__wurst_FlushChildHashtable"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("if h.__wurst_ht_int then h.__wurst_ht_int[p] = nil end"));
            f.getBody().add(LuaAst.LuaLiteral("if h.__wurst_ht_bool then h.__wurst_ht_bool[p] = nil end"));
            f.getBody().add(LuaAst.LuaLiteral("if h.__wurst_ht_real then h.__wurst_ht_real[p] = nil end"));
            f.getBody().add(LuaAst.LuaLiteral("if h.__wurst_ht_str then h.__wurst_ht_str[p] = nil end"));
            f.getBody().add(LuaAst.LuaLiteral("if h.__wurst_ht_handle then h.__wurst_ht_handle[p] = nil end"));
        });

        addNative(Arrays.asList("FlushParentHashtable", "__wurst_FlushParentHashtable"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("h.__wurst_ht_int = {}"));
            f.getBody().add(LuaAst.LuaLiteral("h.__wurst_ht_bool = {}"));
            f.getBody().add(LuaAst.LuaLiteral("h.__wurst_ht_real = {}"));
            f.getBody().add(LuaAst.LuaLiteral("h.__wurst_ht_str = {}"));
            f.getBody().add(LuaAst.LuaLiteral("h.__wurst_ht_handle = {}"));
        });

        addNative(Arrays.asList("RemoveSavedInteger", "__wurst_RemoveSavedInteger"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_int"));
            f.getBody().add(LuaAst.LuaLiteral("if t ~= nil and t[p] then t[p][c] = nil end"));
        });
        addNative(Arrays.asList("RemoveSavedBoolean", "__wurst_RemoveSavedBoolean"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_bool"));
            f.getBody().add(LuaAst.LuaLiteral("if t ~= nil and t[p] then t[p][c] = nil end"));
        });
        addNative(Arrays.asList("RemoveSavedReal", "__wurst_RemoveSavedReal"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_real"));
            f.getBody().add(LuaAst.LuaLiteral("if t ~= nil and t[p] then t[p][c] = nil end"));
        });
        addNative(Arrays.asList("RemoveSavedString", "__wurst_RemoveSavedString"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_str"));
            f.getBody().add(LuaAst.LuaLiteral("if t ~= nil and t[p] then t[p][c] = nil end"));
        });
        addNative(Arrays.asList("RemoveSavedHandle", "__wurst_RemoveSavedHandle"), f -> {
            f.getParams().add(LuaAst.LuaVariable("h", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("p", LuaAst.LuaNoExpr()));
            f.getParams().add(LuaAst.LuaVariable("c", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("local t = h.__wurst_ht_handle"));
            f.getBody().add(LuaAst.LuaLiteral("if t ~= nil and t[p] then t[p][c] = nil end"));
        });

        addNative("typeIdToTypeName", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return \"\""));
        });

        addNative("maxTypeId", f -> {
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

        addNative("instanceCount", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

        addNative("maxInstanceCount", f -> {
            f.getParams().add(LuaAst.LuaVariable("typeId", LuaAst.LuaNoExpr()));
            f.getBody().add(LuaAst.LuaLiteral("return 0"));
        });

    }

    private static void addNative(String name, Consumer<LuaFunction> g) {
        nativeCodes.put(name, f -> {
            f.getParams().removeAll();
            g.accept(f);
        });
    }

    private static void addNative(Iterable<String> names, Consumer<LuaFunction> g) {
        for (String name : names) {
            addNative(name, g);
        }
    }

    private static Iterable<String> withWurstPrefix(String[] names) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String name : names) {
            result.add(name);
            result.add("__wurst_" + name);
        }
        return result;
    }

    public static void get(LuaFunction f) {
        nativeCodes.getOrDefault(f.getName(), ff -> {
            // generate a runtime exception
            f.getBody().add(LuaAst.LuaLiteral("error(\"The native '" + ff.getName() + "' is not implemented.\")"));
        }).accept(f);
    }

}
