package de.peeeq.wurstscript.objectreader;

import java.util.List;

import com.google.common.collect.Lists;

public class ObjectTable {

	private List<ObjectDefinition> objectDefinitions = Lists.newLinkedList();
	
	public void add(ObjectDefinition objDef) {
		objectDefinitions.add(objDef);
	}

}
