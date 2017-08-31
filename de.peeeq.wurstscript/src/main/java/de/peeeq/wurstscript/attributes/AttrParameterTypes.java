package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;

public class AttrParameterTypes {

    public static ParamTypes parameterTypes(FunctionDefinition f) {
        ImmutableList.Builder<ParamTypes.ParamInfo> result = ImmutableList.builder();
        for (int i = 0; i < f.getParameters().size(); i++) {
            result.add(new ParamTypes.ParamInfo(i, f.getParameters().get(i)));
        }
        return new ParamTypes(result.build());
    }

    public static @Nullable WurstType receiverType(FuncDef f) {
        if (f.attrIsDynamicClassMember()) {
            NameDef n = (NameDef) f.attrNearestStructureDef();
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

    public static WurstType receiverType(ConstructorDef constructorDef) {
        return null;
    }
}
