package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.OtherLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;

public class UsedGlobalVariables {

    public static ImmutableList<VarDef> getUsedGlobals(ExprOrStatements e) {
        ImmutableList.Builder<VarDef> result = ImmutableList.builder();
        if (e instanceof FunctionCall funcCall) {
            FuncLink f = funcCall.attrFuncLink();
            if (f != null) {
                result.addAll(f.getDef().attrUsedGlobalVariables());
            }

        } else if (e instanceof ExprNewObject exprNewObject) {
            ConstructorDef constr = exprNewObject.attrConstructorDef();
            if (constr != null) {
                result.addAll(constr.getBody().attrUsedGlobalVariables());
            }
        } else if (e instanceof ExprDestroy stmtDestroy) {
            WurstType t = stmtDestroy.getDestroyedObj().attrTyp();
            if (t instanceof WurstTypeClass ct) {
                OnDestroyDef ondestr = ct.getClassDef().getOnDestroy();
                result.addAll(ondestr.getBody().attrUsedGlobalVariables());
            }
        } else if (e instanceof NameRef nameRef) {
            NameLink def = nameRef.attrNameLink();
            if (def != null && !(def instanceof OtherLink) && def.getDef() instanceof GlobalVarDef) {
                GlobalVarDef varDef = (GlobalVarDef) def.getDef();
                result.add(varDef);
            }
        }
        // check children:
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child instanceof ExprOrStatements child2) {
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
        if (e instanceof FunctionCall funcRef) {
            FuncLink f = funcRef.attrFuncLink();
            if (f != null) {
                result.addAll(f.getDef().attrReadGlobalVariables());
            }

        } else if (e instanceof ExprNewObject exprNewObject) {
            ConstructorDef constr = exprNewObject.attrConstructorDef();
            if (constr != null) {
                result.addAll(constr.getBody().attrReadGlobalVariables());
            }
        } else if (e instanceof ExprDestroy stmtDestroy) {
            WurstType t = stmtDestroy.getDestroyedObj().attrTyp();
            if (t instanceof WurstTypeClass ct) {
                OnDestroyDef ondestr = ct.getClassDef().getOnDestroy();
                result.addAll(ondestr.getBody().attrReadGlobalVariables());
            }
        } else if (e instanceof NameRef nameRef) {
            if (e.getParent() instanceof StmtSet && ((StmtSet) e.getParent()).getUpdatedExpr() == e) {
                // write access
            } else {
                NameLink def = nameRef.attrNameLink();
                if (def != null && !(def instanceof OtherLink) && def.getDef() instanceof GlobalVarDef) {
                    GlobalVarDef varDef = (GlobalVarDef) def.getDef();
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
            if (child instanceof ExprOrStatements child2) {
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
