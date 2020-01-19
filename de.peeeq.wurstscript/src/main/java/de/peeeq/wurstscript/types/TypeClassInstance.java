package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImTypeClassImpl;
import de.peeeq.wurstscript.jassIm.ImTypeVar;
import de.peeeq.wurstscript.jassIm.JassIm;
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

    public static TypeClassInstance fromTypeParam(WurstTypeTypeParam wtp, WurstTypeClassOrInterface wurstTypeInterface, WurstTypeClassOrInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImTypeClassImpl translate(ImTranslator tr) {
                ImTypeVar tv = tr.getTypeVar(wtp.getDef());
                // TODO type var needs something that I can reference now ...
                return JassIm.ImTypeClassImplFromOther(translateConstraintType(tr), );
            }
        };
    }

    public WurstTypeClassOrInterface getConstraintType() {
        return constraint;
    }

    public abstract ImTypeClassImpl translate(ImTranslator tr);
}
