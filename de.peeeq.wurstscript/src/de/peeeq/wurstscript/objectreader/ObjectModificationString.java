package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModificationString extends ObjectModification {
	private String data;


	public ObjectModificationString(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer,
			String stringData) {
		super(parent, modificationId, VariableTypes.STRING, levelCount, dataPointer);
		this.data = stringData;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeStringNullTerminated(data);
	}
	
	@Override
	public String toString() {
		return modificationId + " = " + data;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setString(\"");
		out.append(modificationId);
		out.append("\", \""+data+"\")\n");
		
	}

	
	
}
