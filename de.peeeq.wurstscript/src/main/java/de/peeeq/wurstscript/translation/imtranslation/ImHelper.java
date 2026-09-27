package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Constants;

import java.util.*;

public class ImHelper {

    /** A Jass function takes at most this many parameters. */
    public static final int JASS_MAX_PARAMETERS = 31;

    /**
     * How many Jass parameters a value of this type occupies.
     * <p>
     * A tuple is passed as one parameter per component, so an IM signature which looks well inside the
     * limit can emit a Jass one which is not. Any pass which builds a signature has to count this way
     * rather than counting parameters.
     */
    public static int flattenedJassArity(ImType type) {
        if (type instanceof ImTupleType tupleType) {
            int result = 0;
            List<ImType> types = tupleType.getTypes();
            for (int i = 0; i < types.size(); i++) {
                result += flattenedJassArity(types.get(i));
            }
            return result;
        }
        return 1;
    }

    public static Set<ImFunction> calculateFunctionsOfProg(ImProg prog) {
        ImFunctions functions = prog.getFunctions();
        ImClasses classes = prog.getClasses();
        int functionCount = functions.size();
        for (int i = 0; i < classes.size(); i++) {
            functionCount += classes.get(i).getFunctions().size();
        }
        Set<ImFunction> allFunctions = HashSet.newHashSet(functionCount);
        for (int i = 0; i < functions.size(); i++) {
            allFunctions.add(functions.get(i));
        }
        for (int i = 0; i < classes.size(); i++) {
            ImFunctions classFunctions = classes.get(i).getFunctions();
            for (int j = 0; j < classFunctions.size(); j++) {
                allFunctions.add(classFunctions.get(j));
            }
        }
        return allFunctions;
    }

    static void translateParameters(WParameters params, ImVars result, ImTranslator t) {
        for (int i = 0; i < params.size(); i++) {
            result.add(t.getVarFor(params.get(i)));
        }
    }

    public static ImType toArray(ImType t) {
        if (t instanceof ImArrayType) {
            // already an array (should never happen?)
            return t;
        } if (t instanceof ImArrayTypeMulti) {
            ImArrayTypeMulti mat = ((ImArrayTypeMulti) t);
            ArrayList<Integer> nsize = new ArrayList<>(mat.getArraySize());
            nsize.add(Constants.MAX_ARRAY_SIZE);
            return JassIm.ImArrayTypeMulti(mat.getEntryType(), nsize);
        } else {
            return JassIm.ImArrayType(t);
        }
    }

    public static void replaceVar(List<ImStmt> stmts, final ImVar oldVar, final ImVar newVar) {
        for (int i = 0; i < stmts.size(); i++) {
            replaceVar(stmts.get(i), oldVar, newVar);
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
                throw new CompileError(t, "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImAnyType(ImAnyType at) {
                return JassIm.ImNull(at);
            }

            @Override
            public ImExpr case_ImTupleType(ImTupleType tt) {
                ImExprs res = JassIm.ImExprs();
                List<ImType> types = tt.getTypes();
                for (int i = 0; i < types.size(); i++) {
                    res.add(defaultValueForComplexType(types.get(i)));
                }
                return JassIm.ImTupleExpr(res);
            }

            @Override
            public ImExpr case_ImArrayType(ImArrayType imArrayType) {
                throw new CompileError(t, "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                throw new CompileError(t, "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImVoid(ImVoid imVoid) {
                throw new CompileError(t, "Cannot find default value for type " + t);
            }

            @Override
            public ImExpr case_ImClassType(ImClassType imClassType) {
                return JassIm.ImIntVal(0);
            }

            @Override
            public ImExpr case_ImSimpleType(ImSimpleType st) {
                return defaultValueForType(st);
            }
        });
    }


}
