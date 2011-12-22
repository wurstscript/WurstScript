package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.TypeDef;


/**
 * find the constructor for a "new" call
 *
 */
public class AttrConstructorDef {
	
	public static ConstructorDef calculate(final ExprNewObject node) {
		
		TypeDef typeDef = node.attrTypeDef();
		
		
		
		if (typeDef instanceof ClassDef) {
			
			ClassDef classDef = (ClassDef) typeDef;
			
			List<ConstructorDef> constructors = classDef.attrConstructors();
			
			return OverloadingResolver.resolveExprNew(constructors, node);
			
		} else {
			attr.addError(node.getSource(), "Can only create instances of classes.");
			return null;
		}
		
	}


}
