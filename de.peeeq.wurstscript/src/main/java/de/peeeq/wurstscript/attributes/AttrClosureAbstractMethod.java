package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import org.eclipse.jdt.annotation.Nullable;

public class AttrClosureAbstractMethod {

    public static FuncLink calculate(ExprClosure e) {
        WurstType expected = e.attrExpectedTypAfterOverloading();
        if (expected instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) expected;
            return ct.findSingleAbstractMethod(e);
        }
        return null;
    }


    public static @Nullable FunctionSignature getAbstractMethodSignature(WurstType type) {
        if (type instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) type;
            FuncLink abstractMethod = ct.findSingleAbstractMethod(ct.getDef());
            if (abstractMethod == null) {
                return null;
            }
            return FunctionSignature.fromNameLink(abstractMethod);
        }
        return null;
    }



}
