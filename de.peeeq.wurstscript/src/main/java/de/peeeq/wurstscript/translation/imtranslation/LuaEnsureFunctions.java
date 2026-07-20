package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.Collections;
import java.util.List;

/**
 * Builds portable IM-level bodies for the Lua-target ensureType/stringConcat
 * helpers ({@link ImTranslator#ensureIntFunc} and friends).
 *
 * <p>These used to be {@code IS_NATIVE} stubs with an empty IM body; their
 * actual Lua implementation was supplied only by an unrelated, separately
 * created Lua-AST-level polyfill (in {@code LuaTranslator}/{@code
 * LuaPolyfillSetup}) that happened to pick the exact same Lua global name.
 * Nothing enforced that link - it worked only because the polyfill was
 * always emitted first and native functions skip Wurst's own unique-name
 * mangling (see {@code LuaTranslator.luaFunc}), so the guarded-redefinition
 * Lua code these natives print ("if intEnsure then ... else intEnsure =
 * function ... error('not implemented') ... end end") always found the slot
 * already taken and never actually ran its own (broken) body. This is the
 * same class of bug already found and fixed once for stringConcat's call
 * sites specifically (see the identity-based dispatch in {@code
 * lua.translation.ExprTranslation#translate(ImFunctionCall, ...)} and the
 * {@code userFunctionNamedStringConcatDoesNotBreakConcatenation} regression
 * test) - this removes the coincidence at the source instead of routing
 * around it.
 *
 * <p>Giving these functions real, portable IM bodies also makes them
 * inlinable and dead-code-eliminable like any other Wurst-internal
 * function, instead of always-emitted opaque Lua source - the same
 * treatment already applied to div/mod (see {@link LuaNativeLowering}).
 */
final class LuaEnsureFunctions {
    private static final de.peeeq.wurstscript.ast.Element TRACE = de.peeeq.wurstscript.ast.Ast.NoExpr();

    private LuaEnsureFunctions() {
    }

    /** local n = rawToNumberInt(x); local result = 0; if n ~= nil then local i = rawToInteger(n); if i ~= nil then result = i end end; return result */
    static ImFunction buildEnsureInt(List<ImFunction> out) {
        ImType intType = TypesHelper.imInt();
        ImFunction rawToNumber = rawNative("__wurst_rawToNumberInt", intType, 1);
        ImFunction rawToInteger = rawNative("__wurst_rawToInteger", intType, 1);
        out.add(rawToNumber);
        out.add(rawToInteger);

        ImVar x = JassIm.ImVar(TRACE, intType.copy(), "x", false);
        ImVar n = JassIm.ImVar(TRACE, intType.copy(), "n", false);
        ImVar i = JassIm.ImVar(TRACE, intType.copy(), "i", false);
        ImVar result = JassIm.ImVar(TRACE, intType.copy(), "result", false);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(n), call(rawToNumber, JassIm.ImVarAccess(x))),
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImIntVal(0)),
            JassIm.ImIf(TRACE, notNull(n),
                JassIm.ImStmts(
                    JassIm.ImSet(TRACE, JassIm.ImVarAccess(i), call(rawToInteger, JassIm.ImVarAccess(n))),
                    JassIm.ImIf(TRACE, notNull(i),
                        JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImVarAccess(i))),
                        JassIm.ImStmts())
                ),
                JassIm.ImStmts()),
            JassIm.ImReturn(TRACE, JassIm.ImVarAccess(result))
        );
        ImFunction f = JassIm.ImFunction(TRACE, "__wurst_ensureInt", JassIm.ImTypeVars(), JassIm.ImVars(x), intType.copy(),
            JassIm.ImVars(n, i, result), body, Collections.emptyList());
        out.add(f);
        return f;
    }

    /** local result = false; if x ~= nil then result = x end; return result */
    static ImFunction buildEnsureBool(List<ImFunction> out) {
        ImType boolType = TypesHelper.imBool();
        ImVar x = JassIm.ImVar(TRACE, boolType.copy(), "x", false);
        ImVar result = JassIm.ImVar(TRACE, boolType.copy(), "result", false);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImBoolVal(false)),
            JassIm.ImIf(TRACE, notNull(x),
                JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImVarAccess(x))),
                JassIm.ImStmts()),
            JassIm.ImReturn(TRACE, JassIm.ImVarAccess(result))
        );
        ImFunction f = JassIm.ImFunction(TRACE, "__wurst_ensureBool", JassIm.ImTypeVars(), JassIm.ImVars(x), boolType.copy(),
            JassIm.ImVars(result), body, Collections.emptyList());
        out.add(f);
        return f;
    }

    /** local n = rawToNumberReal(x); local result = 0.0; if n ~= nil then result = n end; return result */
    static ImFunction buildEnsureReal(List<ImFunction> out) {
        ImType realType = TypesHelper.imReal();
        ImFunction rawToNumber = rawNative("__wurst_rawToNumberReal", realType, 1);
        out.add(rawToNumber);

        ImVar x = JassIm.ImVar(TRACE, realType.copy(), "x", false);
        ImVar n = JassIm.ImVar(TRACE, realType.copy(), "n", false);
        ImVar result = JassIm.ImVar(TRACE, realType.copy(), "result", false);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(n), call(rawToNumber, JassIm.ImVarAccess(x))),
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImRealVal("0.")),
            JassIm.ImIf(TRACE, notNull(n),
                JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImVarAccess(n))),
                JassIm.ImStmts()),
            JassIm.ImReturn(TRACE, JassIm.ImVarAccess(result))
        );
        ImFunction f = JassIm.ImFunction(TRACE, "__wurst_ensureReal", JassIm.ImTypeVars(), JassIm.ImVars(x), realType.copy(),
            JassIm.ImVars(n, result), body, Collections.emptyList());
        out.add(f);
        return f;
    }

    /** local result = ""; if x ~= nil then result = rawToString(x) end; return result */
    static ImFunction buildEnsureStr(List<ImFunction> out) {
        ImType stringType = TypesHelper.imString();
        ImFunction rawToString = rawNative("__wurst_rawToString", stringType, 1);
        out.add(rawToString);

        ImVar x = JassIm.ImVar(TRACE, stringType.copy(), "x", false);
        ImVar result = JassIm.ImVar(TRACE, stringType.copy(), "result", false);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImStringVal("")),
            JassIm.ImIf(TRACE, notNull(x),
                JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), call(rawToString, JassIm.ImVarAccess(x)))),
                JassIm.ImStmts()),
            JassIm.ImReturn(TRACE, JassIm.ImVarAccess(result))
        );
        ImFunction f = JassIm.ImFunction(TRACE, "__wurst_ensureStr", JassIm.ImTypeVars(), JassIm.ImVars(x), stringType.copy(),
            JassIm.ImVars(result), body, Collections.emptyList());
        out.add(f);
        return f;
    }

    /**
     * if x ~= nil then
     *     if y ~= nil then result = rawConcat(x, y) else result = x end
     * else result = y end
     * return result
     */
    static ImFunction buildStringConcat(List<ImFunction> out) {
        ImType stringType = TypesHelper.imString();
        ImFunction rawConcat = rawNative("__wurst_rawConcat", stringType, 2);
        out.add(rawConcat);

        ImVar x = JassIm.ImVar(TRACE, stringType.copy(), "x", false);
        ImVar y = JassIm.ImVar(TRACE, stringType.copy(), "y", false);
        ImVar result = JassIm.ImVar(TRACE, stringType.copy(), "result", false);

        ImStmts body = JassIm.ImStmts(
            JassIm.ImIf(TRACE, notNull(x),
                JassIm.ImStmts(JassIm.ImIf(TRACE, notNull(y),
                    JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), call(rawConcat, JassIm.ImVarAccess(x), JassIm.ImVarAccess(y)))),
                    JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImVarAccess(x))))),
                JassIm.ImStmts(JassIm.ImSet(TRACE, JassIm.ImVarAccess(result), JassIm.ImVarAccess(y)))),
            JassIm.ImReturn(TRACE, JassIm.ImVarAccess(result))
        );
        ImFunction f = JassIm.ImFunction(TRACE, "__wurst_stringConcat", JassIm.ImTypeVars(), JassIm.ImVars(x, y), stringType.copy(),
            JassIm.ImVars(result), body, Collections.emptyList());
        out.add(f);
        return f;
    }

    /** A native leaf with {@code paramCount} params and a return, all of the same type. Body supplied by LuaNatives. */
    private static ImFunction rawNative(String name, ImType type, int paramCount) {
        String[] names = {"x", "y"};
        ImVars params = JassIm.ImVars();
        for (int i = 0; i < paramCount; i++) {
            params.add(JassIm.ImVar(TRACE, type.copy(), names[i], false));
        }
        return JassIm.ImFunction(TRACE, name, JassIm.ImTypeVars(), params, type.copy(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
    }

    /**
     * {@code v ~= nil}, tagged as a null of {@link ImAnyType} rather than
     * v's own declared type. This matters specifically for string: {@code
     * EliminateLocalTypes#transformProgram} runs after these functions are
     * built and unconditionally rewrites every string-typed {@code ImNull}
     * node in the program to {@code ImStringVal("")} (Wurst's "null string
     * == empty string" convention for ordinary user code) - tagging with the
     * declared type here would silently turn this into an
     * {@code x ~= ""} check, so a genuinely-nil Lua value (e.g. an
     * uninitialized bound-generic string field) would read as "not nil" and
     * skip normalization. An ImAnyType-tagged null is exempt from that
     * rewrite while still printing as plain Lua {@code nil} either way (see
     * {@code lua.translation.ExprTranslation#translate(ImNull, ...)}), and
     * the comparison's Lua translation only ever looks at the *left*
     * operand's type, so this has no effect on the emitted code.
     */
    private static ImExpr notNull(ImVar v) {
        return JassIm.ImOperatorCall(WurstOperator.NOTEQ, JassIm.ImExprs(JassIm.ImVarAccess(v), JassIm.ImNull(JassIm.ImAnyType())));
    }

    private static ImFunctionCall call(ImFunction f, ImExpr... args) {
        return JassIm.ImFunctionCall(TRACE, f, JassIm.ImTypeArguments(), JassIm.ImExprs(args), false, CallType.NORMAL);
    }
}
