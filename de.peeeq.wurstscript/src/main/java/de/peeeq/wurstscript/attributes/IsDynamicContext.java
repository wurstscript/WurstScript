package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;

/** a dynamic context is a place where we have a 'this' variable */
public class IsDynamicContext {

  public static boolean calculate(Element origElem) {
    Element elem = origElem;
    while (elem != null) {
      if (elem instanceof FuncDef) {
        FuncDef funcDef = (FuncDef) elem;
        return funcDef.attrIsDynamicClassMember();
      } else if (elem instanceof ConstructorDef) {
        return true;
      } else if (elem instanceof OnDestroyDef) {
        return true;
      } else if (elem instanceof GlobalVarDef) {
        GlobalVarDef g = (GlobalVarDef) elem;
        return g.attrIsDynamicClassMember();
      }
      if (elem instanceof ClassDef) {
        ClassDef a = (ClassDef) elem;
        if (origElem.isSubtreeOf(a.getExtendedClass())) {
          return true;
        }
      }
      if (elem instanceof InterfaceDef) {
        InterfaceDef a = (InterfaceDef) elem;
        if (origElem.isSubtreeOf(a.getExtendsList())) {
          return true;
        }
      }
      if (elem instanceof ClassDef) {
        ClassDef a = (ClassDef) elem;
        if (origElem.isSubtreeOf(a.getImplementsList())) {
          return true;
        }
      } else if (elem instanceof ModuleUse) {
        ModuleUse mu = (ModuleUse) elem;
        if (origElem.isSubtreeOf(mu.getTypeArgs())) {
          return true;
        }
      }
      elem = elem.getParent();
    }
    return false;
  }
}
