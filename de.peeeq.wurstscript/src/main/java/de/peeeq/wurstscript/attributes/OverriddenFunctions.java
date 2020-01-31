package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.VarLink;
import de.peeeq.wurstscript.types.WurstTypeModuleInstanciation;
import de.peeeq.wurstscript.validation.WurstValidator;

import java.util.ArrayList;
import java.util.List;

public class OverriddenFunctions {


    public static FunctionDefinition getRealFuncDef(ExtensionFuncDef f) {
        // extension functions cannot be overridden
        return f;
    }

    public static FunctionDefinition getRealFuncDef(NativeFunc f) {
        // native functions cannot be overridden
        return f;
    }

    public static FunctionDefinition getRealFuncDef(FuncDef f) {
        if (f.attrIsPrivate()) {
            // private functions cannot be overridden
            return f;
        }
        if (!isInModuleInstantiation(f)) {
            return f;
        }
        if (f.attrNearestNamedScope() == null) {
            return f;
        }
        if (f.attrNearestNamedScope().getParent() == null) {
            return f;
        }
        WScope scope = f.attrNearestNamedScope().getParent().attrNearestScope();
        return getRealFuncDef(f, scope);
    }

    private static boolean isInModuleInstantiation(Element e) {
        while (e != null) {
            if (e instanceof ModuleInstanciation) {
                return true;
            }
            e = e.getParent();
        }
        return false;
    }

    private static FunctionDefinition getRealFuncDef(FuncDef f, WScope scope) {
        if (scope instanceof StructureDef) {
            StructureDef c = (StructureDef) scope;

            FuncLink fNameLink = FuncLink.create(f, f.attrNearestScope());

            if (c.attrNameLinks().containsKey(f.getName())) {
                for (DefLink nl : c.attrNameLinks().get(f.getName())) {
                    if (nl.getLevel() == c.attrLevel()
                            && nl.getDef() instanceof FunctionDefinition
                            && nl instanceof FuncLink
                            && WurstValidator.canOverride((FuncLink) nl, fNameLink, true)
                            ) {
                        return ((FuncLink) nl).getDef().attrRealFuncDef();
                    }
                }
            }
            if (scope.getParent() != null) {
                return getRealFuncDef(f, scope.getParent().attrNearestNamedScope());
            }
        }
        return f;
    }

    public static FunctionDefinition getRealFuncDef(TupleDef t) {
        return t;
    }

}
