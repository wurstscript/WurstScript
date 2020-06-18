package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.*;

import java.util.*;

public class GetUsages extends UserRequest<List<GetUsages.UsagesData>> {

    private final WFile wFile;
    private final String buffer;
    private final int line;
    private final int column;
    private final boolean global;



    public GetUsages(TextDocumentPositionParams position, BufferManager bufferManager, boolean global) {
        this.wFile = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter() + 1;
        this.global = global;
    }


    @Override
    public List<UsagesData> execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(wFile, buffer, false);
        if (cu == null) {
            return Collections.emptyList();
        }
        Optional<Element> astElem = Utils.getAstElementAtPos(cu, line, column, false);
        Optional<NameDef> nameDef = astElem.flatMap(elem -> Optional.ofNullable(elem.tryGetNameDef()));
        List<UsagesData> usages = new ArrayList<>();
        if (nameDef.isPresent()) {

            if (global || nameDef.get().getSource().getFile().equals(wFile.toString())) {
                // add declaration
                usages.add(
                    new UsagesData(Convert.posToLocation(nameDef.get().attrErrorPos()), DocumentHighlightKind.Write));
            }
            Deque<Element> todo = new ArrayDeque<>();
            if (global) {
                todo.push(modelManager.getModel());
            } else {
                todo.push(cu);
            }
            while (!todo.isEmpty()) {
                Element e = todo.pop();
                // visit children:
                for (int i = 0; i < e.size(); i++) {
                    todo.push(e.get(i));
                }
                NameDef e_def = e.tryGetNameDef();
                if (e_def == nameDef.get()) {
                    UsagesData usagesData = new UsagesData(Convert.posToLocation(e.attrErrorPos()), DocumentHighlightKind.Read);
                    usages.add(usagesData);
                }
            }
        }

        return usages;
    }

    public static class UsagesData {
        private Location location;
        private DocumentHighlightKind kind;


        public UsagesData(Location location, DocumentHighlightKind kind) {
            this.location = location;
            this.kind = kind;
        }

        public String getFilename() {
            return location.getUri();
        }

        public void setFilename(String filename) {
            location.setUri(filename);
        }

        public Range getRange() {
            return location.getRange();
        }

        public void setRange(Range range) {
            location.setRange(range);
        }

        public DocumentHighlightKind getKind() {
            return kind;
        }

        public void setKind(DocumentHighlightKind kind) {
            this.kind = kind;
        }

        public Location getLocation() {
            return location;
        }

        public DocumentHighlight toDocumentHighlight() {
            return new DocumentHighlight(location.getRange(), kind);
        }
    }
}
