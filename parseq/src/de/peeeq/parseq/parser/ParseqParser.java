package de.peeeq.parseq.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

	public ParseqParser(ParseqScanner scanner) {
		this.scanner = scanner;
		prog = new Program();
	}
	
	
	public Program parse() {
		consume(TokenType.PACKAGE);
		parsePackageName();
		
		if (!(sym.typeEquals(TokenType.ABSTRACT_SYNTAX))) {
			abort("Unexpected " + sym + ". Expected 'abstract syntax:'.");
		}
		
		parseAbstractSyntaxRules();
		
		if (!sym.typeEquals(TokenType.EOF)) {
			abort("Unexpected token " + sym + ". Expected end of file.");
		}
		
		return this.prog;		
	}


	private void parsePackageName() {
		List<String> parts = new LinkedList<String>();
		loop: while (true) {
			consume(IDENTIFIER);
			parts.add(sym.getString());
			getNextToken();
			switch (sym.getType()) {
				case DOT:
					break;
				default:
					break loop;
			}
		}
		prog.setPackage(parts);
	}


	private void parseAbstractSyntaxRules() {
		while (true) {
			getNextToken();
			if (sym.typeEquals(TokenType.IDENTIFIER)) {
				parseAbstractSyntaxRule();
			} else {
				break;
			}
		}
	}


	private void parseAbstractSyntaxRule() {
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
	}


	private void parseListDef(String left) {
		getNextToken();
		if (sym.typeEquals(IDENTIFIER)) {
			prog.addListDef(left, sym.getString());
		}
	}


	private void parseAlternatives(String left) {
		List<Alternative> alternatives = new LinkedList<Alternative>();
		Alternative a = parseAlternative();
		while (true) {
			if (sym.typeEquals(PIPE)) {
				getNextToken();
				a = parseAlternative();
				alternatives.add(a);
			} else {
				break;
			}
			getNextToken();
		}
		prog.addCaseDef(left, alternatives);
	}


	private Alternative parseAlternative() {
		if (!sym.typeEquals(IDENTIFIER)) {
			abort("Expected identifier but found " + sym);
		}
		String name = sym.getString();
		getNextToken();
		if (sym.typeEquals(LPAR)) {
			parseConstructor(name);
			getNextToken();
		}
		return new Alternative(name);
	}


	private void parseConstructor(String left) {
		List<Parameter> parameters = new LinkedList<Parameter>();
		loop: while (true) {
			getNextToken();
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
					break;
				default:
					abort("Unexpected " + sym + ". Expected comma or closing paranthesis.");					
			}
		}
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
		} catch (IOException e) {
			abort("IO Error");
		}
	}


	private void abort(String msg) {
		System.err.println(msg);
		System.exit(0);
	}
	
}
