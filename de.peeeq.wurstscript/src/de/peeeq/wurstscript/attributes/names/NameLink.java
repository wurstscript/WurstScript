package de.peeeq.wurstscript.attributes.names;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
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
	private @Nullable WurstType receiverType = null;
	private final NameDef nameDef;
	private @Nullable WurstType returnType;
	private @Nullable List<WurstType> parameterTypes;

	
	private NameLink(Visibility visibility, NameLinkType type,
			WScope definedIn, NameDef nameDef) {
		super();
		this.visibility = visibility;
		this.type = type;
		this.definedIn = definedIn;
		this.nameDef = nameDef;
	}
	
	private NameLink(Visibility visibility, NameLinkType type,
			WScope definedIn, NameDef nameDef, @Nullable WurstType returnType, @Nullable List<WurstType> parameterTypes) {
		super();
		this.visibility = visibility;
		this.type = type;
		this.definedIn = definedIn;
		this.nameDef = nameDef;
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public static NameLink create(NameDef nameDef, WScope definedIn) {
		Visibility visibiliy = calcVisibility(definedIn, nameDef);
		NameLinkType type = calcNameLinkType(nameDef);
		return new NameLink(visibiliy, type, definedIn, nameDef);
	}

	private static @Nullable WurstType calcReceiverType(WScope definedIn,	NameDef nameDef, NameLinkType type) {
		if (type == NameLinkType.FUNCTION) {
			if (nameDef instanceof ExtensionFuncDef) {
				ExtensionFuncDef exF = (ExtensionFuncDef) nameDef;
				return exF.getExtendedType().attrTyp().dynamic(); 
			} else if (nameDef instanceof FuncDef) {
				return getReceiverType(definedIn);
			}
		} else if (type == NameLinkType.VAR) {
			if (nameDef instanceof GlobalVarDef) {
				return getReceiverType(definedIn);
			} else {
				AstElement parent = nameDef.getParent();
				Preconditions.checkNotNull(parent);
				AstElement grandParent = parent.getParent();
				if (nameDef instanceof WParameter) {
					if (grandParent instanceof TupleDef) {
						TupleDef tupleDef = (TupleDef) grandParent;
						return tupleDef.attrTyp();
					}
				} else if (nameDef instanceof EnumMember) {
					if (grandParent instanceof EnumDef) {
						EnumDef enumDef = (EnumDef) grandParent;
						return enumDef.attrTyp();
					}
				}
			}
		}
		return null;
	}

	private static @Nullable WurstType getReceiverType(WScope definedIn) {
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
		} else if (definedIn instanceof EnumDef) {
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


	public @Nullable WurstType getReceiverType() {
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

	public NameLink withTypeArgBinding(Map<TypeParamDef, WurstType> binding) {
		if (binding.isEmpty()) {
			return this;
		}
		WurstType newReturnType = adjustType(getReturnType(), binding);
		boolean changed = newReturnType != returnType;
		List<WurstType> newParamTypes;
		if (getParameterTypes().isEmpty()) {
			newParamTypes = getParameterTypes();
		} else {
			newParamTypes = Lists.newArrayListWithCapacity(getParameterTypes().size());
			for (WurstType pt : getParameterTypes()) {
				WurstType newPt = adjustType(pt, binding);
				if (newPt != pt) {
					changed = true;
				}
				newParamTypes.add(newPt);
			}
		}
		if (changed) {
			return new NameLink(visibility, type, definedIn, nameDef, newReturnType, newParamTypes);
		} else {
			return this;
		}
	}

	private WurstType adjustType(WurstType t, Map<TypeParamDef, WurstType> binding) {
		return t.setTypeArgs(binding);
	}

	
	public WurstType getReturnType() {
		WurstType r = returnType;
		if (r == null) {
			r = nameDef.attrTyp();
			returnType = r;
		}
		return r;
	}
	
	public List<WurstType> getParameterTypes() {
		List<WurstType> pts = parameterTypes;
		if (pts == null) {
			if (nameDef instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) nameDef;
				pts = f.attrParameterTypes();
			} else {
				pts = Collections.emptyList();
			}
			parameterTypes = pts;
		}
		return pts;
	}

	public NameLink withConfigDef() {
		NameDef def = (NameDef) nameDef.attrConfigActualNameDef();
		return new NameLink(visibility, type, definedIn, def, returnType, parameterTypes);
	}
	
	
	
	
}
