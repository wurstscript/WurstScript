package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeInterface extends PscriptTypeNamedScope {


	private InterfaceDef interfaceDef;

	public PscriptTypeInterface(InterfaceDef interfaceDef, boolean staticRef) {
		super(staticRef);
		this.interfaceDef = interfaceDef;
	}

	public PscriptTypeInterface(InterfaceDef interfaceDef, List<PscriptType> newTypes) {
		super(newTypes);
		this.interfaceDef = interfaceDef;
	}

	@Override
	public NamedScope getDef() {
		return interfaceDef;
	}

	public InterfaceDef getInterfaceDef() {
		return interfaceDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams() + " (class)";
	}
	
	@Override
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeInterface(getInterfaceDef(), false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeInterface(getInterfaceDef(), newTypes);
	}
	
}
