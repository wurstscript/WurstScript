package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;

// Utility to access modifiers for any HasModifier element in a sealed-safe way.
public final class ModifiersUtil {
    private ModifiersUtil() {}

    public static Modifiers get(HasModifier h) {
        return switch (h) {
            // Keep this list in sync with HasModifier’s concrete permitted types.
            case ClassDef c              -> c.getModifiers();
            case InterfaceDef i          -> i.getModifiers();
            case ModuleDef m             -> m.getModifiers();
            case ConstructorDef cdef     -> cdef.getModifiers();
            case GlobalVarDef g          -> g.getModifiers();
            case FuncDef f               -> f.getModifiers();
            case NativeFunc n            -> n.getModifiers();
            case TupleDef t              -> t.getModifiers();
            case ExtensionFuncDef e      -> e.getModifiers();
            case TypeParamDef tp         -> tp.getModifiers();

            // If HasModifier ever expands, the compiler will force you to handle new cases here.
            case EnumDef enumDef -> enumDef.getModifiers();
            case EnumMember enumMember -> enumMember.getModifiers();
            case LocalVarDef localVarDef -> localVarDef.getModifiers();
            case ModuleInstanciation moduleInstanciation -> moduleInstanciation.getModifiers();
            case NativeType nativeType -> nativeType.getModifiers();
            case WPackage wPackage -> wPackage.getModifiers();
            case WParameter wParameter -> wParameter.getModifiers();
            case WShortParameter wShortParameter -> wShortParameter.getModifiers();
        };
    }
}
