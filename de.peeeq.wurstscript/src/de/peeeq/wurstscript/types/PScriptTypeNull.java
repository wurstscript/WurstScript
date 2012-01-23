package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;


public class PScriptTypeNull extends PscriptType {

	private static final PScriptTypeNull instance = new PScriptTypeNull();

	// make constructor private as we only need one instance
	private PScriptTypeNull() {}
	
	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		return other instanceof PScriptTypeNull
				|| other.isSubtypeOf(PScriptTypeHandle.instance(), location)
				|| other instanceof PscriptTypeClass
				|| other instanceof PscriptTypeModule
				|| other instanceof PScriptTypeString
				|| other instanceof PScriptTypeCode
				|| other instanceof PscriptTypeTypeParam
				|| other instanceof PscriptTypeClass
				|| other instanceof PscriptTypeInterface
				|| other instanceof PscriptTypeModule;
	}


	@Override
	public String getName() {
		return "null";
	}

	@Override
	public String getFullName() {
		return "null";
	}

	public static PScriptTypeNull instance() {
		return instance;
	}

	@Override
	public String printJass() {
		return getName();
	}

}
