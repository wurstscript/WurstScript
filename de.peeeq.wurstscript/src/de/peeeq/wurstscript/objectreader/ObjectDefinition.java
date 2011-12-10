package de.peeeq.wurstscript.objectreader;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

public class ObjectDefinition {

	private String origObjectId;
	private String newObjectId;
	private List<ObjectModification> modifications = Lists.newLinkedList();

	public ObjectDefinition(String origObjectId, String newObjectId) {
		this.origObjectId =  origObjectId;
		this.newObjectId = newObjectId;
	}

	public void add(ObjectModification mod) {
		modifications.add(mod);
	}
	
	static ObjectDefinition readFromStream(BinaryDataInputStream in, ObjectFileType fileType) throws IOException {
		String origObjectId = in.readString(4);
		String newObjectId = in.readString(4);
		ObjectDefinition def = new ObjectDefinition(origObjectId, newObjectId);
		int numberOfModifications = in.readInt();
		for (int i = 0; i < numberOfModifications; i++) {
			ObjectModification mod = ObjectModification.readFromStream(in, fileType, origObjectId, newObjectId);
			def.add(mod);
		}
		return def;
	}

}
