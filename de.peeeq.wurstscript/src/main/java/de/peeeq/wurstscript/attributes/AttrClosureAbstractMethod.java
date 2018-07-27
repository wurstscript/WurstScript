package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;

public class AttrClosureAbstractMethod {

    public static FuncLink calculate(ExprClosure e) {
        WurstType expected = e.attrExpectedTyp();
        if (expected instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) expected;
            return ct.findSingleAbstractMethod(e);
        }
        return null;
    }


    public static FunctionSignature getAbstractMethodSignature(WurstType type, WurstModel m) {
        if (type instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) type;
            FuncLink abstractMethod = ct.findSingleAbstractMethod(ct.getDef());
            if (abstractMethod == null) {
                return null;
            }
            return FunctionSignature.fromNameLink(abstractMethod, m);
        }
        return null;
    }



}
