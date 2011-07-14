package de.peeeq.pscript.validation;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface ErrorSink {
	/**
	 * adds an error message for the specified object source
	 * @param source
	 * @param message
	 */
	void addError(EObject source, EStructuralFeature feature, String type,
			String message);
	
	void clear();
	
	Collection<PscriptError> getErrors(EObject source);
	
	Collection<PscriptError> getErrors();

	void addWarning(EObject source, EStructuralFeature feature, String type,
			String message);

	
}
