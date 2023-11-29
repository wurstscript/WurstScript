package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class DocumentSymbolRequest extends UserRequest<List<Either<SymbolInformation, DocumentSymbol>>> {

    private final TextDocumentIdentifier textDocument;

    public DocumentSymbolRequest(DocumentSymbolParams params) {
        textDocument = params.getTextDocument();
    }


    @Override
    public List<Either<SymbolInformation, DocumentSymbol>> execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.getCompilationUnit(WFile.create(textDocument.getUri()));
        return symbolsFromCu(cu)
                .stream()
                .map(Either::<SymbolInformation, DocumentSymbol>forRight)
                .collect(Collectors.toList());
    }

    private List<DocumentSymbol> symbolsFromCu(CompilationUnit cu) {
        if (cu == null) {
            return Collections.emptyList();
        }
        List<DocumentSymbol> result = new ArrayList<>();
        for (WPackage p : cu.getPackages()) {
            addSymbolsForPackage(result, p);
        }
        return result;
    }

    private void addSymbolsForPackage(List<DocumentSymbol> result, WPackage p) {
        String name = p.getName();
        result.add(makeDocumentSymbol(p.getNameId(), SymbolKind.Package, name, null));
        for (WEntity e : p.getElements()) {
            addSymbolsForEntity(result, e);
        }
    }

    @NotNull
    private DocumentSymbol makeDocumentSymbol(Element p, SymbolKind kind, String name, List<DocumentSymbol> children) {
        String detail = null;
        if (p instanceof AstElementWithParameters) {
            detail = "(" + HoverInfo.getParameterString((AstElementWithParameters) p) + ")";
        }
        return new DocumentSymbol(name, kind, Convert.range(p), Convert.errorRange(p), detail, children);
    }

    private void addSymbolsForEntity(List<DocumentSymbol> result, WEntity e) {
        e.match(new WEntity.MatcherVoid() {
            private void add(String name, SymbolKind kind) {
                add(name, kind, Collections.emptyList());
            }

            private void add(String name, SymbolKind kind, List<DocumentSymbol> children) {
                if (!e.attrSource().isArtificial()) {
                    result.add(makeDocumentSymbol(e, kind, name, children));
                }
            }

            @Override
            public void case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
                add(Utils.printTypeExpr(extensionFuncDef.getExtendedType()) + "." + extensionFuncDef.getName(), SymbolKind.Function);
            }

            @Override
            public void case_ClassDef(ClassDef classDef) {
                case_ClassOrModule(classDef);
            }

            @Override
            public void case_InterfaceDef(InterfaceDef interfaceDef) {
                String name = interfaceDef.getName();
                List<DocumentSymbol> children = new ArrayList<>();
                add(name, SymbolKind.Interface, children);
                for (FuncDef f : interfaceDef.getMethods()) {
                    addSymbolsForEntity(children, f);
                }
                for (GlobalVarDef v : interfaceDef.getVars()) {
                    addSymbolsForEntity(children, v);
                }
            }

            @Override
            public void case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {

            }

            @Override
            public void case_NativeType(NativeType nativeType) {
                add(nativeType.getName(), SymbolKind.Class);
            }

            @Override
            public void case_InitBlock(InitBlock initBlock) {
                add("init", SymbolKind.Function);
            }

            @Override
            public void case_TupleDef(TupleDef tupleDef) {
                add(tupleDef.getName(), SymbolKind.Class);
            }

            @Override
            public void case_FuncDef(FuncDef funcDef) {
                SymbolKind kind = funcDef.attrIsDynamicClassMember() ? SymbolKind.Method : SymbolKind.Function;
                add(funcDef.getName(), kind);
            }

            @Override
            public void case_NativeFunc(NativeFunc nativeFunc) {
                add(nativeFunc.getName(), SymbolKind.Function);
            }

            @Override
            public void case_GlobalVarDef(GlobalVarDef g) {
                SymbolKind kind = g.attrIsDynamicClassMember() ? SymbolKind.Field : SymbolKind.Variable;
                add(g.getName(), kind);
            }

            @Override
            public void case_EnumDef(EnumDef enumDef) {
                add(enumDef.getName(), SymbolKind.Class);
            }

            @Override
            public void case_TypeParamDef(TypeParamDef typeParamDef) {
                add(typeParamDef.getName(), SymbolKind.Class);
            }

            @Override
            public void case_ModuleDef(ModuleDef moduleDef) {
                case_ClassOrModule(moduleDef);
            }

            public void case_ClassOrModule(ClassOrModule def) {
                String name = def.getName();
                List<DocumentSymbol> children = new ArrayList<>();
                add(name, SymbolKind.Class, children);
                for (ClassDef c : def.getInnerClasses()) {
                    addSymbolsForEntity(children, c);
                }
                for (FuncDef f : def.getMethods()) {
                    addSymbolsForEntity(children, f);
                }
                for (GlobalVarDef v : def.getVars()) {
                    addSymbolsForEntity(children, v);
                }
                for (ConstructorDef c : def.getConstructors()) {
                    if (!c.attrSource().isArtificial()) {
                        children.add(makeDocumentSymbol(c, SymbolKind.Constructor, "construct", Collections.emptyList()));
                    }
                }

            }

        });
    }
}
