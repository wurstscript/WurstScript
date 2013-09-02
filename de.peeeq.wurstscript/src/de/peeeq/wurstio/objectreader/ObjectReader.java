package de.peeeq.wurstio.objectreader;

import java.io.File;

public class ObjectReader {
	
	
	public static void main(String ... args) {
		
		ObjectFile objFile = new ObjectFile(new File("testscripts/data/units.w3u"), ObjectFileType.UNITS);
		
		ObjectTable modT = objFile.getModifiedTable();
		for (ObjectDefinition od : modT.getObjectDefinitions()) {
			System.out.println(od.getNewObjectId() + " (derived from " + od.getOrigObjectId() + ")");
			for (ObjectModification<?> m : od.getModifications()) {
				System.out.println("    " + m.toString());
			}
		}
		
		objFile.writeTo(new File("testscripts/data/units_out.w3u"));
	}
}
