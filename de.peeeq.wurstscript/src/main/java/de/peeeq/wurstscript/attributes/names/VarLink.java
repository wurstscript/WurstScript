package de.peeeq.wurstscript.attributes.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import de.peeeq.datastructures.Deferred;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeVararg;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class VarLink extends DefLink {
    private final NameDef def;
    private final Deferred<WurstType> type;

    public VarLink(Visibility visibility, WScope definedIn, List<TypeParamDef> typeParams, @Nullable WurstType receiverType, NameDef def, Deferred<WurstType> type) {
        super(visibility, definedIn, typeParams, receiverType);
        this.def = def;
        this.type = type;
    }

    public static VarLink create(TypeDef def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        WurstType type = def.attrTyp();
        return new VarLink(visibility, definedIn, typeParams, lreceiverType, def, new Deferred<>(type));
    }

    public static VarLink create(EnumMember def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        WurstType type = def.attrTyp();
        return new VarLink(visibility, definedIn, typeParams, lreceiverType, def, new Deferred<>(type));
    }

    public static VarLink create(VarDef def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        Deferred<WurstType> type;
        if (def.attrOptTypeExpr() instanceof TypeExpr) {
            TypeExpr typeExpr = (TypeExpr) def.attrOptTypeExpr();
            WurstType t = typeExpr.attrTyp().dynamic();
            if (def.attrIsVararg()) {
                t = new WurstTypeVararg(t);
            }
            type = new Deferred<>(t);
        } else {
            // if type has to be inferred, we have to do it deferred to avoid cyclic dependencies
            type = new Deferred<>(def::attrTyp);
        }
        return new VarLink(visibility, definedIn, typeParams, lreceiverType, def, type);
    }



    private static @Nullable WurstType calcReceiverType(WScope definedIn, NameDef nameDef) {
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
        return null;
    }

    public static OptTypeExpr getTypeExpr(WParameter p) {
        return p.getTyp();
    }

    public static OptTypeExpr getTypeExpr(WShortParameter p) {
        return p.getTypOpt();
    }

    public static OptTypeExpr getTypeExpr(GlobalVarDef p) {
        return p.getOptTyp();
    }

    public static OptTypeExpr getTypeExpr(LocalVarDef p) {
        return p.getOptTyp();
    }


    public String getName() {
        return def.getName();
    }


    public NameDef getDef() {
        return def;
    }




    public VarLink withVisibility(Visibility newVis) {
        if (newVis == getVisibility()) {
            return this;
        }
        return new VarLink(newVis, getDefinedIn(), getTypeParams(), getReceiverType(), def, type);
    }


    public NameLinkType getType() {
        return NameLinkType.VAR;
    }


    @Override
    public String toString() {
        String r = "" + getVisibility() + " ";
        if (getReceiverType() != null) {
            r += getReceiverType() + ".";
        }
        return r + Utils.printElementWithSource(Optional.of(def));
    }

    public VarLink withTypeArgBinding(Element context, VariableBinding binding) {
        if (binding.isEmpty()) {
            return this;
        }
        Deferred<WurstType> newType = type.map(oldType -> oldType.setTypeArgs(binding));
        boolean changed = newType != type;
        WurstType newReceiverType = getReceiverType().setTypeArgs(binding);
        changed |= newReceiverType != getReceiverType();


        if (changed) {
            List<TypeParamDef> newTypeParams = getTypeParams().stream()
                    .filter(binding::contains)
                    .collect(Collectors.toList());
            return new VarLink(getVisibility(), getDefinedIn(), newTypeParams, newReceiverType, def, newType);
        } else {
            return this;
        }
    }

    @Override
    public DefLink withGenericTypeParams(List<TypeParamDef> typeParams) {
        if (typeParams.isEmpty()) {
            return this;
        }
        ImmutableList<TypeParamDef> newTypeParams = Utils.concatLists(getTypeParams(), typeParams);
        return new VarLink(getVisibility(), getDefinedIn(), newTypeParams, getReceiverType(), def, type);
    }

    @Override
    public WurstType getTyp() {
        return type.get();
    }

    @Override
    public VarLink withDef(NameDef def) {
        return new VarLink(getVisibility(), getDefinedIn(), getTypeParams(), getReceiverType(), def, type);
    }


    public VarLink withConfigDef() {
        VarDef def = (VarDef) this.def.attrConfigActualNameDef();
        return new VarLink(getVisibility(), getDefinedIn(), getTypeParams(), getReceiverType(), def, type);
    }



    public VarLink hidingPrivate() {
        return (VarLink) super.hidingPrivate();
    }

    public VarLink hidingPrivateAndProtected() {
        return (VarLink) super.hidingPrivateAndProtected();
    }

    public VarLink adaptToReceiverType(WurstType receiverType) {
        return (VarLink) super.adaptToReceiverType(receiverType);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarLink varLink = (VarLink) o;
        return Objects.equals(def, varLink.def);
    }

    @Override
    public int hashCode() {
        return Objects.hash(def);
    }
}
