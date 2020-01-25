package de.peeeq.wurstscript.types;


import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import static de.peeeq.wurstscript.utils.Utils.emptyList;

public abstract class TypeClassInstance {
    private final WurstTypeClassOrInterface constraint;

    public TypeClassInstance(WurstTypeClassOrInterface constraint) {
        this.constraint = constraint;
    }

    public static TypeClassInstance asSubtype(WurstTypeClassOrInterface subType, WurstTypeClassOrInterface constraint) {
        return new TypeClassInstance(constraint) {
            @Override
            public ImExpr translate(Element trace, ImTranslator tr) {
                // using can alloc here, which we will later rewrite to a constant
                // variable access (the dict class has no fields and no constructor/destructor code)
                ImClassType ct = subType.imTranslateToTypeClass(tr);
                return JassIm.ImAlloc(trace, ct);
            }
        };
    }

    protected ImClassType translateConstraintType(ImTranslator tr) {
        return constraint.imTranslateToTypeClass(tr);
    }

    public static TypeClassInstance fromTypeParam(Element trace, WurstTypeTypeParam wtp, WurstTypeClassOrInterface constraint) {
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
