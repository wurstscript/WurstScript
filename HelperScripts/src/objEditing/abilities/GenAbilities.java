package objEditing.abilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import utils.AbilityNames;
import utils.WEStrings;

import com.csvreader.CsvReader;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class GenAbilities {
	static WEStrings strings = new WEStrings().parseFile(new File("./WorldEditStrings.txt"));
	static StringBuilder sb = new StringBuilder();
	static AbilityNames abilityNames = new AbilityNames(new File("./abilityNames.txt"));
	
	static void println(String s)  {
		sb.append(s);
		sb.append("\n");
	}
	static void print(String s)  {
		sb.append(s);
	}
	
	
	static class FieldData {
		String id; 
		String displayName;
		String type;
		int data;
		boolean useLevels;
		
		public FieldData(String id, String displayName, String type, int data,
				boolean useLevels) {
			this.id = id;
			this.displayName = displayName;
			this.type = type;
			this.data = data;
			this.useLevels = useLevels;
		}

		public void printFunc(Set<String> usedFuncs) {
			println("");
			String funcName = camelize(displayName);
			int i=0;
			if (usedFuncs.contains(funcName)) {
				do {
					i++;
				} while(usedFuncs.contains(funcName + i));
				funcName = funcName + i;
			}
			usedFuncs.add(funcName);
			
			print("	function set" + funcName + "(");
			if (useLevels) {
				print("int level, ");
			}
			print(type() + " value)");
			println("");
			print("		def.set");
//			if (useLevels) {
				print("LvlData");
//			}
			print(typePost());
			print("(\""+id+"\", ");
			if (useLevels) {
				print("level, " + data + ", ");
			} else {
				print("0, " + data + ", ");
			}
			println("value)");
			
			
		}

		private String type() {
			switch (type) {
			case "string": return "string";
			case "bool": return "bool";
			case "int": return "int";
			case "real": return "real";
			case "unreal": return "real";
			}
			return "string";
		}
		
		private String typePost() {
			switch (type) {
			case "string": return "String";
			case "bool": return "Boolean";
			case "int": return "Int";
			case "real": return "Real";
			case "unreal": return "Unreal";
			}
			return "String";
		}
		
		
	}
	
	
	public static void main(String[] args) throws IOException {
		List<FieldData> commonData = Lists.newArrayList();
		Multimap<String, FieldData> specificData = HashMultimap.create();
		
		
		File metaData = new File("./AbilityMetaData.csv");
		CsvReader reader = new CsvReader(new FileReader(metaData));
		reader.readHeaders();
		while (reader.readRecord()) {
			String id = reader.get("s");
			String displayName = strings.get(reader.get("displayName"));
			if (displayName == null) {
				displayName = reader.get("field");
			}
			String type = reader.get("type");
			int data = Integer.parseInt(reader.get("data"));
			boolean useLevels = !reader.get("repeat").equals("0");
			String useSpecific = reader.get("useSpecific");
			
			FieldData fd = new FieldData(id, displayName, type, data, useLevels);
			
			if (useSpecific.isEmpty()) {
				commonData.add(fd);
			} else {
				for (String spell : useSpecific.split("[,\\.]")) {
					specificData.put(spell, fd);
				}
			}
		}
		
		
		
		println("package AbilityObjEditing");
		println("import public ObjEditingNatives");
		println("");
		println("public class AbilityDefinition");
		println("	protected ObjectDefinition def");
		println("	");
		println("	construct(string newAbilityId, string origAbilityId)");
		println("		def = createObjectDefinition(\"w3a\", newAbilityId, origAbilityId)");
		
		Set<String> usedNames = Sets.newHashSet();
		for (FieldData fd : commonData) {
			fd.printFunc(usedNames);
		}
		
		
		for (String spell : specificData.keySet()) {
			usedNames.clear();
			println("");
			println("");
			println("");
			String spellName = abilityNames.get(spell);
			println("public class AbilityDefinition"+spellName+" extends AbilityDefinition");
			println("	construct(string newAbilityId)");
			println("		super(newAbilityId, \""+spell+"\")");
			for (FieldData fd : specificData.get(spell)) {
				fd.printFunc(usedNames);
			}
		}
		
		System.out.println(sb.toString());
		Files.write(sb, new File("./AbilityObjEditing.wurst"), Charsets.UTF_8);
		
	}
	public static String camelize(String displayName) {
		return displayName.replaceAll("[^a-zA-Z]", "");
//		StringBuilder s = new StringBuilder();
//		for (String part : displayName.split(" ")) {
//			s.append(part); 
//		}
//		return s.toString();
	}
	

}
