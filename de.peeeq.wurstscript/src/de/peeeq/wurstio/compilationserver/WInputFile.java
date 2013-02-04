package de.peeeq.wurstio.compilationserver;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.peeeq.wurstscript.compilationserver.ResourceInput;
import de.peeeq.wurstscript.compilationserver.WInput;


public class WInputFile implements WInput {

	
	File f;

	public WInputFile(File f) {
		this.f = f;
	}
	
	@Override
	public List<ResourceInput> getCompilationUnits() {
		ResourceInput r = new ResourceInputFile(f.getAbsolutePath(), f.lastModified(), f);		
		return Collections.singletonList(r);
	}

}
