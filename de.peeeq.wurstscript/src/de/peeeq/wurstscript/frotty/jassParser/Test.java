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
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassprinter.JassPrinter;

public class Test {
	public static void main(String ... args) {
		try {
			String inputFile = args[0];
			frottyjassLexer lexer = new frottyjassLexer(new ANTLRFileStream(inputFile));
			CommonTokenStream tokens = new CommonTokenStream();
			tokens.setTokenSource(lexer);
			frottyjassParser parser = new frottyjassParser(tokens);
			// read/parse a file
			
			JassProg prog = null;
			try {
				prog = parser.file();
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
			
			File outputFile = new File("frottyJassTest.j");
			
			StringBuilder sb = new StringBuilder();
			new JassPrinter(true).printProg(sb, prog);
			Files.write(sb.toString(), outputFile, Charsets.UTF_8);

			// run pjass:
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
}
