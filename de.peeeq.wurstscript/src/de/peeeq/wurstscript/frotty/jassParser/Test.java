package de.peeeq.wurstscript.frotty.jassParser;

import java.io.File;
import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.frotty.jassValidator.JassValidator;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassProgs;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class Test {
	
	static frottyjassParser parser = new frottyjassParser(null);
	
	public static void main(String ... args) {
		try {
			String inputFile = args[0];
			
			// read/parse a file
			JassProgs progs = JassAst.JassProgs();
			JassProg prog = null;
			
			//common.j+blizzard.j
			try {
				prog = parseFile("C:/Users/Frotty/Documents/WurstScript/de.peeeq.wurstscript/src/de/peeeq/wurstscript/frotty/jassParser/common.j");
				System.out.println("common.j parsed...");
				File common = new File("common_f.j");
				
				StringBuilder sb = new StringBuilder();
				new JassPrinter(true).printProg(sb, prog);
				Files.write(sb.toString(), common, Charsets.UTF_8);
				progs.add( prog );
				prog = parseFile("C:/Users/Frotty/Documents/WurstScript/de.peeeq.wurstscript/src/de/peeeq/wurstscript/frotty/jassParser/Blizzard.j");
				System.out.println("Blizzard.j parsed...");
				File bliz = new File("bliz_f.j");
				
				sb = new StringBuilder();
				new JassPrinter(true).printProg(sb, prog);
				Files.write(sb.toString(), bliz, Charsets.UTF_8);
				progs.add( prog );
				prog = parseFile(inputFile);
				progs.add( prog );
			} catch (Throwable t) {
				prog = null;
				t.printStackTrace();
			}
			
			// print the errors
			for (String err : parser.getErrors()) {
				System.out.println(err);
			}
			if (parser.getErrors().isEmpty()) {
				System.out.println("file OK!");
			} else {
				return;
			}
			
			/// check prog
			System.out.println("Validating");
			prog.validate();
			
			if (JassErrors.errorCount() > 0) {
				for (String err : JassErrors.getErrors()) {
					System.out.println(err);
				}
				return;
			}
			
			File outputFile = new File("frottyJassTest.j");
			
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);

			// run pjass:
			System.out.println("Pjass...");
			Result pJassResult = Pjass.runPjass(outputFile);
			System.out.println(pJassResult.getMessage());
			if (!pJassResult.isOk()) {
				throw new TestFailException(pJassResult.getMessage());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static JassProg parseFile(String inputFile) throws IOException, RecognitionException {
		frottyjassLexer lexer = new frottyjassLexer(new ANTLRFileStream(inputFile));
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		parser.setTokenStream(tokens);
		return parser.file();
	}
}
