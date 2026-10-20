package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

public interface AssertProperty {
    AssertProperty FLAT = e -> {
        if (e instanceof ImStatementExpr) {
            throw new Error("contains statementExpr " + e);
        }
    };

    AssertProperty NOTUPLES = e -> {
        if (e instanceof ImTupleExpr
                || e instanceof ImTupleSelection
        ) {
            throw new Error("contains tuple exprs " + e);
        }
        if (e instanceof ImVar v) {
            if (TypesHelper.typeContainsTuples(v.getType())) {
                throw new Error("contains tuple var: " + v + " in\n" + v.getParent().getParent());
            }
        }
    };

    static AssertProperty rooted(Element root) {
        return new AssertProperty() {
            ImFunction currentFunction;

            @Override
            public void check(Element e) {
                if (e instanceof ImVar imVar) {
                    checkType(e, imVar.getType());
                } else if (e instanceof ImFunction f) {
                    currentFunction = f;
                    checkType(e, (f).getReturnType());
                } else if (e instanceof ImTypeClassFunc imTypeClassFunc) {
                    checkType(e, imTypeClassFunc.getReturnType());
                } else if (e instanceof ImMethod imMethod) {
                    checkType(e, imMethod.getMethodClass());
                    checkRooted(e, imMethod.getImplementation());
                } else if (e instanceof ImVarargLoop imVarargLoop) {
                    for (ImVarargLoopVar loopVar : imVarargLoop.getLoopVars()) {
                        checkRooted(e, loopVar.getVar());
                    }
                } else if (e instanceof ImTypeVarDispatch imTypeVarDispatch) {
                    checkRooted(e, imTypeVarDispatch.getTypeClassFunc());
                    checkRooted(e, imTypeVarDispatch.getTypeVariable());
                } else if (e instanceof ImVarAccess imVarAccess) {
                    checkRooted(e, imVarAccess.getVar());
                } else if (e instanceof ImVarArrayAccess imVarArrayAccess) {
                    checkRooted(e, imVarArrayAccess.getVar());
                } else if (e instanceof ImMethodCall imMethodCall) {
                    checkRooted(e, imMethodCall.getMethod());
                } else if (e instanceof ImMemberAccess imMemberAccess) {
                    checkRooted(e, imMemberAccess.getVar());
                } else if (e instanceof ImClassRelatedExprWithClass imClassRelatedExprWithClass) {
                    checkType(e, imClassRelatedExprWithClass.getClazz());
                } else if (e instanceof ImFunctionCall imFunctionCall) {
                    checkRooted(e, imFunctionCall.getFunc());
                } else if (e instanceof ImFuncRef imFuncRef) {
                    checkRooted(e, imFuncRef.getFunc());
                } else if (e instanceof ImTypeArgument imTypeArgument) {
                    checkType(e, imTypeArgument.getType());
                }
            }

            private void checkType(Element e, ImType type) {
                if (type instanceof ImArrayType imArrayType) {
                    checkType(e, imArrayType.getEntryType());
                } else if (type instanceof ImArrayTypeMulti imArrayTypeMulti) {
                    checkType(e, imArrayTypeMulti.getEntryType());
                } else if (type instanceof ImClassType imClassType) {
                    checkRooted(e, imClassType.getClassDef());
                    for (ImTypeArgument ta : imClassType.getTypeArguments()) {
                        checkType(e, ta.getType());
                    }
                } else if (type instanceof ImTypeVarRef imTypeVarRef) {
                    checkRooted(e, imTypeVarRef.getTypeVariable());
                }
            }

            public void checkRooted(Element location, Element el) {
                try {
                    Element e = el;
                    while (e != null) {
                        if (e == root) {
                            return;
                        }
                        Element parent = e.getParent();
                        if (parent == null) {
                            break;
                        }
                        checkContains(location, parent, e);
                        if (parent instanceof ImFunction && parent != currentFunction) {
                            throw new CompileError(location, "Element " + el + " is rooted in function " + parent + " but should be in function " + currentFunction);
                        }
                        e = parent;
                    }
                } catch (CompileError e) {
                    throw new CompileError(location, "Element " + el + " not rooted. In ...\n" + location + "\n\n" + e.getMessage());
                }
                throw new CompileError(location, "Element " + el + " not rooted. In ...\n" + location);
            }

            private void checkContains(Element location, Element parent, Element e) {
                for (int i = 0; i < parent.size(); i++) {
                    if (parent.get(i) == e) {
                        return;
                    }
                }
                throw new CompileError(location, "Element " + e + " does not appear in parent " + parent.getClass().getSimpleName() + "." +
                        "\nIn ...\n" + location);
            }
        };
    }

    void check(Element e);
}
