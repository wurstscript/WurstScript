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
    private final String[] lines;
    private final int line;
    private final int column;

    public GetUsages(int requestNr, String filename, String buffer, int line, int column) {
        super(requestNr);
        this.filename = filename;
        this.buffer = buffer;
        this.line = line;
        this.column = column - 1;
        this.lines = buffer.split("\\n|\\r\\n");
        WLogger.info("Get completions in line " + line + ": \n" + "" + currentLine().replace('\t', ' ') + "\n" + "" + Utils.repeat(' ', column - 1) + "^\n"
                + " at column " + column);
    }

    private String currentLine() {
        return lines[line - 1];
    }

    @Override
    public Object execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer);
        AstElement astElem = Utils.getAstElementAtPos(cu, line, column, false);
        NameDef nameDef = astElem.tryGetNameDef();
        List<UsagesData> usages = new ArrayList<>();
        if (nameDef != null) {

            if (nameDef.getSource().getFile().equals(filename)) {
                // add declaration
                usages.add(new UsagesData(filename, nameDef.attrErrorPos().getRange(), DocumentHighlightKind.Write));
            }
            Deque<AstElement> todo = new ArrayDeque<>();
            todo.push(cu);
            while (!todo.isEmpty()) {
                AstElement e = todo.pop();
                // visit children:
                for (int i = 0; i < e.size(); i++) {
                    todo.push(e.get(i));
                }
                NameDef e_def;
                synchronized (modelManager) {
                    e_def = e.tryGetNameDef();
                }
                if (e_def == nameDef) {
                    UsagesData usagesData = new UsagesData(filename, e.attrErrorPos().getRange(), DocumentHighlightKind.Read);
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
