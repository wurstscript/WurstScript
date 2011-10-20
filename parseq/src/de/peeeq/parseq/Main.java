package de.peeeq.parseq;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.peeeq.parseq.ast.Program;
import de.peeeq.parseq.parser.ParseqParser;
import de.peeeq.parseq.parser.ParseqScanner;

public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		ParseqScanner scanner = new ParseqScanner(new FileInputStream("test.parseq"));
		ParseqParser parser = new ParseqParser(scanner);
		Program prog = parser.parse();
		Generator gen = new Generator(prog, "./src-gen/");
		gen.generate();
	}

}
