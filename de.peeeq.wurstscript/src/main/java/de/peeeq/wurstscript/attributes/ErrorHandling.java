package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ListIterator;

public class ErrorHandling {

    public static void addError(Element e, String msg) {
        addErrorOrWarning(e, msg, ErrorType.ERROR);
    }

    public static void addWarning(Element e, String msg) {
        addErrorOrWarning(e, msg, ErrorType.WARNING);
    }

    private static void addErrorOrWarning(Element e, String msg,
                                          ErrorType errorType) throws CompileError {
        ErrorHandler handler = e.getErrorHandler();
        CompileError c = makeCompileError(e, msg, handler, errorType);
        if (c != null) {
            handler.sendError(c);
        }
    }

    private static @Nullable CompileError makeCompileError(Element e, String msg,
                                                           ErrorHandler handler, CompileError.ErrorType errorType) throws CompileError {
        WPos pos = e.attrErrorPos();
        if (errorType == ErrorType.ERROR && handler.isUnitTestMode()) {
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
                    return null;
                }
            }
        }
        return new CompileError(pos, msg, errorType);
    }


    private static boolean equal(WPos a, WPos b) {
        return a.getLeftPos() == b.getLeftPos() && a.getRightPos() == b.getRightPos();
    }

    private static boolean bigger(WPos a, WPos b) {
        return a.getLeftPos() <= b.getLeftPos() && a.getRightPos() > b.getRightPos()
                || a.getLeftPos() < b.getLeftPos() && a.getRightPos() >= b.getRightPos();
    }

    public static ErrorHandler getErrorHandler(Element e) {
        if (e.getParent() == null) {
            throw new Error("Trying to get error handler of element not attached to root:\n" + e);
        }
        return e.getParent().getErrorHandler();
    }

    public static ErrorHandler getErrorHandler(CompilationUnit e) {
        return e.getCuInfo().getCuErrorHandler();
    }

    public static ErrorHandler getErrorHandler(WurstModel m) {
        for (CompilationUnit cu : m) {
            return cu.getCuInfo().getCuErrorHandler();
        }
        throw new Error("Empty model.");
    }


}
