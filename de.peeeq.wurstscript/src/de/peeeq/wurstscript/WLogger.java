package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WLogger {

//	static Logger logger;
	static Logger logger;

	static {
			logger = Logger.getLogger("wurstlog");
			logger.setLevel(Level.OFF); // adjust level for debugging
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void warning(String msg) {
		logger.log(Level.WARNING, msg);
	}
	
	public static void severe(String msg) {
//		dialogBox(msg);
		logger.log(Level.SEVERE, msg);
	}

	public static void severe(Throwable t) {
		t.printStackTrace();
		logger.log(Level.SEVERE, "Error", t);
	}

	public static void info(Throwable e) {
		logger.log(Level.INFO, "Error" + e);
		
	}

	public static void setHandler(Handler handler) {
		logger.addHandler(handler);
	}
	
	public static void setLevel(Level level) {
		logger.setLevel(level);
	}
}
