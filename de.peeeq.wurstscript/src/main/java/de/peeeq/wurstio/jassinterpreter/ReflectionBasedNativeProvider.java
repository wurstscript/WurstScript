package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class ReflectionBasedNativeProvider implements NativesProvider {

    protected PrintStream outStream = System.err;

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        Method candidate = null;
        nextMethod:
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals(funcname)) {
                // this is a candidate as it has the correct name
                candidate = method;
                Object r;
                try {
                    if (args.length != method.getParameterTypes().length) {
                        continue;
                    }
                    int i = 0;
                    for (Class<?> paramType : method.getParameterTypes()) {
                        if (!paramType.isAssignableFrom(args[i].getClass())) {
                            continue nextMethod;
                        }
                        i++;
                    }
                    r = method.invoke(this, (Object[]) args);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    WLogger.severe(e);
                    throw new Error(e);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof InterpreterException) {
                        throw (InterpreterException) e.getCause();
                    } if (e.getCause() instanceof Error) {
                        throw (Error) e.getCause();
                    }
                    throw new Error(e.getCause());
                }
                return (ILconst) r;
            }
        }
        String msg = "Calling method " + funcname + "(" +
                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")) + ")";
        msg += "\nwith types " + funcname + "(" +
            Arrays.stream(args).map(o -> o.getClass().getSimpleName()).collect(Collectors.joining(", ")) + ")";
        if (candidate != null) {
            msg += "\nDid you mean " + funcname + "(" +
                    Arrays.stream(candidate.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")?";
        }
        throw new NoSuchNativeException(msg);
    }

    @Override
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

}
