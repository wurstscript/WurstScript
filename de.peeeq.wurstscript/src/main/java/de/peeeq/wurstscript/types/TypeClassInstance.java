package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
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

    public static TypeClassInstance fromObject(WurstTypeClassOrInterface objectType, InterfaceDef supI) {
        return new TypeClassInstance() {
            @Override
            public ImExpr translate(Element trace, ImVar thisVar, ImTranslator tr) {
                ImClass sup = tr.getClassFor(supI);
                ImClass cd = tr.getGenericObjectTypeClassInstance();
                // add instance as superclass if not present yet
                if (cd.getSuperClasses().stream().noneMatch(sc -> sc.getClassDef() == sup)) {
                    cd.getSuperClasses().add(JassIm.ImClassType(sup,
                        JassIm.ImTypeArguments(JassIm.ImTypeArgument(objectType.imTranslateType(tr)))));
                    // add sub methods
                    supI.attrTyp().getMemberMethods(trace).forEach(m -> {
                        ImMethod imMethod = tr.getMethodFor((FuncDef) m.getDef());
                        for (ImMethod cdM : cd.getMethods()) {
                            if (cdM.getName().equals(m.getName())) {
                                imMethod.getSubMethods().add(cdM);
                            }
                        }
                    });
                }
                ImClassType c = JassIm.ImClassType(cd, JassIm.ImTypeArguments());
                return JassIm.ImTypeClassDictValue(trace, c, JassIm.ImExprs());
            }

            @Override
            public String toString() {
                return "Object type class instance";
            }
        };
    }


    public abstract ImExpr translate(Element trace, ImVar thisVar, ImTranslator tr);
}
