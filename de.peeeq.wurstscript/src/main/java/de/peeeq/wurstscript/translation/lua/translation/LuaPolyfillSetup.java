package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.luaAst.*;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_SUPERTYPES;

/**
 * Builds and registers the Wurst Lua infrastructure functions that are always
 * emitted into the generated script, regardless of what user code looks like.
 */
class LuaPolyfillSetup {

    private LuaPolyfillSetup() {}

    static void createArrayInitFunction(LuaTranslator tr) {
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

        tr.arrayInitFunction.getParams().add(LuaAst.LuaVariable("d", LuaAst.LuaNoExpr()));
        for (String c : code) {
            tr.arrayInitFunction.getBody().add(LuaAst.LuaLiteral(c));
        }
        tr.luaModel.add(tr.arrayInitFunction);
    }

    static void createStringConcatFunction(LuaTranslator tr) {
        String[] code = {
            "if x then",
            "    if y then return x .. y else return x end",
            "else",
            "    return y",
            "end"
        };

        tr.stringConcatFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.stringConcatFunction.getParams().add(LuaAst.LuaVariable("y", LuaAst.LuaNoExpr()));
        for (String c : code) {
            tr.stringConcatFunction.getBody().add(LuaAst.LuaLiteral(c));
        }
        tr.luaModel.add(tr.stringConcatFunction);
    }

    static void createInstanceOfFunction(LuaTranslator tr) {
        tr.instanceOfFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.instanceOfFunction.getParams().add(LuaAst.LuaVariable("A", LuaAst.LuaNoExpr()));
        tr.instanceOfFunction.getBody().add(LuaAst.LuaLiteral("return x ~= nil and x." + WURST_SUPERTYPES + "[A]"));
        tr.luaModel.add(tr.instanceOfFunction);
    }

    static void createObjectIndexFunctions(LuaTranslator tr) {
        LuaVariable objectIndexMap = LuaAst.LuaVariable("__wurst_objectIndexMap", LuaAst.LuaExprNull());
        tr.luaModel.add(objectIndexMap);
        tr.deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(objectIndexMap), LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0"))
        ))));

        LuaVariable numberWrapperMap = LuaAst.LuaVariable("__wurst_number_wrapper_map", LuaAst.LuaExprNull());
        tr.luaModel.add(numberWrapperMap);
        tr.deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(numberWrapperMap), LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0"))
        ))));

        {
            String[] code = {
                "if x == nil then",
                "    return 0",
                "end",
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

            tr.toIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                tr.toIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            tr.luaModel.add(tr.toIndexFunction);
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

            tr.fromIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                tr.fromIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            tr.luaModel.add(tr.fromIndexFunction);
        }
    }

    static void createStringIndexFunctions(LuaTranslator tr) {
        LuaVariable map = LuaAst.LuaVariable("__wurst_string_index_map", LuaAst.LuaExprNull());
        tr.luaModel.add(map);
        tr.deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(map), LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("counter", LuaAst.LuaExprIntVal("0")),
            LuaAst.LuaTableNamedField("byString", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields())),
            LuaAst.LuaTableNamedField("byIndex", LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()))
        ))));

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

            tr.stringToIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                tr.stringToIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            tr.luaModel.add(tr.stringToIndexFunction);
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

            tr.stringFromIndexFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
            for (String c : code) {
                tr.stringFromIndexFunction.getBody().add(LuaAst.LuaLiteral(c));
            }
            tr.luaModel.add(tr.stringFromIndexFunction);
        }
    }

    static void createEnsureTypeFunctions(LuaTranslator tr) {
        tr.ensureIntFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.ensureIntFunction.getBody().add(LuaAst.LuaLiteral("local n = tonumber(x)"));
        tr.ensureIntFunction.getBody().add(LuaAst.LuaLiteral("if n == nil then return 0 end"));
        tr.ensureIntFunction.getBody().add(LuaAst.LuaLiteral("local i = math.tointeger(n)"));
        tr.ensureIntFunction.getBody().add(LuaAst.LuaLiteral("if i == nil then return 0 end"));
        tr.ensureIntFunction.getBody().add(LuaAst.LuaLiteral("return i"));
        tr.luaModel.add(tr.ensureIntFunction);

        tr.ensureBoolFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.ensureBoolFunction.getBody().add(LuaAst.LuaLiteral("if x == nil then return false end"));
        tr.ensureBoolFunction.getBody().add(LuaAst.LuaLiteral("return x"));
        tr.luaModel.add(tr.ensureBoolFunction);

        tr.ensureRealFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.ensureRealFunction.getBody().add(LuaAst.LuaLiteral("local n = tonumber(x)"));
        tr.ensureRealFunction.getBody().add(LuaAst.LuaLiteral("if n == nil then return 0.0 end"));
        tr.ensureRealFunction.getBody().add(LuaAst.LuaLiteral("return n"));
        tr.luaModel.add(tr.ensureRealFunction);

        tr.ensureStrFunction.getParams().add(LuaAst.LuaVariable("x", LuaAst.LuaNoExpr()));
        tr.ensureStrFunction.getBody().add(LuaAst.LuaLiteral("if x == nil then return \"\" end"));
        tr.ensureStrFunction.getBody().add(LuaAst.LuaLiteral("return tostring(x)"));
        tr.luaModel.add(tr.ensureStrFunction);
    }
}
