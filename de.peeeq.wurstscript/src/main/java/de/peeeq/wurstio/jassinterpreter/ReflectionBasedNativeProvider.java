package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Optional;

public abstract class ReflectionBasedNativeProvider implements NativesProvider {

    protected PrintStream outStream = System.err;

    public Optional<NativeHandle> find(String funcname) {
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals(funcname)) {
                return Optional.of(new ReflectionBasedNativeHandle(this, method));
            }
        }
        return Optional.empty();
    }

    @Override
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

}
