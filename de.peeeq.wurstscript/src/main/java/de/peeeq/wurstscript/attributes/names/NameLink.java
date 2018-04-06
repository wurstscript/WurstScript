package de.peeeq.wurstscript.attributes.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class NameLink {
    private final Visibility visibility;
    private final NameLinkType type;
    private final WScope definedIn;
    private final @Nullable WurstType receiverType;
    private final NameDef nameDef;
    private final @Nullable WurstType returnType;
    private final @Nullable List<WurstType> parameterTypes;


    public NameLink(Visibility visibility, NameLinkType type, WScope definedIn, @Nullable WurstType receiverType, NameDef nameDef, @Nullable WurstType returnType, @Nullable List<WurstType> parameterTypes) {
        this.visibility = visibility;
        this.type = type;
        this.definedIn = definedIn;
        this.receiverType = receiverType;
        this.nameDef = nameDef;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
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
        return new NameLink(newVis, type, definedIn, receiverType, nameDef, returnType, parameterTypes);
    }


    public int getLevel() {
        return calcLevel(definedIn);
    }


    public NameLinkType getType() {
        return type;
    }


    public @Nullable WurstType getReceiverType() {
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
        WurstType newReceiverType = adjustType(context, getReceiverType(), binding);

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
            return new NameLink(visibility, type, definedIn, newReceiverType, nameDef, newReturnType, newParamTypes);
        } else {
            return this;
        }
    }

    private WurstType adjustType(Element context, WurstType t, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        return t.setTypeArgs(binding);
    }


    public WurstType getReturnType() {
        return returnType;
    }

    public List<WurstType> getParameterTypes() {
        return parameterTypes;
    }

    public NameLink withConfigDef() {
        NameDef def = nameDef.attrConfigActualNameDef();
        return new NameLink(visibility, type, definedIn, receiverType, def, returnType, parameterTypes);
    }


    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        if (this.receiverType == null) {
            return receiverType == null;
        }
        return this.receiverType.isSubtypeOf(receiverType, location);
    }
}
