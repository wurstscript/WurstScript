package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.TypeLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;


public class WurstTypeClass extends WurstTypeClassOrInterface {

    ClassDef classDef;


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

            WurstTypeInterface pti = (WurstTypeInterface) obj;
            for (TypeLink implementedInterface : classDef.attrImplementedInterfaces()) {

                if (implementedInterface.setTypeArgs(getTypeArgBinding()).isSubtypeOf(pti, location)) {
                    return true;
                }
            }
        }
        if (obj instanceof WurstTypeModuleInstanciation) {
            WurstTypeModuleInstanciation n = (WurstTypeModuleInstanciation) obj;
            return n.isParent(this);
        }
        if (classDef.getExtendedClass() instanceof TypeExpr) {
            TypeExpr extendedClass = (TypeExpr) classDef.getExtendedClass();
            WurstType superType = extendedClass.attrTyp();
            superType = superType.setTypeArgs(this.getTypeArgBinding());
            return superType.isSubtypeOf(obj, location);
        }
        return false;
    }

    @Override
    public StructureDef getDef() {
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
