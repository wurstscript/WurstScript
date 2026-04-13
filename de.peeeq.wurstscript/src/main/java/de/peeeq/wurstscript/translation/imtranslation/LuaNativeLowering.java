package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;

import java.util.*;

/**
 * IM-level lowering pass for the Lua backend, run before optimization so the
 * optimizer can inline and eliminate the generated wrappers.
 *
 * <p>Three classes of WC3 BJ calls are transformed:
 * <ol>
 *   <li><b>GetHandleId</b> – replaced 1:1 by {@code __wurst_GetHandleId}, whose Lua
 *       implementation uses a stable table counter for selected opaque runtime handle
 *       families only. Enum-like handle families keep native semantics in Lua.</li>
 *   <li><b>Hashtable natives</b> ({@code SaveInteger}, {@code LoadBoolean}, …) and
 *       <b>context-callback natives</b> ({@code ForForce}, {@code ForGroup}, …) –
 *       replaced 1:1 by their {@code __wurst_} prefixed equivalents, whose Lua
 *       implementations are provided by {@link de.peeeq.wurstscript.translation.lua.translation.LuaNatives}.</li>
 *   <li><b>All other BJ calls with at least one handle-typed parameter</b> – wrapped
 *       by a generated IM function that first checks each required handle param for
 *       {@code null} and returns the type-appropriate default (0 / 0.0 / false / "" / nil),
 *       then delegates to the original BJ function.  This matches Jass behavior, which
 *       silently returns defaults on null-handle calls instead of crashing.
 *       {@code boolexpr} and {@code code} typed params are intentionally skipped: these
 *       are optional/nullable in Jass (e.g. the filter arg of
 *       {@code TriggerRegisterPlayerUnitEvent}) and passing {@code nil} is valid.</li>
 * </ol>
 *
 * <p>IS_NATIVE stubs added for category 1 and 2 are recognised by
 * {@link de.peeeq.wurstscript.translation.lua.translation.LuaTranslator#translateFunc} as
 * Wurst-owned natives and filled in by
 * {@link de.peeeq.wurstscript.translation.lua.translation.LuaNatives}.
 */
public final class LuaNativeLowering {

    /** Hashtable native names that need to be remapped to {@code __wurst_} equivalents. */
    private static final Set<String> HASHTABLE_NATIVE_NAMES = new HashSet<>(Arrays.asList(
        "InitHashtable",
        "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
        "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr",
        "HaveSavedInteger", "HaveSavedBoolean", "HaveSavedReal", "HaveSavedString", "HaveSavedHandle",
        "FlushChildHashtable", "FlushParentHashtable",
        "RemoveSavedInteger", "RemoveSavedBoolean", "RemoveSavedReal", "RemoveSavedString", "RemoveSavedHandle",
        // Handle-typed save/load variants
        "SavePlayerHandle", "SaveWidgetHandle", "SaveDestructableHandle", "SaveItemHandle", "SaveUnitHandle",
        "SaveAbilityHandle", "SaveTimerHandle", "SaveTriggerHandle", "SaveTriggerConditionHandle",
        "SaveTriggerActionHandle", "SaveTriggerEventHandle", "SaveForceHandle", "SaveGroupHandle",
        "SaveLocationHandle", "SaveRectHandle", "SaveBooleanExprHandle", "SaveSoundHandle", "SaveEffectHandle",
        "SaveUnitPoolHandle", "SaveItemPoolHandle", "SaveQuestHandle", "SaveQuestItemHandle",
        "SaveDefeatConditionHandle", "SaveTimerDialogHandle", "SaveLeaderboardHandle", "SaveMultiboardHandle",
        "SaveMultiboardItemHandle", "SaveTrackableHandle", "SaveDialogHandle", "SaveButtonHandle",
        "SaveTextTagHandle", "SaveLightningHandle", "SaveImageHandle", "SaveUbersplatHandle", "SaveRegionHandle",
        "SaveFogStateHandle", "SaveFogModifierHandle", "SaveAgentHandle", "SaveHashtableHandle", "SaveFrameHandle",
        "LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle",
        "LoadAbilityHandle", "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle",
        "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle", "LoadGroupHandle",
        "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle",
        "LoadUnitPoolHandle", "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle",
        "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle", "LoadMultiboardHandle",
        "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle",
        "LoadTextTagHandle", "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle",
        "LoadFogStateHandle", "LoadFogModifierHandle", "LoadHashtableHandle", "LoadFrameHandle"
    ));

    /** Context-callback natives that need to be remapped to {@code __wurst_} equivalents. */
    private static final Set<String> CONTEXT_CALLBACK_NATIVE_NAMES = new HashSet<>(Arrays.asList(
        "ForForce", "GetEnumPlayer",
        "ForGroup", "GetEnumUnit",
        "EnumItemsInRect", "GetEnumItem",
        "EnumDestructablesInRect", "GetEnumDestructable"
    ));

    /** True runtime-object handles that should use Lua-side object identity for GetHandleId. */
    private static final Set<String> OPAQUE_RUNTIME_HANDLE_TYPES = new HashSet<>(Arrays.asList(
        "unit", "item", "destructable", "effect", "lightning", "timer", "trigger",
        "triggeraction", "triggercondition", "boolexpr", "force", "group", "location",
        "rect", "region", "sound", "dialog", "button", "quest", "questitem",
        "leaderboard", "multiboard", "multiboarditem", "trackable", "texttag",
        "image", "ubersplat", "framehandle", "fogmodifier", "hashtable"
    ));

    /**
     * When {@code true}, only opaque runtime-handle families (unit, item, timer, …)
     * are shimmed via {@code __wurst_GetHandleId}; enum-like handle families
     * (eventid, playerevent, …) keep native {@code GetHandleId} semantics.
     *
     * When {@code false} (safe default), ALL {@code GetHandleId} calls are shimmed
     * unconditionally — this matches the pre-selective-shim behaviour and avoids
     * any desync risk while the selective logic is being validated.
     */
    public static final boolean ENABLE_SELECTIVE_GET_HANDLE_ID_SHIMMING = true;

    private LuaNativeLowering() {}

    /**
     * Transforms the IM program in place.
     *
     * <p>Must be called <em>before</em> the optimizer so that the optimizer
     * can inline and eliminate the generated wrappers.
     *
     * <p>Stubs and wrappers are created lazily (on first call-site encounter) and added
     * to prog only after the traversal completes.  This avoids the memory cost of
     * creating wrappers for every BJ function in the IM (common.j declares hundreds of
     * functions, most of which are unreachable in any given program).
     */
    public static void transform(ImProg prog) {
        // Replace all reads of MagicFunctions_isLua with true.
        // This must happen before any optimizer passes so that dead-code elimination
        // can remove Jass-only branches at compile time.
        // We use attrReads() (not a visitor) to target only rvalue uses, avoiding
        // ClassCastException when the same ImVarAccess appears as a write target (lvalue).
        for (ImVar global : prog.getGlobals()) {
            if ("MagicFunctions_isLua".equals(global.getName())) {
                for (ImVarRead read : new ArrayList<>(global.attrReads())) {
                    read.replaceBy(JassIm.ImBoolVal(true));
                }
                break;
            }
        }

        // Maps original BJ function → replacement (IS_NATIVE stub or nil-safety wrapper).
        // Populated lazily during the traversal.
        Map<ImFunction, ImFunction> replacements = new LinkedHashMap<>();
        Map<String, ImFunction> specialNativeStubs = new LinkedHashMap<>();
        // BJ functions that don't need a replacement (not GetHandleId, not hashtable/callback,
        // no handle params). Cached to avoid rechecking the same function at every call site.
        Set<ImFunction> noReplacement = new HashSet<>();
        // All generated functions (stubs and wrappers) are deferred until after the traversal:
        // - Stubs: deferred so ConcurrentModificationException is avoided on prog.getFunctions()
        // - Wrappers: deferred so the visitor doesn't see their internal BJ delegate calls and
        //   recursively wrap them, which would cause infinite wrapping.
        List<ImFunction> deferredAdditions = new ArrayList<>();

        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                ImFunction f = call.getFunc();
                if (ENABLE_SELECTIVE_GET_HANDLE_ID_SHIMMING && isCompatGetHandleIdFunction(f)) {
                    if (shouldRewriteGetHandleId(call)) {
                        ImFunction replacement = specialNativeStubs.computeIfAbsent("__wurst_GetHandleId",
                            name -> createNativeStub(name, f));
                        if (!deferredAdditions.contains(replacement)) {
                            deferredAdditions.add(replacement);
                        }
                        call.replaceBy(JassIm.ImFunctionCall(
                            call.attrTrace(), replacement,
                            JassIm.ImTypeArguments(),
                            call.getArguments().copy(),
                            false, CallType.NORMAL));
                    }
                    return;
                }
                if (!f.isBj()) return;
                if ("GetHandleId".equals(f.getName())) {
                    if (ENABLE_SELECTIVE_GET_HANDLE_ID_SHIMMING && shouldRewriteGetHandleId(call)) {
                        ImFunction replacement = specialNativeStubs.computeIfAbsent("__wurst_GetHandleId",
                            name -> createNativeStub(name, f));
                        if (!deferredAdditions.contains(replacement)) {
                            deferredAdditions.add(replacement);
                        }
                        call.replaceBy(JassIm.ImFunctionCall(
                            call.attrTrace(), replacement,
                            JassIm.ImTypeArguments(),
                            call.getArguments().copy(),
                            false, CallType.NORMAL));
                    }
                    return;
                }
                if (noReplacement.contains(f)) return;

                if (!replacements.containsKey(f)) {
                    ImFunction r = computeReplacement(f);
                    if (r != null) {
                        replacements.put(f, r);
                        deferredAdditions.add(r);
                    } else {
                        noReplacement.add(f);
                    }
                }
                ImFunction replacement = replacements.get(f);

                if (replacement != null) {
                    call.replaceBy(JassIm.ImFunctionCall(
                        call.attrTrace(), replacement,
                        JassIm.ImTypeArguments(),
                        call.getArguments().copy(),
                        false, CallType.NORMAL));
                }
            }

            private ImFunction computeReplacement(ImFunction bj) {
                String name = bj.getName();
                if (HASHTABLE_NATIVE_NAMES.contains(name)) {
                    return createNativeStub("__wurst_" + name, bj);
                } else if (CONTEXT_CALLBACK_NATIVE_NAMES.contains(name)) {
                    return createNativeStub("__wurst_" + name, bj);
                } else if (hasHandleParam(bj)) {
                    return createNilSafeWrapper(bj);
                }
                return null;
            }
        });

        // Add all generated functions after the traversal so their bodies are not visited
        // by the replacement visitor above.
        prog.getFunctions().addAll(deferredAdditions);
    }

    /**
     * Creates a new IS_NATIVE (non-BJ) IM function stub with the same signature as
     * {@code original}.  The Lua translator will fill in the body via
     * {@code LuaNatives.get()} when it encounters the stub.
     *
     * <p>The caller is responsible for adding the stub to prog.getFunctions().
     */
    private static ImFunction createNativeStub(String name, ImFunction original) {
        ImVars params = JassIm.ImVars();
        for (ImVar p : original.getParameters()) {
            params.add(JassIm.ImVar(p.attrTrace(), p.getType().copy(), p.getName(), false));
        }
        return JassIm.ImFunction(
            original.attrTrace(), name,
            JassIm.ImTypeVars(), params,
            original.getReturnType().copy(),
            JassIm.ImVars(), JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
    }

    /**
     * Creates a nil-safety wrapper for {@code bjNative}.
     *
     * <p>The generated function checks each handle-typed parameter against
     * {@code null} and returns the type-appropriate default value if any is
     * null.  Otherwise it delegates to the original BJ function.
     */
    private static ImFunction createNilSafeWrapper(ImFunction bjNative) {
        ImVars params = JassIm.ImVars();
        List<ImVar> paramVars = new ArrayList<>();
        for (ImVar p : bjNative.getParameters()) {
            ImVar copy = JassIm.ImVar(p.attrTrace(), p.getType().copy(), p.getName(), false);
            params.add(copy);
            paramVars.add(copy);
        }

        ImStmts body = JassIm.ImStmts();

        // Null-check each required handle param: if param == null then return <default> end
        // boolexpr and code params are intentionally skipped — they are optional/nullable
        // in Jass (e.g. the filter arg of TriggerRegisterPlayerUnitEvent).
        ImExpr returnDefault = defaultValueExpr(bjNative.getReturnType());
        for (ImVar param : paramVars) {
            if (isHandleType(param.getType()) && !isNullableHandleType(param.getType())) {
                ImExpr condition = JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                    JassIm.ImVarAccess(param),
                    JassIm.ImNull(param.getType().copy())
                ));
                ImStmts thenBlock = JassIm.ImStmts(
                    JassIm.ImReturn(bjNative.attrTrace(), returnDefault.copy())
                );
                body.add(JassIm.ImIf(bjNative.attrTrace(), condition, thenBlock, JassIm.ImStmts()));
            }
        }

        // Delegate to the original BJ native
        ImExprs callArgs = JassIm.ImExprs();
        for (ImVar pv : paramVars) {
            callArgs.add(JassIm.ImVarAccess(pv));
        }
        ImFunctionCall delegate = JassIm.ImFunctionCall(
            bjNative.attrTrace(), bjNative,
            JassIm.ImTypeArguments(), callArgs, false, CallType.NORMAL);

        if (bjNative.getReturnType() instanceof ImVoid) {
            body.add(delegate);
        } else {
            body.add(JassIm.ImReturn(bjNative.attrTrace(), delegate));
        }

        return JassIm.ImFunction(
            bjNative.attrTrace(),
            "__wurst_safe_" + bjNative.getName(),
            JassIm.ImTypeVars(), params,
            bjNative.getReturnType().copy(),
            JassIm.ImVars(), body,
            Collections.emptyList());
    }

    private static boolean hasHandleParam(ImFunction f) {
        for (ImVar p : f.getParameters()) {
            if (isHandleType(p.getType())) {
                return true;
            }
        }
        return false;
    }

    /** Returns true for WC3 handle types (ImSimpleType that is not int/real/boolean/string). */
    static boolean isHandleType(ImType type) {
        if (!(type instanceof ImSimpleType)) {
            return false;
        }
        String n = ((ImSimpleType) type).getTypename();
        return !n.equals("integer") && !n.equals("real") && !n.equals("boolean") && !n.equals("string");
    }

    /**
     * Returns true for handle types that are valid to pass as {@code null} in Jass without
     * triggering a null-handle crash.  These params are skipped in nil-safety wrappers.
     *
     * <p>{@code boolexpr} and {@code code} are the canonical optional types: every WC3
     * API that takes them (filter, condition, action callbacks) accepts {@code null} to
     * mean "no callback".
     */
    static boolean isNullableHandleType(ImType type) {
        if (!(type instanceof ImSimpleType)) {
            return false;
        }
        String n = ((ImSimpleType) type).getTypename();
        return n.equals("boolexpr") || n.equals("code");
    }

    private static boolean shouldRewriteGetHandleId(ImFunctionCall call) {
        if (call.getArguments().size() != 1) {
            return true;
        }
        return usesLuaObjectIdentityHandleId(call.getArguments().get(0).attrTyp());
    }

    public static boolean usesLuaObjectIdentityHandleId(ImType type) {
        if (!(type instanceof ImSimpleType)) {
            return false;
        }
        String typeName = ((ImSimpleType) type).getTypename();
        return OPAQUE_RUNTIME_HANDLE_TYPES.contains(typeName);
    }

    private static boolean isCompatGetHandleIdFunction(ImFunction f) {
        if (f.getParameters().size() != 1
            || !f.getName().endsWith("_getHandleId")
            || f.getName().endsWith("_getTCHandleId")) {
            return false;
        }
        // Restrict to WC3 simple handle types (ImSimpleType). User-defined Wurst classes
        // use ImClassType and must not have their call sites replaced.
        return isHandleType(f.getParameters().get(0).getType());
    }

    /** Returns an IM expression representing the safe default for the given return type. */
    private static ImExpr defaultValueExpr(ImType returnType) {
        if (returnType instanceof ImSimpleType) {
            String n = ((ImSimpleType) returnType).getTypename();
            switch (n) {
                case "integer": return JassIm.ImIntVal(0);
                case "real":    return JassIm.ImRealVal("0.0");
                case "boolean": return JassIm.ImBoolVal(false);
                case "string":  return JassIm.ImStringVal("");
            }
        }
        // void or handle type → null
        if (returnType instanceof ImVoid) {
            return JassIm.ImNull(JassIm.ImVoid());
        }
        return JassIm.ImNull(returnType.copy());
    }
}
