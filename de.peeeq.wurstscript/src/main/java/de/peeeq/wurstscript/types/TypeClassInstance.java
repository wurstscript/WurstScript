package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.InstanceDecl;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.List;

public abstract class TypeClassInstance {
    private final WurstTypeClassOrInterface constraint;

    public TypeClassInstance(WurstTypeClassOrInterface constraint) {
        this.constraint = constraint;
    }

    public static TypeClassInstance fromInstance(InstanceDecl decl, List<WurstType> typeArgs, List<TypeClassInstance> dependencies, WurstTypeInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImExpr translate(Element trace, ImTranslator tr) {
                ImClass c = tr.getClassFor(decl);
                ImTypeArguments imTypeArgs = JassIm.ImTypeArguments();
                for (WurstType ta : typeArgs) {
                    imTypeArgs.add(JassIm.ImTypeArgument(ta.imTranslateType(tr)));
                }
                ImClassType ct = JassIm.ImClassType(c, imTypeArgs);
                ImExprs args = JassIm.ImExprs();
                for (TypeClassInstance dep : dependencies) {
                    ImExpr d = dep.translate(trace, tr);
                    args.add(d);
                }
                return JassIm.ImTypeClassDictValue(trace, ct, args);
            }
        };
    }

    public static TypeClassInstance fromTypeParam(Element trace, WurstTypeTypeParam wtp, WurstTypeInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImExpr translate(Element trace, ImTranslator tr) {
                throw new RuntimeException("TODO");
            }
        };
    }

    public WurstTypeClassOrInterface getConstraintType() {
        return constraint;
    }

    public abstract ImExpr translate(Element trace, ImTranslator tr);
}
