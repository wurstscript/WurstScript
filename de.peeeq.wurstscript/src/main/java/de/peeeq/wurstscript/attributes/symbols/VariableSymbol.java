package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.*;

/**
 *
 */
public class VariableSymbol extends ExportedSymbol {
    private final String varName;

    public VariableSymbol(ExportingScopeSymbol scope, String varName) {
        super(scope);
        this.varName = varName;
    }

    @Override
    public String getName() {
        return scope + "." + varName;
    }

    @Override
    public VarDef resolve(WurstModel model) {
        return scope.resolve(model).attrLookupVariable(varName).orElseGet(() -> {
            throw new CouldNotResolveException();
        });
    }


}
