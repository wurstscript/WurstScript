package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;


public class WurstTypeModuleInstanciation extends WurstTypeNamedScope {

    private ModuleInstanciation moduleInst;

    public WurstTypeModuleInstanciation(ModuleInstanciation moduleInst, boolean isStaticRef) {
        super(isStaticRef);
        if (moduleInst == null) throw new IllegalArgumentException();
        this.moduleInst = moduleInst;
    }

    public WurstTypeModuleInstanciation(ModuleInstanciation moduleInst2, List<WurstTypeBoundTypeParam> newTypes) {
        super(newTypes);
        if (moduleInst2 == null) throw new IllegalArgumentException();
        moduleInst = moduleInst2;
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

    /**
     * check if n is a parent of this
     */
    boolean isParent(WurstTypeNamedScope n) {
        NamedScope ns = this.getDef();
        while (true) {
            ns = ns.getParent().attrNearestNamedScope();
            if (ns == null) {
                return false;
            }
            //if (ns == n.getDef()) {
            if (n.isSubtypeOf(ns.attrTyp(), ns)) {
                return true;
            }
        }
    }

    @Override
    public NamedScope getDef() {
        return moduleInst;
    }

    @Override
    public String getName() {
        return getDef().getName() + printTypeParams() + " (module instanciation in " + moduleInst.getParent().attrNearestNamedScope().attrTyp() + ")";
    }

    @Override
    public WurstType dynamic() {
        if (isStaticRef()) {
            return new WurstTypeModuleInstanciation(moduleInst, false);
        }
        return this;
    }

    @Override
    public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes) {
        return new WurstTypeModuleInstanciation(moduleInst, newTypes);
    }

    @Override
    public ImType imTranslateType() {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return JassIm.ImNull(TypesHelper.imInt());
    }

}
