package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationReal extends ObjectModification {
	private float data;

	public ObjectModificationReal(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer, float data) {
		super(parent, modificationId, VariableTypes.REAL, levelCount, dataPointer);
		this.data = data;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeFloat(data);
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}

	public float getData() {
		return data;
	}

	public void setData(float data) {
		this.data = data;
	}

	@Override
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setReal(\"");
		out.append(modificationId);
		out.append("\", "+data+")\n");
		
	}
	
	

}
