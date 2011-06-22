package de.peeeq.pscript.attributes;

import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.TypeExpr;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

/**
 * 
 * This attribute determines the type of a type expression
 *
 */
public class AttrTypeExprType extends AbstractAttribute<TypeExpr, PscriptType> {


	@Override
	public
	PscriptType calculate(final AttributeManager attributeManager,
			AttributeDependencies dependencies, TypeExpr node) {
		String typeName = node.getName();
		PscriptType result = AttrUtil.getBuildInType(typeName);
		
		if (result == null) {
			// TODO lookup class
			
			return new PscriptTypeError("Could not resolve typename " + typeName);
		}		
		return result;
	}

	

}
