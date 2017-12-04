package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WurstModel;

/**
 *
 */
public abstract class ExportedSymbol extends Symbol {
    protected final ExportingScopeSymbol scope;

    public ExportedSymbol(ExportingScopeSymbol scope) {
        this.scope = scope;
    }

    protected WScope resolveScope(WurstModel model) {
        Element elem = resolve(model);
        if (elem instanceof WScope) {
            return ((WScope) elem);
        }
        throw new RuntimeException("Could not resolve scope " + this + "(was " + elem + ")");
    }
}
