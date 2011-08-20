package de.peeeq.pscript.scoping;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;

import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.PscriptPackage;

public class PscriptMethodDescription implements IEObjectDescription {

	private ClassDef classDef;
	private FuncDef funcDef;

	public PscriptMethodDescription(ClassDef classDef, FuncDef funcDef) {
		this.classDef = classDef;
		this.funcDef = funcDef;
	}

	@Override
	public QualifiedName getName() {
		return QualifiedName.create(funcDef.getName());
	}

	@Override
	public QualifiedName getQualifiedName() {
		return QualifiedName.create(funcDef.getName());
	}

	@Override
	public EObject getEObjectOrProxy() {
		return funcDef;
	}

	@Override
	public URI getEObjectURI() {
		// TODO
		throw new Error("LOL how do i compute URI ?");
	}

	@Override
	public EClass getEClass() {
		throw new Error("not implemented");
	}

	@Override
	public String getUserData(String key) {
		if (key.equals("methods")) {
			// TODO
		}
		throw new Error("userdata key " + key + " not found");
	}

	@Override
	public String[] getUserDataKeys() {
		String[] ar = {"methods"};
		return ar ;
	}

}
