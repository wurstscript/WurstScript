package de.peeeq.wurstscript.translation.lua.printing;

import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.utils.Utils;

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
        for (LuaStatement d : cu) {
            if (d instanceof LuaVariable) {
                // don't translate global variables as locals:
                printVariable((LuaVariable) d, sb, indent);
            } else {
                d.print(sb, indent);
            }
            sb.append("\n\n");
        }
    }

    public static void print(LuaExprArrayAccess e, StringBuilder sb, int indent) {
        e.getLeft().print(sb, indent);
        for (LuaExpr i : e.getIndexes()) {
            sb.append("[");
            i.print(sb, indent);
            sb.append("]");
        }
    }

    public static void print(LuaExprBinary e, StringBuilder sb, int indent) {
        sb.append("(");
        e.getLeftExpr().print(sb, indent);
        sb.append(" ");
        e.getOp().print(sb, indent);
        sb.append(" ");
        e.getRight().print(sb, indent);
        sb.append(")");
    }

    public static void print(LuaExprBoolVal e, StringBuilder sb, int indent) {
        sb.append(e.getValB());
    }

    public static void print(LuaExprFieldAccess e, StringBuilder sb, int indent) {
        e.getReceiver().print(sb, indent);
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
        sb.append("(");
        e.getFuncExpr().print(sb, indent);
        sb.append(")(");
        e.getArguments().print(sb, indent);
        sb.append(")");
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
        e.getReceiver().print(sb, indent);
        sb.append(":");
        sb.append(e.getMethod().getName());
        sb.append("(");
        e.getArguments().print(sb, indent);
        sb.append(")");
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
        sb.append("{");
        e.getTableFields().print(sb, indent);
        sb.append("}");
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
