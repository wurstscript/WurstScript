package de.peeeq.wurstscript.objectreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.peeeq.wurstscript.WLogger;

public class ObjectFile {

	private ObjectFileType fileType;

	public ObjectFile(File file, ObjectFileType fileType) {
		this.fileType = fileType;
		FileInputStream fis = null;
		try {
			BinaryDataInputStream in = new BinaryDataInputStream(file, true);

			int version = in.readInt();
			System.out.println("version = " + version);

			ObjectTable origTable = readTableDefinition(in);
			ObjectTable modifiedTable = readTableDefinition(in);

			// StructUnpacker up = JavaStruct.getUnpacker(fis,
			// ByteOrder.LITTLE_ENDIAN);
			// StructHeader header = new StructHeader();
			// up.readObject(header);
			// System.out.println("x = " + header.fileVersion);
			//
			// StructTableDefinition tableDefinition = new
			// StructTableDefinition();
			// up.readObject(tableDefinition);
			// System.out.println("#objs = " + tableDefinition.numberOfObjects);
			//
			//
			// for (int i=0; i<tableDefinition.numberOfObjects; i++) {
			//
			// }

		} catch (FileNotFoundException e) {
			WLogger.severe(e);
			throw new Error(e);
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new Error(e);
				}
			}
		}

	}

	private ObjectTable readTableDefinition(BinaryDataInputStream in) throws IOException {

		ObjectTable objectTable = new ObjectTable();

		int numberOfObjects = in.readInt();
		System.out.println("	numberOfObjects = " + numberOfObjects);

		for (int i = 0; i < numberOfObjects; i++) {
			ObjectDefinition objDef = readObjectDefinition(in);
			objectTable.add(objDef);
		}

		return objectTable;
	}

	private ObjectDefinition readObjectDefinition(BinaryDataInputStream in) throws IOException {
		
		
		String origObjectId = in.readString(4);
		System.out.println("		origObjectId = " + origObjectId);

		String newObjectId = in.readString(4);
		System.out.println("		newObjectId = " + newObjectId);

		ObjectDefinition def = new ObjectDefinition(origObjectId, newObjectId);
		
		int numberOfModifications = in.readInt();
		System.out.println("		numberOfModifications = " + numberOfModifications);

		for (int i = 0; i < numberOfModifications; i++) {
			ObjectModification mod = readModificationStructure(in);
			def.add(mod);
		}
		
		return def;
	}

	private ObjectModification readModificationStructure(BinaryDataInputStream in) throws IOException {
		String modificationId = in.readString(4);
		System.out.println("		modificationId = " + modificationId);

		int variableType = in.readInt();
		System.out.println("			variableType = " + variableType);

		ObjectModification result = new ObjectModification(modificationId, variableType);
		
		if (fileType.usesLevels()) {
			int levelCount = in.readInt();
			System.out.println("			levelCount = " + levelCount);

			int dataPointer = in.readInt();
			System.out.println("			dataPointer = " + dataPointer);
			
			result.setLevelData(levelCount, dataPointer);
		}

		
		
		switch (variableType) {
			case VariableTypes.INTEGER:
				int intData = in.readInt();
				result.setIntData(intData);
				System.out.println("			intData = " + intData);
				break;
			case VariableTypes.REAL:
			case VariableTypes.UNREAL:
				float floatData = in.readFloat();
				result.setFloatData(floatData);
				System.out.println("			floatData = " + floatData);
				break;
			case VariableTypes.STRING:
				String stringData = in.readNullTerminatedString();
				result.setStringData(stringData);
				System.out.println("			stringData = " + stringData);
				break;
			default:
				throw new Error("unsupported vartype " + variableType);
		}

		String end = in.readString(4);
		System.out.println("		end = " + end);
		
		return result;

		// if (end != 0 && end != originalObjectId && end != newObjectId) {
		// throw new Error("corrupt end value");
		// }
	}

}
