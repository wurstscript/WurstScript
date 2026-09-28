package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns an erased keyed-map read that is used as a primitive into the typed read.
 *
 * <p>A new-generics read ({@code keyedMapGetNative<K, V>}, and a class such as {@code FastKeyedMap}
 * over it) is lowered to the untyped {@code __wurst_keyedMapGet}, which answers nil for a missing key.
 * Where the value is used as an int, real, bool or string, the translation wraps it in the matching
 * ensure: for an int, {@code tonumber}, {@code math.tointeger} and two nil checks on every read. The
 * typed stubs ({@code __wurst_keyedMapGetInt} and friends) answer the Wurst default for a missing key
 * themselves, and for every value a typed put can store they return what the ensure would, so the
 * ensure of a read is the typed read. Measured in the 3.0.0 client, a {@code FastKeyedMap<unit, int>}
 * read cost 2.4 times the typed {@code keyedMapGetInt} because of that ensure.
 *
 * <p>Two shapes are folded: the ensure of the untyped read itself, and the ensure of a call to a
 * wrapper whose whole body returns that read. A wrapper gets a typed twin, one per wrapper and type,
 * which returns the typed read, and the ensured call is redirected to it. Only function calls are
 * followed, not dispatching method calls: {@link LuaMethodCallLowering} turns the hot ones into
 * function calls, and this runs again after it.
 */
public final class LuaTypedKeyedReads {

    private LuaTypedKeyedReads() {
    }

    public static void transform(ImProg prog, ImTranslator translator) {
        ImFunction untypedGet = nativeNamed(prog, LuaKeyedMap.NATIVE_GET);
        if (untypedGet == null) {
            return;
        }
        Map<String, ImFunction> typedStubs = new HashMap<>();
        Map<ImFunction, Map<String, ImFunction>> twins = new HashMap<>();
        List<ImFunction> additions = new ArrayList<>();
        List<Runnable> rewrites = new ArrayList<>();

        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                String stubName = typedStubForEnsure(call.getFunc(), translator);
                if (stubName != null && call.getArguments().size() == 1) {
                    plan(call, call.getArguments().get(0), stubName);
                }
            }

            @Override
            public void visit(ImOperatorCall call) {
                super.visit(call);
                // The bool ensure is printed as `x == true` rather than as a helper call.
                ImExprs args = call.getArguments();
                if (call.getOp() == WurstOperator.EQ && args.size() == 2
                    && args.get(1) instanceof ImBoolVal b && b.getValB()) {
                    plan(call, args.get(0), LuaKeyedMap.NATIVE_GET_BOOL);
                }
            }

            private void plan(ImExpr ensured, ImExpr value, String stubName) {
                if (!(value instanceof ImFunctionCall read)) {
                    return;
                }
                ImFunction target;
                if (isUntypedGet(read.getFunc())) {
                    target = typedStub(stubName);
                } else if (isWrapper(read.getFunc())) {
                    target = twins.computeIfAbsent(read.getFunc(), f -> new HashMap<>())
                        .computeIfAbsent(stubName, name -> twin(read.getFunc(), typedStub(name)));
                } else {
                    return;
                }
                rewrites.add(() -> ensured.replaceBy(JassIm.ImFunctionCall(read.attrTrace(), target,
                    read.getTypeArguments().copy(), read.getArguments().copy(), false, CallType.NORMAL)));
            }

            private ImFunction typedStub(String name) {
                return typedStubs.computeIfAbsent(name, n -> {
                    ImFunction existing = nativeNamed(prog, n);
                    if (existing != null) {
                        return existing;
                    }
                    ImFunction stub = stubLike(untypedGet, n, typedResult(n));
                    additions.add(stub);
                    return stub;
                });
            }

            /** A copy of the wrapper that returns the typed read instead of the untyped one. */
            private ImFunction twin(ImFunction wrapper, ImFunction typed) {
                ImFunction copy = wrapper.copyWithRefs();
                copy.setName(wrapper.getName() + "_" + typedSuffix(typed.getName()));
                copy.setReturnType(typed.getReturnType().copy());
                ImReturn ret = onlyReturn(copy);
                ImFunctionCall inner = (ImFunctionCall) ret.getReturnValue();
                inner.replaceBy(JassIm.ImFunctionCall(inner.attrTrace(), typed, JassIm.ImTypeArguments(),
                    inner.getArguments().copy(), false, CallType.NORMAL));
                additions.add(copy);
                return copy;
            }
        });
        // Rewritten after the walk, so no replaced node is visited during it.
        for (Runnable rewrite : rewrites) {
            rewrite.run();
        }
        prog.getFunctions().addAll(additions);
    }

    private static @Nullable String typedStubForEnsure(@Nullable ImFunction f, ImTranslator translator) {
        if (f == null) {
            return null;
        }
        if (f == translator.ensureIntFunc) {
            return LuaKeyedMap.NATIVE_GET_INT;
        }
        if (f == translator.ensureRealFunc) {
            return LuaKeyedMap.NATIVE_GET_REAL;
        }
        if (f == translator.ensureStrFunc) {
            return LuaKeyedMap.NATIVE_GET_STR;
        }
        return null;
    }

    /**
     * A function whose whole body is {@code return <untypedGet>(...)}, apart from the bare {@code null}
     * statements translation leaves around it.
     */
    private static boolean isWrapper(ImFunction f) {
        if (f.isNative() || !f.getLocals().isEmpty()) {
            return false;
        }
        ImReturn ret = onlyReturn(f);
        return ret != null
            && ret.getReturnValue() instanceof ImFunctionCall inner
            && isUntypedGet(inner.getFunc());
    }

    /**
     * Matched by name: keyed-map lowering can leave more than one stub of that name, one per pass that
     * created it, and each prints as the same Lua native.
     */
    private static boolean isUntypedGet(ImFunction f) {
        return f.isNative() && LuaKeyedMap.NATIVE_GET.equals(f.getName());
    }

    /** The function's one statement besides bare {@code null}s, when that is a return; otherwise null. */
    private static @Nullable ImReturn onlyReturn(ImFunction f) {
        ImReturn found = null;
        for (ImStmt s : f.getBody()) {
            if (s instanceof ImNull) {
                continue;
            }
            if (found != null || !(s instanceof ImReturn ret)) {
                return null;
            }
            found = ret;
        }
        return found;
    }

    private static @Nullable ImFunction nativeNamed(ImProg prog, String name) {
        for (ImFunction f : prog.getFunctions()) {
            if (f.isNative() && name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    private static ImType typedResult(String stubName) {
        return switch (stubName) {
            case LuaKeyedMap.NATIVE_GET_INT -> TypesHelper.imInt();
            case LuaKeyedMap.NATIVE_GET_REAL -> TypesHelper.imReal();
            case LuaKeyedMap.NATIVE_GET_BOOL -> TypesHelper.imBool();
            default -> TypesHelper.imString();
        };
    }

    private static String typedSuffix(String stubName) {
        return stubName.substring(LuaKeyedMap.NATIVE_GET.length()).toLowerCase();
    }

    private static ImFunction stubLike(ImFunction template, String name, ImType result) {
        ImVars params = JassIm.ImVars();
        for (ImVar p : template.getParameters()) {
            params.add(JassIm.ImVar(p.attrTrace(), p.getType().copy(), p.getName(), false));
        }
        return JassIm.ImFunction(template.attrTrace(), name, JassIm.ImTypeVars(), params, result,
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
    }
}
