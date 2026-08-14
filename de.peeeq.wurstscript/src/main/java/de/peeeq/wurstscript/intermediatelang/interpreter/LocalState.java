package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImTypeVar;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

/**
 * Unchanged API. No eager map allocations unless you actually set/get vars/arrays.
 */
public class LocalState extends State {

    private @Nullable ILconst returnVal;
    /**
     * The type arguments this invocation was called with, kept only when the program still
     * contains generics, i.e. when running before generic elimination. Type class dispatch reads
     * the instance bound to a type argument from here.
     */
    private @Nullable Map<ImTypeVar, ImTypeArgument> typeArguments;

    public void setTypeArguments(Map<ImTypeVar, ImTypeArgument> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public @Nullable ImTypeArgument getTypeArgument(ImTypeVar typeVar) {
        return typeArguments == null ? null : typeArguments.get(typeVar);
    }

    public Map<ImTypeVar, ImTypeArgument> getTypeArguments() {
        return typeArguments == null ? java.util.Collections.emptyMap() : typeArguments;
    }

    public LocalState() {
        // no eager allocations
    }

    public LocalState(ILconst returnVal) {
        this.returnVal = returnVal;
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }
}
