package de.peeeq.wurstscript.compilationserver;

import java.io.IOException;
import java.io.Reader;


public abstract class ResourceInput {
	private final String name;
	private final long lastModified;
	
	public ResourceInput(String name, long lastModified) {
		this.name = name;
		this.lastModified = lastModified;
	}
	public String getName() {
		return name;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	abstract public Reader getReader() throws IOException;
	

}
