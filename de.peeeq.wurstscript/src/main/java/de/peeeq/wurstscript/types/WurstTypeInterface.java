package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.attributes.names.TypeLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;


public class WurstTypeInterface extends WurstTypeClassOrInterface {


    private final InterfaceDef interfaceDef;

//	public PscriptTypeInterface(InterfaceDef interfaceDef, boolean staticRef) {
//		super(staticRef);
//		if (interfaceDef == null) throw new IllegalArgumentException();
//		this.interfaceDef = interfaceDef;
//	}

    public WurstTypeInterface(InterfaceDef interfaceDef, List<WurstTypeBoundTypeParam> newTypes, boolean isStaticRef) {
        super(newTypes, isStaticRef);
        if (interfaceDef == null) throw new IllegalArgumentException();
        this.interfaceDef = interfaceDef;
    }

    public WurstTypeInterface(InterfaceDef interfaceDef, List<WurstTypeBoundTypeParam> newTypes) {
        super(newTypes);
        if (interfaceDef == null) throw new IllegalArgumentException();
        this.interfaceDef = interfaceDef;
    }

    @Override
    public InterfaceDef getDef() {
        return interfaceDef;
    }

    public InterfaceDef getInterfaceDef() {
        return interfaceDef;
    }

    @Override
    public String getName() {
        return getDef().getName() + printTypeParams();
    }

    @Override
    public WurstType dynamic() {
        if (isStaticRef()) {
            return new WurstTypeInterface(getInterfaceDef(), getTypeParameters(), false);
        }
        return this;
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return new WurstTypeInterface(getInterfaceDef(), newTypes);
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        TreeMap<TypeParamDef, WurstTypeBoundTypeParam> superMatch = super.matchAgainstSupertypeIntern(other, location, typeParams, mapping);
        if (superMatch != null) {
            return superMatch;
        }

        if (other instanceof WurstTypeInterface) {
            WurstTypeInterface other2 = (WurstTypeInterface) other;
            if (interfaceDef == other2.interfaceDef) {
                // same interface -> check if type params are equal
                return matchTypeParams(getTypeParameters(), other2.getTypeParameters(), location, typeParams, mapping);
            } else {
                // test super interfaces:
                for (TypeLink extended : interfaceDef.attrExtendedInterfaces()) {
                    WurstType extendedType = extended.getTyp(mapping);
                    TreeMap<TypeParamDef, WurstTypeBoundTypeParam> res = extendedType.matchAgainstSupertype(other, location, typeParams, mapping);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }
        return null;
    }


    @Override
    public ImType imTranslateType() {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull();
    }


}
