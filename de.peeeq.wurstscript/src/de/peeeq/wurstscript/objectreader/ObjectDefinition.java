package de.peeeq.wurstscript.objectreader;

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

}
