package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;

import java.util.Optional;

public class PrepareRenameRequest extends UserRequest<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> {

    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;

    public PrepareRenameRequest(PrepareRenameParams params, BufferManager bufferManager) {
        this.filename = WFile.create(params.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(params.getTextDocument());
        this.line = params.getPosition().getLine() + 1;
        this.column = params.getPosition().getCharacter() + 1;
    }

    @Override
    public Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior> execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            throw new RequestFailedException(MessageType.Error, "File " + filename + " is not part of the project.");
        }
        Optional<Element> element = Utils.getAstElementAtPos(cu, line, column, false);
        if (!element.isPresent()) {
            throw new RequestFailedException(MessageType.Error, "No symbol at cursor.");
        }
        Element e = element.get();
        if (!isRenameTarget(e)) {
            throw new RequestFailedException(MessageType.Error, "Selected element cannot be renamed.");
        }
        return Either3.forFirst(Convert.errorRange(e));
    }

    private boolean isRenameTarget(Element e) {
        NameDef nameDef = e.tryGetNameDef();
        if (nameDef != null) {
            return true;
        }
        if (e instanceof TypeExpr) {
            return ((TypeExpr) e).attrTypeDef() != null;
        }
        if (e instanceof WImport) {
            WPackage p = ((WImport) e).attrImportedPackage();
            return p != null;
        }
        return false;
    }
}

