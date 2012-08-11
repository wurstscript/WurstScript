package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationReal extends ObjectModification<Float> {

	public ObjectModificationReal(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer, float data) {
		super(parent, modificationId, VariableTypes.REAL, levelCount, dataPointer, data);
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeFloat(data);
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}


	@Override
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setReal(\"");
		out.append(modificationId);
		out.append("\", "+data+")\n");
		
	}
	
	

}
