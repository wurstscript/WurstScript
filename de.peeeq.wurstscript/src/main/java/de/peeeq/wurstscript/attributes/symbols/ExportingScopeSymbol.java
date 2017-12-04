package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.ExportingScope;
import de.peeeq.wurstscript.ast.WurstModel;

/**
 *
 */
public abstract class ExportingScopeSymbol extends ExportedSymbol {

    public ExportingScopeSymbol(ExportingScopeSymbol scope) {
        super(scope);
    }


    @Override
    public abstract ExportingScope resolve(WurstModel model);
}
