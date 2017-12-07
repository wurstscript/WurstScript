package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public abstract class Provider {
    protected ILInterpreter interpreter;

    public Provider(ILInterpreter interpreter) {
        this.interpreter = interpreter;
    }
}
