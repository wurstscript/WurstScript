package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WPos;

public class ErrorHandling {
	
	public static void addError(AstElement e, String msg) {
		WPos pos = e.attrSource();
		ErrorHandler handler = e.getErrorHandler();
		if (handler.isUnitTestMode()) {
			throw new CompileError(pos, msg);
		}
		for (CompileError err : handler.getErrors()) {
			if (err.getSource().getFile().equals(pos.getFile())
					&& err.getSource().getLine() == pos.getLine()) {
				// return only one error per line
				return;
			}
		}
		CompileError c = new CompileError(pos, msg);
		handler.sendError(c);
	}

	public static ErrorHandler getErrorHandler(AstElement e) {
		if (e.getParent() == null) {
			throw new Error("Trying to get error handler of element not attached to root:\n" + e);
		}
		return e.getParent().getErrorHandler();
	}
	
	public static ErrorHandler getErrorHandler(CompilationUnit e) {
		return e.getCuErrorHandler();
	}

}
