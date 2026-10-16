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
