package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.InstanceDecl;
import de.peeeq.wurstscript.ast.StmtCall;
import de.peeeq.wurstscript.ast.TypeParamConstraint;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.xtend.lib.annotations.ToString;

import java.util.List;

public abstract class TypeClassInstance {

    public static TypeClassInstance fromInstance(InstanceDecl decl, List<WurstType> typeArgs, List<TypeClassInstance> dependencies, WurstTypeInterface constraint) {
        return new TypeClassInstance() {
            @Override
            public ImExpr translate(Element trace, ImVar thisVar, ImTranslator tr) {
                ImClass c = tr.getClassFor(decl);
                ImTypeArguments imTypeArgs = JassIm.ImTypeArguments();
                for (WurstType ta : typeArgs) {
                    imTypeArgs.add(JassIm.ImTypeArgument(ta.imTranslateType(tr)));
                }
                ImClassType ct = JassIm.ImClassType(c, imTypeArgs);
                ImExprs args = JassIm.ImExprs();
                for (TypeClassInstance dep : dependencies) {
                    ImExpr d = dep.translate(trace, thisVar, tr);
                    args.add(d);
                }
                return JassIm.ImTypeClassDictValue(trace, ct, args);
            }

            @Override
            public String toString() {
                return "Instance "  + decl.getImplementedInterface().attrTyp() + " " + Utils.printElementSource(decl);
            }
        };
    }

    public static TypeClassInstance fromTypeParam(Element trace, TypeParamConstraint constraint) {
        return new TypeClassInstance() {
            @Override
            public ImExpr translate(Element trace, ImVar thisVar, ImTranslator tr) {
                ImVar param = tr.getTypeClassParamFor(constraint);
                if (isLocalVar(param)) {
                    return JassIm.ImVarAccess(param);
                } else {
                    if (thisVar == null) {
                        throw new CompileError(trace, "Cannot construct " + this + ", because there is no 'this' available");
                    }
                    // if it is a class field, access the field:
                    return JassIm.ImMemberAccess(trace,
                        JassIm.ImVarAccess(thisVar),
                        JassIm.ImTypeArguments(),
                        param, JassIm.ImExprs());
                }
            }

            @Override
            public String toString() {
                return "Instance from constraint of type parameter " + constraint.parentTypeParam().getName() + " " + Utils.printElementSource(constraint);
            }
        };
    }

    private static boolean isLocalVar(ImVar param) {
        return param != null
            && param.getParent() != null
            && param.getParent().getParent() instanceof ImFunction;
    }


    public abstract ImExpr translate(Element trace, ImVar thisVar, ImTranslator tr);
}
