package de.peeeq.wurstscript.objectreader;

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

}
