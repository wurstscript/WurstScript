package de.peeeq.pscript.scoping;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

public class PscriptScope implements IScope {

	private IScope parentScope;

	public PscriptScope(IScope parentScope) {
		this.parentScope = parentScope;
	}

	@Override
	public IScope getOuterScope() {
		// TODO Auto-generated method stub
		return parentScope.getOuterScope();
	}

	@Override
	public Iterable<IEObjectDescription> getContents() {
		// TODO Auto-generated method stub
		return parentScope.getContents();
	}

	@Override
	public Iterable<IEObjectDescription> getAllContents() {
		// TODO Auto-generated method stub
		return parentScope.getAllContents();
	}

	@Override
	public IEObjectDescription getContentByName(String name) {
		// TODO Auto-generated method stub
		return parentScope.getContentByName(name);
	}

	@Override
	public IEObjectDescription getContentByEObject(EObject object) {
		// TODO Auto-generated method stub
		return parentScope.getContentByEObject(object);
	}

	@Override
	public Iterable<IEObjectDescription> getAllContentsByEObject(EObject object) {
		// TODO Auto-generated method stub
		return parentScope.getAllContentsByEObject(object);
	}

}
