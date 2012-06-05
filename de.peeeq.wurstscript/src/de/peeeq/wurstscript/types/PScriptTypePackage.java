package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImType;


public class PScriptTypePackage extends PscriptTypeNamedScope {

	

	private WPackage pack;

	// make constructor private as we only need one instance
	private PScriptTypePackage(WPackage pack) {
		super(true);
		if (pack == null) throw new IllegalArgumentException();
		this.pack = pack;
	}

	@Override
	public NamedScope getDef() {
		return pack;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (package)";
	}
	
	@Override
	public PscriptType dynamic() {
		throw new Error("Package references cannot be dynamic.");
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return this;
	}

	@Override
	public String[] jassTranslateType() {
		throw new Error("not implemented");
	}

	@Override
	public ImType imTranslateType() {
		throw new Error("not implemented");
	}

}
