package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

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
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setString(\"");
		out.append(modificationId);
		out.append("\", \""+data+"\")\n");
		
	}

	
	
}
