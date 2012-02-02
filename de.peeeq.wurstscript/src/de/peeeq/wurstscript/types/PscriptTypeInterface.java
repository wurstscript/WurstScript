package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;


public class PscriptTypeInterface extends PscriptTypeNamedScope {


	private final InterfaceDef interfaceDef;

//	public PscriptTypeInterface(InterfaceDef interfaceDef, boolean staticRef) {
//		super(staticRef);
//		if (interfaceDef == null) throw new IllegalArgumentException();
//		this.interfaceDef = interfaceDef;
//	}

	public PscriptTypeInterface(InterfaceDef interfaceDef, List<PscriptType> newTypes, boolean isStaticRef) {
		super(newTypes, isStaticRef);
		if (interfaceDef == null) throw new IllegalArgumentException();
		this.interfaceDef = interfaceDef;
	}
	
	public PscriptTypeInterface(InterfaceDef interfaceDef, List<PscriptType> newTypes) {
		super(newTypes);
		if (interfaceDef == null) throw new IllegalArgumentException();
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
			return new PscriptTypeInterface(getInterfaceDef(), getTypeParameters(), false);
		}
		return this;
	}

	@Override
	public PscriptType replaceTypeVars(List<PscriptType> newTypes) {
		return new PscriptTypeInterface(getInterfaceDef(), newTypes);
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (super.isSubtypeOf(other, location)) {
			return true;
		}
		
		if (other instanceof PscriptTypeInterface) {
			PscriptTypeInterface other2 = (PscriptTypeInterface) other;
			if (interfaceDef == other2.interfaceDef) {
				// same interface -> check if type params are equal
				return checkTypeParametersEqual(getTypeParameters(), other2.getTypeParameters(), location);
			} else {
				// test super interfaces:
				for (PscriptTypeInterface extended : interfaceDef.attrExtendedInterfaces() ) {
					if (extended.isSubtypeOf(other, location)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	

	@Override
	public String[] jassTranslateType() {
		// one int for the id and one int for the type
		return new String[]{ "integer", "integer" };
	}
	
}
