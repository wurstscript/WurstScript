package de.peeeq.wurstscript;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class WLogger {

//	static Logger logger;
	static Logger logger;

	static {
		try {
			logger = Logger.getLogger("wurstlog");
			Layout layout = new SimpleLayout();
			DailyRollingFileAppender newAppender = new DailyRollingFileAppender(layout, "logs/wurst.log", "yyyy-MM-dd");
			logger.addAppender(newAppender);
		} catch (Exception e) {
			throw new Error(e);
		}

	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void warning(String msg) {
		logger.warn(msg);
	}
	
	public static void severe(String msg) {
//		dialogBox(msg);
		logger.error(msg);
	}

	private static void dialogBox(String msg) {
		JOptionPane.showMessageDialog(null , msg, "Error", JOptionPane. ERROR_MESSAGE);
	}

	public static void severe(Throwable t) {
		logger.error("Error", t);
	}

	public static void info(Throwable e) {
		logger.info(e);
		
	}
	
}
