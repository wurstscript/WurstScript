package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationInt extends ObjectModification {
	private int data;

	public ObjectModificationInt(String originalObjectId, String newObjectId, String modificationId, int variableType, int levelCount, int dataPointer, int data) {
		super(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer);
		this.data = data;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeInt(data);		
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}
	
	

}
