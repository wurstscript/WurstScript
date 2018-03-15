package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.NameLinkType;
import de.peeeq.wurstscript.types.FunctionSignature;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import org.eclipse.jdt.annotation.Nullable;

public class AttrClosureAbstractMethod {

    public static @Nullable NameLink calculate(ExprClosure e) {
        WurstType expected = e.attrExpectedTyp();
        if (expected instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) expected;
            return ct.findSingleAbstractMethod();
        }
        return null;
    }


    public static @Nullable FunctionSignature getAbstractMethodSignature(WurstType type) {
        if (type instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) type;
            NameLink abstractMethod = ct.findSingleAbstractMethod();
            if (abstractMethod == null) {
                return null;
            }
            FunctionSignature sig = FunctionSignature.fromNameLink(abstractMethod);
            sig = sig.setTypeArgs(abstractMethod.getNameDef(), ct.getTypeArgBinding());
            return sig;
        }
        return null;
    }



}
