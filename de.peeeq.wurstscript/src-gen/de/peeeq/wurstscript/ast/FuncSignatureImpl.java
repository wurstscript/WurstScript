//generated by parseq
package de.peeeq.wurstscript.ast;

class FuncSignatureImpl implements FuncSignature, AstElementIntern {
	FuncSignatureImpl(WPos source, String name, WParameters parameters, OptTypeExpr typ) {
		if (source == null) throw new IllegalArgumentException();
		((AstElementIntern)source).setParent(this);
		this.source = source;
		if (name == null) throw new IllegalArgumentException();
		this.name = name;
		if (parameters == null) throw new IllegalArgumentException();
		((AstElementIntern)parameters).setParent(this);
		this.parameters = parameters;
		if (typ == null) throw new IllegalArgumentException();
		((AstElementIntern)typ).setParent(this);
		this.typ = typ;
	}

	private AstElement parent;
	public AstElement getParent() { return parent; }
	public void setParent(AstElement parent) {
		if (parent != null && this.parent != null) { 			throw new Error("Parent of " + this + " already set: " + this.parent + "\ntried to change to " + parent); 		}
		this.parent = parent;
	}

	private WPos source;
	public void setSource(WPos source) {
		if (source == null) throw new IllegalArgumentException();
		((AstElementIntern)this.source).setParent(null);
		((AstElementIntern)source).setParent(this);
		this.source = source;
	} 
	public WPos getSource() { return source; }

	private String name;
	public void setName(String name) {
		if (name == null) throw new IllegalArgumentException();
		this.name = name;
	} 
	public String getName() { return name; }

	private WParameters parameters;
	public void setParameters(WParameters parameters) {
		if (parameters == null) throw new IllegalArgumentException();
		((AstElementIntern)this.parameters).setParent(null);
		((AstElementIntern)parameters).setParent(this);
		this.parameters = parameters;
	} 
	public WParameters getParameters() { return parameters; }

	private OptTypeExpr typ;
	public void setTyp(OptTypeExpr typ) {
		if (typ == null) throw new IllegalArgumentException();
		((AstElementIntern)this.typ).setParent(null);
		((AstElementIntern)typ).setParent(this);
		this.typ = typ;
	} 
	public OptTypeExpr getTyp() { return typ; }

	public AstElement get(int i) {
		switch (i) {
			case 0: return source;
			case 1: return parameters;
			case 2: return typ;
			default: throw new IllegalArgumentException("Index out of range: " + i);
		}
	}
	public int size() {
		return 3;
	}
	public FuncSignature copy() {
		return new FuncSignatureImpl(source.copy(), name, parameters.copy(), typ.copy());
	}
	@Override public void accept(TopLevelDeclaration.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(FunctionDefinition.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(JassToplevelDeclaration.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(ClassSlot.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(ClassMember.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(WPackage.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(ClassDef.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(WEntity.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(ClassSlots.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(FuncSignature.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(WEntities.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(TypeDef.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(WScope.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(NativeFunc.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(FuncDef.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(CompilationUnit.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public void accept(PackageOrGlobal.Visitor v) {
		source.accept(v);
		parameters.accept(v);
		typ.accept(v);
		v.visit(this);
	}
	@Override public String toString() {
		return "FuncSignature(" + source + ", " +name + ", " +parameters + ", " +typ+")";
	}
	private boolean attr_attrNearestPackage_isCached = false;
	private PackageOrGlobal attr_attrNearestPackage_cache;
	public PackageOrGlobal attrNearestPackage() {
		if (!attr_attrNearestPackage_isCached) {
			attr_attrNearestPackage_cache = de.peeeq.wurstscript.attributes.AttrNearestPackage.calculate(this);
			attr_attrNearestPackage_isCached = true;
		}
		return attr_attrNearestPackage_cache;
	}
	private boolean attr_attrNearestFuncDef_isCached = false;
	private FuncDef attr_attrNearestFuncDef_cache;
	public FuncDef attrNearestFuncDef() {
		if (!attr_attrNearestFuncDef_isCached) {
			attr_attrNearestFuncDef_cache = de.peeeq.wurstscript.attributes.AttrNearestFuncDef.calculate(this);
			attr_attrNearestFuncDef_isCached = true;
		}
		return attr_attrNearestFuncDef_cache;
	}
	private boolean attr_attrNearestClassDef_isCached = false;
	private ClassDef attr_attrNearestClassDef_cache;
	public ClassDef attrNearestClassDef() {
		if (!attr_attrNearestClassDef_isCached) {
			attr_attrNearestClassDef_cache = de.peeeq.wurstscript.attributes.AttrNearestClassDef.calculate(this);
			attr_attrNearestClassDef_isCached = true;
		}
		return attr_attrNearestClassDef_cache;
	}
}
