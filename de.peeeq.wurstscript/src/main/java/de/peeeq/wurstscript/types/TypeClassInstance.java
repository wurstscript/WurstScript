package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public abstract class TypeClassInstance {
    private final WurstTypeClassOrInterface constraint;

    public TypeClassInstance(WurstTypeClassOrInterface constraint) {
        this.constraint = constraint;
    }

    public static TypeClassInstance asSubtype(WurstTypeClassOrInterface subType, WurstTypeClassOrInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImTypeClassImpl translate(ImTranslator tr) {
                return JassIm.ImTypeClassImplFromInterface(translateConstraintType(tr), subType.imTranslateToTypeClass(tr));
            }
        };
    }

    protected ImClassType translateConstraintType(ImTranslator tr) {
        return constraint.imTranslateToTypeClass(tr);
    }

    public static TypeClassInstance fromTypeParam(Element trace, WurstTypeTypeParam wtp, WurstTypeClassOrInterface wurstTypeInterface, WurstTypeClassOrInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImTypeClassImpl translate(ImTranslator tr) {
                ImTypeVar tv = tr.getTypeVar(wtp.getDef());
                ImClassType constr = tr.getConstraintFor(wtp, wurstTypeInterface);
                ImTypeClassConstraint otherImpl = JassIm.ImTypeClassConstraint(trace, constraint.getName(), constr);
                return JassIm.ImTypeClassImplFromOther(translateConstraintType(tr), otherImpl);
            }
        };
    }

    public WurstTypeClassOrInterface getConstraintType() {
        return constraint;
    }

    public abstract ImTypeClassImpl translate(ImTranslator tr);
}
