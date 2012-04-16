package de.peeeq.wurstscript.compilationserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class ResourceInputFile extends ResourceInput {
	public File f;

	public ResourceInputFile(String name, long lastModified, File fi) {
		super(name, lastModified);
		this.f = fi;
	}
	
	@Override
	public Reader getReader() throws FileNotFoundException {
		return new FileReader(f);
	}
}