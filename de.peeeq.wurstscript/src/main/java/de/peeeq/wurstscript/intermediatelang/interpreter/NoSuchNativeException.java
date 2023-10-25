package de.peeeq.wurstscript.intermediatelang.interpreter;

public class NoSuchNativeException extends Exception {

    public NoSuchNativeException(String msg) {
        super(msg, null, true, false);
    }
}
