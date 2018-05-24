package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.attributes.names.FuncLink;

public class EarlyReturn extends Exception {
    private static final long serialVersionUID = 996637533377651375L;

    private FuncLink func;

    public EarlyReturn(FuncLink f) {
        Preconditions.checkNotNull(f);
        this.func = f;
    }

    public FuncLink getFunc() {
        return func;
    }


}
