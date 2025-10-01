package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;

import java.util.List;

final class AstEdits {
    private AstEdits() {}

    static void deleteStmt(ImStmt s) {
        ImStmts parent = (ImStmts) s.getParent();
        parent.remove(s);
    }

    static void insertBefore(ImStmt anchor, ImStmt newStmt) {
        ImStmts parent = (ImStmts) anchor.getParent();
        int idx = parent.indexOf(anchor);
        parent.add(idx, newStmt);
    }

    static void insertAfter(ImStmt anchor, ImStmt newStmt) {
        ImStmts parent = (ImStmts) anchor.getParent();
        int idx = parent.indexOf(anchor);
        parent.add(idx + 1, newStmt);
    }

    static void spliceBefore(ImStmt anchor, ImStmts block) {
        ImStmts parent = (ImStmts) anchor.getParent();
        int idx = parent.indexOf(anchor);
        List<ImStmt> payload = block.removeAll(); // important!
        for (int i = 0; i < payload.size(); i++) {
            parent.add(idx + i, payload.get(i));
        }
    }

    static void spliceAfter(ImStmt anchor, ImStmts block) {
        ImStmts parent = (ImStmts) anchor.getParent();
        int idx = parent.indexOf(anchor) + 1;
        List<ImStmt> payload = block.removeAll();
        for (int i = 0; i < payload.size(); i++) {
            parent.add(idx + i, payload.get(i));
        }
    }

    static void replaceStmtWithMany(ImStmt anchor, ImStmts block) {
        ImStmts parent = (ImStmts) anchor.getParent();
        int idx = parent.indexOf(anchor);
        parent.remove(idx);
        List<ImStmt> payload = block.removeAll();
        for (int i = 0; i < payload.size(); i++) {
            parent.add(idx + i, payload.get(i));
        }
    }
}
