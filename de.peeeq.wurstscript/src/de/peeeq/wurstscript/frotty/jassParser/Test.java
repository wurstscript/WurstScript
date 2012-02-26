package de.peeeq.wurstscript.frotty.jassParser;

import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.peeeq.wurstscript.jassAst.JassProg;
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
			JassProg prog = parser.file();
			
			// print the errors
			for (String err : parser.getErrors()) {
				System.out.println(err);
			}
			if (parser.getErrors().isEmpty()) {
				System.out.println("file OK!");
			}
			
			JassPrinter printer = new JassPrinter(false);
			CharSequence progText = printer.printProg(prog);
			System.out.println(progText);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
