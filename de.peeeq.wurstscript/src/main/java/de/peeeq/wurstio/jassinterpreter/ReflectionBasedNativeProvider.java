package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException;
import de.peeeq.wurstscript.utils.Pair;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class ReflectionBasedNativeProvider implements NativesProvider {

    protected PrintStream outStream = System.err;

    private final HashMap<Pair<String, Integer>, Method> methodMap = new HashMap<>();

    public ReflectionBasedNativeProvider() {
        for (Method method : this.getClass().getMethods()) {
            Pair<String, Integer> keyPair = Pair.create(method.getName(), method.getParameterTypes().length);
            if (methodMap.containsKey(keyPair)) {
                throw new RuntimeException("native entry already exists");
            }
            methodMap.put(keyPair, method);
        }
    }

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        Method method = methodMap.get(Pair.create(funcname, args.length));
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
