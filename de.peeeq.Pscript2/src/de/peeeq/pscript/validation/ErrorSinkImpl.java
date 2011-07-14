package de.peeeq.pscript.validation;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;

@Singleton
public class ErrorSinkImpl implements ErrorSink {

	private Multimap<EObject, PscriptError> errors = HashMultimap.create();
	
	static int counter = 0;
	int index = counter++;
	
	public ErrorSinkImpl() {
		System.out.println(toString() + " .create");
	}
	
	@Override
	public String toString() {
		return "ErrorSink " + index;
	}
	
	@Override
	public void addError(EObject source, EStructuralFeature feature, String type, String message) {
		System.out.println(toString() + " .warning " + message);
		errors.put(source, new PscriptError(true, source, feature, type, message));
	}

	@Override
	public void addWarning(EObject source, EStructuralFeature feature, String type, String message) {
		System.out.println(toString() + " .error " + message);
		errors.put(source, new PscriptError(false, source, feature, type, message));
	}

	@Override
	public void clear() {
		System.out.println(toString() + " .clear");
		errors.clear();
	}

	@Override
	public Collection<PscriptError> getErrors(EObject source) {
		System.out.println(toString() + " .getErrors(" + source + ")");
		return Collections.unmodifiableCollection(errors.get(source));
	}

	@Override
	public Collection<PscriptError> getErrors() {
		System.out.println(toString() + " .getErrors()");
		return Collections.unmodifiableCollection(errors.values());
	}

}
