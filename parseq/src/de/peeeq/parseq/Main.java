package de.peeeq.parseq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import de.peeeq.parseq.asts.Generator;
import de.peeeq.parseq.asts.ast.Program;
import de.peeeq.parseq.asts.parser.ParseqAntlrParserLexer;
import de.peeeq.parseq.asts.parser.ParseqAntlrParserParser;
import de.peeeq.parseq.grammars.parser.GrammarsParserLexer;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GrammarFileeContext;

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
			
			
			Program prog = compileAstSpec(inputFile, outputFolder);
			
			String inputFileG = inputFile + ".g";
			if (new File(inputFileG).exists()) {
				compileGrammarSpec(inputFileG, outputFolder);
			} else {
				System.out.println("No Grammar file given for " + inputFileG);
			}
			
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(t.getMessage());
			System.exit(3);
		}
	}

	public static Program compileAstSpec(String inputFile, String outputFolder)
			throws IOException {
		
		ParseqAntlrParserLexer lexer = new ParseqAntlrParserLexer(new ANTLRFileStream(inputFile));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ParseqAntlrParserParser parser = new ParseqAntlrParserParser(tokens);
		
		ErrorListener errListener = new ErrorListener();
		parser.addErrorListener(errListener);
		
		Program prog = parser.spec().prog;
		
		
		if (errListener.getErrCount() > 0) {
			System.exit(1);
		}
		
		Generator gen = new Generator(prog, outputFolder);
		gen.generate();
		return prog;
	}
	
	public static void compileGrammarSpec(String inputFile, String outputFolder)
			throws IOException {
		
		GrammarsParserLexer lexer = new GrammarsParserLexer(new ANTLRFileStream(inputFile));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		GrammarsParserParser parser = new GrammarsParserParser(tokens);
		
		ErrorListener errListener = new ErrorListener();
		parser.addErrorListener(errListener);

		GrammarFileeContext f = parser.grammarFilee();
		System.out.println("GrammarFileContext: ");
		System.out.println(f.toStringTree(parser));
	}

}
