package de.peeeq.wurstscript.attributes.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeDeferred;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class VarLink extends DefLink {
    private final NameDef def;
    private final WurstType type;

    public VarLink(Visibility visibility, WScope definedIn, List<TypeParamDef> typeParams, @Nullable WurstType receiverType, NameDef def, WurstType type) {
        super(visibility, definedIn, typeParams, receiverType);
        this.def = def;
        this.type = type;
    }

    public static VarLink create(TypeDef def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        WurstType type = def.attrTyp();
        return new VarLink(visibility, definedIn, typeParams, lreceiverType, def, type);
    }

    public static VarLink create(EnumMember def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        WurstType type = def.attrTyp();
        return new VarLink(visibility, definedIn, typeParams, lreceiverType, def, type);
    }

    public static VarLink create(VarDef def, WScope definedIn) {
        Visibility visibility = calcVisibility(definedIn, def);
        List<TypeParamDef> typeParams = Streams.concat(typeParams(definedIn), typeParams(def)).collect(Collectors.toList());
        WurstType lreceiverType = calcReceiverType(definedIn, def);
        WurstType type;
        if (def.attrOptTypeExpr() instanceof TypeExpr) {
            TypeExpr typeExpr = (TypeExpr) def.attrOptTypeExpr();
            type = typeExpr.attrTyp().dynamic();
        } else {
            // if type has to be inferred, we have to do it deferred to avoid cyclic dependencies
            type = new WurstTypeDeferred(def::attrTyp);
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
        return r + Utils.printElementWithSource(def);
    }

    public VarLink withTypeArgBinding(Element context, Map<TypeParamDef, WurstTypeBoundTypeParam> binding) {
        if (binding.isEmpty()) {
            return this;
        }
        WurstType newType = type.setTypeArgs(binding);
        boolean changed = newType != type;
        WurstType newReceiverType = getReceiverType().setTypeArgs(binding);
        changed |= newReceiverType == getReceiverType();


        if (changed) {
            List<TypeParamDef> newTypeParams = getTypeParams().stream()
                    .filter(binding::containsKey)
                    .collect(Collectors.toList());
            return new VarLink(getVisibility(), getDefinedIn(), newTypeParams, newReceiverType, def, newType);
        } else {
            return this;
        }
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



}
