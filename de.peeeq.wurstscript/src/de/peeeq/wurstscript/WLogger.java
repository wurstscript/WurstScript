package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;


public abstract class WLogger {

	private static WLoggerI instance = new WLoggerDefault();
	private static Level level;


	public static void info(Throwable e) {
		instance.info(e);		
	}

	public static void info(String msg) {
		instance.info(msg);
		
	}

	public static void setHandler(Handler handler) {
		instance.setHandler(handler);
	}

	public static void setLevel(Level level) {
		WLogger.level = level;
		instance.setLevel(level);
	}

	public static void severe(Throwable e) {
		instance.severe(e);
		
	}

	public static void severe(String msg) {
		instance.severe(msg);
		
	}

	public static void warning(String msg) {
		instance.warning(msg);
		
	}
	
	public static void setInstance(WLoggerI instance) {
		WLogger.instance = instance;
		instance.setLevel(level);
	}
	
	
}
