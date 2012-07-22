package de.peeeq.wurstscript.compilationserver;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import de.peeeq.wurstscript.utils.FileReading;

public class ResourceInputFile extends ResourceInput {
	public File f;

	public ResourceInputFile(String name, long lastModified, File fi) {
		super(name, lastModified);
		this.f = fi;
	}
	
	@Override
	public Reader getReader() throws IOException {
		return FileReading.getFileReader(f);
	}
}