package de.peeeq.wurstscript;


import ch.qos.logback.classic.Level;

public interface WLoggerI {

    void trace(String msg);

    void debug(String s);

    void info(String msg);

    void warning(String msg);

    void severe(String msg);

    void severe(Throwable t);

    void info(Throwable e);

    void setLevel(Level level);

    void warning(String msg, Throwable e);
}
