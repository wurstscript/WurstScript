package de.peeeq.pscript.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Scope {

	private Scope parent;
	
	private Map<String, Collection<Definition>> definitions = new HashMap<String, Collection<Definition>>();
	
	
	
	
	Collection<Definition> getDefinitions(String name) {
		return definitions.get(name);
	}
	
	void addDefinition(String name, Definition def) {
		Collection<Definition> d = definitions.get(name);
		if (d == null) {
			d = new LinkedList<Definition>();
			definitions.put(name, d);
		}
		d.add(def);
	}

	public Scope getParent() {
		return parent;
	}
	
	
	
}
