package de.peeeq.wurstscript;

import java.io.Reader;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.ScannerError;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.utils.LineOffsets;

public class WurstParser {
	
	private final ErrorHandler errorHandler;
	private final WurstGui gui;
	
	
	public WurstParser(ErrorHandler errorHandler, WurstGui gui) {
		this.errorHandler = errorHandler;
		this.gui = gui;
	}

	public CompilationUnit parse(Reader reader, String source, boolean hasCommonJ) {
		try {
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner, errorHandler);
			parser.setFilename(source);
			Symbol sym = parser.parse();
			
			if (sym.value instanceof CompilationUnit) {
				CompilationUnit root = (CompilationUnit) sym.value;
				removeSyntacticSugar(root, hasCommonJ);
				WPos p = root.attrErrorPos().copy();
				p.setFile(source);
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
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
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
