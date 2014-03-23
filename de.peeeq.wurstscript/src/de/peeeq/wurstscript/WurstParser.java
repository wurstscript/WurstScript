package de.peeeq.wurstscript;

import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import java_cup.runtime.Symbol;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.Interval;

import de.peeeq.wurstscript.antlr.WurstParser.CompilationUnitContext;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.JurstExtendedParser;
import de.peeeq.wurstscript.parser.JurstScanner;
import de.peeeq.wurstscript.parser.ScannerError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.parser.antlr.AntlrWurstParseTreeTransformer;
import de.peeeq.wurstscript.parser.antlr.ExtendedWurstLexer;
import de.peeeq.wurstscript.utils.LineOffsets;

public class WurstParser {

	private final ErrorHandler errorHandler;
	private final WurstGui gui;
	private static boolean useCup = true;

	public WurstParser(ErrorHandler errorHandler, WurstGui gui) {
		this.errorHandler = errorHandler;
		this.gui = gui;
	}

	public CompilationUnit parse(Reader reader, String source, boolean hasCommonJ) {
		if (useCup || source.endsWith(".j")) {
			return parseWithCup(reader, source, hasCommonJ);
		} else {
			return parseWithAntlr(reader, source, hasCommonJ);
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
			parser.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
						String msg, RecognitionException e) {
					LineOffsets offsets = lexer.getLineOffsets();
					int pos = offsets.get(line) + charPositionInLine;
					//msg = msg + " || " + input.getText(new Interval(pos, pos+10));
					
					List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
					Collections.reverse(stack);
					msg += "\nrule stack: "+stack;
					msg = "line "+line+":"+charPositionInLine+" at "+
					offendingSymbol+": "+ msg;
					
					gui.sendError(new CompileError(new WPos(source, offsets, pos, pos+1), msg));
				}

			});

			CompilationUnitContext cu = parser.compilationUnit(); // begin parsing at init rule
			CompilationUnit root = new AntlrWurstParseTreeTransformer(source, errorHandler, lexer.getLineOffsets()).transform(cu);
			removeSyntacticSugar(root, hasCommonJ);
			return root;
			
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		}
	}

	private CompilationUnit parseWithCup(Reader reader, String source,
			boolean hasCommonJ) throws Error {
		try {
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner, errorHandler);
			parser.setFilename(source);
			Symbol sym = parser.parse();

			if (sym.value instanceof CompilationUnit) {
				CompilationUnit root = (CompilationUnit) sym.value;
				removeSyntacticSugar(root, hasCommonJ);
				WPos p = root.attrErrorPos();
				p = p.withFile(source);
				for (ScannerError err : scanner.getErrors()) {
					CompileError ce = err.makeCompilerError(p);
					gui.sendError(ce);
				}
				return root;
			}
			return emptyCompilationUnit();
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (Exception e) {
			gui.sendError(new CompileError(new WPos(source, LineOffsets.dummy, 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
			WLogger.severe(e);
			throw new Error(e);
		}
	}

	public CompilationUnit parseJurst(Reader reader, String source, boolean hasCommonJ) {
		try {
			JurstScanner scanner = new JurstScanner(reader);
			JurstExtendedParser parser = new JurstExtendedParser(scanner, errorHandler);
			parser.setFilename(source);
			Symbol sym = parser.parse();

			if (sym.value instanceof CompilationUnit) {
				CompilationUnit root = (CompilationUnit) sym.value;
				removeSyntacticSugar(root, hasCommonJ);
				WPos p = root.attrErrorPos();
				p = p.withFile(source);
				for (ScannerError err : scanner.getErrors()) {
					CompileError ce = err.makeCompilerError(p);
					gui.sendError(ce);
				}
				return root;
			}
			return emptyCompilationUnit();
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (Exception e) {
			gui.sendError(new CompileError(new WPos(source, LineOffsets.dummy, 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
			WLogger.severe(e);
			throw new Error(e);
		}
	}

	public CompilationUnit emptyCompilationUnit() {
		return Ast.CompilationUnit("<empty compilation unit>", errorHandler, Ast.JassToplevelDeclarations(), Ast.WPackages());
	}

	private void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
		new SyntacticSugar().removeSyntacticSugar(root, hasCommonJ);
	}
}
