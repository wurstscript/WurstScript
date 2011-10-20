package de.peeeq.parseq;

import de.peeeq.parseq.ast.Alternative;
import de.peeeq.parseq.ast.AstBaseTypeDefinition;
import de.peeeq.parseq.ast.AstEntityDefinition;
import de.peeeq.parseq.ast.CaseDef;
import de.peeeq.parseq.ast.ConstructorDef;
import de.peeeq.parseq.ast.ListDef;
import de.peeeq.parseq.ast.Parameter;
import de.peeeq.parseq.ast.Program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class Generator {

	private Program prog;
	private String outputFolder;

	private Multimap<AstEntityDefinition, AstEntityDefinition> directSuperTypes = HashMultimap.create();
	private Multimap<AstEntityDefinition, AstEntityDefinition> directSubTypes = HashMultimap.create();
	private Multimap<CaseDef, AstBaseTypeDefinition> baseTypes =   HashMultimap.create();
	private Multimap<AstBaseTypeDefinition, CaseDef> interfaceTypes = HashMultimap.create();
	private String packageName;
	private String mainName;
	private List<File> oldFiles = new LinkedList<File>();

	public Generator(Program prog, String p_outputFolder) {
		System.out.println(prog);
		
		
		this.prog = prog;
		this.outputFolder = p_outputFolder;
		this.packageName = "";
		for (String pp : prog.getPackageParts()) {
			this.outputFolder += pp + "/";
			if (packageName.length() > 0) {
				packageName += ".";
			}
			packageName += pp;
			mainName = pp;
		}

		File file = new File(this.outputFolder);
		if (!file.exists()) {
			System.out.println("Creating directory " + file);
			file.mkdirs();
		} else {
			System.out.println("Output directory " + file + " already exists");

			for (File s : file.listFiles()) {
				if (s.toString().endsWith(".java")) {
					oldFiles.add(s);
				}
			}

		}

	}

	void generate() {
		System.out.println("calculating types ... ");
		calculateSubTypes();
		
		generateStandardClasses();
		generateStandardList();
		
		System.out.println("generating interfaces ... ");
		generateInterfaceTypes();
		
		System.out.println("generating base classes ... ");
		generateBaseClasses();
		
		System.out.println("generating list classes ...");
		generateLists();
		
		removeOldFiles();
		System.out.println("Done.");
	}

	private void generateStandardList() {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("public interface ParseqList<T> {\n");
		// TODO standard list methods
		sb.append("}\n");
		createFile("ParseqList", sb);
	}

	private void generateLists() {
		for (ListDef l : prog.listDefs) {
			generateList(l);
		}
	}

	private void generateList(ListDef l) {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("public interface " + l.name + " extends ParseqList<"+l.itemType+">, ");
		sb.append(getCommonSupertypeType());
		for (AstEntityDefinition supertype : directSuperTypes.get(l)) {
			sb.append(", ");
			sb.append(supertype.getName());
		}
		
		sb.append("{\n");

		// getParent method:
		sb.append("	" + getCommonSupertypeType() + " getParent();\n");
		
		// TODO standard list functions
		
		// TODO match functions

		sb.append("}\n");
		createFile(l.name, sb);
	}

	private void generateStandardClasses() {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		
		sb.append("public interface SortPos {\n" +
				"	SortPos getParent();" +
				"}\n\n");
		
		sb.append("interface SortPosIntern {\n" +
				"	void setParent(SortPos pos);\n" +
				"}\n\n");
		
		createFile("SortPos", sb);
		
		
		
	}

	private void removeOldFiles() {
		for (File old : oldFiles) {
			System.out.println("removing file: " + old);
			old.renameTo(new File(old.getAbsolutePath() + ".backup"));
		}
	}

	private void calculateSubTypes() {
		for (CaseDef caseDef : prog.caseDefs) {
			for (Alternative alt : caseDef.alternatives) {
				AstEntityDefinition subType = prog.getElement(alt.name);
				directSubTypes.put(caseDef, subType);
				directSuperTypes.put(subType, caseDef);
			}
		}
		// calculate base types of interfaces:
		for (CaseDef caseDef : prog.caseDefs) {
			caclulateCaseDefBaseTypes(caseDef);
		}
		
		// calculate interfaces for base types:
		for (CaseDef caseDef : prog.caseDefs) {
			for (AstBaseTypeDefinition base : baseTypes.get(caseDef)) {
				interfaceTypes.put(base, caseDef);
			}
		}
	}

	private void caclulateCaseDefBaseTypes(CaseDef caseDef) {
		if (baseTypes.containsKey(caseDef)) {
			return; // already calculated
		}
		for (AstEntityDefinition sub : directSubTypes.get(caseDef)) {
			if (sub instanceof AstBaseTypeDefinition) {
				baseTypes.put(caseDef, (AstBaseTypeDefinition) sub);
			} else if (sub instanceof CaseDef) {
				CaseDef caseDef2 = (CaseDef) sub;
				caclulateCaseDefBaseTypes(caseDef2);
				for (AstBaseTypeDefinition sub2 : baseTypes.get(caseDef2)) {
					baseTypes.put(caseDef, sub2);
				}
			}
		}
	}

	private void generateInterfaceTypes() {
		for (CaseDef caseDef : prog.caseDefs) {
			generateInterfaceType(caseDef);
		}
	}

	private void generateInterfaceType(CaseDef c) {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("public interface " + c.name + " extends ");
		sb.append(getCommonSupertypeType());
		for (AstEntityDefinition supertype : directSuperTypes.get(c)) {
			sb.append(", ");
			sb.append(supertype.getName());
		}
		
		sb.append("{\n");

		// getParent method:
		sb.append("	" + getCommonSupertypeType() + " getParent();\n");
		
		// calculate common attributes:
		Set<Parameter> attributes = calculateAttributes(c);
		
		// create getters and setters for parameters:
		for (Parameter p : attributes) {
			sb.append("	void set" + toFirstUpper(p.name) + "(" + p.typ + " " + p.name + ");\n");
			sb.append("	" + p.typ + " get" + toFirstUpper(p.name) + "();\n");
		}
		
		// create match methods:		
		sb.append("	<T> T match(Matcher<T> s);\n");
		sb.append("	void match(MatcherVoid s);\n");
		
		// create Matcher interface:
		sb.append("	public interface Matcher<T> {\n");
		for (AstBaseTypeDefinition baseType : baseTypes.get(c)) {
			sb.append("		T case_" + baseType.getName() + "(" + baseType.getName() + " " + toFirstLower(baseType.getName())+ ");\n");
		}
		sb.append("	}\n\n");

		// create MatchVoid interface:
		sb.append("	public interface MatcherVoid {\n");
		for (AstBaseTypeDefinition baseType : baseTypes.get(c)) {
			sb.append("		void case_" + baseType.getName() + "(" + baseType.getName() + " " + toFirstLower(baseType.getName())+ ");\n");
		}
		sb.append("	}\n\n");
		

		sb.append("}\n");
		createFile(c.name, sb);
	}

	

	private Set<Parameter> calculateAttributes(CaseDef c) {
		Set<Parameter> commonAttributes = null;
		for (AstBaseTypeDefinition base : baseTypes.get(c)) {
			if (base instanceof ConstructorDef) {
				ConstructorDef baseClass = (ConstructorDef) base;
				Set<Parameter> attributes = Sets.newHashSet();
				for (Parameter p : baseClass.parameters) {
					attributes.add(p);
				}
				if (commonAttributes == null) {
					commonAttributes = attributes;
				} else {
					commonAttributes = Sets.intersection(commonAttributes, attributes);
				}
			} else if (base instanceof ListDef) {
				return Sets.newHashSet();
			} else {
				throw new Error("Case not possible.");
			}
		}
		if (commonAttributes == null) {
			commonAttributes = Sets.newHashSet();
		}
		return commonAttributes;
	}

	private void generateBaseClasses() {
		for (ConstructorDef c : prog.constructorDefs) {
			generateBaseClass_Interface(c);
			generateBaseClass_Impl(c);
		}
	}

	private void generateBaseClass_Interface(ConstructorDef c) {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("public interface " + c.name + " extends ");
		sb.append(getCommonSupertypeType());
		for (AstEntityDefinition supertype : directSuperTypes.get(c)) {
			sb.append(", ");
			sb.append(supertype.getName());
		}
		
		sb.append(" {\n");

		// getParent method:
		sb.append("	" + getCommonSupertypeType() + " getParent();\n");

		// create getters and setters for parameters:
		for (Parameter p : c.parameters) {
			sb.append("	void set" + toFirstUpper(p.name) + "(" + p.typ + " " + p.name + ");\n");
			sb.append("	" + p.typ + " get" + toFirstUpper(p.name) + "();\n");
		}

		sb.append("}\n");
		createFile(c.name, sb);
	}

	private void generateBaseClass_Impl(ConstructorDef c) {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("class " + c.name + "Impl implements ");
		sb.append(c.name +  ", " + getCommonSupertypeType() + "Intern {\n");

		// create constructor
		sb.append("	" + c.name + "Impl(");
		boolean first = true;
		for (Parameter p : c.parameters) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(p.typ + " " + p.name);
			first = false;
		}
		sb.append(") {\n");
		for (Parameter p : c.parameters) {
			if (!JavaTypes.primitiveTypes.contains(p.typ)) { 
				// add null checks for non primitive types:
				sb.append("		if (" + p.name	+ " == null) throw new IllegalArgumentException();\n");
				if (!JavaTypes.otherTypes.contains(p.typ)) {
					// we have a generated type. 
					// the new element has a new parent:
					sb.append("		((SortPosIntern)" + p.name + ").setParent(this);\n");
				}
			}
			sb.append("		this." + p.name + " = " + p.name + ";\n");
		}
		sb.append("	}\n\n");
		

		// get/set parent method:
		sb.append("	private " + getCommonSupertypeType() + " parent;\n");
		sb.append("	public " + getCommonSupertypeType() + " getParent() { return parent; }\n");
		sb.append("	public void setParent(" + getCommonSupertypeType() + " parent) {\n" +
				"		if (parent != null && this.parent != null) { throw new Error(\"Parent already set.\"); }\n" +
				"		this.parent = parent;\n" +	
				"	}\n\n");

		// create getters and setters for parameters:
		for (Parameter p : c.parameters) {
			sb.append("	private " + p.typ + " " + p.name + ";\n");
			// setter:
			sb.append("	public void set" + toFirstUpper(p.name) + "(" + p.typ + " " + p.name + ") {\n");
			if (!JavaTypes.primitiveTypes.contains(p.typ)) { 
				// add null checks for non primitive types:
				sb.append("		if (" + p.name	+ " == null) throw new IllegalArgumentException();\n");
				if (!JavaTypes.otherTypes.contains(p.typ)) {
					// we have a generated type. 
					// the removed type looses its parent:
					sb.append("		((SortPosIntern)this." + p.name + ").setParent(null);\n");
					// the new element has a new parent:
					sb.append("		((SortPosIntern)" + p.name + ").setParent(this);\n");
				}
			}
			sb.append("		this." + p.name + " = " + p.name + ";\n" + "	} \n");
			// getter
			sb.append("	public " + p.typ + " get" + toFirstUpper(p.name) + "() { return " + p.name + "; }\n\n");
		}
		
		
		// create match methods
		for (CaseDef superType : interfaceTypes.get(c)) {
			sb.append("	@Override public <T> T match(" + superType.name + ".Matcher<T> matcher) {\n");
			sb.append("		return matcher.case_" + c.name + "(this);\n");
			sb.append("	}\n");
			
			sb.append("	@Override public void match(" + superType.name + ".MatcherVoid matcher) {\n");
			sb.append("		matcher.case_" + c.name + "(this);\n");
			sb.append("	}\n\n");
		}
		

		sb.append("}\n");
		createFile(c.name + "Impl", sb);
	}

	private String getCommonSupertypeType() {
		return "SortPos"; // TODO
	}

	private String toFirstUpper(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
	
	private String toFirstLower(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	private void createFile(String name, StringBuilder sb) {
		String filename = outputFolder + name + ".java";
		File file = new File(filename);

		boolean writeFile = false;
		if (file.exists()) {
			// file already exists -> compare contents
			try {
				String content = Files.toString(file, Charsets.UTF_8);
				if (!sb.toString().equals(content)) {
					// files differ, rewrite:
					writeFile = true;
				}
			} catch (IOException e) {
				abort("Error: Could not read file " + filename);
			}
		} else {
			// file does not exist -> write it
			writeFile = true;
		}
		
		
		if (writeFile) {
			System.out.println("writing file " + file);
			try {
				Files.write(sb, file, Charsets.UTF_8);
			} catch (IOException e) {
				abort("Error: Could not write file " + filename);
			}
		} else {
			System.out.println("not changed: " + file);
		}
		oldFiles.remove(file);
	}

	private void abort(String string) {
		System.err.println(string);
		System.exit(0);
	}

	private void printProlog(StringBuilder sb) {
		sb.append("package " + packageName + ";\n\n");
	}

}
