package de.peeeq.parseq.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.peeeq.parseq.Generator;
import de.peeeq.parseq.Symbol;
import de.peeeq.parseq.TokenType;
import de.peeeq.parseq.ast.Alternative;
import de.peeeq.parseq.ast.Parameter;
import de.peeeq.parseq.ast.Program;
import static de.peeeq.parseq.TokenType.*;

public class ParseqParser {
	private ParseqScanner scanner;
	private Program prog;
	// the current symbol:
	private Symbol sym;

	private boolean debug = false;
	private int debug_indent = 0;
	
	
	public ParseqParser(ParseqScanner scanner) {
		this.scanner = scanner;
		prog = new Program();
	}
	
	
	
	private void debug(String s) {
		if (debug) {
			if (s.startsWith("end")) {
				debug_indent--;
			}
			for (int i=0; i<=debug_indent; i++) {
				System.out.print("  ");
			}
			System.out.println(s);
			if (s.startsWith("start")) {
				debug_indent++;
			}
		}
	}
	
	
	public Program parse() {
		debug("parse");
		consume(TokenType.PACKAGE);
		getNextToken();
		parsePackageName();
		
		if (!(sym.typeEquals(TokenType.ABSTRACT_SYNTAX))) {
			abort("Unexpected " + sym + ". Expected 'abstract syntax:'.");
		}
		
		getNextToken();
		parseAbstractSyntaxRules();
		
		
		if (!(sym.typeEquals(TokenType.ATTRIBUTES))) {
			abort("Unexpected " + sym + ". Expected 'attributes:'.");
		}
		
		getNextToken();
		parseAttributes();
		
		
		
		if (!sym.typeEquals(TokenType.EOF)) {
			abort("Unexpected token " + sym + ". Expected end of file.");
		}
		
		return this.prog;		
	}


	private void parseAttributes() {
		while (sym.getType() == TokenType.IDENTIFIER) {
			parseAttribute();
		}		
	}



	private void parseAttribute() {
		debug("start parseAttribute");
		String typ = sym.getString();
		consume(DOT);
		consume(IDENTIFIER);
		String attr = sym.getString();
		getNextToken();
		String comment = ""; 
		if (sym.typeEquals(STRING_LITERAL)) {
			comment = sym.getString();
			consume(RETURNS);
		} else if (sym.typeEquals(RETURNS)) {
			
		} else {
			abort("Error at token " + sym + ". Expected String literal or 'returns'.");
		}
		getNextToken();
		String returns = join(parseQualifiedIdentifier(), ".");
		if (sym.typeEquals(LT)) {
			getNextToken();
			returns += "<" + join(parseCommaSeperatedQualifiedIdentifiers(), ", ") + ">";
			if (!sym.typeEquals(GT)) {
				abort("Unexpected " + sym + ". Expected `>`");
			}
			getNextToken();
		}		
		if (!sym.typeEquals(IMPLEMENTED_BY)) {
			abort("Unexpected " + sym + ". Expected `implemented by`");
		}
		getNextToken();
		String implementedBy = join(parseQualifiedIdentifier(), ".");
		prog.addAttribute(typ, attr, comment, returns, implementedBy);
		debug("end parseAttribute");
	}



	private List<String> parseCommaSeperatedQualifiedIdentifiers() {
		debug("start parseCommaSeperatedQualifiedIdentifiers");
		List<String> parts = new LinkedList<String>();
		loop: while (true) {
			List<String> ident = parseQualifiedIdentifier();
			parts.add(join(ident, "."));
//			getNextToken();
			switch (sym.getType()) {
				case COMMA:
					getNextToken();
					break;
				default:
					break loop;
			}
		}
		debug("end parseCommaSeperatedQualifiedIdentifiers");
		return parts;
	}



	private String join(List<String> list, String sep) {
		return Generator.join(list, sep);
	}

	
	private List<String> parseCommaSeperatedIdentifiers() {
		debug("start parseCommaSeperatedIdentifiers");
		List<String> parts = new LinkedList<String>();
		loop: while (true) {
			if (!sym.typeEquals(IDENTIFIER)) {
				abort("Error at token " + sym + ". Expected identifier.");
			}
			parts.add(sym.getString());
			getNextToken();
			switch (sym.getType()) {
				case COMMA:
					getNextToken();
					break;
				default:
					break loop;
			}
		}
		debug("end parseCommaSeperatedIdentifiers");
		return parts;
	}


	private List<String> parseQualifiedIdentifier() {
		debug("start parseQualifiedIdentifier");
		List<String> parts = new LinkedList<String>();
		loop: while (true) {
			if (!sym.typeEquals(IDENTIFIER)) {
				abort("Error at token " + sym + ". Expected identifier.");
			}
			parts.add(sym.getString());
			getNextToken();
			switch (sym.getType()) {
				case DOT:
					getNextToken();
					break;
				default:
					break loop;
			}
		}
		debug("end parseQualifiedIdentifier");
		return parts;
	}



	private void parsePackageName() {
		debug("start parsePackageName");
		List<String> parts = parseQualifiedIdentifier();
//		loop: while (true) {
//			consume(IDENTIFIER);
//			parts.add(sym.getString());
//			getNextToken();
//			switch (sym.getType()) {
//				case DOT:
//					break;
//				default:
//					break loop;
//			}
//		}
		prog.setPackage(parts);
		debug("end parsePackageName");
	}


	private void parseAbstractSyntaxRules() {
		debug("start parseAbstractSyntaxRules");
		while (true) {
			if (sym.typeEquals(TokenType.IDENTIFIER)) {
				parseAbstractSyntaxRule();
			} else {
				break;
			}
		}
		debug("end parseAbstractSyntaxRules");
	}


	private void parseAbstractSyntaxRule() {
		debug("start parseAbstractSyntaxRule");
		String left = sym.getString();
		getNextToken();
		switch (sym.getType()) {
			case LPAR:
				parseConstructor(left);
				break;
			case EQ:
				getNextToken();
				parseAlternatives(left);
				break;
			case STAR:
				parseListDef(left);
				break;
			default:
				abort("Unexpected " + sym + ". Expected equal sign or opening paranthesis.");
		}
		debug("end parseAbstractSyntaxRule");
	}


	private void parseListDef(String left) {
		debug("start parseListDef");
		getNextToken();
		if (sym.typeEquals(IDENTIFIER)) {
			prog.addListDef(left, sym.getString());
		}
		getNextToken();
		debug("end parseListDef");
	}


	private void parseAlternatives(String left) {
		debug("start parseAlternatives");
		List<Alternative> alternatives = new LinkedList<Alternative>();
		Alternative a = parseAlternative();
		alternatives.add(a);
		int i=0;
		while (true) {
			debug("parseAlternatives " + ++i);
			if (sym.typeEquals(PIPE)) {
				getNextToken();
				a = parseAlternative();
				alternatives.add(a);
			} else {
				break;
			}
//			getNextToken();
		}
		prog.addCaseDef(left, alternatives);
		debug("end parseAlternatives");
	}


	private Alternative parseAlternative() {
		debug("start parseAlternative");
		if (!sym.typeEquals(IDENTIFIER)) {
			abort("Expected identifier but found " + sym);
		}
		String name = sym.getString();
		getNextToken();
		if (sym.typeEquals(LPAR)) {
			parseConstructor(name);
			
		}
		debug("end parseAlternative");
		return new Alternative(name);
	}


	private void parseConstructor(String left) {
		debug("start parseConstructor");
		List<Parameter> parameters = new LinkedList<Parameter>();
		
		getNextToken(); //skip the left paranthesis
		loop: while (true) {
			switch (sym.getType()) {
				case RPAR:
					break loop;
				case IDENTIFIER:
					String typ = sym.getString();
					consume(IDENTIFIER);
					String name = sym.getString();
					parameters.add(new Parameter(typ, name));
					getNextToken();
					break;
				default:
			}
			switch (sym.getType()) {
				case RPAR:
					break loop;
				case COMMA:
					// expected, read next attr.
					break;
				default:
					abort("Unexpected " + sym + ". Expected comma or closing paranthesis.");					
			}
			getNextToken();
		}
		getNextToken();
		prog.addConstructor(left, parameters);
	}


	private void consume(TokenType expected) {
		getNextToken();
		if (!sym.typeEquals(expected)) {
			abort("Error at token " + sym + ". Expected " + expected);
		}
	}


	private void getNextToken() {
		try {
			sym = scanner.yylex();
			debug("sym = " + sym);
		} catch (IOException e) {
			abort("IO Error");
		}
	}


	private void abort(String msg) {
		System.out.println(msg);
		if (debug) {
			try {
				throw new Error();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
}
