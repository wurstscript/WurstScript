package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;


public class NameLink {
	private final Visibility visibility;
	private int level = -1;
	private final NameLinkType type;
	private final WScope definedIn;
	private boolean receiverTypeCalculted = false;
	private WurstType receiverType = null;
	private final NameDef nameDef;

	
	private NameLink(Visibility visibility, NameLinkType type,
			WScope definedIn, NameDef nameDef) {
		super();
		this.visibility = visibility;
		this.type = type;
		this.definedIn = definedIn;
		this.nameDef = nameDef;
	}

	public static NameLink create(NameDef nameDef, WScope definedIn) {
		Visibility visibiliy = calcVisibility(definedIn, nameDef);
		NameLinkType type = calcNameLinkType(nameDef);
		
		return new NameLink(visibiliy, type, definedIn, nameDef);
	}

	private static WurstType calcReceiverType(WScope definedIn,	NameDef nameDef, NameLinkType type) {
		if (type == NameLinkType.FUNCTION) {
			if (nameDef instanceof ExtensionFuncDef) {
				ExtensionFuncDef exF = (ExtensionFuncDef) nameDef;
				return exF.getExtendedType().attrTyp(); 
			} else if (nameDef instanceof FuncDef) {
				return getReceiverType(definedIn);
			}
		} else if (type == NameLinkType.VAR) {
			if (nameDef instanceof GlobalVarDef) {
				return getReceiverType(definedIn);
			} else if (nameDef instanceof WParameter) {
				if (nameDef.getParent().getParent() instanceof TupleDef) {
					TupleDef tupleDef = (TupleDef) nameDef.getParent().getParent();
					return tupleDef.attrTyp();
				}
			}
		}
		return null;
	}

	private static WurstType getReceiverType(WScope definedIn) {
		if (definedIn instanceof ClassDef) {
			ClassDef classDef = (ClassDef) definedIn;
			return classDef.attrTyp();
		} else if (definedIn instanceof InterfaceDef) {
			InterfaceDef interfaceDef = (InterfaceDef) definedIn;
			return interfaceDef.attrTyp();
		} else if (definedIn instanceof ModuleInstanciation) {
			ModuleInstanciation moduleInstanciation = (ModuleInstanciation) definedIn;
			return moduleInstanciation.attrTyp();
		} else if (definedIn instanceof ModuleDef) {
			ModuleDef moduleDef = (ModuleDef) definedIn;
			return moduleDef.attrTyp();
		}
		return null;
	}

	private static NameLinkType calcNameLinkType(NameDef nameDef) {
		if (nameDef instanceof FunctionDefinition) {
			return NameLinkType.FUNCTION;
		} else {
			return NameLinkType.VAR;
		}
	}

	private static int calcLevel(WScope definedIn) {
		if (definedIn instanceof StructureDef) {
			StructureDef struct = (StructureDef) definedIn;
			return struct.attrLevel(); 
		} else {
			return 0;
		}
	}

	private static Visibility calcVisibility(WScope definedIn, NameDef nameDef) {
		if (definedIn.getParent() instanceof WPackage) {
			if (nameDef.attrIsPublic()) {
				return Visibility.PUBLIC;
			} else {
				return Visibility.PRIVATE_HERE;
			}
		} else if (definedIn instanceof StructureDef) {
			if (nameDef.attrIsPrivate()) {
				return Visibility.PRIVATE_HERE;
			} else if (nameDef.attrIsProtected()) {
				return Visibility.PROTECTED_HERE;
			} else {
				return Visibility.PUBLIC;
			}
		} else if (definedIn instanceof TupleDef) {
			return Visibility.PUBLIC;
		} else {
			return Visibility.LOCAL;
		}
	}
	

	public Visibility getVisibility() {
		return visibility;
	}


	public String getName() {
		return nameDef.getName();
	}


	public NameDef getNameDef() {
		return nameDef;
	}

	
	public WScope getDefinedIn() {
		return definedIn;
	}
	

	public NameLink hidingPrivate() {
		if (visibility == Visibility.PRIVATE_HERE) {
			return withVisibility(Visibility.PRIVATE_OTHER);
		}
		return this;
	}
	
	public NameLink hidingPrivateAndProtected() {
		if (visibility == Visibility.PRIVATE_HERE) {
			return withVisibility(Visibility.PRIVATE_OTHER);
		}
		if (visibility == Visibility.PROTECTED_HERE) {
			return withVisibility(Visibility.PROTECTED_OTHER);
		}
		return this;
	}



	private NameLink withVisibility(Visibility newVis) {
		if (newVis == visibility) {
			return this;
		}
		return new NameLink(newVis, type, definedIn, nameDef);
	}
	
	

	public int getLevel() {
		if (level < 0) {
			level = calcLevel(definedIn);
		}
		return level;
	}


	public NameLinkType getType() {
		return type;
	}


	public WurstType getReceiverType() {
		if (!receiverTypeCalculted) {
			receiverType = calcReceiverType(definedIn, nameDef, type);
			receiverTypeCalculted = true;
		}
		return receiverType;
	}

	@Override
	public String toString() {
		String r = "" + getVisibility() + " ";
		if (getReceiverType() != null) {
			r += receiverType +".";
		}
		return r = r + Utils.printElementWithSource(nameDef);
	}

	
	
	
	
}
