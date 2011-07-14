package de.peeeq.pscript.attributes;

import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

public class AttrVarDefType extends AbstractAttribute<VarDef, PscriptType> {

	@Override
	public PscriptType calculate(AttributeManager attributeManager,
			AttributeDependencies dependencies, VarDef node) {
		
		if (node.getType() != null) {
			return attributeManager.getAttValue(AttrTypeExprType.class, node.getType());
		} else {
			// try to infer the type from the right hand side expression
			if (node.getE() == null) {
				return new PscriptTypeError("cannot infer type of variable " + node.getName());
			} else {
				return attributeManager.getAttValue(AttrExprType.class, node.getE());
			}
		}
	}

}
