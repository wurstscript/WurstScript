package de.peeeq.wurstscript.objectreader;

import java.io.File;

public class ObjectReader {
	
	
	public static void main(String ... args) {
		
		ObjectFile objFile = new ObjectFile(new File("/home/peter/Desktop/etofa_w3x/war3map.w3a"), ObjectFileType.ABILITIES);
		
	}
}
