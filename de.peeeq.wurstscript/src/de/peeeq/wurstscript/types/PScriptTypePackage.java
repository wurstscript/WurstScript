package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;


public class PScriptTypePackage extends PscriptTypeNamedScope {

	

	private WPackage pack;

	// make constructor private as we only need one instance
	private PScriptTypePackage(WPackage pack) {
		super(true);
		this.pack = pack;
	}

	@Override
	public NamedScope getDef() {
		return pack;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + " (package)";
	}

}
