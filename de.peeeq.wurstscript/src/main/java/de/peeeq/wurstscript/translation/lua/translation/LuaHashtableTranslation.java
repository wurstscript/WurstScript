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
 *     <li>a load is {@code (h.T[p] or __wurst_htEmpty)[c]}, then the default the helper answers: {@code or 0},
 *     {@code or 0.0}, {@code == true}, and nothing for a string or a handle, which are nil when absent;</li>
 *     <li>a HaveSaved test is the same read {@code ~= nil};</li>
 *     <li>a save is {@code (h.T[p] or __wurst_htNewChild(h.T, p))[c] = v};</li>
 *     <li>a remove is {@code if h.T[p] then h.T[p][c] = nil end}. Not through the empty child: in Lua 5.3
 *     {@code t[k] = nil} under an absent key inserts a dead key, and nothing may write the shared table;</li>
 *     <li>FlushChildHashtable is {@code h.T[p] = nil} and FlushParentHashtable {@code h.T = {}}, for each T.</li>
 * </ul>
 * A missing key, a nil hashtable and a nil key on a store raise or read nil exactly as in the helpers.
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
        LuaExpr child = LuaAst.LuaExprBinary(entry(operands.get(0), operands.get(1), op.values()), LuaAst.LuaOpOr(),
            LuaAst.LuaExprVarAccess(tr.hashtableEmpty()));
        LuaExpr value = LuaAst.LuaExprArrayAccess(child, LuaAst.LuaExprlist(operands.get(2)));
        if (op.kind() == LuaHashtable.Kind.HAVE) {
            return LuaAst.LuaExprBinary(value, LuaAst.LuaOpUnequals(), LuaAst.LuaExprNull());
        }
        return switch (op.values()) {
            case INT -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpOr(), LuaAst.LuaExprIntVal("0"));
            case REAL -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpOr(), LuaAst.LuaExprRealVal("0.0"));
            case BOOL -> LuaAst.LuaExprBinary(value, LuaAst.LuaOpEquals(), LuaAst.LuaExprBoolVal(true));
            case STR, HANDLE -> value;
        };
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
        switch (kind) {
            case SAVE -> {
                LuaExpr p = operands.get(1);
                LuaExpr created = LuaAst.LuaExprFunctionCall(tr.hashtableNewChild(),
                    LuaAst.LuaExprlist(subtable(h.copy(), op.values()), p.copy()));
                LuaExpr child = LuaAst.LuaExprBinary(entry(h, p, op.values()), LuaAst.LuaOpOr(), created);
                res.add(LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(child, LuaAst.LuaExprlist(operands.get(2))),
                    operands.get(3)));
            }
            case REMOVE -> {
                LuaExpr p = operands.get(1);
                LuaAssignment remove = LuaAst.LuaAssignment(LuaAst.LuaExprArrayAccess(
                    entry(h.copy(), p.copy(), op.values()), LuaAst.LuaExprlist(operands.get(2))), LuaAst.LuaExprNull());
                res.add(LuaAst.LuaIf(entry(h, p, op.values()), LuaAst.LuaStatements(remove), LuaAst.LuaStatements()));
            }
            case FLUSH_CHILD -> {
                for (LuaHashtable.Values values : LuaHashtable.Values.values()) {
                    res.add(LuaAst.LuaAssignment(entry(h.copy(), operands.get(1).copy(), values), LuaAst.LuaExprNull()));
                }
            }
            case FLUSH_PARENT -> {
                for (LuaHashtable.Values values : LuaHashtable.Values.values()) {
                    res.add(LuaAst.LuaAssignment(subtable(h.copy(), values), emptyTable()));
                }
            }
            default -> throw new IllegalStateException("not a statement: " + op);
        }
        return true;
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
