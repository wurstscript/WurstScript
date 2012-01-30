package de.peeeq.parseq.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.Token;

public class Program {

	
	public final List<ListDef> listDefs = new LinkedList<ListDef>();
	public final List<CaseDef> caseDefs = new LinkedList<CaseDef>();
	public final List<ConstructorDef> constructorDefs = new LinkedList<ConstructorDef>();
	public final List<AttributeDef> attrDefs = new LinkedList<AttributeDef>();
	public final Map<String, AstEntityDefinition> definitions = new HashMap<String, AstEntityDefinition>(); 
	private String packageName;
	

	public Program(String packageName) {
		this.packageName = packageName;
	}

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
		int pos = packageName.lastIndexOf('.');
		if (pos >= 0) {
			return packageName.substring(pos+1);
		} else {
			return packageName;
		}
	}

	public boolean hasElement(String e) {
		return definitions.containsKey(e);
	}


	public void addListDef(ListDef listDef) {
		listDefs.add(listDef);
	}

	public void addCaseDef(CaseDef caseDef) {
		caseDefs.add(caseDef);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void addAttribute(List<Parameter> parameters, String typ, String attr, String returnType, String implementedBy, Token doc) {
		String docStr = doc != null ? doc.getText() : "";
		attrDefs.add(new AttributeDef(parameters, typ, attr, docStr, returnType, implementedBy));
	}

	public void addConstructorDef(ConstructorDef c) {
		constructorDefs.add(c);
	}

	
}
