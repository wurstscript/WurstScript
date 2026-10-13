package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeJassFunction implements ExecutableJassFunction {

    private final String name;
    private final Method method;
    private final Object provider;
    /** {@link Method#getParameterTypes} copies the array on every call, and this is asked on every native call. */
    private final Class<?>[] parameterTypes;
    private final int nameParameters;

    public NativeJassFunction(String name, Object provider, Method method) {
        this.name = name;
        this.provider = provider;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.nameParameters = parameterTypes.length > 0 && parameterTypes[0] == String.class ? 1 : 0;
    }

    Class<?>[] parameterTypes() {
        return parameterTypes;
    }

    /**
     * 1 when the method's first parameter is a String: it implements several natives, and is passed the name of the
     * one called before the native's own arguments.
     */
    int nameParameters() {
        return nameParameters;
    }

    /** The arguments the method is invoked with: the native's, after the native's name when the method takes it. */
    Object[] callArguments(ILconst[] arguments) {
        if (nameParameters() == 0) {
            return arguments;
        }
        Object[] result = new Object[arguments.length + 1];
        result[0] = name;
        System.arraycopy(arguments, 0, result, 1, arguments.length);
        return result;
    }

    @Override
    public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
        try {
            int offset = nameParameters();
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] instanceof ILconstNull
                        && !parameterTypes[i + offset].isAssignableFrom(arguments[i].getClass())) {
                    arguments[i] = null;
                }
            }
            Object result = method.invoke(provider, callArguments(arguments));
            return (ILconst) result;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else {
                throw new Error(e.getCause());
            }
        }
    }

    public Method getMethod() {
        return method;
    }

    public Object getProvider() {
        return provider;
    }
}
