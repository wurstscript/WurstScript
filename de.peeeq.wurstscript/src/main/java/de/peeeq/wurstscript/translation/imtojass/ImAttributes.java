package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagCompiletime;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;

public class ImAttributes {


    public static ImFunction getNearestFunc(Element e) {
        while (e != null && !(e instanceof ImFunction)) {
            e = e.getParent();
        }
        return (ImFunction) e;
    }


    public static String translateType(ImArrayType t) {
        return t.getEntryType().translateType();
    }

    public static String translateType(ImArrayTypeMulti imArrayTypeMulti) {
        throw new Error("multi-arrays should be eliminated in earlier phase");
    }


    public static String translateType(ImSimpleType t) {
        return t.getTypename();
    }


    public static String translateType(ImTupleType t) {
        throw new Error("tuples should be eliminated in earlier phase");
    }


    public static String translateType(ImVoid t) {
        return "nothing";
    }


    public static boolean isGlobal(ImVar imVar) {
        return imVar.getParent().getParent() instanceof ImProg;
    }


    public static boolean isBj(ImFunction f) {
        return f.getFlags().contains(FunctionFlagEnum.IS_BJ);
    }

    public static boolean isExtern(ImFunction f) {
        return f.getFlags().contains(FunctionFlagEnum.IS_EXTERN);
    }

    public static boolean isNative(ImFunction f) {
        return f.getFlags().contains(FunctionFlagEnum.IS_NATIVE);
    }

    public static boolean isCompiletime(ImFunction f) {
        return f.getFlags().stream()
                .anyMatch(flag -> flag instanceof FunctionFlagCompiletime);
    }


    public static de.peeeq.wurstscript.ast.Element getTrace(ElementWithTrace t) {
        return t.getTrace();
    }

    public static de.peeeq.wurstscript.ast.Element getTrace(Element t) {
        if (t.getParent() != null) {
            return t.getParent().attrTrace();
        }
        return Ast.NoExpr();
    }


    public static boolean hasFlag(ImFunction f, FunctionFlag flag) {
        return f.getFlags().contains(flag);
    }


    public static ImProg getProg(Element el) {
        Element e = el;
        while (e != null) {
            if (e instanceof ImProg) {
                return (ImProg) e;
            }
            e = e.getParent();
        }
        throw new Error("Element " + el + " not attached to root.");
    }


    public static ImClass attrClass(ImMethod m) {
        if (m.getParent() == null) {
            throw new CompileError(m.attrTrace().attrSource(), "Method " + m.getName() + " not attached.");
        }
        return (ImClass) m.getParent().getParent();
    }


}
