package de.peeeq.wurstio.compilationserver;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import de.peeeq.wurstio.utils.FileReading;
import de.peeeq.wurstscript.compilationserver.ResourceInput;

public class ResourceInputFile extends ResourceInput {
	private File f;

	public ResourceInputFile(String name, long lastModified, File fi) {
		super(name, lastModified);
		this.f = fi;
	}
	
	@Override
	public Reader getReader() throws IOException {
		return FileReading.getFileReader(f);
	}

	public File getFile() {
		return f;
	}
}
