package de.peeeq.wurstio.objectreader;

import java.io.IOException;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediateLang.interpreter.VariableType;

public abstract class ObjectModification<T> { // TODO split into appropiate subclasses

	protected String modificationId;
	

	protected int variableType;
	protected int levelCount;
	protected int dataPointer;
	protected ObjectDefinition parent;
	protected T data;

	public ObjectModification(ObjectDefinition parent, String modificationId, int variableType, int levelCount, int dataPointer, T data) {
		WLogger.info("parent " + parent + ", modificationId " + modificationId + ", variableType " + variableType + ", levelCount " + levelCount + ", dataPointer " + dataPointer + ", data " +  data);
		this.parent = parent;
		this.modificationId = modificationId;
		this.variableType = variableType;
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
		this.data = data;
	}
	
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setLevelData(int levelCount, int dataPointer) {
		this.levelCount = levelCount;
		this.dataPointer = dataPointer;
	}

	
	static ObjectModification<?> readFromStream(BinaryDataInputStream in, ObjectFileType fileType, ObjectDefinition parent) throws IOException {
		String modificationId = in.readString(4);

		int variableType = in.readInt();

		int levelCount = 0;
		int dataPointer = 0;
		if (fileType.usesLevels()) {
			levelCount = in.readInt();
			dataPointer = in.readInt();
		}

		
		
		ObjectModification<?> result;
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

		int end = in.readInt();
		int originalObjectId = parent.getOrigObjectId();
		int newObjectId = parent.getNewObjectId();
		// check the end value, according to spec:
		// int: end of modification structure (this is either 0, or equal to the original object ID or equal to 
		//		the new object ID of the current object, when reading files you can use this to check if the 
		//		format is correct, when writing a file you should use the new object ID of the current object here)
		if (end != 0 && end != originalObjectId && end != newObjectId) {
			// TODO should this be an error visible to the user?
			WLogger.warning("corrupt end value: " + end + ", " + end + ", expected " + originalObjectId
					+ " or " + newObjectId);
		}
		return result;
	}

	public final void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		out.writeString(modificationId, 4);
		out.writeInt(variableType);
		if (fileType.usesLevels()) {
			out.writeInt(levelCount);
			out.writeInt(dataPointer);
		}
		writeDataToStream(out, fileType);
		out.writeInt(parent.getNewObjectId());
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

	public void exportToWurst(Appendable out) throws IOException {
		if (parent.getFileType().usesLevels()) {
			out.append("	u.setLvlData"+getFuncPostfix()+"(\"");
			out.append(modificationId);
			out.append("\", "+levelCount+", "+dataPointer+", "+escapedData()+")\n");
		} else {
			out.append("	u.set"+getFuncPostfix()+"(\"");
			out.append(modificationId);
			out.append("\", "+escapedData()+")\n");
		}
	}


	protected String escapedData() {
		return data.toString();
	}


	protected abstract String getFuncPostfix();


	@SuppressWarnings("unchecked")
	public <K> ObjectModification<K> castTo(K val) {
		if (!data.getClass().equals(val.getClass())) {
			throw new Error("cannot cast from " + data.getClass() + " to " + val.getClass());
		}
		return (ObjectModification<K>) this;
	}

	
	@SuppressWarnings("unchecked")
	public static <T> ObjectModification<T> create(ObjectDefinition parent, String modificationId, VariableType<T> variableType2, int levelCount, int dataPointer, T value) {
		if (variableType2 == VariableType.INTEGER) {
			return (ObjectModification<T>) new ObjectModificationInt(parent, modificationId, levelCount, dataPointer, (Integer) value);
		} else if (variableType2 == VariableType.REAL) {
			return (ObjectModification<T>) new ObjectModificationReal(parent, modificationId, levelCount, dataPointer, (Float) value);
		} else if (variableType2 == VariableType.UNREAL) {
			return (ObjectModification<T>) new ObjectModificationUnreal(parent, modificationId, levelCount, dataPointer, (Float) value);
		} else if (variableType2 == VariableType.STRING) {
			return  (ObjectModification<T>) new ObjectModificationString(parent, modificationId, levelCount, dataPointer, (String) value);
		}
		throw new Error("unsupported vartype " + variableType2);
	}
}
