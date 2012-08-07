package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public abstract class ObjectModification { // TODO split into appropiate subclasses

	protected String modificationId;
	

	protected int variableType;
	protected int levelCount;
	protected int dataPointer;
	protected ObjectDefinition parent;

	public ObjectModification(ObjectDefinition parent, String modificationId, int variableType, int levelCount, int dataPointer) {
		this.parent = parent;
		this.modificationId = modificationId;
		this.variableType = variableType;
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	public void setLevelData(int levelCount, int dataPointer) {
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	
	static ObjectModification readFromStream(BinaryDataInputStream in, ObjectFileType fileType, ObjectDefinition parent) throws IOException {
		String modificationId = in.readString(4);

		int variableType = in.readInt();

		int levelCount = 0;
		int dataPointer = 0;
		if (fileType.usesLevels()) {
			levelCount = in.readInt();
			dataPointer = in.readInt();
		}

		
		
		ObjectModification result;
		switch (variableType) {
			case VariableTypes.INTEGER:
				int intData = in.readInt();
				result = new ObjectModificationInt(parent, modificationId, levelCount, dataPointer, intData);
				break;
			case VariableTypes.REAL:
				float floatData = in.readFloat();
				result = new ObjectModificationReal(parent, modificationId, levelCount, dataPointer, floatData);
				break;
			case VariableTypes.UNREAL:
				float floatData2 = in.readFloat();
				result = new ObjectModificationUnreal(parent, modificationId, levelCount, dataPointer, floatData2);
				break;
			case VariableTypes.STRING:
				String stringData = in.readNullTerminatedString();
				result = new ObjectModificationString(parent, modificationId, levelCount, dataPointer, stringData);
				break;
			default:
				throw new Error("unsupported vartype " + variableType);
		}

		String end = in.readString(4);
		String originalObjectId = parent.getOrigObjectId();
		Object newObjectId = parent.getNewObjectId();
		if (end.length() > 0 && !(end.charAt(0) == 0) && !end.equals(originalObjectId) && !end.equals(newObjectId)) {
			throw new Error("corrupt end value: " + (int) end.charAt(0) + ", " + end + ", expected " + originalObjectId
					+ " or " + newObjectId);
		}
		return result;

		// if (end != 0 && end != originalObjectId && end != newObjectId) {
		// throw new Error("corrupt end value");
		// }
	}

	public final void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeString(modificationId, 4);
		out.writeInt(variableType);
		if (fileType.usesLevels()) {
			out.writeInt(levelCount);
			out.writeInt(dataPointer);
		}
		writeDataToStream(out, fileType);
		out.writeString(parent.getNewObjectId(), 4);
	}

	
	public int getLevelCount() {
		return levelCount;
	}

	public void setLevelCount(int levelCount) {
		this.levelCount = levelCount;
	}

	public int getDataPointer() {
		return dataPointer;
	}

	public void setDataPointer(int dataPointer) {
		this.dataPointer = dataPointer;
	}

	public String getModificationId() {
		return modificationId;
	}
	
	abstract void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException; 

}
