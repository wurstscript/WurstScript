package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.lsp4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class FoldingRangeRequest extends UserRequest<List<FoldingRange>> {

    private final TextDocumentIdentifier textDocument;

    public FoldingRangeRequest(FoldingRangeRequestParams params) {
        textDocument = params.getTextDocument();
    }


    @Override
    public List<FoldingRange> execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.getCompilationUnit(WFile.create(textDocument));
        if (cu == null) {
            return Collections.emptyList();
        }

        List<FoldingRange> result = new ArrayList<>();
        cu.accept(new Element.DefaultVisitor() {
            private void addFoldingRange(Element element, int startOffset, int endOffset) {
                addFoldingRange(element, startOffset, endOffset, "");
            }

            private void addFoldingRange(Element element, int startOffset, int endOffset, String kind) {
                Range range = Convert.range(element);
                range.getStart().setLine(range.getStart().getLine() + startOffset);
                if (range.getEnd().getCharacter() == 0) {
                    range.getEnd().setLine(range.getEnd().getLine() - 1);
                } else {
                    range.getEnd().setLine(range.getEnd().getLine() + endOffset);
                }
                if (range.getStart().getLine() < range.getEnd().getLine()) {
                    FoldingRange foldingRange = new FoldingRange(range.getStart().getLine(), range.getEnd().getLine());
                    foldingRange.setKind(kind);
                    result.add(foldingRange);
                }
            }

            @Override
            public void visit(WImports imports) {
                addFoldingRange(imports, 0, -1, FoldingRangeKind.Imports);
            }

            @Override
            public void visit(ClassDef classDef) {
                addFoldingRange(classDef, StringUtils.isEmpty(classDef.attrComment()) ? 0 : 1, 0);
                super.visit(classDef);
            }

            @Override
            public void visit(FuncDef funcDef) {
                addFoldingRange(funcDef.getBody(), -1, 0);
                super.visit(funcDef);
            }

            @Override
            public void visit(ExtensionFuncDef extensionFuncDef) {
                addFoldingRange(extensionFuncDef.getBody(), -1, 0);
                super.visit(extensionFuncDef);
            }

            @Override
            public void visit(StmtIf ifStmt) {
                addFoldingRange(ifStmt, 0, -1);
                super.visit(ifStmt);
            }

            @Override
            public void visit(InterfaceDef interfaceDef) {
                addFoldingRange(interfaceDef, StringUtils.isEmpty(interfaceDef.description()) ? 0 : 1, -1);
                super.visit(interfaceDef);
            }

            @Override
            public void visit(StmtLoop stmtLoop) {
                addFoldingRange(stmtLoop, 0, -1);
                super.visit(stmtLoop);
            }

            @Override
            public void visit(StmtWhile stmtWhile) {
                addFoldingRange(stmtWhile, 0, 0);
                super.visit(stmtWhile);
            }

            @Override
            public void visit(StmtForIn stmtForIn) {
                addFoldingRange(stmtForIn, 0, 0);
                super.visit(stmtForIn);
            }

            @Override
            public void visit(StmtForFrom stmtForFrom) {
                addFoldingRange(stmtForFrom, 0, 0);
                super.visit(stmtForFrom);
            }

            @Override
            public void visit(StmtForRangeDown stmtForRangeDown) {
                addFoldingRange(stmtForRangeDown, 0, 0);
                super.visit(stmtForRangeDown);
            }

            @Override
            public void visit(StmtForRangeUp stmtForRangeUp) {
                addFoldingRange(stmtForRangeUp, 0, 0);
                super.visit(stmtForRangeUp);
            }

            @Override
            public void visit(InitBlock initBlock) {
                addFoldingRange(initBlock, 0, 0);
                super.visit(initBlock);
            }

            @Override
            public void visit(EnumDef enumDef) {
                addFoldingRange(enumDef, 0, 0);
                super.visit(enumDef);
            }

            @Override
            public void visit(SwitchStmt switchStmt) {
                addFoldingRange(switchStmt, 0, 0);
                super.visit(switchStmt);
            }

            @Override
            public void visit(SwitchCase switchCase) {
                addFoldingRange(switchCase, 0, 0);
                super.visit(switchCase);
            }

        });
        return result;
    }

}
