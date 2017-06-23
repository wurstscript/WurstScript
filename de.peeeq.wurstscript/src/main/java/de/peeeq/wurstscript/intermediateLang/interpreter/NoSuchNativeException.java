package de.peeeq.wurstscript.intermediateLang.interpreter;

public class NoSuchNativeException extends Exception {

    public NoSuchNativeException(String msg) {
        super(msg);
    }
}
