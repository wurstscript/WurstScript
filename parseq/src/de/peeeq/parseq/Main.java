package de.peeeq.parseq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import de.peeeq.parseq.asts.FileGenerator;
import de.peeeq.parseq.asts.Generator;
import de.peeeq.parseq.asts.ast.Program;
import de.peeeq.parseq.asts.parser.ParseqAntlrParserLexer;
import de.peeeq.parseq.asts.parser.ParseqAntlrParserParser;
import de.peeeq.parseq.grammars.GrammarTranslation;
import de.peeeq.parseq.grammars.parser.GrammarsParserLexer;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GrammarFileContext;

public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		try {
			if (args.length != 2) {
				System.out.println("2 parameters required.");
				System.out.println("parameter 1: input file");
				System.out.println("parameter 2: output folder");
				System.exit(2);
				return;
			}
			String inputFile = args[0];
			String outputFolder = args[1];
			
			//			ParseqScanner scanner = new ParseqScanner(new FileInputStream(inputFile));
//			ParseqParser parser = new ParseqParser(scanner);
//			Program prog = parser.parse();
			
			
			Program prog = compileAstSpec(inputFile, outputFolder);
			
			File out = new File(outputFolder, prog.getPackageName().replace('.', '/') + '/');
			
			FileGenerator fileGenerator = new FileGenerator(out);
			Generator gen = new Generator(fileGenerator, prog, outputFolder);
			gen.generate();
			
			
			String inputFileG = inputFile + ".g";
			if (new File(inputFileG).exists()) {
				compileGrammarSpec(fileGenerator, inputFileG, prog);
			} else {
				System.err.println("No Grammar file given for " + inputFileG);
			}
			fileGenerator.removeOldFiles();
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println(t.getMessage());
			System.exit(3);
		}
	}

	public static Program compileAstSpec(String inputFile, String outputFolder)
			throws IOException {
		System.out.println(new File(inputFile).getAbsolutePath());
		ParseqAntlrParserLexer lexer = new ParseqAntlrParserLexer(new ANTLRFileStream(inputFile));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ParseqAntlrParserParser parser = new ParseqAntlrParserParser(tokens);
		
		ErrorListener errListener = new ErrorListener();
		parser.addErrorListener(errListener);
		
		Program prog = parser.spec().prog;
		
		if (errListener.getErrCount() > 0) {
			System.exit(1);
		}
		return prog;
	}
	
	public static void compileGrammarSpec(FileGenerator fileGenerator, String grammarFile, Program prog)
			throws IOException {
		
		GrammarsParserLexer lexer = new GrammarsParserLexer(new ANTLRFileStream(grammarFile));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		GrammarsParserParser parser = new GrammarsParserParser(tokens);
		
		ErrorListener errListener = new ErrorListener();
		parser.addErrorListener(errListener);

		GrammarFileContext f = parser.grammarFile();
		f.result.program = prog;
		
		new GrammarTranslation(fileGenerator, f.result, prog).translate();
		System.out.println("GrammarFileContext: ");
		System.out.println(f.toStringTree(parser));
	}

}
