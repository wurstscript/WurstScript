package de.peeeq.wurstscript.objectreader;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

public class ObjectTable {

	private List<ObjectDefinition> objectDefinitions = Lists.newArrayList();
	
	public void add(ObjectDefinition objDef) {
		objectDefinitions.add(objDef);
	}

	static ObjectTable readFromStream(BinaryDataInputStream in, ObjectFileType fileType) throws IOException {

		ObjectTable objectTable = new ObjectTable();

		int numberOfObjects = in.readInt();

		for (int i = 0; i < numberOfObjects; i++) {
			ObjectDefinition objDef = ObjectDefinition.readFromStream(in, fileType);
			objectTable.add(objDef);
		}

		return objectTable;
	}
	
	
	public List<ObjectDefinition> getObjectDefinitions() {
		return objectDefinitions;
	}

	public void writeToStream(BinaryDataOutputStream out, ObjectFileType fileType) throws IOException {
		
		// write number of objects
		out.writeInt(objectDefinitions.size());
		
		for (ObjectDefinition od : objectDefinitions) {
			od.writeToStream(out, fileType);
		}
		
	}

}
