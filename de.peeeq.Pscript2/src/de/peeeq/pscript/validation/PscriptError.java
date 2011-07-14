package de.peeeq.pscript.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class PscriptError {
	
	
	private boolean isError;
	private EObject source;
	private String msg;
	private EStructuralFeature feature;
	private String type;
	
	public PscriptError(boolean isError, EObject source, EStructuralFeature feature, String type, String msg) {
		this.isError = isError;
		this.source = source;
		this.msg = msg;
		this.feature = feature;
		this.type = type;
	}
	
	public boolean isError() {
		return isError;
	}

	public EObject getSource() {
		return source;
	}

	public String getMsg() {
		return msg;
	}

	

	public String getType() {
		return type;
	}

	public EStructuralFeature getFeature() {
		return feature;
	} 
	
	

}
