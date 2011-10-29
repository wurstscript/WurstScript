package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.utils.NotNullList;


/**
 * find the constructor for a "new" call
 *
 */
public class AttrConstructorDef {
	
	public static ConstructorDef calculate(final ExprNewObject node) {
		
		TypeDef typeDef = node.attrTypeDef();
		
		
		
		if (typeDef instanceof ClassDef) {
			
			ClassDef classDefPos = (ClassDef) typeDef;
			
			List<ConstructorDef> constructors = new NotNullList<ConstructorDef>(); 
			for (ClassSlot elem : classDefPos.getSlots()) {
				if (elem instanceof ConstructorDef) {
					constructors.add((ConstructorDef) elem);
				}
			}
			
			return OverloadingResolver.resolveExprNew(constructors, node);
			
		} else {
			attr.addError(node.getSource(), "Can only create instances of classes.");
			return null;
		}
		
	}


}
