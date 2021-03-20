package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;

public class InnerClasses {

  public static boolean isInnerClass(ClassDef classDef) {
    return classDef.getParent().attrNearestClassOrModule() != null;
  }
}
