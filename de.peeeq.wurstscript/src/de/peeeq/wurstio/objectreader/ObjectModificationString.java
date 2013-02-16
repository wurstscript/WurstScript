package de.peeeq.wurstio.objectreader;

import java.io.IOException;

import de.peeeq.wurstscript.utils.Utils;

public class ObjectModificationString extends ObjectModification<String> {

	public ObjectModificationString(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer,
			String stringData) {
		super(parent, modificationId, VariableTypes.STRING, levelCount, dataPointer, stringData);
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeStringNullTerminated(data);
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}


	@Override
	protected String getFuncPostfix() {
		return "String";
	}

	@Override
	protected String escapedData() {
		return Utils.escapeString(data);
	}
	
	
}
