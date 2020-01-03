package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;


public class WurstTypeClass extends WurstTypeClassOrInterface {

    private ClassDef classDef;


    public WurstTypeClass(ClassDef classDef, List<WurstTypeBoundTypeParam> typeParameters, boolean staticRef) {
        super(typeParameters, staticRef);
        if (classDef == null) throw new IllegalArgumentException();
        this.classDef = classDef;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        VariableBinding superMapping = super.matchAgainstSupertypeIntern(obj, location, mapping, variablePosition);
        if (superMapping != null) {
            return superMapping;
        }
        // check module instantiations:
        if (obj instanceof WurstTypeModuleInstanciation) {
            WurstTypeModuleInstanciation mi = (WurstTypeModuleInstanciation) obj;
            @Nullable ClassDef nearestClass = mi.getDef().attrNearestClassDef();
            if (nearestClass == this.classDef) {
                return extendMapping(mapping, getTypeArgBinding(), location);
            }
        }
        return null;


    }

    private VariableBinding extendMapping(VariableBinding m1, VariableBinding m2, Element location) {
        for (TypeParamDef t : m2.keys()) {
            Option<WurstTypeBoundTypeParam> currentVal = m1.get(t);
            WurstTypeBoundTypeParam m2Val = m2.get(t).get();
            if (currentVal.isDefined()) {
                WurstTypeBoundTypeParam m1Val = currentVal.get();
                if (!m1Val.equalsType(m2Val, location)) {
                    // no match
                    return null;
                }
            } else {
                m1 = m1.set(t, m2Val);
            }
        }
        return m1;
    }

    public ImmutableList<WurstTypeInterface> implementedInterfaces() {
        classDef.getImplementsList().stream().forEach(i -> {
            if(!(i.attrTyp() instanceof WurstTypeInterface)) {
                @Nullable NameDef nameDef = i.tryGetNameDef();
                if(nameDef != null) {
                    i.addError("<" + nameDef.getName() + "> is not an interface.");
                } else {
                    classDef.getNameId().addError("Expecting interface name after `implements`");
                }
            }
        });
        return classDef.getImplementsList().stream()
                .filter(i -> i != null && i.attrTyp() instanceof WurstTypeInterface)
                .map(i -> (WurstTypeInterface) i.attrTyp().setTypeArgs(getTypeArgBinding()))
                .filter(i -> i.level() < level())
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
        WurstType t = extendedClass.attrTyp();
        if (t instanceof WurstTypeClass) {
            WurstTypeClass ct = (WurstTypeClass) t;
            if (ct.level() >= level()) {
                // cyclic dependency
                return null;
            }
            return (WurstTypeClass) ct.setTypeArgs(getTypeArgBinding());
        }
        return null;
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
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImIntVal(0);
    }

    public @Nullable TypeDef lookupInnerType(String typeName) {
        return getDef().getInnerClasses()
            .stream()
            .filter(ic -> ic.getName().equals(typeName))
            .findFirst()
            .orElse(null);
    }


}
