package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeInterface extends WurstTypeNamedScope {


	private final InterfaceDef interfaceDef;

//	public PscriptTypeInterface(InterfaceDef interfaceDef, boolean staticRef) {
//		super(staticRef);
//		if (interfaceDef == null) throw new IllegalArgumentException();
//		this.interfaceDef = interfaceDef;
//	}

	public WurstTypeInterface(InterfaceDef interfaceDef, List<WurstType> newTypes, boolean isStaticRef) {
		super(newTypes, isStaticRef);
		if (interfaceDef == null) throw new IllegalArgumentException();
		this.interfaceDef = interfaceDef;
	}
	
	public WurstTypeInterface(InterfaceDef interfaceDef, List<WurstType> newTypes) {
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
		return getDef().getName() + printTypeParams();
	}
	
	@Override
	public WurstType dynamic() {
		if (isStaticRef()) {
			return new WurstTypeInterface(getInterfaceDef(), getTypeParameters(), false);
		}
		return this;
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		return new WurstTypeInterface(getInterfaceDef(), newTypes);
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (super.isSubtypeOf(other, location)) {
			return true;
		}
		
		if (other instanceof WurstTypeInterface) {
			WurstTypeInterface other2 = (WurstTypeInterface) other;
			if (interfaceDef == other2.interfaceDef) {
				// same interface -> check if type params are equal
				return checkTypeParametersEqual(getTypeParameters(), other2.getTypeParameters(), location);
			} else {
				// test super interfaces:
				for (WurstTypeInterface extended : interfaceDef.attrExtendedInterfaces() ) {
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
	
	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImNull();
	}
}
