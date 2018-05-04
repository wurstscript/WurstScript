package de.peeeq.wurstscript.attributes.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeVararg;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;


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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definedIn == null) ? 0 : definedIn.hashCode());
        result = prime * result + ((nameDef == null) ? 0 : nameDef.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((visibility == null) ? 0 : visibility.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NameLink other = (NameLink) obj;
        if (definedIn != other.definedIn)
            return false;
        if (nameDef != other.nameDef)
            return false;
        if (type != other.type)
            return false;
        if (visibility != other.visibility)
            return false;
        return true;
    }

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

    private static @Nullable WurstType calcReceiverType(WScope definedIn, NameDef nameDef, NameLinkType type) {
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
                Element parent = nameDef.getParent();
                Preconditions.checkNotNull(parent);
                Element grandParent = parent.getParent();
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
            r += receiverType + ".";
        }
        return r + Utils.printElementWithSource(nameDef);
    }

    public NameLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        if (binding.isEmpty()) {
            return this;
        }
        WurstType newReturnType = adjustType(context, getReturnType(), binding);
        boolean changed = newReturnType != returnType;
        List<WurstType> newParamTypes;
        if (getParameterTypes().isEmpty()) {
            newParamTypes = getParameterTypes();
        } else {
            newParamTypes = Lists.newArrayListWithCapacity(getParameterTypes().size());
            for (WurstType pt : getParameterTypes()) {
                WurstType newPt = adjustType(context, pt, binding);
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

    private WurstType adjustType(Element context, WurstType t, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        return t.setTypeArgs(binding);
    }


    public WurstType getReturnType() {
        WurstType r = returnType;
        if (r == null) {
            r = nameDef.attrTyp().dynamic();
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

    public WurstType getParameterType(int i) {
        List<WurstType> parameterTypes = getParameterTypes();
        if (isVarargMethod() && i >= parameterTypes.size() - 1) {
            return ((WurstTypeVararg) parameterTypes.get(parameterTypes.size() - 1)).getBaseType();
        }
        return parameterTypes.get(i);
    }

    private boolean isVarargMethod() {
        List<WurstType> parameterTypes = getParameterTypes();
        if (parameterTypes.size() > 0) {
            return parameterTypes.get(parameterTypes.size() - 1) instanceof WurstTypeVararg;
        }
        return false;
    }

    public NameLink withConfigDef() {
        NameDef def = (NameDef) nameDef.attrConfigActualNameDef();
        return new NameLink(visibility, type, definedIn, def, returnType, parameterTypes);
    }


    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        if (this.receiverType == null) {
            return receiverType == null;
        }
        return this.receiverType.isSubtypeOf(receiverType, location);
    }


}
