package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationInt extends ObjectModification<Integer> {

	public ObjectModificationInt(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer, int intData) {
		super(parent, modificationId, VariableTypes.INTEGER, levelCount, dataPointer, intData);
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeInt(data);		
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}

	

	@Override
	protected String getFuncPostfix() {
		return "Int";
	}
	
	

}
