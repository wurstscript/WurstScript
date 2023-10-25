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


    public abstract String getName();


    public abstract NameDef getDef();


    public WScope getDefinedIn() {
        return definedIn;
    }


    public List<TypeParamDef> getTypeParams() {
        return typeParams;
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
