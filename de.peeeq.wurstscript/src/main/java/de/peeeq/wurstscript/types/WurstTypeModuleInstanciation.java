package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


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
        @Nullable NamedScope outerScope = moduleInst.getParent().attrNearestNamedScope();
        if (outerScope instanceof StructureDef) {
            return outerScope.attrTyp().matchAgainstSupertypeIntern(obj, location, mapping, variablePosition);
        }
        return null;
    }

    private @Nullable ClassOrInterface outerClass() {
        return moduleInst.getParent().attrNearestClassOrInterface();
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
        ArrayDeque<String> scopes = new ArrayDeque<>();
        NamedScope scope = getDef();
        while (scope instanceof ModuleInstanciation) {
            scopes.addFirst(scope.getName());
            scope = scope.getParent().attrNearestNamedScope();
        }
        StringBuilder res = new StringBuilder();
        res.append(outerClass().getName());
        res.append(" (module instanciation ");
        Utils.printSep(res, "/", scopes);
        res.append(")");
        return res.toString();
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
    public ImType imTranslateType(ImTranslator tr) {
        return TypesHelper.imInt();
    }

    @Override
    public ImExprOpt getDefaultValue(ImTranslator tr) {
        return JassIm.ImNull(TypesHelper.imInt());
    }

    @Override
    public boolean isNestedModuleInstantiationIn(WurstType other) {
        if (other instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope otherScope = (WurstTypeNamedScope) other;

            NamedScope outer = moduleInst;
            while ((outer = outer.getParent().attrNearestNamedScope()) != null) {
                if (outer == otherScope.getDef()) {
                    return true;
                }
            }
        }
        return false;
    }

}
