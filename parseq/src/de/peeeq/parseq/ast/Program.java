package de.peeeq.parseq.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Program {

	
	public final List<ListDef> listDefs = new LinkedList<ListDef>();
	public final List<CaseDef> caseDefs = new LinkedList<CaseDef>();
	public final List<ConstructorDef> constructorDefs = new LinkedList<ConstructorDef>();
	public final Map<String, AstEntityDefinition> definitions = new HashMap<String, AstEntityDefinition>(); 
	private List<String> packageParts;
	
	public void addListDef(String name, String itemType) {
		if (definitions.containsKey(name)) {
			throw new Error("Name "+ name + " redefined.");
		}
		ListDef def = new ListDef(name, itemType);
		listDefs.add(def);
		definitions.put(name, def);
	}

	public void addCaseDef(String name, List<Alternative> alternatives) {
		if (definitions.containsKey(name)) {
			throw new Error("Name "+ name + " redefined.");
		}
		CaseDef def = new CaseDef(name, alternatives);
		caseDefs.add(def);
		definitions.put(name, def);
	}

	public void addConstructor(String name, List<Parameter> parameters) {
		if (definitions.containsKey(name)) {
			throw new Error("Name "+ name + " redefined.");
		}
		ConstructorDef def = new ConstructorDef(name, parameters);
		constructorDefs.add(def);
		definitions.put(name, def);
	}

	public void setPackage(List<String> parts) {
		this.packageParts = parts;
	}

	public List<String> getPackageParts() {
		return packageParts;
	}

	public AstEntityDefinition getElement(String sub) {
		return definitions.get(sub);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nCase Types: \n");
		for (CaseDef x : caseDefs) {
			sb.append(x + "\n");
		}
		sb.append("\nConstructors: \n");
		for (ConstructorDef x : constructorDefs) {
			sb.append(x + "\n");
		}
		sb.append("\nLists: \n");
		for (ListDef x : listDefs) {
			sb.append(x + "\n");
		}
		
		return sb.toString();
	}

	public String getLastPackagePart() {
		return packageParts.get(packageParts.size()-1);
	}

	public boolean hasElement(String e) {
		return definitions.containsKey(e);
	}
	
}
