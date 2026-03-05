package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.lsp4j.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Convert {
    public static Location posToLocation(WPos pos) {
        String file = WFile.create(pos.getFile()).getUriString();
        return new Location(file, posToRange(pos));
    }

    public static Range posToRange(WPos pos) {
        int line = pos.getLine() - 1;
        int column = pos.getStartColumn() - 1;
        int endLine = pos.getEndLine() - 1;
        int endColumn = pos.getEndColumn() - 1;
        if (line < 0) {
            line = 0;
        }
        if (endLine < line) {
            endLine = line;
        }
        if (column < 0) {
            column = 0;
        }
        if (endColumn < 0) {
            endColumn = 0;
        }
        if (line == endLine && endColumn <= column) {
            endColumn = column + 1;
        }
        return new Range(
                new Position(line, column),
                new Position(endLine, endColumn)
        );
    }

    public static Location location(Element e) {
        return posToLocation(e.attrSource());
    }

    public static Location errorLocation(Element e) {
        return posToLocation(e.attrErrorPos());
    }

    public static Range range(Element e) {
        return posToRange(e.attrSource());
    }

    public static Range errorRange(Element e) {
        return posToRange(e.attrErrorPos());
    }

    public static PublishDiagnosticsParams createDiagnostics(String extra, WFile filename, List<CompileError> errors) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (CompileError err : errors) {
            Range range = posToLocation(err.getSource()).getRange();
            String message = err.getMessage();

            DiagnosticSeverity severity;
            switch (err.getErrorType()) {
                case WARNING:
                    severity = DiagnosticSeverity.Warning;
                    break;
                case ERROR:
                default:
                    severity = DiagnosticSeverity.Error;
                    break;

            }
            String source = severity == DiagnosticSeverity.Warning ? "Wurst warning" : "Wurst error";
            Diagnostic diagnostic = new Diagnostic(range, message, severity, source);
            String messageLower = message.toLowerCase();
            if (messageLower.contains("deprecated")) {
                diagnostic.setTags(Collections.singletonList(DiagnosticTag.Deprecated));
            }
            diagnostics.add(diagnostic);
        }
        return new PublishDiagnosticsParams(filename.getUriString(), diagnostics);
    }


}
