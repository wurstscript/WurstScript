package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver2.Convert;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.lsp4j.Diagnostic;

public class ExternCompileError {
    private final String fileName;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    private final String message;
    private final ErrorType errorType;

    public static ExternCompileError convert(CompileError err) {
        return new ExternCompileError(err);
    }

    public ExternCompileError(String fileName, int startLine, int startColumn, int endLine, int endColumn,
                              String message, ErrorType errorType) {
        this.fileName = fileName;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.message = message;
        this.errorType = errorType;
    }

    public ExternCompileError(CompileError err) {
        WPos pos = err.getSource();
        this.fileName = pos.getFile();
        startLine = pos.getLine();
        endLine = pos.getEndLine();
        startColumn = pos.getStartColumn();
        endColumn = pos.getEndColumn();
        message = err.getMessage();
        errorType = err.getErrorType();
    }

    public String getFileName() {
        return fileName;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return errorType + " in File " + getFileName() + " line " + getStartLine() + ":" + startColumn
                + " - " + endLine + ":" + endColumn + " :\n " +
                message;
    }

}
