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
        if (e instanceof ImVar) {
            ImVar v = (ImVar) e;
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
                if (e instanceof ImVar) {
                    checkType(e, ((ImVar) e).getType());
                } else if (e instanceof ImFunction) {
                    ImFunction f = (ImFunction) e;
                    currentFunction = f;
                    checkType(e, (f).getReturnType());
                } else if (e instanceof ImTypeClassFunc) {
                    checkType(e, ((ImTypeClassFunc) e).getReturnType());
                } else if (e instanceof ImMethod) {
                    checkType(e, ((ImMethod) e).getMethodClass());
                    checkRooted(e, ((ImMethod) e).getImplementation());
                } else if (e instanceof ImVarargLoop) {
                    checkRooted(e, ((ImVarargLoop) e).getLoopVar());
                } else if (e instanceof ImTypeVarDispatch) {
                    checkRooted(e, ((ImTypeVarDispatch) e).getTypeClassFunc());
                    checkRooted(e, ((ImTypeVarDispatch) e).getTypeVariable());
                } else if (e instanceof ImVarAccess) {
                    checkRooted(e, ((ImVarAccess) e).getVar());
                } else if (e instanceof ImVarArrayAccess) {
                    checkRooted(e, ((ImVarArrayAccess) e).getVar());
                } else if (e instanceof ImMethodCall) {
                    checkRooted(e, ((ImMethodCall) e).getMethod());
                } else if (e instanceof ImMemberAccess) {
                    checkRooted(e, ((ImMemberAccess) e).getVar());
                } else if (e instanceof ImClassRelatedExprWithClass) {
                    checkType(e, ((ImClassRelatedExprWithClass) e).getClazz());
                } else if (e instanceof ImFunctionCall) {
                    checkRooted(e, ((ImFunctionCall) e).getFunc());
                } else if (e instanceof ImFuncRef) {
                    checkRooted(e, ((ImFuncRef) e).getFunc());
                } else if (e instanceof ImTypeArgument) {
                    checkType(e, ((ImTypeArgument) e).getType());
                }
            }

            private void checkType(Element e, ImType type) {
                if (type instanceof ImArrayType) {
                    checkType(e, ((ImArrayType) type).getEntryType());
                } else if (type instanceof ImArrayTypeMulti) {
                    checkType(e, ((ImArrayTypeMulti) type).getEntryType());
                } else if (type instanceof ImClassType) {
                    checkRooted(e, ((ImClassType) type).getClassDef());
                    for (ImTypeArgument ta : ((ImClassType) type).getTypeArguments()) {
                        checkType(e, ta.getType());
                    }
                } else if (type instanceof ImTypeVarRef) {
                    checkRooted(e, ((ImTypeVarRef) type).getTypeVariable());
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
