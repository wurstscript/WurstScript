package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;

import java.util.List;
import java.util.stream.Stream;


public abstract class NameLink {
    protected final List<TypeParamDef> typeParams;
    private final Visibility visibility;
    private final WScope definedIn;

    public NameLink(Visibility visibility, WScope definedIn, List<TypeParamDef> typeParams) {
        this.visibility = visibility;
        this.definedIn = definedIn;
        this.typeParams = typeParams;
    }


    //    @Deprecated
//    public static NameLink create(NameDef nameDef, WScope definedIn) {
//        Visibility visibiliy = calcVisibility(definedIn, nameDef);
//        NameLinkType type = calcNameLinkType(nameDef);
//        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(nameDef)).collect(Collectors.toList());
//        WurstType lreturnType  = null;
//        List<WurstType> lparameterTypes = null;
//        if (nameDef instanceof FunctionDefinition) {
//            FunctionDefinition func = (FunctionDefinition) nameDef;
//            lparameterTypes = func.getParameters().stream()
//                    .map(WParameter::attrTyp)
//                    .collect(Collectors.toList());
//            lreturnType = func.attrTyp();
//        }
//        WurstType lreceiverType = calcReceiverType(definedIn, nameDef, type);
//        return new NameLink(visibiliy, type, definedIn, nameDef, typeParams, lreceiverType, lparameterTypes, lreturnType);
//    }

    protected static Stream<TypeParamDef> typeParams(Element scope) {
        if (scope instanceof AstElementWithTypeParameters) {
            return ((AstElementWithTypeParameters) scope).getTypeParameters().stream();
        }
        return Stream.of();
    }


    private static int calcLevel(WScope definedIn) {
        if (definedIn instanceof StructureDef) {
            StructureDef struct = (StructureDef) definedIn;
            return struct.attrLevel();
        } else {
            return 0;
        }
    }

    protected static Visibility calcVisibility(WScope definedIn, NameDef nameDef) {
        if (definedIn.getParent() instanceof WPackage) {
            WPackage packge = (WPackage) definedIn.getParent();
            if (nameDef.attrIsPublic()) {
                return VisibilityE.PUBLIC;
            } else {
                return packge.visibilityPrivate();
            }
        } else if (definedIn instanceof StructureDef) {
            StructureDef structure = (StructureDef) definedIn;
            if (nameDef.attrIsPrivate()) {
                return structure.visibilityPrivate();
            } else if (nameDef.attrIsProtected()) {
                return structure.visibilityProtected();
            } else {
                return VisibilityE.PUBLIC;
            }
        } else if (definedIn instanceof TupleDef) {
            return VisibilityE.PUBLIC;
        } else if (definedIn instanceof EnumDef) {
            return VisibilityE.PUBLIC;
        } else {
            return VisibilityE.LOCAL;
        }
    }


    public Visibility getVisibility() {
        return visibility;
    }


    public abstract String getName();


    public abstract NameDef getDef();


    public WScope getDefinedIn() {
        return definedIn;
    }


    public List<TypeParamDef> getTypeParams() {
        return typeParams;
    }


    public abstract NameLink withVisibility(Visibility newVis);


    public int getLevel() {
        return calcLevel(definedIn);
    }


    public abstract boolean receiverCompatibleWith(WurstType receiverType, Element location);

    public abstract NameLink withTypeArgBinding(Element context, VariableBinding binding);

    // TODO should it be possible to get the type without providing a mapping for the type-arguments?
    public abstract WurstType getTyp();

    public abstract NameLink withDef(NameDef actual);
}
