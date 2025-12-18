package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.List;


public class WurstTypeInterface extends WurstTypeClassOrInterface {


    private final InterfaceDef interfaceDef;

    public WurstTypeInterface(InterfaceDef interfaceDef, List<WurstTypeBoundTypeParam> newTypes, boolean isStaticRef, VariableBinding captured) {
        super(newTypes, isStaticRef, captured);
        if (interfaceDef == null) throw new IllegalArgumentException();
        this.interfaceDef = interfaceDef;
    }

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

    @Override
    public WurstType replaceTypeVarsWithCaptured(List<WurstTypeBoundTypeParam> newTypes, VariableBinding newCaptured) {
        return new WurstTypeInterface(interfaceDef, newTypes, isStaticRef(), newCaptured);
    }

    @Override
    public ImmutableList<WurstTypeInterface> directSupertypes() {
        return extendedInterfaces();
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


    public ImmutableList<WurstTypeInterface> extendedInterfaces() {
        return interfaceDef.getExtendsList().stream()
                .map(i -> (WurstTypeInterface) i.attrTyp().setTypeArgs(getTypeArgBinding()))
                .filter(i -> i.level() < level())
                .collect(ImmutableList.toImmutableList());
    }


    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(TypesHelper.imInt());
    }


}
