package de.peeeq.wurstscript.attributes;

import java.util.ListIterator;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WPos;

public class ErrorHandling {
	
	public static void addError(AstElement e, String msg) {
		WPos pos = e.attrErrorPos();
		ErrorHandler handler = e.getErrorHandler();
		if (handler.isUnitTestMode()) {
			throw new CompileError(pos, msg);
		}
		ListIterator<CompileError> it = handler.getErrors().listIterator();
		while (it.hasNext()) {
			CompileError err = it.next();
			if (err.getSource().getFile().equals(pos.getFile())) {
				if (bigger(err.getSource(), pos)) {
					// remove bigger errors
					it.remove();
				} else if (bigger(pos, err.getSource()) || equal(pos, err.getSource())) {
					// do not add smaller or equal errors
					return;
				}
			}
		}
		CompileError c = new CompileError(pos, msg);
		handler.sendError(c);
	}

	private static boolean equal(WPos a, WPos b) {
		return a.getLeftPos() == b.getLeftPos() && a.getRightPos() == b.getRightPos();
	}

	private static boolean bigger(WPos a, WPos b) {
		return a.getLeftPos() <= b.getLeftPos() && a.getRightPos() > b.getRightPos()
			|| a.getLeftPos() < b.getLeftPos() && a.getRightPos() >= b.getRightPos();
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
