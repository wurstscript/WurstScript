package de.peeeq.wurstscript.attributes;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.TypeDef;


/**
 * find the constructor for a "new" call
 *
 */
public class AttrConstructorDef {
	
	public static @Nullable ConstructorDef calculate(final ExprNewObject node) {
		
		TypeDef typeDef = node.attrTypeDef();
		
		
		
		if (typeDef instanceof ClassDef) {
			
			ClassDef classDef = (ClassDef) typeDef;
			
			List<ConstructorDef> constructors = classDef.getConstructors();
			
			return OverloadingResolver.resolveExprNew(constructors, node);
			
		} else {
			node.addError("Can only create instances of classes.");
			return null;
		}
		
	}


}
