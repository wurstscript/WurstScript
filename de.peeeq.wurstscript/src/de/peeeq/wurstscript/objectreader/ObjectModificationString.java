package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationString extends ObjectModification {
	private String data;

	public ObjectModificationString(String originalObjectId, String newObjectId, String modificationId, int variableType, int levelCount, int dataPointer, String data) {
		super(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer);
		this.data = data;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeStringNullTerminated(data);
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}

}
