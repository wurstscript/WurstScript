package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.lsp4j.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Convert {
    public static Location posToLocation(WPos pos) {
        String file = WFile.create(pos.getFile()).getUriString();
        return new Location(file, new Range(
                new Position(pos.getLine()-1, pos.getStartColumn()-1),
                new Position(pos.getEndLine()-1, pos.getEndColumn()-1)
        ));
    }

    public static Location location(Element e) {
        return posToLocation(e.attrSource());
    }

    public static Location errorLocation(Element e) {
        return posToLocation(e.attrErrorPos());
    }

    public static PublishDiagnosticsParams createDiagnostics(String extra, WFile filename, List<CompileError> errors) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (CompileError err : errors) {
            Range range = posToLocation(err.getSource()).getRange();
            String message = err.getMessage();

            DiagnosticSeverity severity = DiagnosticSeverity.Error;
            switch (err.getErrorType()) {
                case ERROR:
                    severity = DiagnosticSeverity.Error;
                    break;
                case WARNING:
                    severity = DiagnosticSeverity.Warning;
                    break;
            }
            diagnostics.add(new Diagnostic(range, message, severity, "Wurst"));
        }
        return new PublishDiagnosticsParams(filename.getUriString(), diagnostics);
    }



}
