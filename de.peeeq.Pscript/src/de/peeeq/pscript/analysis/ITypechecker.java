package de.peeeq.pscript.analysis;

import org.eclipse.emf.ecore.EObject;

import de.peeeq.pscript.validation.PscriptJavaValidator;

public interface ITypechecker {

	void check(EObject x, PscriptJavaValidator pscriptJavaValidator);

}
