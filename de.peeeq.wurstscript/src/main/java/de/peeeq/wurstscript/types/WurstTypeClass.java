package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import fj.data.TreeMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class WurstTypeClass extends WurstTypeClassOrInterface {

    private ClassDef classDef;


    public WurstTypeClass(ClassDef classDef, List<WurstTypeBoundTypeParam> typeParameters, boolean staticRef) {
        super(typeParameters, staticRef);
        if (classDef == null) throw new IllegalArgumentException();
        this.classDef = classDef;
    }



    public ImmutableList<WurstTypeInterface> implementedInterfaces() {
        return classDef.getImplementsList().stream()
                .map(i -> (WurstTypeInterface) i.attrTyp().setTypeArgs(getTypeArgBinding()))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * A type for the class extended by this class (or null if none)
     */
    public @Nullable WurstTypeClass extendedClass() {
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

    @Override
    public ImmutableList<? extends WurstTypeClassOrInterface> directSupertypes() {
        WurstTypeClass ec = extendedClass();
        if (ec == null) {
            return implementedInterfaces();
        } else {
            ImmutableList.Builder<WurstTypeClassOrInterface> builder = ImmutableList.builder();
            builder.add(ec);
            builder.addAll(implementedInterfaces());
            return builder.build();
        }
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
