package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

import java.io.IOException;
import java.util.List;

public class ImPrinter {


    public static void print(ImProg p, Appendable sb, int indent) {
        for (ImVar g : p.getGlobals()) {
            g.print(sb, indent);
            append(sb, "\n");
        }
        append(sb, "\n\n");
        for (ImFunction f : p.getFunctions()) {
            f.print(sb, indent);
            append(sb, "\n");
        }
        append(sb, "\n\n");
        for (ImClass c : p.getClasses()) {
            append(sb, "class ");
            append(sb, c.getName());
            append(sb, smallHash(c));
            append(sb, " extends ");
            for (ImClass sc : c.getSuperClasses()) {
                append(sb, sc.getName());
                append(sb, smallHash(sc));
                append(sb, " ");
            }
            append(sb, "{\n");
            for (ImVar f : c.getFields()) {
                f.print(sb, 1);
                append(sb, "\n");
            }
            append(sb, "\n\n");
            for (ImMethod m : c.getMethods()) {
                if (m.getIsAbstract()) {
                    append(sb, "	abstract");
                }
                append(sb, "	method ");
                append(sb, m.getName());
                append(sb, smallHash(m));
                append(sb, " implemented by ");
                append(sb, m.getImplementation().getName());
                append(sb, "\n");
                for (ImMethod sm : m.getSubMethods()) {
                    append(sb, "		sub: ");
                    append(sb, sm.getName());
                    append(sb, smallHash(sm));
                    append(sb, "\n");
                }
                append(sb, "\n");
            }

            append(sb, "}\n\n");
        }

    }


    private static void append(Appendable sb, Object s) {
        append(sb, s.toString());
    }

    private static void append(Appendable sb, String s) {
        try {
            sb.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void print(ImSimpleType p, Appendable sb, int indent) {
        append(sb, p.getTypename());
    }

    public static void print(ImArrayType t, Appendable sb, int indent) {
        append(sb, "array ");
        append(sb, t.getTypename());
    }

    public static void print(ImTupleArrayType p, Appendable sb, int indent) {
        append(sb, "array<");
        boolean first = true;
        for (ImType t : p.getTypes()) {
            if (!first) append(sb, ", ");
            t.print(sb, indent);
            first = false;
        }
        append(sb, ">");
    }

    public static void print(ImTupleType p, Appendable sb, int indent) {
        append(sb, "<");
        boolean first = true;
        for (ImType t : p.getTypes()) {
            if (!first) append(sb, ", ");
            t.print(sb, indent);
            first = false;
        }
        append(sb, ">");
    }

    public static void print(ImVoid p, Appendable sb, int indent) {
        append(sb, "void");
    }

    public static void print(ImFunction p, Appendable sb, int indent) {
        for (FunctionFlag flag : p.getFlags()) {
            append(sb, flag);
            append(sb, " ");
        }
        append(sb, "function ");
        append(sb, p.getName());
        append(sb, "(");
        boolean first = true;
        for (ImVar p1 : p.getParameters()) {
            if (!first) append(sb, ", ");
            p1.print(sb, indent);
            first = false;
        }
        append(sb, ") returns ");
        p.getReturnType().print(sb, indent);
        append(sb, "{ \n");
        for (ImVar v : p.getLocals()) {
            indent(sb, indent + 1);
            append(sb, "local ");
            v.print(sb, indent + 1);
            append(sb, "\n");
        }
        p.getBody().print(sb, indent + 1);
        append(sb, "}\n\n");
    }

    public static void print(ImIf p, Appendable sb, int indent) {
        append(sb, "if ");
        p.getCondition().print(sb, indent);
        append(sb, "{\n");
        p.getThenBlock().print(sb, indent + 1);
        indent(sb, indent);
        append(sb, "} else {\n");
        p.getElseBlock().print(sb, indent + 1);
        indent(sb, indent);
        append(sb, "}");

    }

    private static void indent(Appendable sb, int indent) {
        for (int i = 0; i < indent; i++) {
            append(sb, "    ");
        }
    }


    public static void print(ImLoop p, Appendable sb, int indent) {
        append(sb, "loop {\n");
        p.getBody().print(sb, indent + 1);
        indent(sb, indent);
        append(sb, "}");
    }

    public static void print(ImExitwhen p, Appendable sb, int indent) {
        append(sb, "exitwhen ");
        p.getCondition().print(sb, indent);
    }

    public static void print(ImReturn p, Appendable sb, int indent) {
        append(sb, "return ");
        p.getReturnValue().print(sb, indent);
    }

    public static void print(ImSet p, Appendable sb, int indent) {
        append(sb, p.getLeft().getName());
        append(sb, smallHash(p.getLeft()));
        append(sb, " = ");
        p.getRight().print(sb, indent);
    }

    public static void print(ImSetTuple p, Appendable sb, int indent) {
        append(sb, p.getLeft().getName());
        append(sb, smallHash(p.getLeft()));
        append(sb, " #");
        append(sb, p.getTupleIndex());
        append(sb, " = ");
        p.getRight().print(sb, indent);
    }

    public static void print(ImSetArray p, Appendable sb, int indent) {
        append(sb, p.getLeft().getName());
        append(sb, smallHash(p.getLeft()));
        append(sb, "[");
        p.getIndex().print(sb, indent);
        append(sb, "]");
        append(sb, " = ");
        p.getRight().print(sb, indent);
    }

    public static void print(ImSetArrayTuple p, Appendable sb, int indent) {
        append(sb, p.getLeft().getName());
        append(sb, smallHash(p.getLeft()));
        append(sb, "[");
        p.getIndex().print(sb, indent);
        append(sb, "]");
        append(sb, " #");
        append(sb, p.getTupleIndex());
        append(sb, " = ");
        p.getRight().print(sb, indent);
    }

    public static void print(ImNoExpr p, Appendable sb, int indent) {
        append(sb, "%nothing%");
    }

    public static void print(ImStatementExpr p, Appendable sb, int indent) {
        append(sb, "{\n");
        p.getStatements().print(sb, indent + 1);
        indent(sb, indent + 1);
        append(sb, ">>>  ");
        p.getExpr().print(sb, indent);
        append(sb, "}");
    }


    public static void print(ImFunctionCall p, Appendable sb, int indent) {
        append(sb, p.getFunc().getName());
        printArgumentList(sb, indent, p.getArguments());
    }


    public static void printArgumentList(Appendable sb, int indent,
                                         List<ImExpr> args) {
        append(sb, "(");
        boolean first = true;
        for (ImExpr a : args) {
            if (!first) {
                append(sb, ", ");
            }
            a.print(sb, indent);
            first = false;
        }
        append(sb, ")");
    }


    public static void print(ImVarAccess p, Appendable sb, int indent) {
        append(sb, p.getVar().getName());
        append(sb, smallHash(p.getVar()));

    }

    private static String smallHash(Object g) {
        String c = "" + g.hashCode();
        return c.substring(0, Math.min(3, c.length() - 1));
    }

    public static void print(ImVarArrayAccess p, Appendable sb, int indent) {
        append(sb, p.getVar().getName());
        append(sb, smallHash(p.getVar()));
        append(sb, "[");
        p.getIndex().print(sb, indent);
        append(sb, "]");
    }


    public static void print(ImTupleExpr p, Appendable sb, int indent) {
        append(sb, "<");
        boolean first = true;
        for (ImExpr e : p.getExprs()) {
            if (!first) append(sb, ", ");
            e.print(sb, indent);
            first = false;
        }
        append(sb, ">");
    }


    public static void print(ImTupleSelection p, Appendable sb, int indent) {
        p.getTupleExpr().print(sb, indent);
        append(sb, "#");
        append(sb, p.getTupleIndex());
    }


    public static void print(ImIntVal p, Appendable sb, int indent) {
        append(sb, p.getValI());
    }


    public static void print(ImRealVal p, Appendable sb, int indent) {
        append(sb, p.getValR());
    }


    public static void print(ImStringVal p, Appendable sb, int indent) {
        append(sb, '"');
        append(sb, p.getValS());
        append(sb, '"');
    }


    public static void print(ImBoolVal p, Appendable sb, int indent) {
        append(sb, p.getValB() ? "true" : "false");
    }


    public static void print(ImFuncRef p, Appendable sb, int indent) {
        append(sb, "function ");
        append(sb, p.getFunc().getName());
    }


    public static void print(ImNull p, Appendable sb, int indent) {
        append(sb, "null");
    }


    public static void print(ImStmts ss, Appendable sb, int indent) {
        for (ImStmt s : ss) {
            indent(sb, indent);
            s.print(sb, indent);
            append(sb, ";\n");
        }
    }


    public static void print(ImVar v, Appendable sb, int indent) {
        v.getType().print(sb, indent);
        append(sb, " ");
        append(sb, v.getName());
        append(sb, smallHash(v));
    }


    public static void print(ImOperatorCall e, Appendable sb, int indent) {
        append(sb, "(");
        if (e.getArguments().size() == 2) {
            // binary operator
            e.getArguments().get(0).print(sb, indent);
            append(sb, " ");
            append(sb, e.getOp());
            append(sb, " ");
            e.getArguments().get(1).print(sb, indent);
        } else {
            append(sb, e.getOp());
            for (ImExpr a : e.getArguments()) {
                append(sb, " ");
                a.print(sb, indent);
            }
        }
        append(sb, ")");
    }


    public static String asString(ImPrintable p) {
        Appendable sb = new StringBuilder();
        try {
            p.print(sb, 0);
        } catch (Throwable t) {
            t.printStackTrace();
            append(sb, "## error {");
            append(sb, t);
            append(sb, "}");
        }
        return sb.toString();
    }


    public static void print(ImMethodCall mc, Appendable sb,
                             int indent) {
        mc.getReceiver().print(sb, 0);
        append(sb, ".");
        append(sb, mc.getMethod().getName());
        printArgumentList(sb, 0, mc.getArguments());
    }


    public static void print(ImMemberAccess e, Appendable sb,
                             int indent) {
        e.getReceiver().print(sb, 0);
        append(sb, ".");
        append(sb, e.getVar().getName());
    }


    public static void print(ImAlloc e, Appendable sb, int indent) {
        append(sb, "#alloc(class ");
        append(sb, e.getClazz().getName());
        append(sb, ")");

    }


    public static void print(ImDealloc e, Appendable sb, int indent) {
        append(sb, "#dealloc(class ");
        append(sb, e.getClazz().getName());
        append(sb, ", ");
        e.getObj().print(sb, 0);
        append(sb, ")");
    }


    public static void print(ImInstanceof e, Appendable sb,
                             int indent) {
        e.getObj().print(sb, 0);
        append(sb, " instanceof ");
        append(sb, e.getClazz().getName());


    }


    public static void print(ImTypeIdOfClass e, Appendable sb,
                             int indent) {
        append(sb, e.getClass().getName());
        append(sb, ".typeId");

    }


    public static void print(ImTypeIdOfObj e, Appendable sb,
                             int indent) {
        e.getObj().print(sb, 0);
        append(sb, ".typeId");
    }


    public static void print(ImArrayTypeMulti imArrayTypeMulti,
                             Appendable sb, int indent) {
        append(sb, "array ");
        append(sb, imArrayTypeMulti.getTypename());
        append(sb, " size: ");
        append(sb, imArrayTypeMulti.getArraySize());

    }


    public static void print(ImSetArrayMulti imSetArrayMulti, Appendable sb,
                             int indent) {
        append(sb, imSetArrayMulti.getLeft().getName());
        append(sb, smallHash(imSetArrayMulti.getLeft()));
        append(sb, "[");
        imSetArrayMulti.getIndices().get(0).print(sb, indent);
        append(sb, "]");
        append(sb, "[");
        imSetArrayMulti.getIndices().get(1).print(sb, indent);
        append(sb, "]");
        append(sb, " = ");
        imSetArrayMulti.getRight().print(sb, indent);

    }


    public static void print(ImVarArrayMultiAccess imVarArrayMultiAccess,
                             Appendable sb, int indent) {
        append(sb, imVarArrayMultiAccess.getVar().getName());
        append(sb, smallHash(imVarArrayMultiAccess.getVar()));

    }


    public static void print(ImGetStackTrace e, Appendable sb, int indent) {
        append(sb, "#getStackTrace()");
    }


    public static void print(ImCompiletimeExpr e, Appendable sb, int indent) {
        append(sb, "compiletime<<");
        e.getExpr().print(sb, indent);
        append(sb, ">>");
    }

    public static void print(ImVarargLoop e, Appendable sb, int indent) {
        append(sb, "foreach vararg ");
        e.getLoopVar().print(sb, indent);
        append(sb, " {\n");
        e.getBody().print(sb, indent + 1);
        indent(sb, indent);
        append(sb, "}");
    }


}
