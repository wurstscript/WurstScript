package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.TypeLink;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;

public class Level {

    public static int get(ClassDef classDef) {
        int level = 1;
        if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
            WurstTypeClass wtc = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
            level = Math.max(level, 1 + wtc.getClassDef().attrLevel());
        }
        for (TypeExpr inTe : classDef.getImplementsList()) {
            if (inTe.attrTyp() instanceof WurstTypeInterface) {
                WurstTypeInterface wti = (WurstTypeInterface) inTe.attrTyp();
                level = Math.max(level, 1 + wti.getDef().attrLevel());
            }
        }
        for (ModuleInstanciation m : classDef.getModuleInstanciations()) {
            level = Math.max(level, 1 + m.attrLevel());
        }
        return level;
    }

    public static int get(InterfaceDef in) {
        int level = 1;
        for (TypeExpr inTe : in.getExtendsList()) {
            if (inTe.attrTyp() instanceof WurstTypeInterface) {
                WurstTypeInterface wti = (WurstTypeInterface) inTe.attrTyp();
                level = Math.max(level, 1 + wti.getDef().attrLevel());
            }
        }
        return level;
    }

    public static int get(ModuleDef classDef) {
        int level = 1;
        for (ModuleInstanciation m : classDef.getModuleInstanciations()) {
            level = Math.max(level, 1 + m.attrLevel());
        }
        return level;
    }

    public static int get(ModuleInstanciation classDef) {
        int level = 1;
        for (ModuleInstanciation m : classDef.getModuleInstanciations()) {
            level = Math.max(level, 1 + m.attrLevel());
        }
        return level;
    }

}
