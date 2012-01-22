package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WPackage;


public class PscriptTypeInterface extends PscriptTypeNamedScope {


	private final InterfaceDef interfaceDef;

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
		return getDef().getName() + printTypeParams() + " (interface)";
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

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (other instanceof PscriptTypeInterface) {
			PscriptTypeInterface other2 = (PscriptTypeInterface) other;
			InterfaceDef i = interfaceDef;
			InterfaceDef otherI = other2.interfaceDef;
			return i == otherI; // FIXME interfaces can extends other interfaces
		}
		return false;
	}
	
}
