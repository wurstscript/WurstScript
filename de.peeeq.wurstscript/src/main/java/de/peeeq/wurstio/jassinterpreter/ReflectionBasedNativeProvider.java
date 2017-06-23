package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediateLang.interpreter.NoSuchNativeException;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class ReflectionBasedNativeProvider implements NativesProvider {

    protected PrintStream outStream = System.out;

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        Method candidate = null;
        nextMethod:
        for (Method method : this.getClass().getMethods()) {
            if (method.getName().equals(funcname)) {
                // this is a candidate as it has the correct name
                candidate = method;
                Object r = null;
                try {
                    if (args.length != method.getParameterTypes().length) {
                        continue nextMethod;
//						throw new Error("Wrong number of parameters when calling " +funcname+
//								" . Expected " +
//								method.getParameterTypes().length + " but found " +
//								args.length);
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
                    if (e.getCause() instanceof Error) {
                        throw (Error) e.getCause();
                    }
                    throw new Error(e.getCause());
                }
                return (ILconst) r;
            }
        }
        String[] parameterTypes = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = "" + args[i];
        }
        String msg = "Calling method " + funcname + "(" +
                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")) + ")";
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