package de.peeeq.wurstscript;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

import de.peeeq.wurstscript.antlr.WurstParser.CompilationUnitContext;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jurst.AntlrJurstParseTreeTransformer;
import de.peeeq.wurstscript.jurst.ExtendedJurstLexer;
import de.peeeq.wurstscript.jurst.antlr.JurstParser;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.parser.antlr.AntlrWurstParseTreeTransformer;
import de.peeeq.wurstscript.parser.antlr.ExtendedWurstLexer;
import de.peeeq.wurstscript.utils.LineOffsets;

public class WurstParser {

	private static final int MAX_SYNTAX_ERRORS = 10;
	private final ErrorHandler errorHandler;
	private final WurstGui gui;

	public WurstParser(ErrorHandler errorHandler, WurstGui gui) {
		this.errorHandler = errorHandler;
		this.gui = gui;
	}

	public CompilationUnit parse(Reader reader, String source, boolean hasCommonJ) {
		try (java.util.Scanner s = new java.util.Scanner(reader)) {
			s.useDelimiter("\\A");
			String input = s.hasNext() ? s.next() : "";
			CompilationUnit cu1 = parseWithAntlr(new StringReader(input), source, hasCommonJ);
			return cu1;
		}
	}


	private CompilationUnit parseWithAntlr(Reader reader, final String source, boolean hasCommonJ) {
		try {
			final ANTLRInputStream input = new ANTLRInputStream(reader);
			// create a lexer that feeds off of input CharStream
			final ExtendedWurstLexer lexer = new ExtendedWurstLexer(input);
			// create a buffer of tokens pulled from the lexer
			TokenStream tokens = new CommonTokenStream(lexer);
			// create a parser that feeds off the tokens buffer
			de.peeeq.wurstscript.antlr.WurstParser parser = new de.peeeq.wurstscript.antlr.WurstParser(tokens);
			ANTLRErrorListener l = new BaseErrorListener() {

				int errorCount = 0;
				
				@Override
				public void syntaxError(@SuppressWarnings("null") Recognizer<?, ?> recognizer, @SuppressWarnings("null") Object offendingSymbol, int line, int charPositionInLine,
						@SuppressWarnings("null") String msg, @SuppressWarnings("null") RecognitionException e) {
					
					LineOffsets offsets = lexer.getLineOffsets();
					int pos;
					int posStop;
					if (offendingSymbol instanceof Token) {
						Token token = (Token) offendingSymbol;
						pos = token.getStartIndex();
						posStop = token.getStopIndex()+1;
					} else {
						pos = offsets.get(line) + charPositionInLine;
						posStop = pos+1;
					}
					
//					msg = msg + " || " + input.getText(new Interval(pos, pos+10));
//					
//					msg = "line "+line+": "+ msg;
//					
//					if (recognizer instanceof Parser) {
//						List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
//						Collections.reverse(stack);
//						msg += "\nrule stack: "+stack;
//					}
					while (pos>0 && input.getText(new Interval(pos, posStop)).matches("\\s*")) {
						pos--;
					}
					CompileError err = new CompileError(new WPos(source, offsets, pos, posStop), msg);
					gui.sendError(err);
					
					errorCount++;
					if (errorCount > MAX_SYNTAX_ERRORS) {
						throw new TooManyErrorsException();
					}
				}

			};
			lexer.addErrorListener(l);
			parser.addErrorListener(l);

			CompilationUnitContext cu = parser.compilationUnit(); // begin parsing at init rule
			CompilationUnit root = new AntlrWurstParseTreeTransformer(source, errorHandler, lexer.getLineOffsets()).transform(cu);
			removeSyntacticSugar(root, hasCommonJ);
			return root;
			
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		} catch (TooManyErrorsException e) {
			WLogger.info("Stopped parsing file " + source + ", too many errors");
			return emptyCompilationUnit();
		}
	}



	public CompilationUnit parseJurst(Reader reader, String source, boolean hasCommonJ) {
		return parseJurstWithAntlr(reader, source, hasCommonJ);
	}

	private CompilationUnit parseJurstWithAntlr(Reader reader, final String source,	boolean hasCommonJ) {
		try {
			final ANTLRInputStream input = new ANTLRInputStream(reader);
			// create a lexer that feeds off of input CharStream
			final ExtendedJurstLexer lexer = new ExtendedJurstLexer(input);
			// create a buffer of tokens pulled from the lexer
			TokenStream tokens = new CommonTokenStream(lexer);
			// create a parser that feeds off the tokens buffer
			JurstParser parser = new JurstParser(tokens);
			ANTLRErrorListener l = new BaseErrorListener() {
				
				int errorCount = 0;

				@Override
				public void syntaxError(@SuppressWarnings("null") Recognizer<?, ?> recognizer, @SuppressWarnings("null") Object offendingSymbol, int line, int charPositionInLine,
						@SuppressWarnings("null") String msg, @SuppressWarnings("null") RecognitionException e) {
					
					LineOffsets offsets = lexer.getLineOffsets();
					int pos;
					int posStop;
					if (offendingSymbol instanceof Token) {
						Token token = (Token) offendingSymbol;
						pos = token.getStartIndex();
						posStop = token.getStopIndex()+1;
					} else {
						pos = offsets.get(line) + charPositionInLine;
						posStop = pos+1;
					}
					
					//msg = msg + " || " + input.getText(new Interval(pos, pos+10));
					
					msg = "line "+line+": "+ msg;
					
//					if (recognizer instanceof Parser) {
//						List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
//						Collections.reverse(stack);
//						msg += "\nrule stack: "+stack;
//					}
					while (pos>0 && input.getText(new Interval(pos, posStop)).matches("\\s*")) {
						pos--;
					}
					CompileError err = new CompileError(new WPos(source, offsets, pos, posStop), msg);
					gui.sendError(err);
					
					
					errorCount++;
					if (errorCount > MAX_SYNTAX_ERRORS) {
						throw new TooManyErrorsException();
					}
				}

			};
			lexer.addErrorListener(l);
			parser.addErrorListener(l);

			de.peeeq.wurstscript.jurst.antlr.JurstParser.CompilationUnitContext cu = parser.compilationUnit(); // begin parsing at init rule
			CompilationUnit root = new AntlrJurstParseTreeTransformer(source, errorHandler, lexer.getLineOffsets()).transform(cu);
			removeSyntacticSugar(root, hasCommonJ);
			return root;
			
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		} catch (TooManyErrorsException e) {
			WLogger.info("Stopped parsing file " + source + ", too many errors");
			return emptyCompilationUnit();
		}
	}


	public CompilationUnit emptyCompilationUnit() {
		return Ast.CompilationUnit("<empty compilation unit>", errorHandler, Ast.JassToplevelDeclarations(), Ast.WPackages());
	}

	private void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
		new SyntacticSugar().removeSyntacticSugar(root, hasCommonJ);
	}
	
	class TooManyErrorsException extends RuntimeException {}
}
