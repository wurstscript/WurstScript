package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeJassFunction implements ExecutableJassFunction {

    private Method method;
    private Object provider;

    public NativeJassFunction(Object provider, Method method) {
        this.provider = provider;
        this.method = method;
    }

    @Override
    public ILconst execute(JassInterpreter jassInterpreter, ILconst[] arguments) {
        try {
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