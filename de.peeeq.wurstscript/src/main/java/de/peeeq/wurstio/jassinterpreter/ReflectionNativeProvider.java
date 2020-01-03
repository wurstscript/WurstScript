package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstio.jassinterpreter.providers.*;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException;
import de.peeeq.wurstscript.utils.Utils;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ReflectionNativeProvider implements NativesProvider {
    private HashMap<String, NativeJassFunction> methodMap = new HashMap<>();

    public ReflectionNativeProvider(AbstractInterpreter interpreter) {
        addProvider(new GamecacheProvider(interpreter));
        addProvider(new ForceProvider(interpreter));
        addProvider(new HandleProvider(interpreter));
        addProvider(new GroupProvider(interpreter));
        addProvider(new HashtableProvider(interpreter));
        addProvider(new MathProvider(interpreter));
        addProvider(new OutputProvider(interpreter));
        addProvider(new WurstflectionProvider(interpreter));
        addProvider(new StringProvider(interpreter));
        addProvider(new UnitProvider(interpreter));
        addProvider(new PlayerProvider(interpreter));
        addProvider(new TriggerProvider(interpreter));
        addProvider(new TimerProvider(interpreter));
        addProvider(new LocationProvider(interpreter));
        addProvider(new RectProvider(interpreter));
        addProvider(new ItemProvider(interpreter));
        addProvider(new ConversionProvider(interpreter));
        addProvider(new DestructableProvider(interpreter));
        addProvider(new DialogProvider(interpreter));
        addProvider(new EffectProvider(interpreter));
        addProvider(new RegionProvider(interpreter));
        addProvider(new ImageProvider(interpreter));
        addProvider(new IntegerProvider(interpreter));
        addProvider(new FrameProvider(interpreter));
    }

    public NativeJassFunction getFunctionPair(String funcName) {
        return methodMap.get(funcName);
    }

    private void addProvider(Provider provider) {
        for (Method method : provider.getClass().getMethods()) {
            Implements annotation = method.getAnnotation(Implements.class);
            if (annotation != null) {
                String[] funcNames = annotation.funcNames();
                for (String funcName : funcNames) {
                    if (methodMap.containsKey(funcName)) {
                        throw new Error("Trying to add multiple implementations of <" + funcName + ">");
                    }
                    methodMap.put(funcName, new NativeJassFunction(provider, method));
                }
            } else {
                methodMap.put(method.getName(), new NativeJassFunction(provider, method));
            }
        }
    }

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        String msg = "Calling method " + funcname + "(" +
            Utils.printSep(", ", args) + ")";
        WLogger.trace(msg);

        NativeJassFunction candidate = methodMap.get(funcname);
        if (candidate == null) {
            throw new NoSuchNativeException("");
        }

        if (candidate.getMethod().getParameterCount() == args.length) {
            String[] parameterTypes = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = "" + args[i];
                if (!candidate.getMethod().getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                    if (args[i] instanceof ILconstNull) {
                        // handle null as a special case and pass it to the native as a Java null
                        args[i] = null;
                        continue;
                    }
                    throw new Error("The native <" + funcname + "> expects different parameter " + i + "!" +
                            "\n\tExpected: " + candidate.getMethod().getParameterTypes()[i].getSimpleName() + " Actual: " + parameterTypes[i]);
                }
            }
        }
        try {
            return (ILconst) candidate.getMethod().invoke(candidate.getProvider(), (Object[]) args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else if (e.getCause() instanceof InterpreterException) {
                throw (InterpreterException) e.getCause();
            }
            throw new Error(e.getCause());
        }
    }

    @Override
    public void setOutStream(PrintStream outStream) {

    }
}
