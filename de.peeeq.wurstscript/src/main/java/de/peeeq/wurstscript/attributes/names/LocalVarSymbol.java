package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Objects;

/**
 *
 */
public class LocalVarSymbol extends Symbol<VarDef> {
    private final VarDef varDef;

    public LocalVarSymbol(VarDef varDef) {
        this.varDef = varDef;
    }

    @Override
    public VarDef getDef(WurstModel m) {
        return varDef;
    }

    @Override
    public String getName() {
        return varDef.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalVarSymbol that = (LocalVarSymbol) o;
        return Objects.equals(varDef, that.varDef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varDef);
    }

    @Override
    public String toString() {
        return Utils.printElementWithSource(varDef);
    }
}
