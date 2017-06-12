package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;

import de.peeeq.wurstscript.utils.Utils;


public abstract class WLogger {

	private static WLoggerI instance = new WLoggerDefault();
	private static Level level;
	private static StringBuilder logSb = null;

	private static void keep(String level, Throwable e) {
		if (logSb == null) 
			return;
		logSb.append(level + "Exception " + "\n");
		logSb.append(e.getMessage() + " \n");
		logSb.append(Utils.printExceptionWithStackTrace(e));
		logSb.append("\n\n\n");
	}
	
	private static void keep(String string, String msg) {
		if (logSb == null) 
			return;
		logSb.append(level + ":" + msg + "\n");
	}

	public static void info(Throwable e) {
		keep("info", e);
		instance.info(e);		
	}


	public static void info(String msg) {
		keep("info", msg);
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
		keep("severe", e);
		instance.severe(e);
		
	}

	public static void severe(String msg) {
		keep("severe", msg);
		instance.severe(msg);
		
	}

	public static void warning(String msg) {
		keep("warning", msg);
		instance.warning(msg);
		
	}
	
	

	public static void keepLogs(boolean keepLogs) {
		if (keepLogs) {
			logSb = new StringBuilder();
		} else {
			logSb = null;
		}
	}

	public static String getLog() {
		if (logSb != null) {
			return logSb.toString();
		} else {
			return "no logs...";
		}
	}
	
	
}
