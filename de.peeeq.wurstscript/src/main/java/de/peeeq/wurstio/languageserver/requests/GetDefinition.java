package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.List;

public class GetDefinition extends UserRequest<Either<List<? extends Location>, List<? extends LocationLink>>> {

    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;


    public GetDefinition(TextDocumentPositionParams position, BufferManager bufferManager) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter() + 1;
    }


    @Override
    public Either<List<? extends Location>, List<? extends LocationLink>> execute(ModelManager modelManager) {
        return Either.forLeft(execute2(modelManager));
    }


    private List<? extends Location> execute2(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return Collections.emptyList();
        }
        Element e = Utils.getAstElementAtPos(cu, line, column, false).get();
        WLogger.info("get definition at: " + e.getClass().getSimpleName());
        if (e instanceof FuncRef) {
            FuncRef funcRef = (FuncRef) e;
            FunctionDefinition decl = funcRef.attrFuncDef();
            return linkTo(decl);
        } else if (e instanceof NameRef) {
            NameRef nameRef = (NameRef) e;
            NameDef decl = nameRef.attrNameDef();
            return linkTo(decl);
        } else if (e instanceof TypeExpr) {
            TypeExpr typeExpr = (TypeExpr) e;
            TypeDef decl = typeExpr.attrTypeDef();
            return linkTo(decl);
        } else if (e instanceof WImport) {
            WImport wImport = (WImport) e;
            WPackage p = wImport.attrImportedPackage();
            if (p == null) {
                return Collections.emptyList();
            }
            return linkTo(p);
        } else if (e instanceof ExprNewObject) {
            ExprNewObject exprNew = (ExprNewObject) e;
            ConstructorDef def = exprNew.attrConstructorDef();
            return linkTo(def);
        } else if (e instanceof ModuleUse) {
            ModuleUse use = (ModuleUse) e;
            ModuleDef def = use.attrModuleDef();
            return linkTo(def);
        } else if (e instanceof ExprBinary) {
            ExprBinary eb = (ExprBinary) e;
            FunctionDefinition def = eb.attrFuncDef();
            return linkTo(def);
        } else if (e instanceof SomeSuperConstructorCall) {
            SomeSuperConstructorCall sc = (SomeSuperConstructorCall) e;
            ConstructorDef constructor = (ConstructorDef) sc.getParent();
            ConstructorDef superConstructor = constructor.attrSuperConstructor();
            return linkTo(superConstructor);
        }
        return Collections.emptyList();
    }

    private List<? extends Location> linkTo(AstElementWithSource decl) {
        if (decl == null) {
            return Collections.emptyList();
        }
        WPos pos = decl.attrErrorPos();
        return Collections.singletonList(Convert.posToLocation(pos));
    }

    static class DefinitionInfo {
        private String filename;
        private int line;
        private int column;

        public DefinitionInfo(String filename, int line, int column) {
            this.filename = filename;
            this.line = line;
            this.column = column;
        }

        public String getFilename() {
            return filename;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }
    }
}
