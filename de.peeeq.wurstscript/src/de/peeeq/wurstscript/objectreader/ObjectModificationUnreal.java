package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

/**
 * same as {@link ObjectModificationReal} but with 0 <= data <= 1  
 */
public class ObjectModificationUnreal extends ObjectModification {
	private float data;

	public ObjectModificationUnreal(ObjectDefinition parent, String modificationId, int levelCount, int dataPointer,
			float floatData) {
		super(parent, modificationId, VariableTypes.UNREAL, levelCount, dataPointer);
		this.data = floatData;
	}

	@Override
	void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeFloat(data);
	}

	@Override
	public String toString() {
		return modificationId + " = " + data + "  (unreal)";
	}

	public float getData() {
		return data;
	}

	public void setData(float data) {
		this.data = data;
	}

	@Override
	public void exportToWurst(Appendable out) throws IOException {
		out.append("	u.setUnreal(\"");
		out.append(modificationId);
		out.append("\", "+data+")\n");
		
	}
	

	
	
}
