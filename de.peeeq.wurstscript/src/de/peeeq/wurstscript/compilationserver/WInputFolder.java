package de.peeeq.wurstscript.compilationserver;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;


public class WInputFolder implements WInput {

	private File dir;

	public WInputFolder(File dir) {
		this.dir = dir;
	}
	
	@Override
	public List<ResourceInput> getCompilationUnits() {
		List<ResourceInput> result = Lists.newArrayList();
		scanfolder(result, dir);
		return result;
	}

	private void scanfolder(List<ResourceInput> result, File f) {
		if (f.isFile()) {
			result.add(new ResourceInputFile(f.getAbsolutePath(), f.lastModified(), f));
		} else if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				scanfolder(result, child);
			}
		}
		
	}

}
