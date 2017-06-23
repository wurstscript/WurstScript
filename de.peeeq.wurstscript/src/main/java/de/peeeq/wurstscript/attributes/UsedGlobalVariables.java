package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;

public class UsedGlobalVariables {

    public static ImmutableList<VarDef> getUsedGlobals(ExprOrStatements e) {
        ImmutableList.Builder<VarDef> result = ImmutableList.builder();
        if (e instanceof FunctionCall) {
            FunctionCall funcCall = (FunctionCall) e;
            FunctionDefinition f = funcCall.attrFuncDef();
            if (f != null) {
                result.addAll(f.attrUsedGlobalVariables());
            }

        } else if (e instanceof ExprNewObject) {
            ExprNewObject exprNewObject = (ExprNewObject) e;
            ConstructorDef constr = exprNewObject.attrConstructorDef();
            if (constr != null) {
                result.addAll(constr.getBody().attrUsedGlobalVariables());
            }
        } else if (e instanceof ExprDestroy) {
            ExprDestroy stmtDestroy = (ExprDestroy) e;
            WurstType t = stmtDestroy.getDestroyedObj().attrTyp();
            if (t instanceof WurstTypeClass) {
                WurstTypeClass ct = (WurstTypeClass) t;
                OnDestroyDef ondestr = ct.getClassDef().getOnDestroy();
                result.addAll(ondestr.getBody().attrUsedGlobalVariables());
            }
        } else if (e instanceof NameRef) {
            NameRef nameRef = (NameRef) e;
            NameDef def = nameRef.attrNameDef();
            if (def instanceof GlobalVarDef) {
                GlobalVarDef varDef = (GlobalVarDef) def;
                result.add(varDef);
            }
        }
        // check children:
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child instanceof ExprOrStatements) {
                ExprOrStatements child2 = (ExprOrStatements) child;
                result.addAll(child2.attrUsedGlobalVariables());
            }
        }
        return result.build();
    }


    public static ImmutableList<VarDef> getUsedGlobals(FunctionImplementation func) {
        return func.getBody().attrUsedGlobalVariables();
    }

    public static ImmutableList<VarDef> getUsedGlobals(NativeFunc nativeFunc) {
        return ImmutableList.of();
    }

    public static ImmutableList<VarDef> getUsedGlobals(TupleDef tupleDef) {
        return ImmutableList.of();
    }


    // ----

    public static ImmutableList<VarDef> getReadGlobals(ExprOrStatements e) {
        Builder<VarDef> result = ImmutableList.builder();
        collectReadGlobals(e, result);
        return result.build();
    }


    private static void collectReadGlobals(Element e, Builder<VarDef> result) {
        if (e instanceof FunctionCall) {
            FunctionCall funcRef = (FunctionCall) e;
            FunctionDefinition f = funcRef.attrFuncDef();
            if (f != null) {
                result.addAll(f.attrReadGlobalVariables());
            }

        } else if (e instanceof ExprNewObject) {
            ExprNewObject exprNewObject = (ExprNewObject) e;
            ConstructorDef constr = exprNewObject.attrConstructorDef();
            if (constr != null) {
                result.addAll(constr.getBody().attrReadGlobalVariables());
            }
        } else if (e instanceof ExprDestroy) {
            ExprDestroy stmtDestroy = (ExprDestroy) e;
            WurstType t = stmtDestroy.getDestroyedObj().attrTyp();
            if (t instanceof WurstTypeClass) {
                WurstTypeClass ct = (WurstTypeClass) t;
                OnDestroyDef ondestr = ct.getClassDef().getOnDestroy();
                result.addAll(ondestr.getBody().attrReadGlobalVariables());
            }
        } else if (e instanceof NameRef) {
            NameRef nameRef = (NameRef) e;
            if (e.getParent() instanceof StmtSet && ((StmtSet) e.getParent()).getUpdatedExpr() == e) {
                // write access
            } else {
                NameDef def = nameRef.attrNameDef();
                if (def instanceof GlobalVarDef) {
                    GlobalVarDef varDef = (GlobalVarDef) def;
                    result.add(varDef);
                }
            }
        } else if (e instanceof ExprClosure) {
            // do not collect vars in closures, because closures are usually called later
            return;
        }
        // check children:
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child instanceof ExprOrStatements) {
                ExprOrStatements child2 = (ExprOrStatements) child;
                result.addAll(child2.attrReadGlobalVariables());
            } else {
                collectReadGlobals(child, result);
            }
        }
    }


    public static ImmutableList<VarDef> getReadGlobals(FunctionImplementation func) {
        return func.getBody().attrReadGlobalVariables();
    }

    public static ImmutableList<VarDef> getReadGlobals(NativeFunc nativeFunc) {
        return ImmutableList.of();
    }

    public static ImmutableList<VarDef> getReadGlobals(TupleDef tupleDef) {
        return ImmutableList.of();
    }


    public static ImmutableList<VarDef> getReadGlobals(InitBlock initBlock) {
        return initBlock.getBody().attrReadGlobalVariables();
    }


}
