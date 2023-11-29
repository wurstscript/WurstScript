package tests.wurstscript.tests;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class ExtraMatchers {

    public static <X, Y> Matcher<X> get(String method, Matcher<Y> m) {
        return new BaseMatcher<X>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("under " + method + " ")
                        .appendDescriptionOf(m);
            }

            @Override
            public boolean matches(Object item) {
                Class<?> clazz = item.getClass();
                try {
                    Method meth = clazz.getMethod(method);
                    Y x = (Y) meth.invoke(item);
                    return m.matches(x);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
