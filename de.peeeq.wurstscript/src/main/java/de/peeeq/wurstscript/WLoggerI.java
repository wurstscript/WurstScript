package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;

public interface WLoggerI {

	public abstract void info(String msg);

	public abstract void warning(String msg);

	public abstract void severe(String msg);

	public abstract void severe(Throwable t);

	public abstract void info(Throwable e);

	public abstract void setHandler(Handler handler);

	public abstract void setLevel(Level level);

}