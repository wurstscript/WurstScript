package de.peeeq.parseq;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import de.peeeq.parseq.ast.Program;
import de.peeeq.parseq.parser.ParseqAntlrParserLexer;
import de.peeeq.parseq.parser.ParseqAntlrParserParser;
import de.peeeq.parseq.parser.ParseqScanner;

public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		try {
			if (args.length != 2) {
				System.out.println("2 parameters required.");
				System.exit(2);
				return;
			}
			String inputFile = args[0];
			String outputFolder = args[1];
			
			//			ParseqScanner scanner = new ParseqScanner(new FileInputStream(inputFile));
//			ParseqParser parser = new ParseqParser(scanner);
//			Program prog = parser.parse();
			ParseqAntlrParserLexer lexer = new ParseqAntlrParserLexer(new ANTLRFileStream(inputFile));
			CommonTokenStream tokens = new CommonTokenStream();
			tokens.setTokenSource(lexer);
			ParseqAntlrParserParser parser = new ParseqAntlrParserParser(tokens);
			Program prog = parser.spec();
			for (String error : parser.getErrors()) {
				System.err.println(error);
			}
			if (parser.getErrors().size() > 0) {
				return;
			}
			Generator gen = new Generator(prog, outputFolder);
			gen.generate();
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(t.getMessage());
			System.exit(3);
		}
	}

}
