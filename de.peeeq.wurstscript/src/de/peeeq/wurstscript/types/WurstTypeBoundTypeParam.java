package de.peeeq.wurstscript.types;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.ImplicitFuncs;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public class WurstTypeBoundTypeParam extends WurstType {

	
	private final TypeParamDef typeParamDef;
	private final WurstType baseType;
	private FuncDef fromIndex;
	private FuncDef toIndex;
	private boolean indexInitialized = false;
	private AstElement context;

	public WurstTypeBoundTypeParam(TypeParamDef def, WurstType baseType, AstElement context) {
		this.typeParamDef = def;
		this.baseType = baseType;
		this.context = context;
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return baseType.isSubtypeOfIntern(other, location);
	}

	@Override
	public String getName() {
		return baseType.getName();
	}

	@Override
	public String getFullName() {
		return typeParamDef.getName() + "<--" + baseType.getFullName();
	}


	public WurstType getBaseType() {
		return baseType;
	}

	@Override
	public ImType imTranslateType() {
		return baseType.imTranslateType();
	}

	@Override
	public ImExprOpt getDefaultValue() {
		return JassIm.ImIntVal(0);
	}

	
	@Override
	public WurstType dynamic() {
		return baseType.dynamic();
	}
	
	@Override
	public boolean canBeUsedInInstanceOf() {
		return baseType.canBeUsedInInstanceOf();
	}
	
	@Override
	public boolean allowsDynamicDispatch() {
		return baseType.allowsDynamicDispatch();
	}
	
	@Override
	public void addMemberMethods(AstElement node, String name,
			List<NameLink> result) {
		baseType.addMemberMethods(node, name, result);
	}
	
	@Override
	public Stream<NameLink> getMemberMethods(AstElement node) {
		return baseType.getMemberMethods(node);
	}
	
	@Override
	public boolean isStaticRef() {
		return baseType.isStaticRef();
	}
	
	@Override
	public boolean isCastableToInt() {
		return true; // because baseType must always be castable to int 
		//return baseType.isCastableToInt();
	}
	
	@Override
	public WurstType normalize() {
		return baseType.normalize();
	}

	public FuncDef getFromIndex() {
		initIndex();
		return fromIndex;
	}
	
	public FuncDef getToIndex() {
		initIndex();
		return toIndex;
	}

	private void initIndex() {
		if (indexInitialized) {
			return;
		}
		// if type does support generics natively, try to find implicit conversion functions 
		if (!baseType.supportsGenerics()) {
			fromIndex = ImplicitFuncs.findFromIndexFunc(baseType, context);
			toIndex = ImplicitFuncs.findToIndexFunc(baseType, context);
		}
		indexInitialized = true;
	}

	
}
