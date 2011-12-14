package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.NamedScope;

public abstract class PscriptTypeNamedScope extends PscriptType {

	private boolean isStaticRef;
	
	public PscriptTypeNamedScope(boolean isStaticRef) {
		this.isStaticRef = isStaticRef;
	}

	@Override
	public String getName() {
		return getDef().getName();
	}

	public abstract NamedScope getDef();

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public String printJass() {
		// classes are just translated to integers:
		return "integer";
	}
	
	public boolean isStaticRef() {
		return isStaticRef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType obj) {
		if (obj instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope other = (PscriptTypeNamedScope) obj;
			return other.isStaticRef() == this.isStaticRef() 
					&& other.getDef().equals(this.getDef()); 
		}
		return false;
	}

}