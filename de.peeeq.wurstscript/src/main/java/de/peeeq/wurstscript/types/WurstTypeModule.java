package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.stream.Stream;


public class WurstTypeModule extends WurstTypeNamedScope {

    private final ModuleDef moduleDef;

    public WurstTypeModule(ModuleDef moduleDef, boolean isStaticRef) {
        super(isStaticRef);
        if (moduleDef == null) throw new IllegalArgumentException();
        this.moduleDef = moduleDef;
    }

    public WurstTypeModule(ModuleDef moduleDef2, List<WurstTypeBoundTypeParam> newTypes) {
        super(newTypes);
        if (moduleDef2 == null) throw new IllegalArgumentException();
        moduleDef = moduleDef2;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        VariableBinding superMapping = super.matchAgainstSupertypeIntern(obj, location, mapping, variablePosition);
        if (superMapping != null) {
            return superMapping;
        }
        if (obj instanceof WurstTypeModuleInstanciation) {
            WurstTypeModuleInstanciation n = (WurstTypeModuleInstanciation) obj;
            if (n.isParent(this)) {
                return mapping;
            }
        }
        return null;
    }

    @Override
    public ModuleDef getDef() {
        return moduleDef;
    }

    @Override
    public String getName() {
        return getDef().getName() + printTypeParams() + " (module)";
    }

    @Override
    public WurstType dynamic() {
        if (isStaticRef()) {
            return new WurstTypeModule(moduleDef, false);
        }
        return this;
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return new WurstTypeModule(moduleDef, newTypes);
    }

    @Override
    public ImType imTranslateType(ImTranslator tr) {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(TypesHelper.imInt());
    }

    @Override
    public boolean isCastableToInt() {
        return true;
    }

    @Override
    public void addMemberMethods(Element node, String name, List<FuncLink> result) {
        // module methods cannot be called (only of module instantiations)
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        // module methods cannot be called (only of module instantiations)
        return Stream.empty();
    }
}
