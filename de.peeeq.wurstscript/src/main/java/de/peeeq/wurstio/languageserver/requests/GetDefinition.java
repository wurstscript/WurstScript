package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.AttrFuncDef;
import de.peeeq.wurstscript.attributes.CofigOverridePackages;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GetDefinition extends UserRequest<Either<List<? extends Location>, List<? extends LocationLink>>> {
    public enum LookupType {
        DEFINITION,
        DECLARATION,
        TYPE_DEFINITION,
        IMPLEMENTATION
    }

    private final WFile filename;
    private final String buffer;
    private final int line;
    private final int column;
    private final LookupType lookupType;


    public GetDefinition(TextDocumentPositionParams position, BufferManager bufferManager) {
        this(position, bufferManager, LookupType.DEFINITION);
    }

    public GetDefinition(TextDocumentPositionParams position, BufferManager bufferManager, LookupType lookupType) {
        this.filename = WFile.create(position.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(position.getTextDocument());
        this.line = position.getPosition().getLine() + 1;
        this.column = position.getPosition().getCharacter() + 1;
        this.lookupType = lookupType;
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
        Optional<Element> element = Utils.getAstElementAtPos(cu, line, column, false);
        if (!element.isPresent()) {
            return Collections.emptyList();
        }
        Element e = element.get();
        WLogger.info("get definition at: " + e.getClass().getSimpleName());
        if (lookupType == LookupType.TYPE_DEFINITION) {
            return typeDefinitionFor(e);
        }

        NameDef configuredDecl = getConfiguredDeclarationAtPos(e);
        if (configuredDecl != null) {
            NameDef originalDecl = getOriginalConfigDeclaration(configuredDecl);
            if (originalDecl != null) {
                return linkTo(originalDecl);
            }
        }
        FunctionDefinition indexOpDecl = getIndexOperatorDeclarationAtPos(e);
        if (indexOpDecl != null) {
            return linkTo(indexOpDecl);
        }
        if (e instanceof FuncRef funcRef) {
            FunctionDefinition decl = funcRef.attrFuncDef();
            return linkTo(decl);
        } else if (e instanceof NameRef nameRef) {
            NameDef decl = nameRef.attrNameDef();
            return linkTo(decl);
        } else if (e instanceof TypeExpr typeExpr) {
            TypeDef decl = typeExpr.attrTypeDef();
            return linkTo(decl);
        } else if (e instanceof WImport wImport) {
            WPackage p = wImport.attrImportedPackage();
            if (p == null) {
                return Collections.emptyList();
            }
            return linkTo(p);
        } else if (e instanceof ExprNewObject exprNew) {
            ConstructorDef def = exprNew.attrConstructorDef();
            return linkTo(def);
        } else if (e instanceof ModuleUse use) {
            ModuleDef def = use.attrModuleDef();
            return linkTo(def);
        } else if (e instanceof ExprBinary eb) {
            FunctionDefinition def = eb.attrFuncDef();
            return linkTo(def);
        } else if (e instanceof SomeSuperConstructorCall sc) {
            ConstructorDef constructor = (ConstructorDef) sc.getParent();
            ConstructorDef superConstructor = constructor.attrSuperConstructor();
            return linkTo(superConstructor);
        }
        return Collections.emptyList();
    }

    private FunctionDefinition getIndexOperatorDeclarationAtPos(Element e) {
        Element target = e;
        if (target instanceof Indexes && target.getParent() instanceof NameRef) {
            target = target.getParent();
        }
        if (!(target instanceof NameRef) || !(target instanceof AstElementWithIndexes)) {
            return null;
        }

        NameRef nr = (NameRef) target;
        AstElementWithIndexes withIndexes = (AstElementWithIndexes) target;
        if (withIndexes.getIndexes().size() != 1) {
            return null;
        }
        NameLink link = nr.attrNameLink();
        if (link == null || link.getTyp() instanceof WurstTypeArray) {
            return null;
        }

        WurstType receiverType = link.getTyp();
        WurstType indexType = withIndexes.getIndexes().get(0).attrTyp();
        if (nr.getParent() instanceof StmtSet && ((StmtSet) nr.getParent()).getUpdatedExpr() == nr) {
            StmtSet set = (StmtSet) nr.getParent();
            FuncLink f = AttrFuncDef.getIndexSetOperator(nr, receiverType, indexType, set.getRight().attrTyp());
            return f == null ? null : f.getDef();
        }

        FuncLink f = AttrFuncDef.getIndexGetOperator(nr, receiverType, indexType);
        return f == null ? null : f.getDef();
    }

    private List<? extends Location> typeDefinitionFor(Element e) {
        if (e instanceof TypeExpr typeExpr) {
            return linkTo(typeExpr.attrTypeDef());
        }
        if (e instanceof NameRef nameRef) {
            NameDef def = nameRef.attrNameDef();
            if (def != null) {
                return linkToType(def.attrTyp());
            }
        }
        if (e instanceof FuncRef funcRef) {
            FunctionDefinition def = funcRef.attrFuncDef();
            if (def != null) {
                return linkToType(def.attrReturnTyp());
            }
        }
        if (e instanceof Expr expr) {
            return linkToType(expr.attrTyp());
        }
        return Collections.emptyList();
    }

    private List<? extends Location> linkToType(WurstType type) {
        if (type instanceof WurstTypeNamedScope wurstTypeNamedScope) {
            AstElementWithSource def = wurstTypeNamedScope.getDef();
            return linkTo(def);
        }
        return Collections.emptyList();
    }

    private NameDef getConfiguredDeclarationAtPos(Element e) {
        if (e instanceof NameDef nameDef) {
            return nameDef;
        }
        Element current = e;
        while (current != null) {
            if (current instanceof NameDef) {
                return (NameDef) current;
            }
            current = current.getParent();
        }
        return null;
    }

    private NameDef getOriginalConfigDeclaration(NameDef nameDef) {
        if (!(nameDef instanceof GlobalVarDef) || !nameDef.hasAnnotation("@config")) {
            return null;
        }
        PackageOrGlobal nearestPackage = nameDef.attrNearestPackage();
        if (!(nearestPackage instanceof WPackage configPackage)) {
            return null;
        }
        if (!configPackage.getName().endsWith(CofigOverridePackages.CONFIG_POSTFIX)) {
            return null;
        }
        WPackage originalPackage = CofigOverridePackages.getOriginalPackage(configPackage);
        if (originalPackage == null) {
            return null;
        }
        NameLink originalVar = originalPackage.getElements().lookupVarNoConfig(nameDef.getName(), false);
        return originalVar == null ? null : originalVar.getDef();
    }

    private List<? extends Location> linkTo(AstElementWithSource decl) {
        if (decl == null) {
            return Collections.emptyList();
        }
        WPos pos = decl.attrErrorPos();
        return Collections.singletonList(Convert.posToLocation(pos));
    }

    static class DefinitionInfo {
        private final String filename;
        private final int line;
        private final int column;

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
