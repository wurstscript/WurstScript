package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * On Lua a function whose source return type is a tuple returns its scalar components as Lua multiple results
 * ({@code return x, y}), and a call takes them into locals of its own ({@code local a, b = f()}). Jass has no
 * multiple results, so there each component but the first goes through a global of the function
 * ({@link ImTranslator#getTupleTempReturnVarsFor}): a global write in the function and a global read after the call
 * for each, which Lua has no need for.
 * <p>
 * {@link EliminateTuples} makes this shape, and it holds from there to the backend:
 * <ul>
 * <li>A function returning two or more scalar components (a nested tuple counts its leaves) has the flat tuple of
 * those as its return type, and each of its returns returns a tuple expression of them. A function returning one
 * component returns it as an ordinary value.</li>
 * <li>A call of such a function, or of a method implemented by one, is a statement, or the right side of an
 * assignment to a <em>results local</em>: a local of the flat tuple type, which only such assignments write.</li>
 * <li>A results local is read only by a selection of one of its components ({@code t.k}).</li>
 * <li>There is no other tuple: no tuple variable, tuple expression or selection anywhere else.</li>
 * </ul>
 * To the optimizer a results local is a local like any other: a call writes it and a selection reads it, so it takes
 * part in the analyses unchanged (LocalMerger may give two results locals of the same type one variable). A result
 * which no call reads is dropped by the garbage removal, as the global which carried it on Jass is.
 */
public final class LuaMultipleResults {

    private LuaMultipleResults() {
    }

    /**
     * The return type of a function with the source return type {@code type} on Lua: the flat tuple of its scalar
     * components when it has two or more, otherwise null (the function returns an ordinary value).
     */
    public static ImTupleType resultsType(ImType type) {
        if (!(type instanceof ImTupleType)) {
            return null;
        }
        List<ImType> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        collectLeaves(type, "", types, names);
        return types.size() < 2 ? null : JassIm.ImTupleType(types, names);
    }

    private static void collectLeaves(ImType type, String prefix, List<ImType> types, List<String> names) {
        if (type instanceof ImTupleType tuple) {
            for (int i = 0; i < tuple.getTypes().size(); i++) {
                String name = tuple.getNames().get(i);
                collectLeaves(tuple.getTypes().get(i), prefix.isEmpty() ? name : prefix + "_" + name, types, names);
            }
        } else {
            types.add(type.copy());
            names.add(prefix);
        }
    }

    /**
     * A value of the source type {@code type} built from its scalar components: a tuple expression of the same
     * nesting whose leaves, numbered in order from 0, are {@code leaf} of their number.
     */
    public static ImExpr shape(ImType type, IntFunction<ImExpr> leaf) {
        return shape(type, leaf, new int[1]);
    }

    private static ImExpr shape(ImType type, IntFunction<ImExpr> leaf, int[] next) {
        if (type instanceof ImTupleType tuple) {
            ImExprs parts = JassIm.ImExprs();
            for (ImType part : tuple.getTypes()) {
                parts.add(shape(part, leaf, next));
            }
            return JassIm.ImTupleExpr(parts);
        }
        return leaf.apply(next[0]++);
    }

    /** Whether {@code v} is a results local: a local of a function whose type is a tuple. */
    public static boolean isResultsLocal(ImVar v) {
        return v.getType() instanceof ImTupleType
            && v.getParent() instanceof ImVars vars
            && vars.getParent() instanceof ImFunction f
            && f.getLocals() == vars;
    }

    /** Whether {@code e} is a call whose results are multiple results. */
    public static boolean isMultipleResultsCall(Element e) {
        return e instanceof ImFunctionCall functionCall && functionCall.getFunc().getReturnType() instanceof ImTupleType
            || e instanceof ImMethodCall methodCall && methodCall.getMethod().getImplementation() != null
            && methodCall.getMethod().getImplementation().getReturnType() instanceof ImTupleType;
    }

    /**
     * What {@code e} evaluates to, without the statement expressions around it: until the flatten which follows the
     * tuple elimination, the call a results local is assigned can be behind the statements which prepare its
     * arguments.
     */
    public static ImExpr valueOf(ImExpr e) {
        while (e instanceof ImStatementExpr statementExpr) {
            e = statementExpr.getExpr();
        }
        return e;
    }

    /** The return type of the function a multiple-results call calls. */
    public static ImTupleType resultsTypeOf(ImExpr call) {
        ImFunction f = call instanceof ImMethodCall methodCall
            ? methodCall.getMethod().getImplementation()
            : ((ImFunctionCall) call).getFunc();
        return (ImTupleType) f.getReturnType();
    }

    /** Throws when {@code prog} is not of the shape described above. */
    public static void assertShape(ImProg prog) {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVar v) {
                super.visit(v);
                if (TypesHelper.typeContainsTuples(v.getType())) {
                    if (!isResultsLocal(v)) {
                        fail(v, "a tuple variable which is not a local");
                    }
                    for (ImType component : ((ImTupleType) v.getType()).getTypes()) {
                        if (TypesHelper.typeContainsTuples(component) || component instanceof ImArrayType
                            || component instanceof ImArrayTypeMulti) {
                            fail(v, "a results local which is not a flat tuple of scalars");
                        }
                    }
                }
            }

            @Override
            public void visit(ImFunction f) {
                super.visit(f);
                if (f.getReturnType() instanceof ImTupleType tuple) {
                    for (ImType component : tuple.getTypes()) {
                        if (TypesHelper.typeContainsTuples(component)) {
                            fail(f, "multiple results which are not flat");
                        }
                    }
                }
            }

            @Override
            public void visit(ImTupleExpr e) {
                super.visit(e);
                if (!(e.getParent() instanceof ImReturn ret)
                    || !(ret.getNearestFunc().getReturnType() instanceof ImTupleType tuple)
                    || tuple.getTypes().size() != e.getExprs().size()) {
                    fail(e, "a tuple expression which is not the multiple results of a return");
                }
            }

            @Override
            public void visit(ImReturn ret) {
                super.visit(ret);
                if (ret.getNearestFunc().getReturnType() instanceof ImTupleType
                    && !(ret.getReturnValue() instanceof ImTupleExpr)) {
                    fail(ret, "a return of multiple results which does not return a tuple expression");
                }
            }

            @Override
            public void visit(ImTupleSelection e) {
                super.visit(e);
                if (!(e.getTupleExpr() instanceof ImVarAccess access) || !isResultsLocal(access.getVar())
                    || e.getTupleIndex() < 0
                    || e.getTupleIndex() >= ((ImTupleType) access.getVar().getType()).getTypes().size()
                    || e.isUsedAsLValue()) {
                    fail(e, "a tuple selection which is not a read of a results local");
                }
            }

            @Override
            public void visit(ImVarAccess e) {
                super.visit(e);
                if (!isResultsLocal(e.getVar())) {
                    return;
                }
                if (e.getParent() instanceof ImTupleSelection) {
                    return;
                }
                if (!(e.getParent() instanceof ImSet set) || set.getLeft() != e
                    || !isMultipleResultsCall(valueOf(set.getRight()))
                    || !resultsTypeOf(valueOf(set.getRight())).equalsType(e.getVar().getType())) {
                    fail(e, "a results local used other than as the target of a call's results or by a selection");
                }
            }

            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                checkCallPosition(call);
            }

            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                checkCallPosition(call);
            }

            private void checkCallPosition(ImExpr call) {
                if (!isMultipleResultsCall(call)) {
                    return;
                }
                Element value = call;
                while (value.getParent() instanceof ImStatementExpr statementExpr && statementExpr.getExpr() == value) {
                    value = statementExpr;
                }
                if (!(value.getParent() instanceof ImStmts)
                    && !(value.getParent() instanceof ImSet set && set.getRight() == value
                        && set.getLeft() instanceof ImVarAccess target && isResultsLocal(target.getVar()))) {
                    fail(call, "a call of multiple results which is neither a statement nor assigned to a results local");
                }
            }
        });
    }

    private static void fail(Element e, String what) {
        Element owner = e;
        while (owner != null && !(owner instanceof ImFunction)) {
            owner = owner.getParent();
        }
        throw new IllegalStateException("Lua multiple results: " + what + ": " + e
            + (owner == null ? "" : "\nin " + owner));
    }
}
