package de.peeeq.pscript.attributes.infrastructure;

import org.eclipse.emf.ecore.EObject;


public interface AttributeManager {
	public <N extends EObject,T> T getAttValue(Class<? extends AbstractAttribute<N,T>> attr, N node);

	public void reset();
}
