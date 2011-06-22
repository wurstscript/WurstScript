package de.peeeq.pscript.attributes.infrastructure;

import org.eclipse.emf.ecore.EObject;


public abstract class AbstractAttribute<N extends EObject, T> {

	public abstract T calculate(AttributeManager attributeManager, AttributeDependencies dependencies, N node);
	
	
}
