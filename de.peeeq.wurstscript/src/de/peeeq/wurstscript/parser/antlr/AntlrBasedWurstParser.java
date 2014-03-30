package de.peeeq.wurstscript.parser.antlr;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import de.peeeq.wurstscript.antlr.WurstBaseVisitor;
import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.antlr.WurstParser.CompilationUnitContext;
import de.peeeq.wurstscript.antlr.WurstParser.EntityContext;
import de.peeeq.wurstscript.antlr.WurstParser.EnumDefContext;
import de.peeeq.wurstscript.utils.Utils;

public class AntlrBasedWurstParser {

	
	public static void main(String[] args) throws IOException {
		String test = Utils.join(new String[] {
				"package Foo",
				"enum Bla",
				"	// hi",
				"	Abc",
				"	Def",	
		}, "\n");
		// create a CharStream that reads from standard input
		ANTLRInputStream input = new ANTLRInputStream(test);
		// create a lexer that feeds off of input CharStream
		TokenSource lexer = new ExtendedWurstLexer(input);
		// create a buffer of tokens pulled from the lexer
		TokenStream tokens = new CommonTokenStream(lexer);
		// create a parser that feeds off the tokens buffer
		WurstParser parser = new WurstParser(tokens);
		CompilationUnitContext cu = parser.compilationUnit(); // begin parsing at init rule
		System.out.println(cu.toStringTree(parser)); // print LISP-style tree
	}
	
}
