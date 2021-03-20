package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;

public class Level {

  public static int get(ClassDef classDef) {
    int level = 1;
    if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
      WurstTypeClass wtc = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
      try {
        level = Math.max(level, 1 + wtc.getClassDef().attrLevel());
      } catch (CyclicDependencyError e) {
        classDef
            .getExtendedClass()
            .addError("Class " + classDef.getName() + " has a cyclic class hierarchy.");
      }
    }
    for (TypeExpr inTe : classDef.getImplementsList()) {
      if (inTe.attrTyp() instanceof WurstTypeInterface) {
        WurstTypeInterface wti = (WurstTypeInterface) inTe.attrTyp();
        try {
          level = Math.max(level, 1 + wti.getDef().attrLevel());
        } catch (CyclicDependencyError e) {
          inTe.addError("Class " + classDef.getName() + " has a cyclic class hierarchy.");
        }
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
        try {
          level = Math.max(level, 1 + wti.getDef().attrLevel());
        } catch (CyclicDependencyError e) {
          inTe.addError("Interface " + in.getName() + " has a cyclic class hierarchy.");
        }
      }
    }
    return level;
  }

  public static int get(ModuleDef classDef) {
    int level = 1;
    for (ModuleInstanciation m : classDef.getModuleInstanciations()) {
      try {
        level = Math.max(level, 1 + m.attrLevel());
      } catch (CyclicDependencyError e) {
        m.addError("Module " + classDef.getName() + " depends on itself.");
      }
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
