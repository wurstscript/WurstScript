package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.LuaKeyedMap;
import de.peeeq.wurstscript.translation.imtranslation.LuaMultipleResults;
import de.peeeq.wurstscript.translation.imtranslation.LuaTraps;
import de.peeeq.wurstscript.translation.lua.printing.LuaPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_ABORT_THREAD_SENTINEL;
import de.peeeq.wurstscript.jassIm.ImFunction;

public class StmtTranslation {

    public static void translate(ImExpr e, List<LuaStatement> res, LuaTranslator tr) {
        // In Lua mode, package init functions are called directly and wrapped with xpcall.
        if (e instanceof ImFunctionCall call) {
            if (tr.imTr.luaInitFunctions.containsKey(call.getFunc())) {
                emitLuaInitXpcall(call.getFunc(), res, tr);
                return;
            }
            String keyedWrite = LuaKeyedMap.writeStubName(tr.imTr, call.getFunc());
            if (keyedWrite != null) {
                translateKeyedMapWrite(call, keyedWrite, res, tr);
                return;
            }
        }
        LuaExpr expr = e.translateToLua(tr);
        res.add(expr);
    }

    /** How an operand of a keyed-map store may be moved: see {@link #translateKeyedMapWrite}. */
    private enum WriteOperand {
        /** A literal or a local: the same value wherever it is read, and reading it does nothing. */
        STABLE,
        /** A global or a table read: reading it does nothing, but an effect before it can change it. */
        READ,
        /** Anything else, such as a call. */
        EFFECT
    }

    private static WriteOperand writeOperand(ImExpr e) {
        if (e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImStringVal
            || e instanceof ImBoolVal || e instanceof ImNull) {
            return WriteOperand.STABLE;
        }
        if (LuaTraps.mayRaise(e)) {
            // It must be evaluated even when the key is nil, as the stub's argument was: an array field read
            // through a null object raises.
            return WriteOperand.EFFECT;
        }
        if (e instanceof ImVarAccess access) {
            return access.getVar().isGlobal() ? WriteOperand.READ : WriteOperand.STABLE;
        }
        if (e instanceof ImVarArrayAccess access && readsOnly(access.getIndexes())) {
            return WriteOperand.READ;
        }
        if (e instanceof ImMemberAccess access && writeOperand(access.getReceiver()) != WriteOperand.EFFECT
            && readsOnly(access.getIndexes())) {
            return WriteOperand.READ;
        }
        return WriteOperand.EFFECT;
    }

    private static boolean readsOnly(ImExprs exprs) {
        for (ImExpr e : exprs) {
            if (writeOperand(e) == WriteOperand.EFFECT) {
                return false;
            }
        }
        return true;
    }

    /**
     * A keyed-map put or remove as the table store it stands for, {@code t[k] = v} or
     * {@code t[k] = nil}, instead of a call to a stub with that body. Storing under a nil key is an
     * error in Lua, where reading one only answers nil, so a key which may be nil is tested first: a
     * null element stores nothing and reads as absent. Any key but a literal may be nil, an int too
     * when it is a field read through an object which is null, so only a literal is stored without
     * the test.
     *
     * <p>Each operand is evaluated once and in the order of the call. A plain store does that by
     * itself. The test reads the key twice and evaluates the table and the value only when the key
     * is not nil, so there an operand with an effect, and each operand before it which is not
     * {@link WriteOperand#STABLE}, is first evaluated into a local.
     */
    private static void translateKeyedMapWrite(ImFunctionCall call, String stub, List<LuaStatement> res,
                                               LuaTranslator tr) {
        boolean put = LuaKeyedMap.NATIVE_PUT.equals(stub);
        ImExprs args = call.getArguments();
        int arity = put ? 3 : 2;
        if (args.size() != arity) {
            throw new CompileError(call.attrTrace().attrSource(),
                "Lua backend: " + stub + " expects " + arity + " arguments, got " + args.size() + ".");
        }
        ImExpr key = args.get(1);
        // Only a literal: a key of any type can hold nil, read through an object which is null (LuaTraps).
        boolean keyNeverNil = key instanceof ImIntVal || key instanceof ImRealVal || key instanceof ImStringVal
            || key instanceof ImBoolVal;
        List<LuaExpr> operands = new ArrayList<>();
        for (ImExpr arg : args) {
            operands.add(arg.translateToLua(tr));
        }
        boolean[] intoLocal = new boolean[arity];
        if (!keyNeverNil) {
            int lastEffect = -1;
            for (int i = 0; i < arity; i++) {
                if (writeOperand(args.get(i)) == WriteOperand.EFFECT) {
                    lastEffect = i;
                }
            }
            for (int i = 0; i <= lastEffect; i++) {
                intoLocal[i] = writeOperand(args.get(i)) != WriteOperand.STABLE;
            }
        }
        // A statement must not start with '(' (Lua would join it onto the previous line as a call).
        intoLocal[0] |= !LuaPrinter.startsWithName(operands.get(0));
        String[] names = {"__wurst_map", "__wurst_key", "__wurst_value"};
        for (int i = 0; i < arity; i++) {
            if (intoLocal[i]) {
                LuaVariable local = LuaAst.LuaVariable(tr.uniqueName(names[i]), operands.get(i));
                res.add(local);
                operands.set(i, LuaAst.LuaExprVarAccess(local));
            }
        }
        LuaExpr value = put ? operands.get(2) : LuaAst.LuaExprNull();
        LuaExpr keyExpr = operands.get(1);
        LuaAssignment store = LuaAst.LuaAssignment(
            LuaAst.LuaExprArrayAccess(operands.get(0), LuaAst.LuaExprlist(keyNeverNil ? keyExpr : keyExpr.copy())),
            value);
        if (keyNeverNil) {
            res.add(store);
        } else {
            res.add(LuaAst.LuaIf(LuaAst.LuaExprBinary(keyExpr, LuaAst.LuaOpUnequals(), LuaAst.LuaExprNull()),
                LuaAst.LuaStatements(store), LuaAst.LuaStatements()));
        }
    }

    private static void emitLuaInitXpcall(ImFunction initFunc, List<LuaStatement> res, LuaTranslator tr) {
        String funcName = tr.luaFunc.getFor(initFunc).getName();
        String packageName = tr.imTr.luaInitFunctions.getOrDefault(initFunc, "?");
        String errHandler = "function(err) if err == \"" + WURST_ABORT_THREAD_SENTINEL + "\" then return end"
                + " BJDebugMsg(\"lua init error: \" .. tostring(err))"
                + " xpcall(function() " + ExprTranslation.callErrorFunc(tr, "tostring(err)") + " end,"
                + " function(err2) if err2 == \"" + WURST_ABORT_THREAD_SENTINEL + "\" then return end"
                + " BJDebugMsg(\"error reporting error: \" .. tostring(err2)) end) end";
        // no local/do-block: locals hidden in literals are invisible to the
        // 200-locals accounting in enforceLuaLocalLimits
        res.add(LuaAst.LuaLiteral("if not xpcall(" + funcName + ", " + errHandler + ") then"));
        res.add(LuaAst.LuaLiteral("    " + ExprTranslation.callErrorFunc(tr, "\"Could not initialize package " + packageName + ".\"")));
        res.add(LuaAst.LuaLiteral("end"));
    }

    public static void translate(ImExitwhen s, List<LuaStatement> res, LuaTranslator tr) {
        LuaIf r = LuaAst.LuaIf(s.getCondition().translateToLua(tr),
            LuaAst.LuaStatements(LuaAst.LuaBreak()),
            LuaAst.LuaStatements());
        res.add(r);
    }

    public static void translate(ImLoop s, List<LuaStatement> res, LuaTranslator tr) {
        LuaNumericFor counted = LuaNumericFor.match(s);
        if (counted == null) {
            res.add(LuaAst.LuaWhile(LuaAst.LuaExprBoolVal(true), tr.translateStatements(s.getBody())));
            return;
        }
        LuaVariable counter = tr.luaVar.getFor(counted.counter);
        LuaExpr from = takeCounterInitialisation(res, counter);
        LuaExprOpt step = counted.step == 1
            ? LuaAst.LuaNoExpr()
            : LuaAst.LuaExprIntVal("" + counted.step);
        LuaStatements body = LuaAst.LuaStatements();
        for (ImStmt stmt : counted.innerStatements()) {
            stmt.translateStmtToLua(body, tr);
        }
        res.add(LuaAst.LuaFor(counter, from, counted.bound.translateToLua(tr), step, body));
    }

    public static void translate(ImIf s, List<LuaStatement> res, LuaTranslator tr) {
        res.add(LuaAst.LuaIf(s.getCondition().translateToLua(tr),
            tr.translateStatements(s.getThenBlock()),
            tr.translateStatements(s.getElseBlock())));
    }

    public static void translate(ImReturn s, List<LuaStatement> res, LuaTranslator tr) {
        if (s.getReturnValue() instanceof ImTupleExpr results) {
            res.add(LuaAst.LuaReturnValues(tr.translateExprList(results.getExprs())));
            return;
        }
        res.add(LuaAst.LuaReturn(tr.translateOptional(s.getReturnValue())));
    }

    public static void translate(ImSet s, List<LuaStatement> res, LuaTranslator tr) {
        if (s.getLeft() instanceof ImVarAccess target && LuaMultipleResults.isResultsLocal(target.getVar())) {
            // the results of a call, one local for each
            LuaExprlist targets = LuaAst.LuaExprlist();
            for (LuaVariable component : tr.resultVars(target.getVar())) {
                targets.add(LuaAst.LuaExprVarAccess(component));
            }
            res.add(LuaAst.LuaMultipleAssignment(targets, s.getRight().translateToLua(tr)));
            return;
        }
        LuaExpr left;
        if (s.getLeft() instanceof ImVarArrayAccess) {
            // Assignment LHS must stay a writable table access, never an ensured r-value wrapper.
            left = ExprTranslation.translateArrayAccessRaw((ImVarArrayAccess) s.getLeft(), tr);
        } else {
            left = s.getLeft().translateToLua(tr);
        }
        LuaExpr right = s.getRight().translateToLua(tr);
        res.add(LuaAst.LuaAssignment(left, right));
    }


    /**
     * The counter's initial assignment was translated just before the loop, possibly followed by
     * the cached bound; it becomes the start value of the numeric for. It is only moved past the
     * bound's assignment when it is a literal, which no evaluation order can observe: a start
     * expression with a call or a variable read would otherwise run after the bound instead of
     * before it. Otherwise the loop starts from the counter variable itself.
     */
    private static LuaExpr takeCounterInitialisation(List<LuaStatement> res, LuaVariable counter) {
        for (int index = res.size() - 1, skipped = 0; index >= 0 && skipped <= 1; index--, skipped++) {
            if (!(res.get(index) instanceof LuaAssignment assignment)
                || !(assignment.getLeft() instanceof LuaExprVarAccess target)) {
                break;
            }
            if (target.getVar() == counter) {
                LuaExpr from = assignment.getRight();
                if (skipped > 0 && (!(from instanceof LuaExprIntVal)
                    || reads(((LuaAssignment) res.get(index + 1)).getRight(), counter))) {
                    // The skipped bound assignment reads the counter (for i = 0 to i + n), so the
                    // literal has to be stored before it after all.
                    return LuaAst.LuaExprVarAccess(counter);
                }
                res.remove(index);
                from.setParent(null);
                return from;
            }
        }
        return LuaAst.LuaExprVarAccess(counter);
    }

    private static boolean reads(de.peeeq.wurstscript.luaAst.Element e, LuaVariable var) {
        if (e instanceof LuaExprVarAccess access && access.getVar() == var) {
            return true;
        }
        for (int i = 0; i < e.size(); i++) {
            if (reads(e.get(i), var)) {
                return true;
            }
        }
        return false;
    }

    public static void translate(ImVarargLoop loop, List<LuaStatement> res, LuaTranslator tr) {
        List<ImVar> loopVars = loop.getLoopVars().stream()
            .map(ImVarargLoopVar::getVar)
            .collect(Collectors.toList());
        // The loop is built from real AST nodes (a while loop) instead of literal
        // 'for ... do' / 'end' lines: the printer stops printing a statement list
        // after a return/break (Lua forbids trailing statements), which would
        // truncate a literal closing 'end' and produce unparseable output.
        LuaVariable args = LuaAst.LuaVariable(tr.uniqueName("__args"), LuaAst.LuaLiteral("table.pack(...)"));
        LuaVariable i = LuaAst.LuaVariable(tr.uniqueName("__i"), LuaAst.LuaExprIntVal("0"));
        res.add(args);
        res.add(i);
        LuaStatements body = LuaAst.LuaStatements();
        for (ImVar loopVar : loopVars) {
            body.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(i),
                LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(i), LuaAst.LuaOpPlus(), LuaAst.LuaExprIntVal("1"))));
            body.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(loopVar)),
                LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(args), LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(i)))));
        }
        tr.translateStatements(body, loop.getBody());
        res.add(LuaAst.LuaWhile(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(i), LuaAst.LuaOpLess(),
                LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(args), "n")),
            body));
    }

}
