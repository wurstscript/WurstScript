package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class WurstTypeClass extends WurstTypeClassOrInterface {

    private ClassDef classDef;


    public WurstTypeClass(ClassDef classDef, List<WurstTypeBoundTypeParam> typeParameters, boolean staticRef) {
        super(typeParameters, staticRef);
        if (classDef == null) throw new IllegalArgumentException();
        this.classDef = classDef;
    }

    @Override
    @Nullable TreeMap<TypeParamDef, WurstTypeBoundTypeParam> matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, Collection<TypeParamDef> typeParams, TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping) {
        TreeMap<TypeParamDef, WurstTypeBoundTypeParam> superMapping = super.matchAgainstSupertypeIntern(obj, location, typeParams, mapping);
        if (superMapping != null) {
            return superMapping;
        }
        if (obj instanceof WurstTypeInterface) {
            // TODO probably not a good idea to get type arg binding after the fact, implemented interfaces should return List<WurstTypeInterface> with typeArgs already set


        }
        // TODO this looks fishy, so I disabled it, but it's probably required for accessing module members
//        if (obj instanceof WurstTypeModuleInstanciation) {
//            WurstTypeModuleInstanciation n = (WurstTypeModuleInstanciation) obj;
//            return n.isParent(this);
//        }
        WurstTypeClass extendedClass = extendedClass();
        if (extendedClass != null) {
            TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping2 = extendedClass.matchAgainstSupertypeIntern(obj, location, typeParams, mapping);
            if (mapping2 != null) {
                return mapping2;
            }
        }
        // try if one of my interfaces implements the right type:
        // OPT this could be optimized -- only do this if obj is an interface type
        for (WurstTypeInterface implementedInterface : implementedInterfaces()) {

            TreeMap<TypeParamDef, WurstTypeBoundTypeParam> mapping2 = implementedInterface.matchAgainstSupertypeIntern(obj, location, typeParams, mapping);
            if (mapping2 != null) {
                return mapping2;
            }
        }
        return null;
    }

    public List<WurstTypeInterface> implementedInterfaces() {
        return classDef.getImplementsList().stream()
                .map(i -> (WurstTypeInterface) i.attrTyp().setTypeArgs(getTypeArgBinding()))
                .collect(Collectors.toList());
    }

    /**
     * A type for the class extended by this class (or null if none)
     */
    private @Nullable WurstTypeClass extendedClass() {
        OptTypeExpr extendedClass = classDef.getExtendedClass();
        if (extendedClass instanceof NoTypeExpr) {
            return null;
        }
        WurstType unboundType = extendedClass.attrTyp();
        return (WurstTypeClass) unboundType.setTypeArgs(getTypeArgBinding());
    }

    @Override
    public ClassDef getDef() {
        return classDef;
    }

    public ClassDef getClassDef() {
        return classDef;
    }

    @Override
    public String getName() {
        return getDef().getName() + printTypeParams();
    }

    @Override
    public WurstType dynamic() {
        return new WurstTypeClass(getClassDef(), getTypeParameters(), false);
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return new WurstTypeClass(classDef, newTypes, isStaticRef());
    }

    @Override
    public ImType imTranslateType() {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImIntVal(0);
    }

}
