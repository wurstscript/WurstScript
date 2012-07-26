package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

/**
 * same as {@link ObjectModificationReal} but with 0 <= data <= 1  
 */
public class ObjectModificationUnreal extends ObjectModification {
	private float data;

	public ObjectModificationUnreal(String originalObjectId, String newObjectId, String modificationId, int variableType, int levelCount, int dataPointer, float data) {
		super(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer);
		this.data = data;
	}
	
	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeFloat(data);
	}

	@Override
	public String toString() {
		return modificationId + " = " + data + "  (unreal)";
	}
	

}
