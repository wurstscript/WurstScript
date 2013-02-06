package de.peeeq.wurstscript.compilationserver;

import java.io.IOException;
import java.io.Reader;

public class ResourceInputReader extends ResourceInput {

	private Reader reader;

	public ResourceInputReader(String name, long lastModified, Reader reader) {
		super(name, lastModified);
		this.reader = reader;
	}

	@Override
	public Reader getReader() throws IOException {
		return reader;
	}

}
