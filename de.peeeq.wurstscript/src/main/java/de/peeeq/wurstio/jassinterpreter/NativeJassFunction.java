package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeJassFunction implements ExecutableJassFunction {

    private final Method method;
    private final Object provider;

    public NativeJassFunction(Object provider, Method method) {
        this.provider = provider;
        this.method = method;
    }

    @Override
    public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
        try {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] instanceof ILconstNull
                        && !method.getParameterTypes()[i].isAssignableFrom(arguments[i].getClass())) {
                    arguments[i] = null;
                }
            }
            Object result = method.invoke(provider, (Object[]) arguments);
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
