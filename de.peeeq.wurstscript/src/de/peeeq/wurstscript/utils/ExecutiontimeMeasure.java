package de.peeeq.wurstscript.utils;

import java.io.Closeable;
import java.io.IOException;

import de.peeeq.wurstscript.WLogger;

public class ExecutiontimeMeasure implements Closeable {

	private String message;
	private long startTime;

	public ExecutiontimeMeasure(String message) {
		this.message = message;
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	public void close() throws IOException {
		long time = System.currentTimeMillis() - startTime;
		WLogger.info("Executed " + message + " in " + time + "ms.");
	}

}
