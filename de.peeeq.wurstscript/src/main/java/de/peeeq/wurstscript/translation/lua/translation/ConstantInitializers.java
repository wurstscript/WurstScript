package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.SideEffectFreeNatives;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.*;
import java.util.function.Function;

/**
 * Which source initializers of constant globals a hot-reload payload may run again (docs/LUA_HOT_RELOAD.md,
 * "Constants"). Nothing but its initializer writes a constant, so running the edited initializer gives the value a
 * restarted map would have, provided the initializer does nothing but compute that value: it reads only its own locals,
 * other constants initialised before it and the game's constants, writes only its own global, and calls only pure
 * natives, the backend's operator intrinsics and functions which follow the same rules. The stack-trace bookkeeping,
 * which every call pushes and pops, does not count. An initializer which allocates, calls an impure native or writes
 * array storage is reported as not pure: the map keeps its value.
 */
final class ConstantInitializers {

    private final ImTranslator imTr;
    /** Each piece of retained global storage, and the source initializer which writes it. */
    private final Map<ImVar, ImFunction> owners = new IdentityHashMap<>();
    private final Map<ImFunction, Integer> positions = new IdentityHashMap<>();
    private final Map<ImFunction, Summary> summaries = new IdentityHashMap<>();
    private final Set<ImFunction> inProgress = Collections.newSetFromMap(new IdentityHashMap<>());
    /** The compiler's own natives which the backend prints as an operator or a conversion: they only compute. */
    private final Set<ImFunction> luaOperators = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Whether a function only computes its result, and the initializers of the constants it reads. */
    private record Summary(boolean pure, Set<ImFunction> reads) {
        static final Summary IMPURE = new Summary(false, Set.of());
    }

    private ConstantInitializers(ImTranslator imTr, List<ImFunction> order) {
        this.imTr = imTr;
        for (ImFunction f : Arrays.asList(imTr.luaRawFloorDivIntFunc, imTr.luaRawFmodIntFunc, imTr.luaRawFloorModIntFunc,
            imTr.luaRawConcatFunc, imTr.luaRawOrEmptyFunc, imTr.luaRawR2IFunc, imTr.luaRawToNumberIntFunc,
            imTr.luaRawToNumberRealFunc, imTr.luaRawToIntegerFunc, imTr.luaRawToStringFunc)) {
            if (f != null) {
                luaOperators.add(f);
            }
        }
        for (ImFunction initializer : order) {
            positions.put(initializer, positions.size());
            for (ImVar storage : imTr.loweredGlobalStorage(imTr.luaGlobalInitializers.get(initializer))) {
                owners.put(storage, initializer);
            }
        }
    }

    /**
     * The constants among {@code order}, the source initializers in the order the map runs them, by the Lua name
     * {@code luaName} gives each initializer: whether a payload may run it again, and the constants it reads.
     */
    static Map<String, LuaHotReload.Constant> analyse(ImTranslator imTr, List<ImFunction> order,
                                                      Function<ImFunction, String> luaName) {
        ConstantInitializers analysis = new ConstantInitializers(imTr, order);
        Map<String, LuaHotReload.Constant> result = new LinkedHashMap<>();
        for (ImFunction initializer : order) {
            if (analysis.isConstant(initializer)) {
                Summary summary = analysis.initializer(initializer);
                List<String> reads = new ArrayList<>();
                for (ImFunction read : summary.reads()) {
                    reads.add(luaName.apply(read));
                }
                result.put(luaName.apply(initializer), new LuaHotReload.Constant(summary.pure(), List.copyOf(reads)));
            }
        }
        return result;
    }

    private boolean isConstant(ImFunction initializer) {
        return imTr.luaGlobalInitializers.get(initializer).getTrace() instanceof GlobalVarDef g && g.attrIsConstant();
    }

    private Summary initializer(ImFunction initializer) {
        Set<ImVar> storage = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImVar v : imTr.loweredGlobalStorage(imTr.luaGlobalInitializers.get(initializer))) {
            if (v.getType() instanceof ImArrayType || v.getType() instanceof ImArrayTypeMulti) {
                // Running an array initializer again would keep the entries the edit no longer stores.
                return Summary.IMPURE;
            }
            storage.add(v);
        }
        Body body = new Body(initializer, storage);
        if (!body.statements(initializer.getBody())) {
            return Summary.IMPURE;
        }
        int position = positions.get(initializer);
        List<ImFunction> reads = new ArrayList<>();
        for (ImFunction read : body.reads) {
            // A constant initialised later, or this one itself, held its default when this one ran at the start.
            if (positions.get(read) >= position) {
                return Summary.IMPURE;
            }
            reads.add(read);
        }
        reads.sort(Comparator.comparingInt(positions::get));
        return new Summary(true, new LinkedHashSet<>(reads));
    }

    private Summary function(ImFunction f) {
        Summary known = summaries.get(f);
        if (known != null) {
            return known;
        }
        if (!inProgress.add(f)) {
            // Recursion: not worth proving.
            return Summary.IMPURE;
        }
        Body body = new Body(f, Set.of());
        Summary result = body.statements(f.getBody()) ? new Summary(true, body.reads) : Summary.IMPURE;
        inProgress.remove(f);
        summaries.put(f, result);
        return result;
    }

    /** The check of one function body: what it may write, and the constants it reads so far. */
    private final class Body {
        private final Set<ImVar> locals = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<ImVar> storage;
        final Set<ImFunction> reads = new LinkedHashSet<>();

        Body(ImFunction f, Set<ImVar> storage) {
            locals.addAll(f.getLocals());
            locals.addAll(f.getParameters());
            this.storage = storage;
        }

        boolean statements(ImStmts stmts) {
            for (ImStmt s : stmts) {
                if (!statement(s)) {
                    return false;
                }
            }
            return true;
        }

        private boolean statement(ImStmt s) {
            if (s instanceof ImSet set) {
                return written(set.getLeft()) && expr(set.getRight());
            } else if (s instanceof ImIf i) {
                return expr(i.getCondition()) && statements(i.getThenBlock()) && statements(i.getElseBlock());
            } else if (s instanceof ImLoop loop) {
                return statements(loop.getBody());
            } else if (s instanceof ImExitwhen exit) {
                return expr(exit.getCondition());
            } else if (s instanceof ImReturn ret) {
                return !(ret.getReturnValue() instanceof ImExpr e) || expr(e);
            } else if (s instanceof ImExpr e) {
                return expr(e);
            }
            return false;
        }

        private boolean written(ImLExpr left) {
            if (left instanceof ImVarAccess va) {
                return locals.contains(va.getVar()) || storage.contains(va.getVar())
                    || imTr.stackTraceStorage.contains(va.getVar());
            } else if (left instanceof ImVarArrayAccess aa) {
                return imTr.stackTraceStorage.contains(aa.getVar()) && exprs(aa.getIndexes());
            }
            return false;
        }

        private boolean read(ImVar v) {
            if (locals.contains(v) || imTr.stackTraceStorage.contains(v)) {
                return true;
            }
            ImFunction owner = owners.get(v);
            if (owner != null) {
                if (!isConstant(owner)) {
                    return false;
                }
                reads.add(owner);
                return true;
            }
            // A constant of common.j or blizzard.j, which the game defines.
            return v.getIsBJ() && v.getTrace() instanceof GlobalVarDef g && g.attrIsConstant();
        }

        private boolean exprs(ImExprs es) {
            for (ImExpr e : es) {
                if (!expr(e)) {
                    return false;
                }
            }
            return true;
        }

        private boolean expr(ImExpr e) {
            if (e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImStringVal || e instanceof ImBoolVal
                || e instanceof ImNull) {
                return true;
            } else if (e instanceof ImOperatorCall op) {
                return exprs(op.getArguments());
            } else if (e instanceof ImVarAccess va) {
                return read(va.getVar());
            } else if (e instanceof ImVarArrayAccess aa) {
                return read(aa.getVar()) && exprs(aa.getIndexes());
            } else if (e instanceof ImFunctionCall call) {
                return call(call);
            } else if (e instanceof ImCast cast) {
                return expr(cast.getExpr());
            } else if (e instanceof ImTupleExpr tuple) {
                return exprs(tuple.getExprs());
            } else if (e instanceof ImTupleSelection selection) {
                return expr(selection.getTupleExpr());
            } else if (e instanceof ImStatementExpr se) {
                return statements(se.getStatements()) && expr(se.getExpr());
            }
            return false;
        }

        private boolean call(ImFunctionCall call) {
            if (call.getCallType() == CallType.EXECUTE || !exprs(call.getArguments())) {
                return false;
            }
            ImFunction f = call.getFunc();
            if (luaOperators.contains(f)) {
                return true;
            }
            if (f.isNative() || f.isBj() || f.isExtern()) {
                return SideEffectFreeNatives.isFunctionPure(f.getName());
            }
            Summary summary = function(f);
            reads.addAll(summary.reads());
            return summary.pure();
        }
    }
}
