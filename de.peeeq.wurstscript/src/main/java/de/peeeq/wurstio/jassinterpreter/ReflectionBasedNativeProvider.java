package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class ReflectionBasedNativeProvider implements NativesProvider {

    protected PrintStream outStream = System.err;

    /** The methods by name, then by their number of parameters, so that a call allocates no key. */
    private final HashMap<String, Method[]> methodMap = new HashMap<>();

    public ReflectionBasedNativeProvider() {
        for (Method method : this.getClass().getMethods()) {
            int arity = method.getParameterCount();
            Method[] byArity = methodMap.get(method.getName());
            if (byArity == null || byArity.length <= arity) {
                Method[] grown = new Method[arity + 1];
                if (byArity != null) {
                    System.arraycopy(byArity, 0, grown, 0, byArity.length);
                }
                byArity = grown;
                methodMap.put(method.getName(), byArity);
            }
            if (byArity[arity] != null) {
                throw new RuntimeException("native entry already exists");
            }
            byArity[arity] = method;
        }
    }

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        Method[] byArity = methodMap.get(funcname);
        Method method = byArity != null && args.length < byArity.length ? byArity[args.length] : null;
        if (method == null) {
            String msg = "Calling method " + funcname + "(" +
                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", ")) + ")";
            msg += "\nwith types " + funcname + "(" +
                Arrays.stream(args).map(o -> o.getClass().getSimpleName()).collect(Collectors.joining(", ")) + ")";
//        if (candidate != null) {
//            msg += "\nDid you mean " + funcname + "(" +
//                Arrays.stream(candidate.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")?";
//        }
            throw new NoSuchNativeException(msg);
        }
        try {
            return (ILconst) method.invoke(this, (Object[]) args);
        } catch (IllegalAccessException | IllegalArgumentException e) {
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

    @Override
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

}
