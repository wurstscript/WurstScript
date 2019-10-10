package de.peeeq.wurstscript.utils;

import java.util.function.Supplier;

public class Lazy<T> {
    private boolean done = false;
    private T value;
    private final Supplier<T> f;

    private Lazy(Supplier<T> f) {
        this.f = f;
    }

    public static <T> Lazy<T> create(Supplier<T> f) {
        return new Lazy<>(f);
    }


    public T get() {
        if (!done) {
            value = f.get();
            done = true;
        }
        return value;
    }


}
