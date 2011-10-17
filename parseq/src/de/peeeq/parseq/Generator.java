package de.peeeq.parseq;

import de.peeeq.parseq.ast.ConstructorDef;
import de.peeeq.parseq.ast.Parameter;
import de.peeeq.parseq.ast.Program;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Generator {

	private Program prog;

	public Generator(Program prog) {
		this.prog = prog;
	}
	
	void generate() {
		
		generateBaseClasses();
		generateInterfaceTypes();
	}

	private void generateInterfaceTypes() {
		// TODO
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
		sb.append("interface " + c.name + " extends ");
		sb.append(getBaseType() + " ");
		// TODO calculate base types
		sb.append("{\n");
		
		// getParent method:
		sb.append(getBaseType() + " getParent();");
		
		// create getters and setters for parameters:
		for (Parameter p : c.parameters) {
			sb.append("void set" + toFirstUpper(p.name) + "(" + p.typ + " " + p.name + ");\n");
			sb.append(p.typ + " get" + toFirstUpper(p.name) + "();\n");
		}
		
		sb.append("}\n");
		createFile(c.name, sb);
	}
	
	private void generateBaseClass_Impl(ConstructorDef c) {
		StringBuilder sb = new StringBuilder();
		printProlog(sb);
		sb.append("class " + c.name + "Impl implements ");
		sb.append(c.name + " {\n");
		
		// TODO create constructor
		
		// getParent method:
		sb.append("private " + getBaseType() + " parent;\n");
		sb.append(getBaseType() + " getParent() { return parent; }\n");
		
		// create getters and setters for parameters:
		for (Parameter p : c.parameters) {
			sb.append("private " + p.typ + " " + p.name +";\n");
			sb.append("void set" + toFirstUpper(p.name) + "(" + p.typ + " " + p.name + ") {" +
					"if ("+ p.name + " == null) throw new IllegalArgumentException();\n" +
					"this." + p.name + " = " + p.name + ";\n" + 
					"} \n");
			sb.append(p.typ + " get" + toFirstUpper(p.name) + "() { return " + p.name + "; }\n");
		}
		
		sb.append("}\n");
		createFile(c.name +"Impl", sb);
	}
	

	private String getBaseType() {
		return "SortPos"; // TODO
	}

	private String toFirstUpper(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private void createFile(String name, StringBuilder sb) {
		String filename = name + ".java";
		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(filename));
			writer.write(sb.toString());
		} catch (IOException e) {
			abort("Error: Could not write file "+ filename);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private void abort(String string) {
		System.err.println(string);
		System.exit(0);
	}

	private void printProlog(StringBuilder sb) {
		sb.append("package de.peeeq.test;\n\n");
	}
	
	
}
