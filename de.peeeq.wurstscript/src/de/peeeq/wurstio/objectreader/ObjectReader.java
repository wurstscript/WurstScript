package de.peeeq.wurstio.objectreader;

import java.io.File;

import de.peeeq.wurstscript.WLogger;

public class ObjectReader {
	
	
	public static void main(String ... args) {
		
		ObjectFile objFile = new ObjectFile(new File("testscripts/data/units.w3u"), ObjectFileType.UNITS);
		
		ObjectTable modT = objFile.getModifiedTable();
		for (ObjectDefinition od : modT.getObjectDefinitions()) {
			WLogger.info(od.getNewObjectId() + " (derived from " + od.getOrigObjectId() + ")");
			for (ObjectModification<?> m : od.getModifications()) {
				WLogger.info("    " + m.toString());
			}
		}
		
		objFile.writeTo(new File("testscripts/data/units_out.w3u"));
	}
}
