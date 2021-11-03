package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ReflectionBasedNativeHandle implements NativesProvider.NativeHandle {
    private final Object receiver;
    private final Method method;

    public ReflectionBasedNativeHandle(Object receiver, Method method) {
        this.receiver = receiver;
        this.method = method;
    }

    @Override
    public ILconst invoke(ILconst[] args) {
        try {
            return (ILconst) method.invoke(receiver, (Object[]) args);
        } catch (IllegalAccessException | IllegalArgumentException | ClassCastException e) {
            WLogger.severe(e);
            throw new Error(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof InterpreterException) {
                throw (InterpreterException) e.getCause();
            }
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw new Error(e.getCause());
        }
    }
}
