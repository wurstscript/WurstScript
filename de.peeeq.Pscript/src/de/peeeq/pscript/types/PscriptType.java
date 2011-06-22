package de.peeeq.pscript.types;

import de.peeeq.pscript.analysis.Definition;

public abstract class PscriptType implements Definition {
	public abstract boolean isSubtypeOf(PscriptType other);
	public abstract boolean isSupertypeOf(PscriptType other);
	public abstract String getName();
	public abstract String getFullName();
	
	@Override public String toString() {
		return getName();
	}
}
