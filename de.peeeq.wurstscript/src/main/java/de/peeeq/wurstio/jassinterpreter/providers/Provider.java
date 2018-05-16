package de.peeeq.wurstio.jassinterpreter.providers;

import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public abstract class Provider {
    protected AbstractInterpreter interpreter;

    public Provider(AbstractInterpreter interpreter) {
        Preconditions.checkNotNull(interpreter);
        this.interpreter = interpreter;
    }
}
