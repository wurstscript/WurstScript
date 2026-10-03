package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <p>The ensure of the untyped read itself is folded, and so is the ensure of a call to a wrapper. A
 * wrapper returns one value on every path, directly or through a local nothing else reads (the shape
 * stack traces give a returning function), and that value is the untyped read, a call to another
 * wrapper, or a method call {@link LuaMethodCallLowering#canLowerDirectly} would lower whose
 * implementation is a wrapper - so a delegation chain such as {@code op_index -> get} folds as a
 * whole. Each wrapper gets a typed twin per type, whose other statements stay as they are and whose
 * returned value is the typed read or the next twin, called directly. This has to happen before
 * inlining: the inliner expands the ensure in its first round, before a later round exposes the read
 * behind a delegation. A chain starts at an ensured function call, or at an ensured method call that
 * cannot dispatch anywhere else, in a loop or not: such a call becomes a direct call of the twin, the
 * way the Lua emitter prints it anyway. A read in a function a hot loop calls, such as a map lookup in
 * a library, is folded too. A method call that can dispatch is left alone.
 *
 * <p>Stubs are matched by identity, through the registry {@link LuaNativeLowering#lowerKeyedTables}
 * fills, never by their generated names.
 */
public final class LuaTypedKeyedReads {

    private LuaTypedKeyedReads() {
    }

    public static void transform(ImProg prog, ImTranslator translator) {
        ImFunction untypedGet = translator.luaKeyedStubs.get(LuaKeyedMap.NATIVE_GET);
        if (untypedGet != null) {
            new Folder(prog, translator, untypedGet).run();
        }
    }

    private static final class Folder {
        private final ImProg prog;
        private final ImTranslator translator;
        private final ImFunction untypedGet;
        private final Map<ImFunction, Map<String, ImFunction>> twins = new HashMap<>();
        private final List<ImFunction> additions = new ArrayList<>();
        /** The wrappers being examined, so a recursive chain is rejected rather than followed forever. */
        private final Set<ImFunction> examining = new HashSet<>();

        Folder(ImProg prog, ImTranslator translator, ImFunction untypedGet) {
            this.prog = prog;
            this.translator = translator;
            this.untypedGet = untypedGet;
        }

        void run() {
            List<Runnable> rewrites = new ArrayList<>();
            prog.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImFunctionCall call) {
                    super.visit(call);
                    String stubName = typedStubForEnsure(call.getFunc(), translator);
                    // The value is the first argument. Stack traces append a parameter to every
                    // non-native function, the ensure helpers included; the typed stub needs none.
                    if (stubName != null && !call.getArguments().isEmpty()) {
                        plan(call, call.getArguments().get(0), stubName, rewrites);
                    }
                }

                @Override
                public void visit(ImOperatorCall call) {
                    super.visit(call);
                    // The bool ensure is printed as `x == true` rather than as a helper call.
                    ImExprs args = call.getArguments();
                    if (call.getOp() == WurstOperator.EQ && args.size() == 2
                        && args.get(1) instanceof ImBoolVal b && b.getValB()) {
                        plan(call, args.get(0), LuaKeyedMap.NATIVE_GET_BOOL, rewrites);
                    }
                }
            });
            // Rewritten after the walk, so no replaced node is visited during it.
            for (Runnable rewrite : rewrites) {
                rewrite.run();
            }
            prog.getFunctions().addAll(additions);
        }

        private void plan(ImExpr ensured, ImExpr value, String stubName, List<Runnable> rewrites) {
            // The replacement copies the arguments when it runs, after the rewrites of reads nested in them.
            if (value instanceof ImFunctionCall read) {
                ImFunction target;
                if (read.getFunc() == untypedGet) {
                    target = typedStub(stubName);
                } else if (isWrapper(read.getFunc())) {
                    target = twin(read.getFunc(), stubName);
                } else {
                    return;
                }
                rewrites.add(() -> ensured.replaceBy(call(read.attrTrace(), target,
                    read.getTypeArguments().copy(), read.getArguments().copy())));
            } else if (value instanceof ImMethodCall m && isReadSource(m)) {
                ImFunction target = twin(m.getMethod().getImplementation(), stubName);
                rewrites.add(() -> ensured.replaceBy(directCall(m, target)));
            }
        }

        private boolean isWrapper(ImFunction f) {
            if (f == null || f.isNative() || !examining.add(f)) {
                return false;
            }
            try {
                ImExpr source = returnedSource(f);
                return source != null && isReadSource(source);
            } finally {
                examining.remove(f);
            }
        }

        private boolean isReadSource(ImExpr e) {
            if (e instanceof ImFunctionCall c) {
                return c.getFunc() == untypedGet || isWrapper(c.getFunc());
            }
            if (e instanceof ImMethodCall m) {
                return LuaMethodCallLowering.canLowerDirectly(m.getMethod())
                    && isWrapper(m.getMethod().getImplementation());
            }
            return false;
        }

        private ImFunction typedStub(String name) {
            return translator.luaKeyedStubs.computeIfAbsent(name, n -> {
                ImFunction stub = stubLike(untypedGet, n, typedResult(n));
                additions.add(stub);
                return stub;
            });
        }

        /** The wrapper's typed copy: its returned value is the typed read, or the next wrapper's twin. */
        private ImFunction twin(ImFunction wrapper, String stubName) {
            Map<String, ImFunction> byType = twins.computeIfAbsent(wrapper, w -> new HashMap<>());
            ImFunction existing = byType.get(stubName);
            if (existing != null) {
                return existing;
            }
            ImFunction copy = wrapper.copyWithRefs();
            byType.put(stubName, copy);
            copy.setName(wrapper.getName() + "_" + typedSuffix(stubName));
            copy.setReturnType(typedResult(stubName));
            ImExpr source = returnedSource(copy);
            if (source == null) {
                throw new IllegalStateException("copy of " + wrapper.getName() + " lost its returned value");
            }
            // A local that carries the value to the return takes the typed value's type too.
            for (ImVar carrier : carriers(copy)) {
                carrier.setType(typedResult(stubName));
            }
            ImExpr replacement;
            if (source instanceof ImFunctionCall c && c.getFunc() == untypedGet) {
                replacement = call(c.attrTrace(), typedStub(stubName), JassIm.ImTypeArguments(), c.getArguments().copy());
            } else if (source instanceof ImFunctionCall c) {
                replacement = call(c.attrTrace(), twin(c.getFunc(), stubName), c.getTypeArguments().copy(),
                    c.getArguments().copy());
            } else {
                ImMethodCall m = (ImMethodCall) source;
                replacement = directCall(m, twin(m.getMethod().getImplementation(), stubName));
            }
            source.replaceBy(replacement);
            additions.add(copy);
            return copy;
        }
    }

    private static ImFunctionCall call(de.peeeq.wurstscript.ast.Element trace, ImFunction f, ImTypeArguments typeArgs,
                                       ImExprs args) {
        return JassIm.ImFunctionCall(trace, f, typeArgs, args, false, CallType.NORMAL);
    }

    /** A call of f in place of a method call nothing overrides, receiver first, as LuaMethodCallLowering lowers it. */
    private static ImFunctionCall directCall(ImMethodCall m, ImFunction f) {
        ImExprs args = JassIm.ImExprs(m.getReceiver().copy());
        args.addAll(m.getArguments().copy().removeAll());
        return call(m.attrTrace(), f, m.getTypeArguments().copy(), args);
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
     * The one value f returns on every path: each return returns that node, or a local that is
     * assigned only that node and read by nothing but returns. Null when f has any other shape.
     */
    private static @Nullable ImExpr returnedSource(ImFunction f) {
        List<ImReturn> returns = returnsOf(f);
        if (returns.isEmpty()) {
            return null;
        }
        ImExpr source = null;
        for (ImReturn r : returns) {
            ImExpr value;
            if (r.getReturnValue() instanceof ImVarAccess va && f.getLocals().contains(va.getVar())) {
                value = soleAssignment(f, va.getVar());
            } else if (r.getReturnValue() instanceof ImExpr e) {
                value = e;
            } else {
                return null;
            }
            if (value == null || (source != null && source != value)) {
                return null;
            }
            source = value;
        }
        return source;
    }

    private static List<ImReturn> returnsOf(ImFunction f) {
        List<ImReturn> returns = new ArrayList<>();
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImReturn r) {
                super.visit(r);
                returns.add(r);
            }
        });
        return returns;
    }

    /** The locals through which the value reaches a return. */
    private static List<ImVar> carriers(ImFunction f) {
        List<ImVar> result = new ArrayList<>();
        for (ImReturn r : returnsOf(f)) {
            if (r.getReturnValue() instanceof ImVarAccess va && f.getLocals().contains(va.getVar())
                && !result.contains(va.getVar())) {
                result.add(va.getVar());
            }
        }
        return result;
    }

    /** The right side of the one assignment to v, if v is read nowhere but by returns; otherwise null. */
    private static @Nullable ImExpr soleAssignment(ImFunction f, ImVar v) {
        List<ImExpr> assigned = new ArrayList<>();
        boolean[] ok = {true};
        f.getBody().accept(new ImStmts.DefaultVisitor() {
            @Override
            public void visit(ImSet s) {
                super.visit(s);
                if (s.getLeft() instanceof ImVarAccess left && left.getVar() == v) {
                    assigned.add(s.getRight());
                }
            }

            @Override
            public void visit(ImVarAccess a) {
                super.visit(a);
                if (a.getVar() == v) {
                    boolean isAssignedTarget = a.getParent() instanceof ImSet s && s.getLeft() == a;
                    boolean isReturned = a.getParent() instanceof ImReturn;
                    if (!isAssignedTarget && !isReturned) {
                        ok[0] = false;
                    }
                }
            }
        });
        return ok[0] && assigned.size() == 1 ? assigned.get(0) : null;
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
