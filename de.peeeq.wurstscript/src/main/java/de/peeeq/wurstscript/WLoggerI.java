package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;

public interface WLoggerI {

    void info(String msg);

    void warning(String msg);

    void severe(String msg);

    void severe(Throwable t);

    void info(Throwable e);

    void setHandler(Handler handler);

    void setLevel(Level level);

}