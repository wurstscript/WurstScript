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
            public ImExpr translate(ImTranslator tr) {
                throw new RuntimeException("TODO");
            }
        };
    }

    protected ImClassType translateConstraintType(ImTranslator tr) {
        return constraint.imTranslateToTypeClass(tr);
    }

    public static TypeClassInstance fromTypeParam(Element trace, WurstTypeTypeParam wtp, WurstTypeClassOrInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImExpr translate(ImTranslator tr) {
                throw new RuntimeException("TODO");
            }
        };
    }

    public WurstTypeClassOrInterface getConstraintType() {
        return constraint;
    }

    public abstract ImExpr translate(ImTranslator tr);
}
