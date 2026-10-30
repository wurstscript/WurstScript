package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;

/**
 * IM-level lowering pass for the Lua backend, run before optimization so the
 * optimizer can inline and eliminate the generated wrappers.
 *
 * <p>Three classes of WC3 BJ calls are transformed:
 * <ol>
 *   <li><b>GetHandleId</b>: replaced 1:1 by {@code __wurst_GetHandleId}, whose Lua
 *       implementation uses a stable table counter for selected opaque runtime handle
 *       families only. Enum-like handle families keep native semantics in Lua.</li>
 *   <li><b>Hashtable natives</b> ({@code SaveInteger}, {@code LoadBoolean}, …):
 *       replaced 1:1 by their {@code __wurst_} prefixed equivalents, whose Lua
 *       implementations are provided by {@link de.peeeq.wurstscript.translation.lua.translation.LuaNatives}.
 *       The backend prints a hashtable operation where it is called instead, as the table accesses
 *       it stands for, and calls its helper only where it cannot ({@link LuaHashtable}).
 *       The enumeration natives ({@code ForGroup}, {@code ForForce}, {@code EnumItemsInRect},
 *       {@code EnumDestructablesInRect}) and their {@code GetEnum} getters are the game's own: it
 *       keeps the enumerated object per enumeration, nested ones included, as on Jass.</li>
 *   <li><b>All other BJ calls with at least one handle-typed parameter</b>: wrapped
 *       by a generated IM function that first checks each required handle param for
 *       {@code null} and returns the type-appropriate default (0 / 0.0 / false / "" / nil),
 *       then delegates to the original BJ function.  This matches Jass behavior, which
 *       silently returns defaults on null-handle calls instead of crashing.
 *       {@code boolexpr} and {@code code} typed params are intentionally skipped: these
 *       are optional/nullable in Jass (e.g. the filter arg of
 *       {@code TriggerRegisterPlayerUnitEvent}) and passing {@code nil} is valid.
 *       The guards are chosen per call: an argument which reads a common.j constant is never nil and is
 *       not tested.
 *       <p>KNOWN LIMITATION: natives where {@code null} in a non-boolexpr/code handle
 *       param is a meaningful "clear/reset" argument are silently skipped by the
 *       wrapper instead of being forwarded. If such a native is identified, add it
 *       to an exemption list here rather than weakening the general nil-guard.</li>
 * </ol>
 *
 * <p>IS_NATIVE stubs added for category 1 and 2 are recognised by
 * {@link de.peeeq.wurstscript.translation.lua.translation.LuaTranslator#translateFunc} as
 * Wurst-owned natives and filled in by
 * {@link de.peeeq.wurstscript.translation.lua.translation.LuaNatives}.
 */
public final class LuaNativeLowering {

    /** True runtime-object handles that should use Lua-side object identity for GetHandleId. */
    private static final Set<String> OPAQUE_RUNTIME_HANDLE_TYPES = new HashSet<>(Arrays.asList(
        "unit", "item", "destructable", "effect", "lightning", "timer", "trigger",
        "triggeraction", "triggercondition", "boolexpr", "force", "group", "location",
        "rect", "region", "sound", "dialog", "button", "quest", "questitem",
        "leaderboard", "multiboard", "multiboarditem", "trackable", "texttag",
        "image", "ubersplat", "framehandle", "fogmodifier", "hashtable"
    ));

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
    /**
     * Replaces the KeyedTable, KeyedMap and CodeList operations with their Lua stubs, and empties
     * the destroy operations.
     *
     * <p>Separate from {@link #transform} so it can run <b>before</b> stack-trace injection. That
     * pass appends a parameter to every affected function, and on Lua every non-native function is
     * affected, so the exact signatures these operations are recognised by stop matching. Nothing
     * reported that: the Jass bodies simply survived onto Lua, where {@code wurstKeyOf} then
     * answered with its placeholder, so every element shared one key and a set claimed to hold
     * everything. Stack traces are on by default in a release build, so that was the common case
     * rather than an exotic one. Runs before the tree shake, which drops the bodies it replaced.
     *
     * <p>Membership becomes a table keyed directly by the element. Done before optimization rather
     * than at emission because the inliner runs in between: a call inlined before an emission-time
     * rewrite would keep the hashtable body while a surviving one got the Lua table, mixing an
     * integer class id with a table index for the same value. Replacing the call makes every site
     * agree.
     *
     * <p>Idempotent: once the calls point at stubs, nothing matches on a second run.
     */
    public static void lowerKeyedTables(ImProg prog, ImTranslator translator) {
        // A keyed-table destroy currently has no observable Lua storage to clear. Keep it as an
        // ordinary empty function which the inliner can remove.
        for (ImFunction f : prog.getFunctions()) {
            if (LuaKeyedTable.isDestroy(f)) {
                f.getBody().clear();
                f.getLocals().clear();
            }
        }

        // Remove keyed-table destroy calls outright rather than leaving an empty function for the
        // inliner to clean up: inlining only runs under -inline, and even then the Lua register
        // budget can refuse a caller. Arguments move into a statement expression so anything they
        // do still happens, as the garbage removal keeps them.
        removeDestroyCalls(prog);

        // Shared with any earlier run, so a second lowering reuses a stub instead of adding another of
        // the same name, and later passes can match the stub by identity.
        Map<String, ImFunction> stubs = translator.luaKeyedStubs;
        List<ImFunction> additions = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                ImFunction f = call.getFunc();
                String stubName = LuaKeyedTable.nativeStubFor(f);
                if (stubName == null) {
                    stubName = LuaKeyedMap.nativeStubFor(f);
                }
                if (stubName == null) {
                    stubName = LuaCodeList.nativeStubFor(f);
                }
                if (stubName == null) {
                    return;
                }
                ImFunction replacement = stubs.computeIfAbsent(stubName, name -> {
                    ImFunction created = createNativeStub(name, f);
                    additions.add(created);
                    return created;
                });
                call.replaceBy(JassIm.ImFunctionCall(
                    call.attrTrace(), replacement,
                    JassIm.ImTypeArguments(),
                    call.getArguments().copy(),
                    false, CallType.NORMAL));
            }
        });
        prog.getFunctions().addAll(additions);
    }

    private static void removeDestroyCalls(Element e) {
        if (e instanceof ImStmts stmts) {
            ListIterator<ImStmt> it = stmts.listIterator();
            while (it.hasNext()) {
                ImStmt s = it.next();
                if (s instanceof ImFunctionCall call && LuaKeyedTable.isDestroy(call.getFunc())) {
                    ImStmts argStmts = JassIm.ImStmts();
                    for (ImExpr arg : new ArrayList<>(call.getArguments())) {
                        arg.setParent(null);
                        argStmts.add(arg);
                    }
                    s = ImHelper.statementExprVoid(argStmts);
                    it.set(s);
                }
                removeDestroyCalls(s);
            }
        } else {
            for (int i = 0; i < e.size(); i++) {
                removeDestroyCalls(e.get(i));
            }
        }
    }

    public static void transform(ImProg prog, ImTranslator translator) {
        transform(prog, translator, CallLoweringObserver.NONE);
    }

    public static void transform(ImProg prog, ImTranslator translator, CallLoweringObserver observer) {
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

        // Idempotent: transformProgToLua runs this earlier, before stack-trace injection.
        lowerKeyedTables(prog, translator);

        removeRedundantTypeAssurance(prog, translator);
        lowerStringConcatenation(prog, translator);
        lowerDivMod(prog, translator);
        lowerRealToInt(prog, translator, observer);

        // Maps original BJ function → replacement (IS_NATIVE hashtable stub).
        // Populated lazily during the traversal.
        Map<ImFunction, ImFunction> replacements = new LinkedHashMap<>();
        // Nil-safety wrappers by name, one per BJ function and set of guarded parameters.
        Map<String, ImFunction> nilSafeWrappers = new LinkedHashMap<>();
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
                // Here, before the call it is an argument of is visited: a replacement below is built
                // from a copy of the arguments, which would leave a call lowered later unlowered.
                if (JassKeyOfLowering.isCheckedKeyOf(f)) {
                    lowerKeyOf(call, translator, specialNativeStubs, deferredAdditions);
                    return;
                }
                if (isCompatGetHandleIdFunction(f)) {
                    if (shouldRewriteGetHandleId(call)) {
                        ImFunction replacement = specialNativeStubs.computeIfAbsent("__wurst_GetHandleId",
                            name -> createNativeStub(name, f));
                        if (!deferredAdditions.contains(replacement)) {
                            deferredAdditions.add(replacement);
                        }
                        replaceCall(call, JassIm.ImFunctionCall(
                            call.attrTrace(), replacement,
                            JassIm.ImTypeArguments(),
                            call.getArguments().copy(),
                            false, CallType.NORMAL), observer);
                    }
                    return;
                }
                if (!f.isBj()) return;
                if ("GetHandleId".equals(f.getName())) {
                    if (shouldRewriteGetHandleId(call)) {
                        ImFunction replacement = specialNativeStubs.computeIfAbsent("__wurst_GetHandleId",
                            name -> createNativeStub(name, f));
                        if (!deferredAdditions.contains(replacement)) {
                            deferredAdditions.add(replacement);
                        }
                        replaceCall(call, JassIm.ImFunctionCall(
                            call.attrTrace(), replacement,
                            JassIm.ImTypeArguments(),
                            call.getArguments().copy(),
                            false, CallType.NORMAL), observer);
                    }
                    return;
                }
                if (noReplacement.contains(f)) return;

                ImFunction replacement;
                if (LuaHashtable.isNative(f.getName())) {
                    replacement = replacements.computeIfAbsent(f, bj -> {
                        // Kept by the translator, so the optimizer and the backend match the stub by identity.
                        ImFunction stub = translator.luaHashtableStubs.computeIfAbsent(LuaHashtable.stubName(bj.getName()),
                            stubName -> createNativeStub(stubName, bj));
                        // A hashtable stub of an earlier run is already in the program, unless it was removed.
                        if (stub.getParent() == null) {
                            deferredAdditions.add(stub);
                        }
                        return stub;
                    });
                } else if (f.isNative() && hasGuardedParam(f)) {
                    // Decided per call: an argument which is never nil needs no test.
                    List<ImVar> params = f.getParameters();
                    boolean[] guarded = new boolean[params.size()];
                    boolean any = false;
                    for (int i = 0; i < guarded.length; i++) {
                        guarded[i] = isGuardedParam(params.get(i)) && !isNeverNil(call.getArguments().get(i), prog);
                        any |= guarded[i];
                    }
                    if (!any) return;
                    replacement = nilSafeWrappers.computeIfAbsent(nilSafeWrapperName(f, guarded), name -> {
                        ImFunction wrapper = createNilSafeWrapper(f, guarded, name, translator, deferredAdditions);
                        deferredAdditions.add(wrapper);
                        return wrapper;
                    });
                } else {
                    noReplacement.add(f);
                    return;
                }

                replaceCall(call, JassIm.ImFunctionCall(
                    call.attrTrace(), replacement,
                    JassIm.ImTypeArguments(),
                    call.getArguments().copy(),
                    false, CallType.NORMAL), observer);
            }
        });

        // Add all generated functions after the traversal so their bodies are not visited
        // by the replacement visitor above.
        prog.getFunctions().addAll(deferredAdditions);
    }

    /**
     * Lowers a direct call of {@code wurstKeyOf} to the key Jass computes for it.
     *
     * <p>Jass gives the intrinsic a body once generic elimination has made its parameter type
     * concrete ({@link JassKeyOfLowering}). Lua keeps generics erased, so the intrinsic kept its
     * placeholder body there and a direct call answered 0 for every element. The call is lowered
     * where it is made instead, from the type it is made with, and costs nothing: an int and a
     * class instance (an integer id on both targets) are their own key, and a handle's key is its
     * id, so the call becomes the {@code GetHandleId} call Lua makes of a handle of its type. The
     * keyed-table operations, whose Jass bodies key through the intrinsic, are replaced as a whole
     * before the tree shake, which takes those bodies away.
     *
     * <p>A type with no stable integer key is rejected, as on Jass, and so is a call from generic
     * code: Lua erases its element type, so there is nothing to choose the key by.
     */
    private static void lowerKeyOf(ImFunctionCall call, ImTranslator translator,
                                   Map<String, ImFunction> specialNativeStubs, List<ImFunction> deferredAdditions) {
        ImExpr value = call.getArguments().get(0);
        ImType type = call.getTypeArguments().size() == 1
            ? call.getTypeArguments().get(0).getType()
            : value.attrTyp();
        if (TypesHelper.isIntType(type)) {
            value.setParent(null);
            call.replaceBy(value);
            return;
        }
        if (type instanceof ImClassType) {
            // A null instance is nil on Lua; the cast to int keys it 0, as on Jass (an object's id otherwise).
            value.setParent(null);
            call.replaceBy(JassIm.ImCast(value, type, TypesHelper.imInt()));
            return;
        }
        if (type instanceof ImTypeVarRef || type instanceof ImAnyType) {
            throw new CompileError(call.attrTrace().attrErrorPos(),
                JassKeyOfLowering.KEY_OF + " needs a concrete element type on Lua, but this call is made with "
                    + typeName(type) + ": generic code is erased on Lua, so no key can be chosen for it. Call "
                    + JassKeyOfLowering.KEY_OF + " where the element type is known.");
        }
        if (!isHandleType(type) || isSimpleType(type, "code")) {
            throw new CompileError(call.attrTrace().attrErrorPos(),
                JassKeyOfLowering.KEY_OF + " cannot key " + typeName(type) + " on Lua: only int, class "
                    + "instances and handles have a stable integer key.");
        }
        ImFunction getHandleId = translator.handleIdNative();
        if (getHandleId == null) {
            throw new CompileError(call.attrTrace().attrErrorPos(),
                JassKeyOfLowering.KEY_OF + " keys a handle by its id, but the program declares no native GetHandleId.");
        }
        // The same choice as for a call of GetHandleId itself, so both give a handle the same id.
        ImFunction target;
        if (usesLuaObjectIdentityHandleId(value.attrTyp())) {
            target = specialNativeStubs.computeIfAbsent("__wurst_GetHandleId",
                name -> createNativeStub(name, getHandleId));
        } else {
            target = getHandleId;
        }
        // A function nothing called before may have been shaken out of the program (a native which was never
        // translated is added by the translator, behind the traversal's end).
        if (target.getParent() == null && !deferredAdditions.contains(target)) {
            deferredAdditions.add(target);
        }
        value.setParent(null);
        call.replaceBy(JassIm.ImFunctionCall(call.attrTrace(), target, JassIm.ImTypeArguments(),
            JassIm.ImExprs(value), false, CallType.NORMAL));
    }

    private static boolean isSimpleType(ImType t, String name) {
        return t instanceof ImSimpleType st && name.equals(st.getTypename());
    }

    private static String typeName(ImType t) {
        if (t instanceof ImSimpleType st) {
            return st.getTypename();
        }
        if (t instanceof ImTypeVarRef ref) {
            return ref.getTypeVariable().getName();
        }
        return t.toString();
    }

    /**
     * An erased generic value is normalised with {@code __wurst_ensureInt} and friends when it
     * reaches a concrete primitive context, because erased storage can hold nil for a primitive.
     * When the value provably comes from typed storage or a typed default (see
     * {@link LuaTypedValues}) the normalisation is the identity and the call is dropped.
     */
    private static void removeRedundantTypeAssurance(ImProg prog, ImTranslator translator) {
        LuaTypedValues typedValues = new LuaTypedValues(prog);
        List<ImFunctionCall> redundant = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (call.getArguments().size() != 1) {
                    return;
                }
                ImFunction target = call.getFunc();
                ImType type;
                if (target == translator.ensureIntFunc) {
                    type = TypesHelper.imInt();
                } else if (target == translator.ensureRealFunc) {
                    type = TypesHelper.imReal();
                } else if (target == translator.ensureStrFunc) {
                    type = TypesHelper.imString();
                } else {
                    return;
                }
                if (typedValues.isTyped(call.getArguments().get(0), type)) {
                    redundant.add(call);
                }
            }
        });
        for (ImFunctionCall call : redundant) {
            ImExpr value = call.getArguments().get(0);
            value.setParent(null);
            call.replaceBy(value);
        }
    }

    /**
     * Rewrites string PLUS before the optimizer's first garbage-collection
     * pass. The concat helper is an ordinary IM function, so introducing its
     * calls only later in EliminateLocalTypes would let the optimizer remove
     * its definition first and leave dangling Lua calls behind.
     */
    private static void lowerStringConcatenation(ImProg prog, ImTranslator translator) {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImOperatorCall call) {
                super.visit(call);
                ImExprs args = call.getArguments();
                if (call.getOp() == WurstOperator.PLUS && args.size() == 2
                    && TypesHelper.isStringType(args.get(0).attrTyp())
                    && TypesHelper.isStringType(args.get(1).attrTyp())) {
                    call.replaceBy(concatenation(call, translator));
                }
            }
        });
    }

    /**
     * Jass joins a null string to nothing, and a Lua string can be nil, so a concatenation of two
     * strings which might both be nil goes through the helper that checks for it. An operand which
     * cannot be nil needs no check: with both known the {@code ..} operator is all there is, and
     * with one known only the other is made safe, as {@code x or ""}. The common concatenations
     * of a literal with a conversion or with one variable then print as a plain expression,
     * where the helper inlined into each of them was a nest of nil checks.
     */
    private static ImExpr concatenation(ImOperatorCall call, ImTranslator translator) {
        ImExprs args = call.getArguments();
        boolean leftKnown = neverNil(args.get(0), translator);
        boolean rightKnown = neverNil(args.get(1), translator);
        if (!leftKnown && !rightKnown) {
            return callWithStacktrace(call.attrTrace(), translator.stringConcatFunc, args.copy());
        }
        ImExpr left = args.get(0).copy();
        ImExpr right = args.get(1).copy();
        if (!leftKnown) {
            left = intrinsicCall(call, translator.luaRawOrEmptyFunc, left);
        }
        if (!rightKnown) {
            right = intrinsicCall(call, translator.luaRawOrEmptyFunc, right);
        }
        return intrinsicCall(call, translator.luaRawConcatFunc, left, right);
    }

    /**
     * Natives whose result is a string whatever the runtime does. Only {@code I2S}: the backend prints
     * it as {@code tostring}, which cannot answer nil. A native which merely returns a string in the
     * game, such as {@code R2S}, is not trusted to: a runtime of one's own may declare it to answer nil.
     * The emulated string load is matched by identity ({@link LuaHashtable#isStringLoad}).
     */
    private static final Set<String> STRING_NATIVES_NEVER_NIL = Set.of("I2S");

    /**
     * Whether the string {@code e} cannot be nil on Lua. Asked here, before the optimiser, and again
     * by the backend of the operand a {@code luaRawOrEmptyFunc} guard finally holds: inlining turns a
     * call such as {@code int.toString()} into the {@code I2S} it returns, which needs no guard.
     */
    public static boolean neverNil(ImExpr e, ImTranslator translator) {
        if (e instanceof ImStringVal) {
            return true;
        }
        if (e instanceof ImFunctionCall imFunctionCall) {
            ImFunction f = imFunctionCall.getFunc();
            // a lowered concatenation always has two strings to join; tostring answers a string
            return f == translator.luaRawConcatFunc
                || f == translator.luaRawOrEmptyFunc
                || f == translator.luaRawToStringFunc
                || (f.isNative() && STRING_NATIVES_NEVER_NIL.contains(f.getName()))
                || LuaHashtable.isStringLoad(translator, f);
        }
        return false;
    }

    private static ImFunctionCall intrinsicCall(ImOperatorCall at, ImFunction f, ImExpr... args) {
        return JassIm.ImFunctionCall(at.attrTrace(), f, JassIm.ImTypeArguments(), JassIm.ImExprs(args),
            false, CallType.NORMAL);
    }

    private static final de.peeeq.wurstscript.ast.Element SYNTHETIC_TRACE = de.peeeq.wurstscript.ast.Ast.NoExpr();

    /**
     * Rewrites {@code DIV_INT}/{@code MOD_INT}/{@code MOD_REAL}/{@code JASS_MOD_INT} operator calls
     * into calls against small, portable IM functions (not natives), instead
     * of them being lowered directly to opaque, always-emitted Lua helper
     * functions at Lua-emission time (after {@code ImOptimizer} has already
     * run). Because these functions now live in the IM tree before inlining
     * and dead-code elimination, non-constant div/mod at a hot call site can
     * actually get inlined, and the helpers disappear entirely from programs
     * that never use div/mod - both of which were previously impossible.
     *
     * <p>Constant-constant div/mod already folds away earlier (see
     * {@code SimpleRewrites}/{@code ConstantAndCopyPropagation}) and never
     * reaches this rewrite; this only applies to runtime-value operands.
     *
     * <p>Exception: {@code I2S(1 div 0)} is ErrorHandling's deliberate,
     * always-non-constant-looking crash trap - {@code
     * lua.translation.ExprTranslation#translate(ImFunctionCall, ...)} pattern
     * matches that exact {@code ImOperatorCall(DIV_INT, [1, 0])} shape as the
     * sole argument of an {@code I2S} call and turns it into the {@code
     * __wurst_abort_thread} sentinel every callback xpcall handler ignores.
     * Lowering it here first would replace that shape with a call to the
     * portable {@code __wurst_intDiv} helper, which the sentinel check does
     * not recognize - the trap would then raise a real Lua {@code n//0}
     * runtime error instead of the sentinel, breaking every callback error
     * handler's "was this an intentional abort" check. Leave that one
     * expression untouched so the existing recognition still fires.
     */
    private static void lowerDivMod(ImProg prog, ImTranslator translator) {
        DivModFunctions funcs = new DivModFunctions(translator);
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImOperatorCall call) {
                super.visit(call);
                if (call.getArguments().size() != 2) {
                    return;
                }
                ImFunction target;
                if (call.getOp() == WurstOperator.DIV_INT) {
                    if (isIntentionalThreadAbortDivByZero(call)) {
                        return;
                    }
                    target = funcs.intDiv();
                } else if (call.getOp() == WurstOperator.MOD_INT) {
                    // For a positive constant divisor the ModuloInteger correction is exactly Lua's
                    // floored %, one VM opcode instead of a C call and a branch. A constant dividend
                    // keeps the helper so the optimizer can still fold the whole expression.
                    ImExpr dividend = call.getArguments().get(0);
                    ImExpr divisor = call.getArguments().get(1);
                    if (divisor instanceof ImIntVal imIntVal && imIntVal.getValI() > 0
                        && !(dividend instanceof ImIntVal)) {
                        target = funcs.rawFloorModInt();
                    } else {
                        target = funcs.modInt();
                    }
                } else if (call.getOp() == WurstOperator.MOD_REAL) {
                    target = funcs.modReal();
                } else if (call.getOp() == WurstOperator.JASS_MOD_INT) {
                    target = funcs.jassModInt();
                } else {
                    return;
                }
                List<ImExpr> args = call.getArguments().removeAll();
                call.replaceBy(JassIm.ImFunctionCall(call.attrTrace(), target,
                    JassIm.ImTypeArguments(), JassIm.ImExprs(args), false, CallType.NORMAL));
            }
        });
        // Added only after the traversal completes - prog.getFunctions() is
        // being iterated by the accept() call above, same reasoning as
        // deferredAdditions in transform().
        prog.getFunctions().addAll(funcs.createdFunctions());
    }

    private static void replaceCall(ImFunctionCall source, ImFunctionCall lowered, CallLoweringObserver observer) {
        observer.replaced(source, lowered);
        source.replaceBy(lowered);
    }

    /**
     * Rewrites calls to the native {@code R2I} to {@code __wurst_R2I}, which truncates in Lua arithmetic:
     * {@code x // 1 | 0} for a non-negative real, and its negation for a negative one. That is exact
     * wherever the result is a 32-bit integer, which is the range it covers. NaN, the infinities and
     * everything outside that range call the native, so their results are the engine's.
     *
     * <p>On Lua {@code R2I} is an engine call. Measured on the 3.0.0 client, the engine and this
     * arithmetic return the same for every value probed, the 32-bit edges and values outside the range
     * included. The helper returns one intrinsic call, so it inlines anywhere, and the intrinsic is
     * printed as a single Lua expression ({@code ExprTranslation#realToInt}) with no call in range.
     */
    private static void lowerRealToInt(ImProg prog, ImTranslator translator, CallLoweringObserver observer) {
        ImFunction[] shim = {null};
        List<ImFunction> added = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                ImFunction f = call.getFunc();
                if (!f.isBj() || !"R2I".equals(f.getName()) || call.getArguments().size() != 1) {
                    return;
                }
                if (shim[0] == null) {
                    shim[0] = buildRealToInt(translator, added);
                }
                replaceCall(call, JassIm.ImFunctionCall(call.attrTrace(), shim[0], JassIm.ImTypeArguments(),
                    JassIm.ImExprs(call.getArguments().removeAll()), false, CallType.NORMAL), observer);
            }
        });
        if (shim[0] != null) {
            prog.getFunctions().addAll(added);
            prog.getFunctions().add(shim[0]);
        }
    }

    /**
     * The R2I intrinsic, printed as Lua arithmetic by ExprTranslation#realToInt: made once per program, and added to
     * {@code addTo} by whichever asks first. The R2I lowering and the real modulo share it.
     */
    private static ImFunction rawRealToInt(ImTranslator translator, List<ImFunction> addTo) {
        if (translator.luaRawR2IFunc == null) {
            ImVar arg = JassIm.ImVar(SYNTHETIC_TRACE, TypesHelper.imReal(), "x", false);
            translator.luaRawR2IFunc = JassIm.ImFunction(SYNTHETIC_TRACE, "__wurst_rawR2I", JassIm.ImTypeVars(),
                JassIm.ImVars(arg), TypesHelper.imInt(), JassIm.ImVars(), JassIm.ImStmts(),
                Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
            addTo.add(translator.luaRawR2IFunc);
        }
        return translator.luaRawR2IFunc;
    }

    /** return rawR2I(x): one intrinsic call, printed as Lua arithmetic by ExprTranslation#realToInt. */
    private static ImFunction buildRealToInt(ImTranslator translator, List<ImFunction> addTo) {
        ImType realType = TypesHelper.imReal();
        ImType intType = TypesHelper.imInt();
        ImFunction rawR2I = rawRealToInt(translator, addTo);

        ImVar x = JassIm.ImVar(SYNTHETIC_TRACE, realType.copy(), "x", false);
        ImStmts body = JassIm.ImStmts(JassIm.ImReturn(SYNTHETIC_TRACE, JassIm.ImFunctionCall(SYNTHETIC_TRACE, rawR2I,
            JassIm.ImTypeArguments(), JassIm.ImExprs(JassIm.ImVarAccess(x)), false, CallType.NORMAL)));
        return JassIm.ImFunction(SYNTHETIC_TRACE, "__wurst_R2I", JassIm.ImTypeVars(), JassIm.ImVars(x), intType.copy(),
            JassIm.ImVars(), body, Collections.emptyList());
    }

    /**
     * True for exactly the {@code ImOperatorCall(DIV_INT, [1, 0])} shape that
     * is the sole argument of a call to the native {@code I2S} - mirrors
     * {@code lua.translation.ExprTranslation#isIntentionalThreadAbortCall}
     * from the operator-call side, so both stay in agreement about what
     * counts as the deliberate crash trap.
     */
    private static boolean isIntentionalThreadAbortDivByZero(ImOperatorCall call) {
        ImExpr left = call.getArguments().get(0);
        ImExpr right = call.getArguments().get(1);
        if (!(left instanceof ImIntVal leftInt) || leftInt.getValI() != 1) {
            return false;
        }
        if (!(right instanceof ImIntVal rightInt) || rightInt.getValI() != 0) {
            return false;
        }
        // call's direct parent is the ImExprs argument-list container, not
        // the ImFunctionCall itself - go up one more level to reach it (the
        // same double getParent() pattern this codebase uses elsewhere for
        // list-contained IM/AST elements).
        Element argsList = call.getParent();
        Element parent = argsList == null ? null : argsList.getParent();
        if (!(parent instanceof ImFunctionCall parentCall)) {
            return false;
        }
        return parentCall.getArguments().size() == 1
            && parentCall.getArguments().get(0) == call
            && "I2S".equals(parentCall.getFunc().getName());
    }

    private static ImFunctionCall callWithStacktrace(de.peeeq.wurstscript.ast.Element trace, ImFunction f, ImExprs args) {
        int stacktraceIndex = stacktraceParamIndex(f);
        if (stacktraceIndex >= 0) {
            args.add(stacktraceIndex, JassIm.ImStringVal("when calling " + f.getName()
                + StackTraceInjector2.getCallPos(trace.attrErrorPos())));
        }
        return JassIm.ImFunctionCall(trace, f, JassIm.ImTypeArguments(), args, false, CallType.NORMAL);
    }

    private static int stacktraceParamIndex(ImFunction f) {
        for (int i = 0; i < f.getParameters().size(); i++) {
            if (StackTraceInjector2.STACK_POS_PARAM.equals(f.getParameters().get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Lazily builds (and memoizes) the div/mod helper functions and the tiny
     * raw-Lua-primitive natives they delegate to (Wurst's IM has no
     * floor-division/fmod operator of its own).
     */
    private static final class DivModFunctions {
        private final ImTranslator translator;
        private final List<ImFunction> created = new ArrayList<>();
        private ImFunction rawFloorDivInt;
        private ImFunction rawFmodInt;
        private ImFunction rawFloorModInt;
        private ImFunction intDiv;
        private ImFunction modInt;
        private ImFunction modReal;

        private DivModFunctions(ImTranslator translator) {
            this.translator = translator;
        }

        List<ImFunction> createdFunctions() {
            return created;
        }

        ImFunction intDiv() {
            if (intDiv == null) {
                intDiv = buildIntDiv(rawFloorDivInt());
                translator.luaIntDivFunc = intDiv;
                created.add(intDiv);
            }
            return intDiv;
        }

        /** For an int divisor b > 0, Lua's floored % is exactly the ModuloInteger result. */
        ImFunction modInt() {
            if (modInt == null) {
                modInt = buildMod("__wurst_modInt", TypesHelper.imInt(), JassIm.ImIntVal(0),
                    WurstOperator.GREATER, JassIm.ImIntVal(0), rawFmodInt(), rawFloorModInt());
                translator.luaModIntFunc = modInt;
                created.add(modInt);
            }
            return modInt;
        }

        /**
         * Blizzard.j's ModuloReal, which a real modulo calls on Jass: {@code a - R2I(a / b) * b}, plus b when that
         * is negative, with R2I as the game's Lua computes it. Measured on the 3.0.0 client, that gives Jass's
         * result in the game's Lua for every operand both read alike, where math.fmod and Lua's float % do not:
         * 360 % 7 is 3 (fmod and % give 3.0000153), 7.5 % 360 is 7.5 (both give 7.4999995), -7.5 % 2.5 is 0
         * (fmod gives 2.4999993). The test Lua computes in double, where all three agree.
         */
        ImFunction modReal() {
            if (modReal == null) {
                modReal = buildBlizzardModReal(rawRealToInt(translator, created));
                translator.luaModRealFunc = modReal;
                created.add(modReal);
            }
            return modReal;
        }

        ImFunction jassModInt() {
            return rawFmodInt();
        }

        private ImFunction rawFloorDivInt() {
            if (rawFloorDivInt == null) {
                rawFloorDivInt = rawNative("__wurst_rawFloorDivInt", TypesHelper.imInt());
                translator.luaRawFloorDivIntFunc = rawFloorDivInt;
                created.add(rawFloorDivInt);
            }
            return rawFloorDivInt;
        }

        private ImFunction rawFmodInt() {
            if (rawFmodInt == null) {
                rawFmodInt = rawNative("__wurst_rawFmodInt", TypesHelper.imInt());
                translator.luaRawFmodIntFunc = rawFmodInt;
                created.add(rawFmodInt);
            }
            return rawFmodInt;
        }

        /** Lua's floored {@code %}; only correct for a positive divisor, which the caller guarantees. */
        ImFunction rawFloorModInt() {
            if (rawFloorModInt == null) {
                rawFloorModInt = rawNative("__wurst_rawFloorModInt", TypesHelper.imInt());
                translator.luaRawFloorModIntFunc = rawFloorModInt;
                created.add(rawFloorModInt);
            }
            return rawFloorModInt;
        }

        /** A native leaf with two params and a return, translated as a Lua backend intrinsic. */
        private static ImFunction rawNative(String name, ImType numType) {
            ImVar a = JassIm.ImVar(SYNTHETIC_TRACE, numType.copy(), "a", false);
            ImVar b = JassIm.ImVar(SYNTHETIC_TRACE, numType.copy(), "b", false);
            return JassIm.ImFunction(SYNTHETIC_TRACE, name, JassIm.ImTypeVars(), JassIm.ImVars(a, b), numType.copy(),
                JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        }

        /**
         * local q = rawFloorDiv(a, b)
         * if q < 0 and q * b ~= a then q = q + 1 end
         * return q
         * (Lua's // truncates toward -inf; Jass integer division truncates toward zero.)
         */
        private static ImFunction buildIntDiv(ImFunction rawFloorDiv) {
            ImType intType = TypesHelper.imInt();
            ImVar a = JassIm.ImVar(SYNTHETIC_TRACE, intType.copy(), "a", false);
            ImVar b = JassIm.ImVar(SYNTHETIC_TRACE, intType.copy(), "b", false);
            ImVar q = JassIm.ImVar(SYNTHETIC_TRACE, intType.copy(), "q", false);

            ImStmts body = JassIm.ImStmts(
                JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(q), call(rawFloorDiv, JassIm.ImVarAccess(a), JassIm.ImVarAccess(b))),
                JassIm.ImIf(SYNTHETIC_TRACE,
                    JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(
                        JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(q), JassIm.ImIntVal(0))),
                        JassIm.ImOperatorCall(WurstOperator.NOTEQ, JassIm.ImExprs(
                            JassIm.ImOperatorCall(WurstOperator.MULT, JassIm.ImExprs(JassIm.ImVarAccess(q), JassIm.ImVarAccess(b))),
                            JassIm.ImVarAccess(a)
                        ))
                    )),
                    JassIm.ImStmts(JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(q),
                        JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(q), JassIm.ImIntVal(1))))),
                    JassIm.ImStmts()
                ),
                JassIm.ImReturn(SYNTHETIC_TRACE, JassIm.ImVarAccess(q))
            );
            return JassIm.ImFunction(SYNTHETIC_TRACE, "__wurst_intDiv", JassIm.ImTypeVars(), JassIm.ImVars(a, b), intType.copy(),
                JassIm.ImVars(q), body, Collections.emptyList());
        }

        /**
         * local r
         * if b > 0 then r = rawFloorMod(a, b)
         * else r = rawFmod(a, b); if r < 0 then r = r + b end end
         * return r
         * (Wurst mod follows Blizzard.j's ModuloInteger/ModuloReal: truncated remainder, plus the
         * divisor when the remainder is negative. Lua's % is floored, which gives the same for the
         * divisors {@code positiveTest} admits, as one VM operation instead of a C call and a branch.
         * Once inlined where the divisor is a literal, the test folds and only one branch is left.)
         */
        private static ImFunction buildMod(String name, ImType numType, ImExpr zeroLiteral,
                                           WurstOperator positiveTest, ImExpr positiveBound,
                                           ImFunction rawFmod, ImFunction rawFloorMod) {
            ImVar a = JassIm.ImVar(SYNTHETIC_TRACE, numType.copy(), "a", false);
            ImVar b = JassIm.ImVar(SYNTHETIC_TRACE, numType.copy(), "b", false);
            ImVar r = JassIm.ImVar(SYNTHETIC_TRACE, numType.copy(), "r", false);

            ImStmts body = JassIm.ImStmts(
                JassIm.ImIf(SYNTHETIC_TRACE,
                    JassIm.ImOperatorCall(positiveTest, JassIm.ImExprs(JassIm.ImVarAccess(b), positiveBound)),
                    JassIm.ImStmts(JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(r),
                        call(rawFloorMod, JassIm.ImVarAccess(a), JassIm.ImVarAccess(b)))),
                    JassIm.ImStmts(
                        JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(r), call(rawFmod, JassIm.ImVarAccess(a), JassIm.ImVarAccess(b))),
                        JassIm.ImIf(SYNTHETIC_TRACE,
                            JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(r), zeroLiteral)),
                            JassIm.ImStmts(JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(r),
                                JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(r), JassIm.ImVarAccess(b))))),
                            JassIm.ImStmts()
                        ))
                ),
                JassIm.ImReturn(SYNTHETIC_TRACE, JassIm.ImVarAccess(r))
            );
            return JassIm.ImFunction(SYNTHETIC_TRACE, name, JassIm.ImTypeVars(), JassIm.ImVars(a, b), numType.copy(),
                JassIm.ImVars(r), body, Collections.emptyList());
        }

        /**
         * local q = a / b; local r = a - rawR2I(q) * b; if r < 0 then r = r + b end; return r
         * (Blizzard.j's ModuloReal. The quotient is a local because the intrinsic is printed in place only for a
         * variable; the integer it gives converts to a real in the multiplication, as I2R does.)
         */
        private static ImFunction buildBlizzardModReal(ImFunction rawR2I) {
            ImType realType = TypesHelper.imReal();
            ImVar a = JassIm.ImVar(SYNTHETIC_TRACE, realType.copy(), "a", false);
            ImVar b = JassIm.ImVar(SYNTHETIC_TRACE, realType.copy(), "b", false);
            ImVar q = JassIm.ImVar(SYNTHETIC_TRACE, realType.copy(), "q", false);
            ImVar r = JassIm.ImVar(SYNTHETIC_TRACE, realType.copy(), "r", false);

            ImStmts body = JassIm.ImStmts(
                JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(q),
                    JassIm.ImOperatorCall(WurstOperator.DIV_REAL, JassIm.ImExprs(JassIm.ImVarAccess(a), JassIm.ImVarAccess(b)))),
                JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(r), JassIm.ImOperatorCall(WurstOperator.MINUS,
                    JassIm.ImExprs(JassIm.ImVarAccess(a), JassIm.ImOperatorCall(WurstOperator.MULT,
                        JassIm.ImExprs(call(rawR2I, JassIm.ImVarAccess(q)), JassIm.ImVarAccess(b)))))),
                JassIm.ImIf(SYNTHETIC_TRACE,
                    JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(r), JassIm.ImRealVal("0."))),
                    JassIm.ImStmts(JassIm.ImSet(SYNTHETIC_TRACE, JassIm.ImVarAccess(r),
                        JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(r), JassIm.ImVarAccess(b))))),
                    JassIm.ImStmts()
                ),
                JassIm.ImReturn(SYNTHETIC_TRACE, JassIm.ImVarAccess(r))
            );
            return JassIm.ImFunction(SYNTHETIC_TRACE, "__wurst_modReal", JassIm.ImTypeVars(), JassIm.ImVars(a, b),
                realType.copy(), JassIm.ImVars(q, r), body, Collections.emptyList());
        }

        private static ImFunctionCall call(ImFunction f, ImExpr... args) {
            return JassIm.ImFunctionCall(SYNTHETIC_TRACE, f, JassIm.ImTypeArguments(), JassIm.ImExprs(args), false, CallType.NORMAL);
        }
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
            params.add(JassIm.ImVar(p.attrTrace(), erasedForStub(p.getType()), p.getName(), false));
        }
        return JassIm.ImFunction(
            original.attrTrace(), name,
            JassIm.ImTypeVars(), params,
            erasedForStub(original.getReturnType()),
            JassIm.ImVars(), JassIm.ImStmts(),
            Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
    }

    /**
     * A type safe to put in a stub's signature.
     *
     * <p>Stubs are built with no type variables of their own, so copying an ImTypeVarRef would
     * leave the stub referring to a variable owned by the function it replaced - a free variable,
     * and malformed IM for every pass that walks types afterwards. A native stub is never generic:
     * its body is hand-written Lua that does not consult the type, so erasing is the whole fix
     * rather than rebinding a variable nothing will read.
     */
    private static ImType erasedForStub(ImType t) {
        return t instanceof ImTypeVarRef ? JassIm.ImAnyType() : t.copy();
    }

    /**
     * Creates a nil-safety wrapper for {@code bjNative} named {@code name}.
     *
     * <p>The generated function checks each parameter marked in {@code guarded}
     * against {@code null} and returns the type-appropriate default value if any is
     * null.  Otherwise it delegates to the original BJ function.
     */
    private static ImFunction createNilSafeWrapper(ImFunction bjNative, boolean[] guarded, String name,
                                                   ImTranslator translator, List<ImFunction> deferredAdditions) {
        ImVars params = JassIm.ImVars();
        List<ImVar> paramVars = new ArrayList<>();
        for (ImVar p : bjNative.getParameters()) {
            ImVar copy = JassIm.ImVar(p.attrTrace(), p.getType().copy(), p.getName(), false);
            params.add(copy);
            paramVars.add(copy);
        }

        ImStmts body = JassIm.ImStmts();

        // Null-check each guarded param: if param == null then return <default> end
        // A null enum handle is no missing handle: the engine reads it as the handle of id 0, so the
        // wrapper passes that one on (measured on the 3.0.0 client: GetUnitState(u, null) is the life,
        // IsUnitType(hero, null) is true, GetPlayerState(p, null) is the game result, and
        // UnitDamageTarget with a null attack, damage and weapon type deals the damage).
        ImExpr returnDefault = defaultValueExpr(bjNative.getReturnType());
        for (int i = 0; i < paramVars.size(); i++) {
            ImVar param = paramVars.get(i);
            if (guarded[i]) {
                ImExpr condition = JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(
                    JassIm.ImVarAccess(param),
                    JassIm.ImNull(param.getType().copy())
                ));
                // Only a native reads it so: a blizzard.j helper sees the null, which Jass keeps, and may compare it.
                ImFunction converter = bjNative.isNative()
                    ? translator.enumConverter(((ImSimpleType) param.getType()).getTypename())
                    : null;
                ImStmts thenBlock;
                if (converter != null) {
                    // A converter nothing else called may have been shaken out of the program.
                    if (converter.getParent() == null && !deferredAdditions.contains(converter)) {
                        deferredAdditions.add(converter);
                    }
                    thenBlock = JassIm.ImStmts(JassIm.ImSet(bjNative.attrTrace(), JassIm.ImVarAccess(param),
                        JassIm.ImFunctionCall(bjNative.attrTrace(), converter, JassIm.ImTypeArguments(),
                            JassIm.ImExprs(JassIm.ImIntVal(0)), false, CallType.NORMAL)));
                } else {
                    thenBlock = JassIm.ImStmts(
                        JassIm.ImReturn(bjNative.attrTrace(), returnDefault.copy())
                    );
                }
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
            name,
            JassIm.ImTypeVars(), params,
            bjNative.getReturnType().copy(),
            JassIm.ImVars(), body,
            Collections.emptyList());
    }

    /**
     * {@code __wurst_safe_GetUnitState} when every guardable parameter is tested, otherwise with the
     * names of the tested ones before the native's ({@code __wurst_safe_whichUnit_GetUnitState}), so a
     * call of the wrapper still reads as a call of the native.
     */
    private static String nilSafeWrapperName(ImFunction bj, boolean[] guarded) {
        StringBuilder name = new StringBuilder("__wurst_safe_");
        boolean all = true;
        for (int i = 0; i < guarded.length; i++) {
            ImVar param = bj.getParameters().get(i);
            if (guarded[i]) {
                name.append(param.getName()).append('_');
            } else if (isGuardedParam(param)) {
                all = false;
            }
        }
        return all ? "__wurst_safe_" + bj.getName() : name.append(bj.getName()).toString();
    }

    private static boolean hasGuardedParam(ImFunction f) {
        for (ImVar p : f.getParameters()) {
            if (isGuardedParam(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A handle parameter which the wrapper tests for null. boolexpr and code params are intentionally skipped
     * because they are optional/nullable in Jass (e.g. the filter arg of TriggerRegisterPlayerUnitEvent).
     */
    private static boolean isGuardedParam(ImVar param) {
        return isHandleType(param.getType()) && !isNullableHandleType(param.getType());
    }

    /**
     * A read of a common.j constant made by its converter ({@code constant unitstate UNIT_STATE_MAX_LIFE =
     * ConvertUnitState(1)}). The game defines it before any script runs and nothing can assign it, so it is
     * never nil, and an argument which reads it needs no nil test.
     */
    private static boolean isNeverNil(ImExpr arg, ImProg prog) {
        return arg instanceof ImVarAccess access && ImHelper.isNonNullHandleConstant(access.getVar(), prog);
    }

    /** Returns true for WC3 handle types (ImSimpleType that is not int/real/boolean/string). */
    public static boolean isHandleType(ImType type) {
        if (!(type instanceof ImSimpleType imSimpleType)) {
            return false;
        }
        String n = imSimpleType.getTypename();
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
        if (!(type instanceof ImSimpleType imSimpleType)) {
            return false;
        }
        String n = imSimpleType.getTypename();
        return n.equals("boolexpr") || n.equals("code");
    }

    private static boolean shouldRewriteGetHandleId(ImFunctionCall call) {
        if (call.getArguments().size() != 1) {
            return true;
        }
        return usesLuaObjectIdentityHandleId(call.getArguments().get(0).attrTyp());
    }

    public static boolean usesLuaObjectIdentityHandleId(ImType type) {
        if (!(type instanceof ImSimpleType imSimpleType)) {
            return false;
        }
        String typeName = imSimpleType.getTypename();
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
        if (returnType instanceof ImSimpleType imSimpleType) {
            String n = imSimpleType.getTypename();
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
