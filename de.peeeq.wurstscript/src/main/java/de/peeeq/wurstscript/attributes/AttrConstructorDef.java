package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.TypeDef;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;

/** find the constructor for a "new" call */
public class AttrConstructorDef {

  public static @Nullable ConstructorDef calculate(final ExprNewObject node) {

    TypeDef typeDef = node.attrTypeDef();

    if (typeDef instanceof ClassDef) {

      ClassDef classDef = (ClassDef) typeDef;

      List<ConstructorDef> constructors = classDef.getConstructors();

      ConstructorDef constr = OverloadingResolver.resolveExprNew(constructors, node);

      if (constr != null && constr.attrIsPrivate() && !node.isSubtreeOf(classDef)) {
        node.addError("This constructor for class " + classDef.getName() + " is not visible here.");
      }

      return constr;
    } else {
      node.addError("Can only create instances of classes.");
      return null;
    }
  }
}
