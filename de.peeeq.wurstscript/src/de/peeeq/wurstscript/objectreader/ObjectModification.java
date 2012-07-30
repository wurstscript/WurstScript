package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public abstract class ObjectModification { // TODO split into appropiate subclasses

	protected String modificationId;
	protected int variableType;
	protected int levelCount;
	protected int dataPointer;
	private String originalObjectId;
	private String newObjectId;

	public ObjectModification(String originalObjectId, String newObjectId, String modificationId, int variableType, int levelCount, int dataPointer) {
		this.originalObjectId = originalObjectId;
		this.newObjectId = newObjectId;
		this.modificationId = modificationId;
		this.variableType = variableType;
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	public void setLevelData(int levelCount, int dataPointer) {
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	
	static ObjectModification readFromStream(BinaryDataInputStream in, ObjectFileType fileType, String originalObjectId, String newObjectId) throws IOException {
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
				result = new ObjectModificationInt(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer, intData);
				break;
			case VariableTypes.REAL:
				float floatData = in.readFloat();
				result = new ObjectModificationUnreal(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer, floatData);
				break;
			case VariableTypes.UNREAL:
				float floatData2 = in.readFloat();
				result = new ObjectModificationUnreal(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer, floatData2);
				break;
			case VariableTypes.STRING:
				String stringData = in.readNullTerminatedString();
				result = new ObjectModificationString(originalObjectId, newObjectId, modificationId, variableType, levelCount, dataPointer, stringData);
				break;
			default:
				throw new Error("unsupported vartype " + variableType);
		}

		String end = in.readString(4);
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
		out.writeString(newObjectId, 4);
	}
	
	abstract void writeDataToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException; 

}
