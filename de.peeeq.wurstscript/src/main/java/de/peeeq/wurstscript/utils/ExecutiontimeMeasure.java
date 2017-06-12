package de.peeeq.wurstscript.utils;

import de.peeeq.wurstscript.WLogger;

public class ExecutiontimeMeasure implements AutoCloseable {

	private String message;
	private long startTime;

	public ExecutiontimeMeasure(String message) {
		this.message = message;
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	public void close() {
		long time = System.currentTimeMillis() - startTime;
		WLogger.info("Executed " + message + " in " + time + "ms.");
	}

}
