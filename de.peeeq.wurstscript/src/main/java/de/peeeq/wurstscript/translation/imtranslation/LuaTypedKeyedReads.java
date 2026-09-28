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
 * wrapper that returns that read on every path, directly or through a local nothing else reads (the
 * shape stack traces give a returning function). A wrapper gets a typed twin, one per wrapper and
 * type, in which that read is the typed one; its other statements stay as they are. The ensured call
 * is redirected to the twin. Only function calls are followed, not dispatching method calls:
 * {@link LuaMethodCallLowering} turns the hot ones into function calls, and this runs again after it.
 *
 * <p>Stubs are matched by identity, through the registry {@link LuaNativeLowering#lowerKeyedTables}
 * fills, never by their generated names.
 */
public final class LuaTypedKeyedReads {

    private LuaTypedKeyedReads() {
    }

    public static void transform(ImProg prog, ImTranslator translator) {
        ImFunction untypedGet = translator.luaKeyedStubs.get(LuaKeyedMap.NATIVE_GET);
        if (untypedGet == null) {
            return;
        }
        Map<ImFunction, Map<String, ImFunction>> twins = new HashMap<>();
        List<ImFunction> additions = new ArrayList<>();
        List<Runnable> rewrites = new ArrayList<>();

        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                String stubName = typedStubForEnsure(call.getFunc(), translator);
                // The value is the first argument. Stack traces append a parameter to every
                // non-native function, the ensure helpers included; the typed stub needs none.
                if (stubName != null && !call.getArguments().isEmpty()) {
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
                if (read.getFunc() == untypedGet) {
                    target = typedStub(stubName);
                } else if (returnedRead(read.getFunc(), untypedGet) != null) {
                    target = twins.computeIfAbsent(read.getFunc(), f -> new HashMap<>())
                        .computeIfAbsent(stubName, name -> twin(read.getFunc(), typedStub(name)));
                } else {
                    return;
                }
                rewrites.add(() -> ensured.replaceBy(JassIm.ImFunctionCall(read.attrTrace(), target,
                    read.getTypeArguments().copy(), read.getArguments().copy(), false, CallType.NORMAL)));
            }

            private ImFunction typedStub(String name) {
                return translator.luaKeyedStubs.computeIfAbsent(name, n -> {
                    ImFunction stub = stubLike(untypedGet, n, typedResult(n));
                    additions.add(stub);
                    return stub;
                });
            }

            /** A copy of the wrapper whose returned read is the typed one. */
            private ImFunction twin(ImFunction wrapper, ImFunction typed) {
                ImFunction copy = wrapper.copyWithRefs();
                copy.setName(wrapper.getName() + "_" + typedSuffix(typed.getName()));
                copy.setReturnType(typed.getReturnType().copy());
                ImFunctionCall inner = returnedRead(copy, untypedGet);
                if (inner == null) {
                    throw new IllegalStateException("copy of " + wrapper.getName() + " lost its returned read");
                }
                // A local that carries the read to the return takes the typed value's type too.
                for (ImVar carrier : carriers(copy, inner)) {
                    carrier.setType(typed.getReturnType().copy());
                }
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
     * The one untyped read that f returns on every path, either directly or through a local which is
     * assigned only that read and read only by the returns; null when f has any other shape.
     */
    private static @Nullable ImFunctionCall returnedRead(ImFunction f, ImFunction untypedGet) {
        if (f.isNative()) {
            return null;
        }
        List<ImReturn> returns = new ArrayList<>();
        List<ImFunctionCall> reads = new ArrayList<>();
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImReturn r) {
                super.visit(r);
                returns.add(r);
            }

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                if (c.getFunc() == untypedGet) {
                    reads.add(c);
                }
            }
        });
        if (returns.isEmpty() || reads.size() != 1) {
            return null;
        }
        ImFunctionCall read = reads.get(0);
        for (ImReturn r : returns) {
            if (r.getReturnValue() == read) {
                continue;
            }
            if (r.getReturnValue() instanceof ImVarAccess va && carriesOnly(f, va.getVar(), read)) {
                continue;
            }
            return null;
        }
        return read;
    }

    /** The locals through which the read reaches a return. */
    private static List<ImVar> carriers(ImFunction f, ImFunctionCall read) {
        List<ImVar> result = new ArrayList<>();
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImReturn r) {
                super.visit(r);
                if (r.getReturnValue() instanceof ImVarAccess va && !result.contains(va.getVar())) {
                    result.add(va.getVar());
                }
            }
        });
        return result;
    }

    /** Whether v is a local of f assigned nothing but read, and read nowhere but by returns. */
    private static boolean carriesOnly(ImFunction f, ImVar v, ImFunctionCall read) {
        if (!f.getLocals().contains(v)) {
            return false;
        }
        boolean[] ok = {true};
        int[] writes = {0};
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImSet s) {
                super.visit(s);
                if (s.getLeft() instanceof ImVarAccess left && left.getVar() == v) {
                    writes[0]++;
                    if (s.getRight() != read) {
                        ok[0] = false;
                    }
                }
            }

            @Override
            public void visit(ImVarAccess a) {
                super.visit(a);
                if (a.getVar() != v) {
                    return;
                }
                boolean isAssignedTarget = a.getParent() instanceof ImSet s && s.getLeft() == a;
                boolean isReturned = a.getParent() instanceof ImReturn;
                if (!isAssignedTarget && !isReturned) {
                    ok[0] = false;
                }
            }
        });
        return ok[0] && writes[0] == 1;
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
