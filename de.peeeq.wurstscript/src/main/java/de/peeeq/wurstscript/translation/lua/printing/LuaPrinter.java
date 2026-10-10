package de.peeeq.wurstscript.translation.lua.printing;

import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LuaPrinter {

    public static void print(LuaAssignment s, StringBuilder sb, int indent) {
        s.getLeft().print(sb, indent);
        sb.append(" = ");
        s.getRight().print(sb, indent);
    }

    public static void print(LuaBreak s, StringBuilder sb, int indent) {
        sb.append("break");
    }

    public static void print(LuaCompilationUnit cu, StringBuilder sb, int indent) {
        boolean statementBlock = false;
        for (LuaStatement d : cu) {
            if (d instanceof LuaVariable luaVariable) {
                // don't translate global variables as locals:
                printVariable(luaVariable, sb, indent);
                sb.append("\n");
                statementBlock = true;
            } else if (d instanceof LuaChunkLocal local && local.getDefinition() instanceof LuaVariable) {
                d.print(sb, indent);
                sb.append("\n");
                statementBlock = true;
            } else if(d instanceof LuaAssignment) {
                // these are top level assignments that are not inside functions
                d.print(sb, indent);
                sb.append("\n");
                statementBlock = true;
            } else {
                // every other statement is considered a block and has an empty line after it
                if(statementBlock) {
                    sb.append("\n");
                }
                d.print(sb, indent);
                sb.append("\n\n");
                statementBlock = false;
            }
        }
    }

    public static void print(LuaExprArrayAccess e, StringBuilder sb, int indent) {
        printPostfixReceiver(e.getLeft(), sb, indent);
        for (LuaExpr i : e.getIndexes()) {
            sb.append("[");
            i.print(sb, indent);
            sb.append("]");
        }
    }

    public static void print(LuaExprBinary e, StringBuilder sb, int indent) {
        if (e.getOp() instanceof LuaOpConcatString && isConcatenation(e.getLeftExpr())) {
            printConcatenation(e, sb, indent);
            return;
        }
        sb.append("(");
        if (continuesChain(e, e.getLeftExpr())) {
            printChain(e, sb, indent);
        } else {
            e.getLeftExpr().print(sb, indent);
            printOperatorAndRight(e, sb, indent);
        }
        sb.append(")");
    }

    private static void printOperatorAndRight(LuaExprBinary e, StringBuilder sb, int indent) {
        sb.append(" ");
        e.getOp().print(sb, indent);
        sb.append(" ");
        e.getRight().print(sb, indent);
    }

    /**
     * Prints a left-nested chain of operators as one flat expression inside a single pair of
     * parentheses: {@code (a + b + c)}, not {@code ((a + b) + c)}. Lua parses every binary operator
     * except {@code ..} and {@code ^} as left associative, so {@code a - b + c} is {@code (a - b) + c}
     * and the parentheses around the left operand change nothing. They are not free: luac nests one
     * parser level per parenthesis and rejects a chunk nested more than 200 levels
     * ("too many C levels"), which a sum of 200 terms reached; a flat chain is a loop in its parser.
     * Walks the spine instead of recursing, so the chain length costs no stack either.
     */
    private static void printChain(LuaExprBinary outermost, StringBuilder sb, int indent) {
        List<LuaExprBinary> spine = new ArrayList<>();
        LuaExprBinary current = outermost;
        spine.add(current);
        while (continuesChain(current, current.getLeftExpr())) {
            current = (LuaExprBinary) current.getLeftExpr();
            spine.add(current);
        }
        current.getLeftExpr().print(sb, indent);
        for (int i = spine.size() - 1; i >= 0; i--) {
            printOperatorAndRight(spine.get(i), sb, indent);
        }
    }

    /**
     * True when {@code left}, the left operand of {@code parent}, may print without parentheses:
     * it is an operation of the same left associative precedence level, so the unparenthesised text
     * parses back to the same tree. A different level keeps its parentheses, as does a right
     * operand (which is never checked here): {@code a - (b - c)} is not {@code a - b - c}.
     */
    private static boolean continuesChain(LuaExprBinary parent, LuaExpr left) {
        if (!(left instanceof LuaExprBinary leftBinary)) {
            return false;
        }
        int level = chainLevel(parent.getOp());
        return level >= 0 && level == chainLevel(leftBinary.getOp());
    }

    /**
     * The Lua precedence level of an operator whose left-nested chains print flat, or -1 for one
     * which keeps its parentheses. Only the levels (not their order) matter, and they follow the
     * Lua 5.3 manual, section 3.4.8: {@code or}; {@code and}; {@code + -}; {@code * / // %}.
     * Not listed: {@code ..}, which is right associative, so the text {@code a .. b .. c} parses as
     * {@code a .. (b .. c)}, not as {@code (a .. b) .. c}; it has its own printing, see
     * {@link #printConcatenation}. Also not listed, so always parenthesised: the comparisons, where
     * a chain reads as a range check but compares a boolean; and any operator added later until its
     * level is decided.
     */
    private static int chainLevel(LuaOpBinary op) {
        if (op instanceof LuaOpOr) {
            return 1;
        } else if (op instanceof LuaOpAnd) {
            return 2;
        } else if (op instanceof LuaOpPlus || op instanceof LuaOpMinus) {
            return 3;
        } else if (op instanceof LuaOpMult || op instanceof LuaOpDiv || op instanceof LuaOpFloorDiv
            || op instanceof LuaOpMod) {
            return 4;
        }
        return -1;
    }

    /**
     * The most operands one flat group of a concatenation holds. A flat {@code a .. b .. c} is one
     * CONCAT over consecutive registers and the parser nests one level per {@code ..}, so a group
     * costs as many registers and parser levels as it has operands. Sixteen is more than the parts
     * of nearly every message, and a chain of up to 4096 parts then holds at most 46 registers for
     * its operands, which fit beside the 199 locals a function may keep before they are spilled
     * (Lua has 255).
     */
    private static final int CONCATENATION_GROUP = 16;

    private static boolean isConcatenation(LuaExpr e) {
        return e instanceof LuaExprBinary binary && binary.getOp() instanceof LuaOpConcatString;
    }

    /**
     * Prints a left-nested chain of {@code ..} flat: {@code (a .. b .. c)}, not {@code ((a .. b) .. c)}.
     * The nested text concatenates once per operator and builds every intermediate string; the flat
     * one is a single CONCAT into one buffer. The text parses to another tree, {@code a .. (b .. c)},
     * because {@code ..} is right associative, which is why {@link #chainLevel} leaves it out. Here the
     * value is still the same: the backend only concatenates strings (operands which might be nil are
     * guarded with {@code or ""}), and joining strings is associative, with no metamethod to observe
     * the grouping. The operands are still evaluated from left to right.
     *
     * <p>A long chain is split into parenthesised groups of at most {@link #CONCATENATION_GROUP}
     * operands, and those into groups again, so neither the parser levels (luac stops at 200,
     * "too many C levels") nor the registers grow with its length.
     */
    private static void printConcatenation(LuaExprBinary outermost, StringBuilder sb, int indent) {
        List<LuaExpr> operands = new ArrayList<>();
        LuaExpr current = outermost;
        while (isConcatenation(current)) {
            LuaExprBinary binary = (LuaExprBinary) current;
            operands.add(binary.getRight());
            current = binary.getLeftExpr();
        }
        operands.add(current);
        Collections.reverse(operands);
        printConcatenation(operands, 0, operands.size(), sb, indent);
    }

    private static void printConcatenation(List<LuaExpr> operands, int from, int to, StringBuilder sb, int indent) {
        if (to - from == 1) {
            operands.get(from).print(sb, indent);
            return;
        }
        // Each part holds at most 'span' operands, a power of the group size, and there are at most
        // CONCATENATION_GROUP parts, so the nesting is logarithmic in the length of the chain.
        long span = 1;
        while (span * CONCATENATION_GROUP < to - from) {
            span *= CONCATENATION_GROUP;
        }
        sb.append("(");
        for (int start = from; start < to; start += (int) span) {
            if (start > from) {
                sb.append(" .. ");
            }
            printConcatenation(operands, start, (int) Math.min(to, start + span), sb, indent);
        }
        sb.append(")");
    }

    public static void print(LuaExprBoolVal e, StringBuilder sb, int indent) {
        sb.append(e.getValB());
    }

    public static void print(LuaExprFieldAccess e, StringBuilder sb, int indent) {
        printPostfixReceiver(e.getReceiver(), sb, indent);
        sb.append(".");
        sb.append(e.getFieldName());
    }

    public static void print(LuaExprFuncRef e, StringBuilder sb, int indent) {
        sb.append(e.getFunc().getName());
    }

    public static void print(LuaExprFunctionAbstraction e, StringBuilder sb, int indent) {
        sb.append("function (");
        e.getParams().print(sb, indent);
        sb.append(") \n");
        e.getBody().print(sb, indent + 2);
        printIndent(sb, indent + 1);
        sb.append("end");
    }

    private static void printIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("	");
        }
    }

    public static void print(LuaExprFunctionCall e, StringBuilder sb, int indent) {
        sb.append(e.getFunc().getName());
        sb.append("(");
        e.getArguments().print(sb, indent);
        sb.append(")");
    }

    public static void print(LuaExprFunctionCallByName e, StringBuilder sb, int indent) {
        sb.append(e.getFuncName());
        sb.append("(");
        e.getArguments().print(sb, indent);
        sb.append(")");
    }

    public static void print(LuaExprFunctionCallE e, StringBuilder sb, int indent) {
        // A function abstraction needs the parentheses; a name, field, index or call is already a
        // valid prefix expression, and a statement must not start with '(' (Lua would attach it to
        // the previous line).
        printPostfixReceiver(e.getFuncExpr(), sb, indent);
        sb.append("(");
        e.getArguments().print(sb, indent);
        sb.append(")");
    }

    /** True when {@code e} prints as a Lua prefix expression without added parentheses. */
    public static boolean isPrefixExpression(LuaExpr e) {
        return e instanceof LuaExprVarAccess
            || e instanceof LuaExprFuncRef
            || e instanceof LuaExprFieldAccess
            || e instanceof LuaExprArrayAccess
            || e instanceof LuaCallExpr;
    }

    /**
     * True when {@code e} prints starting with a name, so it can open a statement: Lua joins a
     * statement which starts with '(' onto the previous line as a call. Walks the receiver chain,
     * because a field access on a parenthesised receiver still starts with '('.
     */
    public static boolean startsWithName(LuaExpr e) {
        if (e instanceof LuaExprVarAccess || e instanceof LuaExprFuncRef
            || e instanceof LuaExprFunctionCall || e instanceof LuaExprFunctionCallByName) {
            return true;
        }
        if (e instanceof LuaExprFieldAccess access) {
            return startsWithName(access.getReceiver());
        }
        if (e instanceof LuaExprArrayAccess access) {
            return startsWithName(access.getLeft());
        }
        if (e instanceof LuaExprMethodCall call) {
            return startsWithName(call.getReceiver());
        }
        if (e instanceof LuaExprFunctionCallE call) {
            return startsWithName(call.getFuncExpr());
        }
        return false;
    }

    public static void print(LuaExprIntVal e, StringBuilder sb, int indent) {
        sb.append(e.getValI());
    }

    public static void print(LuaExprlist e, StringBuilder sb, int indent) {
        boolean first = true;
        for (LuaExpr ee : e) {
            if (!first) {
                sb.append(", ");
            }
            ee.print(sb, indent);
            first = false;
        }
    }

    public static void print(LuaExprMethodCall e, StringBuilder sb, int indent) {
        printPostfixReceiver(e.getReceiver(), sb, indent);
        sb.append(":");
        sb.append(e.getMethod().getName());
        sb.append("(");
        e.getArguments().print(sb, indent);
        sb.append(")");
    }

    private static void printPostfixReceiver(LuaExpr receiver, StringBuilder sb, int indent) {
        if (isPrefixExpression(receiver)) {
            receiver.print(sb, indent);
        } else {
            sb.append("(");
            receiver.print(sb, indent);
            sb.append(")");
        }
    }

    public static void print(LuaExprNull e, StringBuilder sb, int indent) {
        sb.append("nil");
    }

    public static void print(LuaExprRealVal e, StringBuilder sb, int indent) {
        sb.append(e.getValR());
    }

    public static void print(LuaExprStringVal e, StringBuilder sb, int indent) {
        sb.append(Utils.escapeString(e.getValS()));
    }

    public static void print(LuaExprUnary e, StringBuilder sb, int indent) {
        e.getOpU().print(sb, indent);
        sb.append("(");
        e.getRight().print(sb, indent);
        sb.append(")");
    }

    public static void print(LuaExprVarAccess e, StringBuilder sb, int indent) {
        sb.append(e.getVar().getName());
    }

    public static void print(LuaFunction f, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        printFunction(f, sb, indent);
    }

    /**
     * A local of the main chunk. A function printed after it reaches it as an upvalue, one
     * instruction, instead of looking its name up in _ENV, the table of every global.
     */
    public static void print(LuaChunkLocal l, StringBuilder sb, int indent) {
        LuaDefinition definition = l.getDefinition();
        if (definition instanceof LuaVariable v) {
            print(v, sb, indent);
        } else if (definition instanceof LuaFunction f) {
            printIndent(sb, indent);
            sb.append("local ");
            printFunction(f, sb, indent);
        } else {
            throw new IllegalArgumentException("A method is a table field and cannot be a local: " + definition);
        }
    }

    private static void printFunction(LuaFunction f, StringBuilder sb, int indent) {
        sb.append("function ");
        sb.append(f.getName());
        sb.append("(");
        f.getParams().print(sb, indent);
        sb.append(") \n");
        f.getBody().print(sb, indent + 1);
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void print(LuaMethod f, StringBuilder sb, int indent) {
        printIndent(sb, indent);
        sb.append("function ");
        f.getReceiver().print(sb, indent);
        sb.append(":");
        sb.append(f.getName());
        sb.append("(");
        f.getParams().print(sb, indent);
        sb.append(") \n");
        f.getBody().print(sb, indent + 1);
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void print(LuaIf s, StringBuilder sb, int indent) {
        sb.append("if ");
        s.getCond().print(sb, indent);
        sb.append(" then\n");
        s.getThenStmts().print(sb, indent + 1);
        if (!s.getElseStmts().isEmpty()) {
            printIndent(sb, indent);
            sb.append("else");
            if (s.getElseStmts().size() == 1 && s.getElseStmts().get(0) instanceof LuaIf) {
                LuaIf luaIf = (LuaIf) s.getElseStmts().get(0);
                luaIf.print(sb, indent);
                return;
            } else {
                sb.append("\n");
                s.getElseStmts().print(sb, indent + 1);
            }
        }
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void print(LuaModel m, StringBuilder sb, int indent) {
        for (LuaCompilationUnit cu : m) {
            cu.print(sb, indent);
        }
    }

    public static void print(LuaNoExpr n, StringBuilder sb, int indent) {
        // nothing
    }

    public static void print(LuaOpAnd luaOpAnd, StringBuilder sb, int indent) {
        sb.append("and");
    }

    public static void print(LuaOpConcatString luaOpConcatString, StringBuilder sb, int indent) {
        sb.append("..");
    }

    public static void print(LuaOpDiv luaOpDiv, StringBuilder sb, int indent) {
        sb.append("/");
    }

    public static void print(LuaOpFloorDiv luaOpFloorDiv, StringBuilder sb, int indent) {
        sb.append("//");
    }

    public static void print(LuaOpBitOr luaOpBitOr, StringBuilder sb, int indent) {
        sb.append("|");
    }

    public static void print(LuaOpEquals luaOpEquals, StringBuilder sb, int indent) {
        sb.append("==");
    }

    public static void print(LuaOpGreater luaOpGreater, StringBuilder sb, int indent) {
        sb.append(">");
    }

    public static void print(LuaOpGreaterEq luaOpGreaterEq, StringBuilder sb, int indent) {
        sb.append(">=");
    }

    public static void print(LuaOpLess luaOpLess, StringBuilder sb, int indent) {
        sb.append("<");
    }

    public static void print(LuaOpLessEq luaOpLessEq, StringBuilder sb, int indent) {
        sb.append("<=");
    }

    public static void print(LuaOpMinus luaOpMinus, StringBuilder sb, int indent) {
        sb.append("-");
    }

    public static void print(LuaOpMod luaOpMod, StringBuilder sb, int indent) {
        sb.append("%");
    }

    public static void print(LuaOpMult luaOpMult, StringBuilder sb, int indent) {
        sb.append("*");
    }

    public static void print(LuaOpNot luaOpNot, StringBuilder sb, int indent) {
        sb.append("not");
    }

    public static void print(LuaOpOr luaOpOr, StringBuilder sb, int indent) {
        sb.append("or");
    }

    public static void print(LuaOpPlus luaOpPlus, StringBuilder sb, int indent) {
        sb.append("+");
    }

    public static void print(LuaOpUnequals luaOpUnequals, StringBuilder sb, int indent) {
        sb.append("~=");
    }

    public static void print(LuaParams luaParams, StringBuilder sb, int indent) {
        boolean first = true;
        for (LuaVariable p : luaParams) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(p.getName());
            first = false;
        }
    }

    public static void print(LuaReturn s, StringBuilder sb, int indent) {
        sb.append("return ");
        s.getRetVal().print(sb, indent);
    }

    public static void print(LuaStatements stmts, StringBuilder sb, int indent) {
        for (LuaStatement s : stmts) {
            printIndent(sb, indent);
            s.print(sb, indent);
            sb.append("\n");
            if (s instanceof LuaReturn || s instanceof LuaBreak) {
                // there can be no statement after return or break ...
                break;
            }
        }
    }

    public static void print(LuaTableConstructor e, StringBuilder sb, int indent) {
        sb.append("({");
        e.getTableFields().print(sb, indent);
        sb.append("})");
    }

    public static void print(LuaTableExprField e, StringBuilder sb, int indent) {
        sb.append("[");
        e.getFieldKey().print(sb, indent);
        sb.append("] = ");
        e.getVal().print(sb, indent);
    }

    public static void print(LuaTableFields fields, StringBuilder sb, int indent) {
        for (LuaTableField f : fields) {
            f.print(sb, indent);
            sb.append(", ");
        }
    }

    public static void print(LuaTableNamedField e, StringBuilder sb, int indent) {
        sb.append(e.getFieldName());
        sb.append("=");
        e.getVal().print(sb, indent);
    }

    public static void print(LuaTableSingleField e, StringBuilder sb, int indent) {
        e.getVal().print(sb, indent);
    }

    public static void print(LuaVariable v, StringBuilder sb, int indent) {
        sb.append("local ");
        printVariable(v, sb, indent);
    }

    private static void printVariable(LuaVariable v, StringBuilder sb, int indent) {
        sb.append(v.getName());
        if (v.getInitialValue() instanceof LuaExpr) {
            sb.append(" = ");
            v.getInitialValue().print(sb, indent);
        }
    }

    public static void print(LuaFor s, StringBuilder sb, int indent) {
        sb.append("for ");
        sb.append(s.getLoopVar().getName());
        sb.append(" = ");
        s.getFrom().print(sb, indent);
        sb.append(", ");
        s.getTo().print(sb, indent);
        if (s.getStep() instanceof LuaExpr) {
            sb.append(", ");
            s.getStep().print(sb, indent);
        }
        sb.append(" do\n");
        s.getBody().print(sb, indent + 1);
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void print(LuaWhile s, StringBuilder sb, int indent) {
        sb.append("while ");
        s.getCond().print(sb, indent);
        sb.append(" do\n");
        s.getBody().print(sb, indent + 1);
        printIndent(sb, indent);
        sb.append("end");
    }

    public static void print(LuaLiteral e, StringBuilder sb, int indent) {
        sb.append(e.getLuaCode());
    }

}
