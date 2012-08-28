package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeClass extends WurstTypeNamedScope {

	ClassDef classDef;


	public WurstTypeClass(ClassDef classDef, List<WurstType> typeParameters, boolean staticRef) {
		super(typeParameters, staticRef);
		if (classDef == null) throw new IllegalArgumentException();
		this.classDef = classDef;
	}

	@Override
	public boolean isSubtypeOf(WurstType obj, AstElement location) {
		if (super.isSubtypeOf(obj, location)) {
			return true;
		}
		if (obj instanceof WurstTypeInterface) {
			WurstTypeInterface pti = (WurstTypeInterface) obj;
			for (WurstTypeInterface implementedInterface : classDef.attrImplementedInterfaces()) {
				if (implementedInterface.setTypeArgs(getTypeArgBinding()).isSubtypeOf(pti, location)) {
					return true;
				}
			}
		}
		if (classDef.getExtendedClass() instanceof TypeExpr) {
			TypeExpr extendedClass = (TypeExpr) classDef.getExtendedClass();
			WurstType superType = extendedClass.attrTyp();
			return superType.isSubtypeOf(obj, location);
		}
		return false;
	}
	
	@Override
	public NamedScope getDef() {
		return classDef;
	}

	public ClassDef getClassDef() {
		return classDef;
	}
	
	@Override
	public String getName() {
		return getDef().getName() + printTypeParams();
	}
	
	@Override
	public WurstType dynamic() {
		return new WurstTypeClass(getClassDef(), getTypeParameters(), false);
	}

	@Override
	public WurstType replaceTypeVars(List<WurstType> newTypes) {
		return new WurstTypeClass(classDef, newTypes, isStaticRef());
	}

	@Override
	public String[] jassTranslateType() {
		return WurstTypeInt.instance().jassTranslateType();
	}
	
	@Override
	public ImType imTranslateType() {
		return TypesHelper.imInt();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}
	
}
