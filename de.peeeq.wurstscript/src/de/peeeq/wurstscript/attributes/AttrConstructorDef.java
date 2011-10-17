package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ClassSlotPos;
import de.peeeq.wurstscript.ast.ConstructorDefPos;
import de.peeeq.wurstscript.ast.ExprNewObjectPos;
import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.utils.NotNullList;


/**
 * find the constructor for a "new" call
 *
 */
public class AttrConstructorDef extends Attribute<ExprNewObjectPos, ConstructorDefPos> {

	
	public AttrConstructorDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected ConstructorDefPos calculate(final ExprNewObjectPos node) {
		
		TypeDefPos typeDef = attr.typeDef.get(node);
		
		
		
		if (typeDef instanceof ClassDefPos) {
			
			ClassDefPos classDefPos = (ClassDefPos) typeDef;
			
			List<ConstructorDefPos> constructors = new NotNullList<ConstructorDefPos>(); 
			for (ClassSlotPos elem : classDefPos.slots()) {
				if (elem instanceof ConstructorDefPos) {
					constructors.add((ConstructorDefPos) elem);
				}
			}
			
			return OverloadingResolver.resolveExprNew(attr, constructors, node);
			
		} else {
			attr.addError(node.source(), "Can only create instances of classes.");
		}
		return null;
	}


}
