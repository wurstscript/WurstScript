package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstio.jassinterpreter.providers.*;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReflectionNativeProvider implements NativesProvider {
    private final HashMap<String, NativeJassFunction> methodMap = new HashMap<>();
    /** Providers that print to the interpreter output, so that redirecting it reaches them. */
    private final List<OutputProvider> outputProviders = new ArrayList<>();

    public ReflectionNativeProvider(AbstractInterpreter interpreter) {
        addProvider(new AbilityProvider(interpreter));
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
        addProvider(new LuaEnsureTypeProvider(interpreter));
        addProvider(new QuestProvider(interpreter));
        addProvider(new QuestItemProvider(interpreter));
    }

    public NativeJassFunction getFunctionPair(String funcName) {
        return methodMap.get(funcName);
    }

    private void addProvider(Provider provider) {
        if (provider instanceof OutputProvider outputProvider) {
            outputProviders.add(outputProvider);
        }
        for (Method method : provider.getClass().getMethods()) {
            Implements annotation = method.getAnnotation(Implements.class);
            if (annotation != null) {
                String[] funcNames = annotation.funcNames();
                for (String funcName : funcNames) {
                    if (methodMap.containsKey(funcName)) {
                        throw new Error("Trying to add multiple implementations of <" + funcName + ">");
                    }
                    methodMap.put(funcName, new NativeJassFunction(funcName, provider, method));
                }
            } else if (method.getDeclaringClass() != Object.class) {
                // Two providers with a method of the same name would make the later one win silently
                // (FrameProvider once shadowed ConversionProvider's ConvertOriginFrameType, whose handles keep
                // their identity). Overloads within one provider stay as they were.
                NativeJassFunction previous = methodMap.put(method.getName(), new NativeJassFunction(method.getName(), provider, method));
                if (previous != null && previous.getProvider() != provider) {
                    throw new Error("Trying to add multiple implementations of <" + method.getName() + ">: "
                        + previous.getProvider().getClass().getSimpleName() + " and " + provider.getClass().getSimpleName());
                }
            }
        }
    }

    @Override
    public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
        NativeJassFunction candidate = methodMap.get(funcname);
        if (candidate == null) {
            throw new NoSuchNativeException("");
        }

        // A method whose first parameter is a String implements several natives and is told which one was called.
        Class<?>[] declared = candidate.parameterTypes();
        int offset = candidate.nameParameters();
        if (declared.length == args.length + offset) {
            for (int i = 0; i < args.length; i++) {
                if (!declared[i + offset].isAssignableFrom(args[i].getClass())) {
                    if (args[i] instanceof ILconstNull) {
                        // handle null as a special case and pass it to the native as a Java null
                        args[i] = null;
                        continue;
                    }
                    throw new Error("The native <" + funcname + "> expects different parameter " + i + "!" +
                            "\n\tExpected: " + declared[i + offset].getSimpleName() + " Actual: " + args[i]);
                }
            }
        }
        try {
            return (ILconst) candidate.getMethod().invoke(candidate.getProvider(), candidate.callArguments(args));
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
        for (OutputProvider provider : outputProviders) {
            provider.setOutStream(outStream);
        }
    }
}
