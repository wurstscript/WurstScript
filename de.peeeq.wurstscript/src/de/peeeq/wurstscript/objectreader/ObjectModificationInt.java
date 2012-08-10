package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationInt extends ObjectModification {
	private int data;

	public ObjectModificationInt(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer, int intData) {
		super(parent, modificationId, VariableTypes.INTEGER, levelCount, dataPointer);
		this.data = intData;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeInt(data);		
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	@Override
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setInt(\"");
		out.append(modificationId);
		out.append("\", "+data+")\n");
		
	}
	
	

}
