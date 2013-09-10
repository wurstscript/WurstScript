package de.peeeq.parseq.asts;

public class TemplateCyclicDependencyError {

	public static void writeTo(StringBuilder sb, String commonSupName) {
		println(sb, "public class CyclicDependencyError extends RuntimeException {");
		println(sb, "	private static final long serialVersionUID = 1L;");
		println(sb, "	private final "+commonSupName+" element;");
		println(sb, "	private final String attributeName;");
		println(sb, "	public CyclicDependencyError("+commonSupName+" element, String attributeName) {");
		println(sb, "		this.element = element;");
		println(sb, "		this.attributeName = attributeName;");
		println(sb, "	}");
		println(sb, "	public "+commonSupName+" getElement() {");
		println(sb, "		return element;");
		println(sb, "	}");
		println(sb, "	public String getAttributeName() {");
		println(sb, "		return attributeName;");
		println(sb, "	}");
		println(sb, "}");
	}

	private static void println(StringBuilder sb, String s) {
		sb.append(s);
		sb.append("\n");
	}

}
