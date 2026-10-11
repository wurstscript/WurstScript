package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.LuaHashtable;

import java.util.ArrayList;
import java.util.List;

/**
 * The hashtable natives printed where they are called, as the table accesses their helpers consist of
 * ({@link LuaHashtable} has the representation). A helper call costs a global lookup and a call on every
 * execution, and maps call these in their hottest code. With {@code h, p, c, v} the arguments and {@code T}
 * the subtable of the value kind:
 * <ul>
 *     <li>InitHashtable is the table constructor;</li>
 *     <li>a load is {@code (h and h.T[p] or __wurst_htEmpty)[c]}, then the default the helper answers:
 *     {@code or 0}, {@code or 0.0}, {@code == true}, and nothing for a string; a handle load answers the value only
 *     when the set of tags it accepts holds the tag saved with it, {@code (__wurst_htAccepts_X[tag] and value or nil)}
 *     with tag the same read of {@code __wurst_ht_htype};</li>
 *     <li>a HaveSaved test is the same read {@code ~= nil};</li>
 *     <li>a save is {@code if h then (h.T[p] or __wurst_htNewChild(h.T, p))[c] = v end}; a handle save also writes
 *     its tag, and does nothing for a null handle ({@code if h and v ~= nil then});</li>
 *     <li>a remove is {@code if h and h.T[p] then h.T[p][c] = nil end}, for a handle in the tag subtable too. Not
 *     through the empty child: in Lua 5.3 {@code t[k] = nil} under an absent key inserts a dead key, and nothing may
 *     write the shared table;</li>
 *     <li>FlushChildHashtable is {@code h.T[p] = nil} and FlushParentHashtable {@code h.T = __wurst_htFlushed}, for
 *     each T, under {@code if h then}. The flushed table ignores writes, so a flushed hashtable keeps nothing.</li>
 * </ul>
 * Each answers what Jass answers ({@link LuaHashtable}), as the helpers do; a nil hashtable reads defaults and
 * ignores writes.
 *
 * <p>A call evaluates each argument once and in order, before the body. Printed in place an operand is read
 * once more on some paths (a save which creates the child, a remove) or not at all (the child key of a remove
 * whose child is absent), and the later operands are read after part of the body ran, which only touches the
 * compiler-owned tables. That is the same only when no operand has an effect, so an operation is printed in
 * place only when each operand is a literal, a variable or a read ({@link StmtTranslation#readsOnly}), and is
 * otherwise the call of its helper, which is then defined ({@link LuaTranslator#hashtableHelper}).
 */
final class LuaHashtableTranslation {

    /** The main-chunk locals the in-place operations use, each declared when first used. */
    static final String EMPTY = "__wurst_htEmpty";
    static final String NEW_CHILD = "__wurst_htNewChild";
    static final String FLUSHED = "__wurst_htFlushed";
    static final String ACCEPTS = "__wurst_htAccepts_";

    private LuaHashtableTranslation() {
    }

    /** An operation in an expression: InitHashtable, a load or a test in place, anything else as the call. */
    static LuaExpr expression(ImFunctionCall call, LuaHashtable.Op op, LuaTranslator tr) {
        ImExprs args = checkArity(call, op);
        if (op.kind() == LuaHashtable.Kind.INIT) {
            return newHashtable();
        }
        boolean read = op.kind() == LuaHashtable.Kind.LOAD || op.kind() == LuaHashtable.Kind.HAVE;
        if (!read || !StmtTranslation.readsOnly(args)) {
            return LuaAst.LuaExprFunctionCall(tr.hashtableHelper(call.getFunc()), tr.translateExprList(args));
        }
        List<LuaExpr> operands = translate(args, tr);
        LuaExpr value = read(operands, op.values(), tr);
        if (op.kind() == LuaHashtable.Kind.HAVE) {
            return LuaAst.LuaExprBinary(value, LuaAst.LuaOpUnequals(), LuaAst.LuaExprNull());
        }
        return switch (op.values()) {
            case INT -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpOr(), LuaAst.LuaExprIntVal("0"));
            case REAL -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpOr(), LuaAst.LuaExprRealVal("0.0"));
            case BOOL -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpEquals(), LuaAst.LuaExprBoolVal(true));
            case STR -> value;
            case HANDLE -> {
                LuaExpr accepted = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.hashtableAccepts(op.handleType())),
                    LuaAst.LuaExprlist(read(copies(operands), LuaHashtable.Values.HANDLE_TYPE, tr)));
                yield LuaAst.LuaExprBinary(LuaAst.LuaExprBinary(accepted, LuaAst.LuaOpAnd(), value), LuaAst.LuaOpOr(),
                    LuaAst.LuaExprNull());
            }
            case HANDLE_TYPE -> throw new IllegalStateException("no operation reads the tags: " + op);
        };
    }

    /** {@code (h and h.T[p] or __wurst_htEmpty)[c]}: the value under the keys, nil when there is none. */
    private static LuaExpr read(List<LuaExpr> operands, LuaHashtable.Values values, LuaTranslator tr) {
        LuaExpr child = LuaAst.LuaExprBinary(
            LuaAst.LuaExprBinary(operands.get(0).copy(), LuaAst.LuaOpAnd(), entry(operands.get(0).copy(), operands.get(1).copy(), values)),
            LuaAst.LuaOpOr(), LuaAst.LuaExprVarAccess(tr.hashtableEmpty()));
        return LuaAst.LuaExprArrayAccess(child, LuaAst.LuaExprlist(operands.get(2).copy()));
    }

    private static List<LuaExpr> copies(List<LuaExpr> operands) {
        List<LuaExpr> result = new ArrayList<>();
        for (LuaExpr operand : operands) {
            result.add(operand.copy());
        }
        return result;
    }

    /**
     * A store, a remove or a flush in place as statements, when no operand has an effect. Returns false for
     * anything else, which is then translated as an expression.
     */
    static boolean statement(ImFunctionCall call, LuaHashtable.Op op, List<LuaStatement> res, LuaTranslator tr) {
        LuaHashtable.Kind kind = op.kind();
        if (kind == LuaHashtable.Kind.INIT || kind == LuaHashtable.Kind.LOAD || kind == LuaHashtable.Kind.HAVE) {
            return false;
        }
        ImExprs args = checkArity(call, op);
        if (!StmtTranslation.readsOnly(args)) {
            return false;
        }
        List<LuaExpr> operands = translate(args, tr);
        LuaExpr h = operands.get(0);
        LuaStatements body = LuaAst.LuaStatements();
        LuaExpr guard = h.copy();
        switch (kind) {
            case SAVE -> {
                LuaExpr value = operands.get(3);
                if (op.values() == LuaHashtable.Values.HANDLE) {
                    if (value instanceof LuaExprNull) {
                        // saving a null handle stores nothing
                        return true;
                    }
                    guard = LuaAst.LuaExprBinary(guard, LuaAst.LuaOpAnd(),
                        LuaAst.LuaExprBinary(value.copy(), LuaAst.LuaOpUnequals(), LuaAst.LuaExprNull()));
                    body.add(store(operands, LuaHashtable.Values.HANDLE_TYPE, LuaAst.LuaExprStringVal(op.handleType()), tr));
                }
                body.add(0, store(operands, op.values(), value, tr));
            }
            case REMOVE -> {
                LuaExpr p = operands.get(1);
                guard = LuaAst.LuaExprBinary(guard, LuaAst.LuaOpAnd(), entry(h.copy(), p.copy(), op.values()));
                body.add(LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(entry(h.copy(), p.copy(), op.values()),
                    LuaAst.LuaExprlist(operands.get(2).copy())), LuaAst.LuaExprNull()));
                if (op.values() == LuaHashtable.Values.HANDLE) {
                    // every handle has a tag under the same keys, so the tag subtable has the child too
                    body.add(LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(
                        entry(h.copy(), p.copy(), LuaHashtable.Values.HANDLE_TYPE), LuaAst.LuaExprlist(operands.get(2).copy())),
                        LuaAst.LuaExprNull()));
                }
            }
            case FLUSH_CHILD -> {
                for (LuaHashtable.Values values : LuaHashtable.Values.values()) {
                    body.add(LuaAst.LuaAssignment(entry(h.copy(), operands.get(1).copy(), values), LuaAst.LuaExprNull()));
                }
            }
            case FLUSH_PARENT -> {
                for (LuaHashtable.Values values : LuaHashtable.Values.values()) {
                    body.add(LuaAst.LuaAssignment(subtable(h.copy(), values), LuaAst.LuaExprVarAccess(tr.hashtableFlushed())));
                }
            }
            default -> throw new IllegalStateException("not a statement: " + op);
        }
        res.add(LuaAst.LuaIf(guard, body, LuaAst.LuaStatements()));
        return true;
    }

    /** {@code (h.T[p] or __wurst_htNewChild(h.T, p))[c] = v}. */
    private static LuaStatement store(List<LuaExpr> operands, LuaHashtable.Values values, LuaExpr value, LuaTranslator tr) {
        LuaExpr h = operands.get(0);
        LuaExpr p = operands.get(1);
        LuaExpr created = LuaAst.LuaExprFunctionCall(tr.hashtableNewChild(),
            LuaAst.LuaExprlist(subtable(h.copy(), values), p.copy()));
        LuaExpr child = LuaAst.LuaExprBinary(entry(h.copy(), p.copy(), values), LuaAst.LuaOpOr(), created);
        return LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(child, LuaAst.LuaExprlist(operands.get(2).copy())), value.copy());
    }

    /** {@code { __wurst_ht_int = {}, ... }}: the representation InitHashtable creates. */
    private static LuaExpr newHashtable() {
        LuaTableFields fields = LuaAst.LuaTableFields();
        for (LuaHashtable.Values values : LuaHashtable.Values.values()) {
            fields.add(LuaAst.LuaTableNamedField(values.field, emptyTable()));
        }
        return LuaAst.LuaTableConstructor(fields);
    }

    /** The shared child of every absent parent key, which nothing writes. */
    static LuaVariable createEmpty(LuaTranslator tr) {
        LuaVariable empty = LuaAst.LuaVariable(EMPTY, emptyTable());
        tr.declareChunkLocal(empty);
        return empty;
    }

    /**
     * The table every subtable of a flushed hashtable is: Jass destroys a hashtable on FlushParentHashtable, so
     * nothing written to it stays. Its children are never stored, and every read under it answers nil.
     */
    static LuaVariable createFlushed(LuaTranslator tr) {
        LuaVariable flushed = LuaAst.LuaVariable(FLUSHED, LuaAst.LuaLiteral(
            "setmetatable({}, { __newindex = function() end })"));
        tr.declareChunkLocal(flushed);
        return flushed;
    }

    /** {@code { Unit = true, Widget = true, Agent = true }}: the tags a load of {@code type} answers the handle for. */
    static LuaVariable createAccepts(String type, LuaTranslator tr) {
        LuaTableFields fields = LuaAst.LuaTableFields();
        for (String tag : LuaHashtable.acceptedTags(type)) {
            fields.add(LuaAst.LuaTableNamedField(tag, LuaAst.LuaExprBoolVal(true)));
        }
        LuaVariable accepts = LuaAst.LuaVariable(ACCEPTS + type, LuaAst.LuaTableConstructor(fields));
        tr.declareChunkLocal(accepts);
        return accepts;
    }

    /** {@code (t, p)}: stores a new child under the parent key and answers it. */
    static LuaFunction createNewChild(LuaTranslator tr) {
        LuaVariable table = LuaAst.LuaVariable("t", LuaAst.LuaNoExpr());
        LuaVariable parentKey = LuaAst.LuaVariable("p", LuaAst.LuaNoExpr());
        LuaVariable child = LuaAst.LuaVariable("x", emptyTable());
        LuaFunction newChild = LuaAst.LuaFunction(NEW_CHILD, LuaAst.LuaParams(table, parentKey),
            LuaAst.LuaStatements(
                child,
                LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(table),
                    LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(parentKey))), LuaAst.LuaExprVarAccess(child)),
                LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(child))));
        tr.declareChunkLocal(newChild);
        return newChild;
    }

    /** {@code h.T}. */
    private static LuaExpr subtable(LuaExpr h, LuaHashtable.Values values) {
        return LuaAst.LuaExprFieldAccess(h, values.field);
    }

    /** {@code h.T[p]}: the child table under a parent key, nil when there is none. */
    private static LuaExpr entry(LuaExpr h, LuaExpr p, LuaHashtable.Values values) {
        return LuaAst.LuaExprArrayAccess(subtable(h, values), LuaAst.LuaExprlist(p));
    }

    private static LuaExpr emptyTable() {
        return LuaAst.LuaTableConstructor(LuaAst.LuaTableFields());
    }

    private static List<LuaExpr> translate(ImExprs args, LuaTranslator tr) {
        List<LuaExpr> operands = new ArrayList<>();
        for (ImExpr arg : args) {
            operands.add(arg.translateToLua(tr));
        }
        return operands;
    }

    private static ImExprs checkArity(ImFunctionCall call, LuaHashtable.Op op) {
        ImExprs args = call.getArguments();
        if (args.size() != op.kind().arity) {
            throw new CompileError(call.attrTrace().attrSource(), "Lua backend: " + call.getFunc().getName()
                + " expects " + op.kind().arity + " arguments, got " + args.size() + ".");
        }
        return args;
    }
}
