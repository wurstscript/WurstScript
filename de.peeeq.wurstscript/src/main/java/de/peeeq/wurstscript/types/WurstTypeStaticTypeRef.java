package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import org.eclipse.jdt.annotation.Nullable;

public class WurstTypeStaticTypeRef extends WurstType {

    private final WurstType base;

    public WurstTypeStaticTypeRef(WurstType base) {
        this.base = base;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType other, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (other instanceof WurstTypeStaticTypeRef) {
            return base.matchAgainstSupertype(((WurstTypeStaticTypeRef) other).base, location, mapping, variablePosition);
        }
        return null;
    }

    @Override
    public String getName() {
        return "static " + base.getName();
    }

    @Override
    public String getFullName() {
        return "static reference to " + base.getFullName();
    }

    @Override
    public ImType imTranslateType() {
        return base.imTranslateType();
    }

    @Override
    public ImExprOpt getDefaultValue() {
        return base.getDefaultValue();
    }

    @Override
    public WurstType dynamic() {
        return base;
    }

}
