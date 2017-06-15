package de.peeeq.wurstio.languageserver2;

import de.peeeq.wurstscript.parser.WPos;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 *
 */
public class Convert {
    public static Location posToLocation(WPos pos) {
        return new Location(pos.getFile(), new Range(
                new Position(pos.getLine(), pos.getStartColumn()),
                new Position(pos.getEndLine(), pos.getEndColumn())
        ));
    }
}
