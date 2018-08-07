package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.attributes.names.FuncLink;

public class EarlyReturn extends Exception {
    private static EarlyReturn instance = new EarlyReturn();
    private static final long serialVersionUID = 996637533377651375L;

    private FuncLink func;

    public static EarlyReturn get(FuncLink link) {
        Preconditions.checkNotNull(link);
        instance.func = link;
        return instance;
    }

    public FuncLink getFunc() {
        return func;
    }

}
