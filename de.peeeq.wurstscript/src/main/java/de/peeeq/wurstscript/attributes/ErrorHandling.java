package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
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

    private static @Nullable CompileError makeCompileError(
        Element e, String msg, ErrorHandler handler, CompileError.ErrorType errorType) throws CompileError {

        // Preserve unit-test semantics (throw eagerly)
        if (errorType == ErrorType.ERROR && handler.isUnitTestMode()) {
            throw new CompileError(e.attrErrorPos(), msg);
        }

        // Eager pos (like original), but we will only scan the same-file bucket
        WPos pos = e.attrErrorPos();
        final String file = pos.getFile();
        final int left  = pos.getLeftPos();
        final int right = pos.getRightPos();

        // Fast path: no existing items for this file
        List<CompileError> bucket = handler.getBucketForFile(file, errorType);
        if (bucket != null && !bucket.isEmpty()) {
            // Compare only within this file
            ListIterator<CompileError> it = bucket.listIterator();
            while (it.hasNext()) {
                CompileError err = it.next();
                WPos ep = err.getSource();
                // same file by construction
                final int eLeft  = ep.getLeftPos();
                final int eRight = ep.getRightPos();

                if (bigger(eLeft, eRight, left, right)) {
                    // remove bigger error and keep going (might remove multiple)
                    it.remove();              // from file bucket
                    handler.removeFromGlobal(err); // from global list
                } else if (bigger(left, right, eLeft, eRight) || equal(left, right, eLeft, eRight)) {
                    // do not add smaller or equal errors
                    return null;
                }
            }
        }

        return new CompileError(pos, msg, errorType);
    }

    private static boolean equal(int aL, int aR, int bL, int bR) {
        return aL == bL && aR == bR;
    }

    private static boolean bigger(int aL, int aR, int bL, int bR) {
        return (aL <= bL && aR >  bR) || (aL <  bL && aR >= bR);
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
