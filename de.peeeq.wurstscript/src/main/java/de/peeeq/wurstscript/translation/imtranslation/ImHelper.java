package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImHelper {

    static void translateParameters(WParameters params, ImVars result, ImTranslator t) {
        for (WParameter p : params) {
            result.add(t.getVarFor(p));
        }
    }

//	static ImVar translateParam(WParameter p) {
//		return tra
//		return JassIm.ImVar(p.attrTyp().imTranslateType(), p.getName());
//	}

    public static ImType toArray(ImType t) {
        if (t instanceof ImSimpleType) {
            ImSimpleType imSimpleType = (ImSimpleType) t;
            return JassIm.ImArrayType(imSimpleType);
        } else if (t instanceof ImTupleType) {
            ImTupleType imTupleType = (ImTupleType) t;
            return JassIm.ImArrayType(imTupleType);
        } else if (t instanceof ImArrayType) {
            // already an array
            return t;
        } else if (t instanceof ImArrayTypeMulti) {
            ImArrayTypeMulti mat = ((ImArrayTypeMulti) t);
            ArrayList<Integer> nsize = new ArrayList<>(mat.getArraySize());
            nsize.add(8192);
            return JassIm.ImArrayTypeMulti(mat.getEntryType(), nsize);
        }
        throw new Error("Can't make array type from " + t);
    }

    public static void replaceVar(List<ImStmt> stmts, final ImVar oldVar, final ImVar newVar) {
        for (ImStmt s : stmts) {
            replaceVar(s, oldVar, newVar);
        }
    }

    public static void replaceVar(ImStmt s, final ImVar oldVar, final ImVar newVar) {
        s.accept(new VarReplaceVisitor() {

            @Override
            ImVar getReplaceVar(ImVar v) {
                super.visit(v);
                return v == oldVar ? newVar : null;
            }
        });
    }


    public static void replaceVar(ImStmt s, final Map<ImVar, ImVar> substitutions) {
        s.accept(new VarReplaceVisitor() {
            @Override
            ImVar getReplaceVar(ImVar v) {
                super.visit(v);
                return substitutions.get(v);
            }
        });
    }

    public static ImNull nullExpr() {
        return JassIm.ImNull(JassIm.ImVoid());
    }

    public static ImStatementExpr statementExprVoid(ImStmts stmts) {
        return JassIm.ImStatementExpr(stmts, nullExpr());
    }

    public static ImStatementExpr statementExprVoid(ImStmt... stmts) {
        return ImHelper.statementExprVoid(JassIm.ImStmts(stmts));
    }



    abstract static class VarReplaceVisitor extends ImStmt.DefaultVisitor {
        abstract ImVar getReplaceVar(ImVar v);

        @Override
        public void visit(ImVarArrayAccess e) {
            super.visit(e);
            ImVar newVar = getReplaceVar(e.getVar());
            if (newVar != null) {
                e.setVar(newVar);
            }

        }


        @Override
        public void visit(ImVarAccess e) {
            super.visit(e);
            ImVar newVar = getReplaceVar(e.getVar());
            if (newVar != null) {
                e.setVar(newVar);
            }
        }
    }

    public static void replaceElem(Element oldElem, Element newElement) {
        Element parent = oldElem.getParent();
        if (parent == null) throw new Error("Element has no parent: " + oldElem);
        for (int i = 0; i < parent.size(); i++) {
            if (parent.get(i) == oldElem) {
                parent.set(i, newElement);
                return;
            }
        }
        throw new Error("Element " + oldElem + " not found in parent. This should never happen ;)");
    }

    public static ImExpr defaultValueForType(ImSimpleType t) {
        String type = t.getTypename();
        switch (type) {
            case "integer":
                return JassIm.ImIntVal(0);
            case "boolean":
                return JassIm.ImBoolVal(false);
            case "real":
                return JassIm.ImRealVal("0.");
            default:
                return JassIm.ImNull(t);
        }
    }

    public static ImExpr defaultValueForComplexType(ImType t) {
        return t.match(new ImType.Matcher<ImExpr>() {
            @Override
            public ImExpr case_ImArrayTypeMulti(ImArrayTypeMulti imArrayTypeMulti) {
                throw new CompileError(t.attrTrace().attrErrorPos(), "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImTupleType(ImTupleType tt) {
                ImExprs res = JassIm.ImExprs();
                for (ImType it : tt.getTypes()) {
                    res.add(defaultValueForComplexType(it));
                }
                return JassIm.ImTupleExpr(res);
            }

            @Override
            public ImExpr case_ImArrayType(ImArrayType imArrayType) {
                throw new CompileError(t.attrTrace().attrErrorPos(), "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImVoid(ImVoid imVoid) {
                throw new CompileError(t.attrTrace().attrErrorPos(), "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImSimpleType(ImSimpleType st) {
                return defaultValueForType(st);
            }
        });
    }


}
