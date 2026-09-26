package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaExprIntVal;
import de.peeeq.wurstscript.luaAst.LuaFor;
import de.peeeq.wurstscript.luaAst.LuaStatements;
import de.peeeq.wurstscript.luaAst.LuaVariable;
import de.peeeq.wurstscript.translation.imtranslation.EliminateLocalTypes;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Recognises the counted loop the front end emits for {@code for i = a to b} (and {@code downto},
 * and a constant {@code step}) so the backend can print a numeric {@code for}, one VM instruction
 * per iteration instead of a comparison, a conditional break and an add.
 *
 * <p>The IM shape is {@code loop { exitwhen i > to; body; i = i + step }} with the initial
 * assignment just before the loop. Lua's loop variable is a fresh local which the loop owns, so
 * the shape is only used when the counter provably has no reader after the loop: its final value
 * is the one thing the two forms disagree on. Everything else (the bound evaluated once, a
 * {@code break} or {@code return} inside, nested loops) behaves the same.
 */
final class LuaNumericFor {
    final ImVar counter;
    final ImExpr bound;
    final int step;
    final ImLoop loop;

    private LuaNumericFor(ImVar counter, ImExpr bound, int step, ImLoop loop) {
        this.counter = counter;
        this.bound = bound;
        this.step = step;
        this.loop = loop;
    }

    /** The loop's body without the exit test and the increment. */
    List<ImStmt> innerStatements() {
        ImStmts body = loop.getBody();
        return new ArrayList<>(body.subList(1, body.size() - 1));
    }

    static @Nullable LuaNumericFor match(ImLoop loop) {
        ImStmts body = loop.getBody();
        if (body.size() < 2) {
            return null;
        }
        if (!(body.get(0) instanceof ImExitwhen exit) || !(exit.getCondition() instanceof ImOperatorCall test)
            || test.getArguments().size() != 2 || !(test.getArguments().get(0) instanceof ImVarAccess testVar)) {
            return null;
        }
        if (!(body.get(body.size() - 1) instanceof ImSet increment)
            || !(increment.getLeft() instanceof ImVarAccess incrementVar)
            || !(increment.getRight() instanceof ImOperatorCall add)
            || add.getArguments().size() != 2
            || !(add.getArguments().get(0) instanceof ImVarAccess addVar)
            || !(add.getArguments().get(1) instanceof ImIntVal stepVal)) {
            return null;
        }
        ImVar counter = testVar.getVar();
        if (incrementVar.getVar() != counter || addVar.getVar() != counter) {
            return null;
        }
        if (counter.isGlobal() || !EliminateLocalTypes.isIntegerOrLocalInteger(counter.getType())) {
            return null;
        }
        int step;
        if (test.getOp() == WurstOperator.GREATER && add.getOp() == WurstOperator.PLUS) {
            step = stepVal.getValI();
        } else if (test.getOp() == WurstOperator.LESS && add.getOp() == WurstOperator.MINUS) {
            step = -stepVal.getValI();
        } else {
            return null;
        }
        if (step == 0) {
            return null;
        }
        ImExpr bound = test.getArguments().get(1);
        if (bound instanceof ImVarAccess boundVar) {
            // The while form re-reads the bound every iteration; a local nobody writes inside the
            // loop reads the same on every iteration, while a global can change behind a call.
            if (boundVar.getVar().isGlobal() || isAssignedWithin(loop, boundVar.getVar())) {
                return null;
            }
        } else if (!(bound instanceof ImIntVal)) {
            return null;
        }
        for (int i = 0; i < body.size() - 1; i++) {
            if (isAssignedWithin(body.get(i), counter)) {
                return null;
            }
        }
        if (!counterIsDeadAfter(loop, counter)) {
            return null;
        }
        return new LuaNumericFor(counter, bound, step, loop);
    }

    private static boolean isAssignedWithin(Element e, ImVar v) {
        if (e instanceof ImSet set && set.getLeft() instanceof ImVarAccess target && target.getVar() == v) {
            return true;
        }
        if (e instanceof ImVarargLoopVar loopVar && loopVar.getVar() == v) {
            return true;
        }
        for (int i = 0; i < e.size(); i++) {
            if (isAssignedWithin(e.get(i), v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when no read of the counter can observe the value it holds after the loop.
     *
     * <p>Inside an enclosing loop, a read anywhere outside this loop may run after it on a later
     * iteration, so none is allowed. Otherwise, in evaluation order, the first access after the
     * loop must be an assignment in the same statement list, which every later read then sees.
     */
    private static boolean counterIsDeadAfter(ImLoop loop, ImVar counter) {
        Element outermostLoop = null;
        for (Element e = loop.getParent(); e != null && !(e instanceof ImFunction); e = e.getParent()) {
            if (e instanceof ImLoop || e instanceof ImVarargLoop) {
                outermostLoop = e;
            }
        }
        List<Access> accesses = new ArrayList<>();
        Element scope = outermostLoop != null ? outermostLoop : loop.getNearestFunc().getBody();
        collectAccesses(scope, counter, loop, false, accesses);
        if (outermostLoop != null) {
            for (Access access : accesses) {
                if (!access.insideLoop && !access.write) {
                    return false;
                }
            }
            return true;
        }
        boolean afterLoop = false;
        for (Access access : accesses) {
            if (access.insideLoop) {
                afterLoop = true;
                continue;
            }
            if (!afterLoop) {
                continue;
            }
            return access.write && access.node.getParent() == loop.getParent();
        }
        return true;
    }

    private record Access(Element node, boolean write, boolean insideLoop) {
    }

    private static void collectAccesses(Element e, ImVar v, ImLoop loop, boolean insideLoop, List<Access> out) {
        boolean inside = insideLoop || e == loop;
        if (e instanceof ImSet set && set.getLeft() instanceof ImVarAccess target && target.getVar() == v) {
            out.add(new Access(set, true, inside));
            collectAccesses(set.getRight(), v, loop, inside, out);
            return;
        }
        if (e instanceof ImVarargLoopVar loopVar && loopVar.getVar() == v) {
            out.add(new Access(loopVar, true, inside));
            return;
        }
        if (e instanceof ImVarAccess access && access.getVar() == v) {
            out.add(new Access(access, false, inside));
            return;
        }
        for (int i = 0; i < e.size(); i++) {
            collectAccesses(e.get(i), v, loop, inside, out);
        }
    }

    /**
     * Turns a numeric for back into the counted while loop it was recognised from:
     * {@code i = from; while true do if i > to then break end; body; i = i + step end}.
     */
    static void demote(LuaFor loop) {
        LuaStatements parent = (LuaStatements) loop.getParent();
        int index = parent.indexOf(loop);
        LuaVariable counter = loop.getLoopVar();
        int step = loop.getStep() instanceof LuaExprIntVal stepVal ? Integer.parseInt(stepVal.getValI()) : 1;
        LuaStatements body = loop.getBody();
        body.setParent(null);
        body.add(0, LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(counter),
                step > 0 ? LuaAst.LuaOpGreater() : LuaAst.LuaOpLess(), loop.getTo().copy()),
            LuaAst.LuaStatements(LuaAst.LuaBreak()),
            LuaAst.LuaStatements()));
        body.add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(counter),
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(counter),
                step > 0 ? LuaAst.LuaOpPlus() : LuaAst.LuaOpMinus(),
                LuaAst.LuaExprIntVal("" + Math.abs(step)))));
        parent.set(index, LuaAst.LuaWhile(LuaAst.LuaExprBoolVal(true), body));
        parent.add(index, LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(counter), loop.getFrom().copy()));
    }
}
