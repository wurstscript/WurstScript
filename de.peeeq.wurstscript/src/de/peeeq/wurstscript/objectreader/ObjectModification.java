package de.peeeq.wurstscript.objectreader;

import java.io.IOException;

public class ObjectModification {

	private String modificationId;
	private int variableType;
	private int levelCount;
	private int dataPointer;
	private int intData;
	private float floa;
	private String stringData;
	private float floatData;

	public ObjectModification(String modificationId, int variableType) {
		this.modificationId = modificationId;
		this.variableType = variableType;
	}

	public void setLevelData(int levelCount, int dataPointer) {
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	public void setIntData(int intData) {
		this.intData = intData;
	}

	public void setFloatData(float floatData) {
		this.floatData = floatData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
	}
	
	static ObjectModification readFromStream(BinaryDataInputStream in, ObjectFileType fileType, String originalObjectId, String newObjectId) throws IOException {
		String modificationId = in.readString(4);

		int variableType = in.readInt();

		ObjectModification result = new ObjectModification(modificationId, variableType);
		
		if (fileType.usesLevels()) {
			int levelCount = in.readInt();

			int dataPointer = in.readInt();
			
			result.setLevelData(levelCount, dataPointer);
		}

		
		
		switch (variableType) {
			case VariableTypes.INTEGER:
				int intData = in.readInt();
				result.setIntData(intData);
				break;
			case VariableTypes.REAL:
			case VariableTypes.UNREAL:
				float floatData = in.readFloat();
				result.setFloatData(floatData);
				break;
			case VariableTypes.STRING:
				String stringData = in.readNullTerminatedString();
				result.setStringData(stringData);
				break;
			default:
				throw new Error("unsupported vartype " + variableType);
		}

		String end = in.readString(4);
//		if (end.length() > 0 && !(end.charAt(0) == 0) && !end.equals(originalObjectId) && !end.equals(newObjectId)) {
//			throw new Error("corrupt end value: " + (int) end.charAt(0) + ", " + end + ", expected " + originalObjectId
//					+ " or " + newObjectId);
//		}
		return result;

		// if (end != 0 && end != originalObjectId && end != newObjectId) {
		// throw new Error("corrupt end value");
		// }
	}


}
