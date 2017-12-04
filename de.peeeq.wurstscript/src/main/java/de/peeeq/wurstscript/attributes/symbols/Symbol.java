package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WurstModel;

/**
 * TODO implement equals and hashCode
 */
public abstract class Symbol {


    @Override
    public String toString() {
        return getName();
    }

    public abstract String getName();

    public abstract Element resolve(WurstModel model);

    public class CouldNotResolveException extends RuntimeException {
        CouldNotResolveException() {
            super("Could not resolve " + Symbol.this);
        }
    }
}
