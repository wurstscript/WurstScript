package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;


public class PscriptTypeInterface extends PscriptTypeNamedScope {


	private final InterfaceDef interfaceDef;
	private final WPackage pack;

	public PscriptTypeInterface(InterfaceDef interfaceDef, WPackage pack, boolean staticRef) {
		super(staticRef);
		this.pack = pack;
		this.interfaceDef = interfaceDef;
	}

	public PscriptTypeInterface(InterfaceDef interfaceDef, WPackage pack, List<PscriptType> newTypes) {
		super(newTypes);
		this.pack = pack;
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
		return getDef().getName() + printTypeParams() + " (interface)";
	}
	
	@Override
	public PscriptType dynamic() {
		if (isStaticRef()) {
			return new PscriptTypeInterface(getInterfaceDef(), pack, false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeInterface(getInterfaceDef(), pack, newTypes);
	}

	public WPackage getPack() {
		return pack;
	}
	
}
