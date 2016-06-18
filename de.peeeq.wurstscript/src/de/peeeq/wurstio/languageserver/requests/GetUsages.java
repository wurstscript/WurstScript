package de.peeeq.wurstio.languageserver.requests;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.Range;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.utils.Utils;

public class GetUsages extends UserRequest {

    private final String filename;
    private final String buffer;
    private final int line;
    private final int column;
    private final boolean global;

    public GetUsages(int requestNr, String filename, String buffer, int line, int column, boolean global) {
        super(requestNr);
        this.filename = filename;
        this.buffer = buffer;
        this.line = line;
        this.column = column - 1;
        this.global = global;
    }


    @Override
    public Object execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer);
        AstElement astElem = Utils.getAstElementAtPos(cu, line, column, false);
        NameDef nameDef = astElem.tryGetNameDef();
        List<UsagesData> usages = new ArrayList<>();
        if (nameDef != null) {

            if (global || nameDef.getSource().getFile().equals(filename)) {
                // add declaration
                usages.add(new UsagesData(nameDef.getSource().getFile(), nameDef.attrErrorPos().getRange(), DocumentHighlightKind.Write));
            }
            Deque<AstElement> todo = new ArrayDeque<>();
            if (global) {
                todo.push(modelManager.getModel());
            } else {
                todo.push(cu);
            }
            while (!todo.isEmpty()) {
                AstElement e = todo.pop();
                // visit children:
                for (int i = 0; i < e.size(); i++) {
                    todo.push(e.get(i));
                }
                NameDef e_def = e.tryGetNameDef();
                if (e_def == nameDef) {
                    UsagesData usagesData = new UsagesData(e.attrSource().getFile()
                            , e.attrErrorPos().getRange(), DocumentHighlightKind.Read);
                    usages.add(usagesData);
                }
            }
        }

        return usages;
    }

    static enum DocumentHighlightKind {
        Text, Read, Write
    }

    static class UsagesData {
        private String filename;
        private Range range;
        private DocumentHighlightKind kind;

        public UsagesData(String filename, Range range, DocumentHighlightKind kind) {
            this.setFilename(filename);
            this.setRange(range);
            this.setKind(kind);
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public DocumentHighlightKind getKind() {
            return kind;
        }

        public void setKind(DocumentHighlightKind kind) {
            this.kind = kind;
        }

    }
}
