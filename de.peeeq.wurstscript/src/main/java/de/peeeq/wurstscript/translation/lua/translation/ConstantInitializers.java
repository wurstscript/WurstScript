package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.SideEffectFreeNatives;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Which source initializers of constant globals a hot-reload payload may run again (docs/LUA_HOT_RELOAD.md,
 * "Constants"). Nothing but its initializer writes a constant, so running the edited initializer gives the value a
 * restarted map would have, provided the initializer does nothing but compute that value: it reads only its own locals,
 * other constants initialised before it and the game's constants, writes only its own global, and calls only natives
 * whose result depends on their arguments alone, the backend's operator intrinsics and functions which follow the same
 * rules. The stack-trace bookkeeping,
 * which every call pushes and pops, does not count. An initializer which allocates or calls an impure native is
 * reported as not pure: the map keeps its value.
 *
 * <p>An array constant (an array literal) is pure when it writes its own one-dimensional storage at constant indices
 * only; its {@link LuaHotReload.Constant#length} is then the number of entries it writes, which a payload clears past
 * when an edit drops some. Code may also assign an element of a constant array: storage written outside its
 * initializer ({@link #writtenElsewhere}) is listed with the variables, so it keeps its value, and so does every constant
 * which reads one of its elements.
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

    /**
     * The natives whose result depends on their arguments alone, with the conversions to the game's constant handles.
     * {@link SideEffectFreeNatives#isFunctionPure} also has getters of game state (GetPlayerTeam, GetWorldBounds,
     * GetUnitDefaultMoveSpeed ...), which a payload could find changed since the map started.
     */
    private static final Set<String> ARGUMENT_FUNCTIONS = Set.of(
        "Sin", "Cos", "Tan", "Asin", "Acos", "Atan", "Atan2", "SquareRoot", "Pow", "Deg2Rad", "Rad2Deg",
        "I2R", "R2I", "I2S", "R2S", "R2SW", "S2I", "S2R", "StringCase", "StringLength", "SubString", "StringHash",
        "And", "Or", "Not", "BlzBitOr", "BlzBitAnd", "BlzBitXor",
        "Player", "GetPlayerId", "OrderId", "OrderId2String", "AbilityId", "AbilityId2String");

    private static boolean isArgumentFunction(String name) {
        return ARGUMENT_FUNCTIONS.contains(name) || name.startsWith("Convert") && SideEffectFreeNatives.isFunctionPure(name);
    }

    /**
     * Whether a function only computes its result, the initializers of the constants it reads, and the functions of the
     * program it calls, directly or through others.
     */
    private record Summary(boolean pure, Set<ImFunction> reads, Set<ImFunction> calls) {
        static final Summary IMPURE = new Summary(false, Set.of(), Set.of());
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
                List<String> calls = new ArrayList<>();
                for (ImFunction call : summary.calls()) {
                    calls.add(luaName.apply(call));
                }
                result.put(luaName.apply(initializer), new LuaHotReload.Constant(summary.pure(), List.copyOf(reads),
                    List.copyOf(calls), analysis.length(initializer)));
            }
        }
        return result;
    }

    /**
     * The array storage of the constants among {@code order} which code other than their initializer writes: code may
     * assign an element of a constant array, so such storage holds the map's state like a variable.
     */
    static Set<ImVar> writtenElsewhere(ImTranslator imTr, ImProg prog, List<ImFunction> order) {
        ConstantInitializers analysis = new ConstantInitializers(imTr, order);
        Set<ImVar> result = Collections.newSetFromMap(new IdentityHashMap<>());
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                for (ImVar written : analysis.writtenArrays(set.getLeft())) {
                    ImFunction owner = analysis.owners.get(written);
                    if (owner != null && analysis.isConstant(owner) && set.getNearestFunc() != owner) {
                        result.add(written);
                    }
                }
            }
        });
        return result;
    }

    /** The arrays whose elements an assignment to {@code left} writes. */
    private List<ImVar> writtenArrays(ImExpr left) {
        if (left instanceof ImVarArrayAccess aa) {
            return List.of(aa.getVar());
        } else if (left instanceof ImVarAccess va && isArray(va.getVar())) {
            return List.of(va.getVar());
        } else if (left instanceof ImTupleSelection selection) {
            return writtenArrays(selection.getTupleExpr());
        } else if (left instanceof ImStatementExpr se) {
            return writtenArrays(se.getExpr());
        } else if (left instanceof ImTupleExpr tuple) {
            List<ImVar> result = new ArrayList<>();
            for (ImExpr e : tuple.getExprs()) {
                result.addAll(writtenArrays(e));
            }
            return result;
        }
        return List.of();
    }

    private static boolean isArray(ImVar v) {
        return v.getType() instanceof ImArrayType || v.getType() instanceof ImArrayTypeMulti;
    }

    /**
     * How many entries the initializer of an array constant writes: one past the largest index, when it writes its
     * one-dimensional storage at constant indices only. Null for any other initializer.
     */
    private @Nullable Integer length(ImFunction initializer) {
        Set<ImVar> storage = Collections.newSetFromMap(new IdentityHashMap<>());
        storage.addAll(imTr.loweredGlobalStorage(imTr.luaGlobalInitializers.get(initializer)));
        if (storage.stream().noneMatch(ConstantInitializers::isArray)) {
            return null;
        }
        int[] length = {0};
        boolean[] constantIndices = {true};
        initializer.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                if (set.getLeft() instanceof ImVarArrayAccess aa && storage.contains(aa.getVar())) {
                    if (aa.getIndexes().size() == 1 && aa.getIndexes().get(0) instanceof ImIntVal index && index.getValI() >= 0) {
                        length[0] = Math.max(length[0], index.getValI() + 1);
                    } else {
                        constantIndices[0] = false;
                    }
                }
            }
        });
        return constantIndices[0] ? length[0] : null;
    }

    private boolean isConstant(ImFunction initializer) {
        return imTr.luaGlobalInitializers.get(initializer).getTrace() instanceof GlobalVarDef g && g.attrIsConstant();
    }

    private Summary initializer(ImFunction initializer) {
        Set<ImVar> storage = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImVar v : imTr.loweredGlobalStorage(imTr.luaGlobalInitializers.get(initializer))) {
            if (v.getType() instanceof ImArrayTypeMulti multi && multi.getArraySize().size() > 1) {
                // A payload clears the entries an edit drops by their index, which needs one dimension.
                return Summary.IMPURE;
            }
            storage.add(v);
        }
        if (storage.stream().anyMatch(ConstantInitializers::isArray) && length(initializer) == null) {
            // Only entries at constant indices can be cleared when an edit drops them.
            return Summary.IMPURE;
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
        return new Summary(true, new LinkedHashSet<>(reads), body.calls);
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
        Summary result = body.statements(f.getBody()) ? new Summary(true, body.reads, body.calls) : Summary.IMPURE;
        inProgress.remove(f);
        summaries.put(f, result);
        return result;
    }

    /** The check of one function body: what it may write, and the constants it reads so far. */
    private final class Body {
        private final Set<ImVar> locals = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<ImVar> storage;
        final Set<ImFunction> reads = new LinkedHashSet<>();
        final Set<ImFunction> calls = new LinkedHashSet<>();

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
                // An entry of its own array literal ({@link #length} checked the indices are constants).
                return (imTr.stackTraceStorage.contains(aa.getVar()) || storage.contains(aa.getVar()))
                    && exprs(aa.getIndexes());
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
                // An element of a constant array is read like a constant. Code may assign one: such storage is listed
                // with the variables (writtenElsewhere), so what reads it keeps its value.
                return (imTr.stackTraceStorage.contains(aa.getVar()) || owners.containsKey(aa.getVar()) && read(aa.getVar()))
                    && exprs(aa.getIndexes());
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
                return isArgumentFunction(f.getName());
            }
            Summary summary = function(f);
            reads.addAll(summary.reads());
            calls.add(f);
            calls.addAll(summary.calls());
            return summary.pure();
        }
    }
}
