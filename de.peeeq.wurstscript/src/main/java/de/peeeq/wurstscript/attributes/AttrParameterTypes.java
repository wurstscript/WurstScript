package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;

public class AttrParameterTypes {

    public static ImmutableList<WurstType> parameterTypesIncludingReceiver(FunctionDefinition f) {
        ImmutableList.Builder<WurstType> result = ImmutableList.builder();
        if (f.attrReceiverType() != null) {
            result.add(f.attrReceiverType());
        }
        result.addAll(f.attrParameterTypes());
        return result.build();
    }

    public static ImmutableList<WurstType> parameterTypes(FunctionDefinition f) {
        ImmutableList.Builder<WurstType> result = ImmutableList.builder();
        for (WParameter p : f.getParameters()) {
            result.add(p.attrTyp());
        }
        return result.build();
    }

    public static @Nullable WurstType receiverType(FuncDef f) {
        if (f.attrIsDynamicClassMember()) {
            NameDef n = f.attrNearestStructureDef();
            return n.attrTyp();
        }
        return null;
    }

    public static WurstType receiverType(ExtensionFuncDef f) {
        return f.getExtendedType().attrTyp().dynamic();
    }

    public static @Nullable WurstType receiverType(TupleDef tupleDef) {
        return null;
    }

    public static @Nullable WurstType receiverType(NativeFunc f) {
        return null;
    }

}
