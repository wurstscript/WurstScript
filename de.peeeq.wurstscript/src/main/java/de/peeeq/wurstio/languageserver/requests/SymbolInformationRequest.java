package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstscript.ast.*;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class SymbolInformationRequest extends UserRequest<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> {

    private final String query;

    public SymbolInformationRequest(WorkspaceSymbolParams params) {
        query = params.getQuery().toLowerCase();
    }

    @Override
    public Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> execute(ModelManager modelManager) {
        return Either.forLeft(symbolsFromModel(modelManager.getModel()));
    }

    private List<SymbolInformation> symbolsFromModel(WurstModel model) {
        List<SymbolInformation> list = new ArrayList<>();
        for (CompilationUnit cu : model) {
            for (SymbolInformation si : symbolsFromCu(cu)) {
                if ((si.getContainerName() + "." + si.getName()).toLowerCase().contains(query)) {
                    list.add(si);
                }
            }
        }
        return list;
    }

    private List<SymbolInformation> symbolsFromCu(CompilationUnit cu) {
        if (cu == null) {
            return Collections.emptyList();
        }
        List<SymbolInformation> result = new ArrayList<>();
        for (WPackage p : cu.getPackages()) {
            addSymbolsForPackage(result, p);
        }
        return result;
    }

    private void addSymbolsForPackage(List<SymbolInformation> result, WPackage p) {
        result.add(new SymbolInformation(p.getName(), SymbolKind.Package, Convert.errorLocation(p), ""));
        for (WEntity e : p.getElements()) {
            addSymbolsForEntity(result, p.getName(), e);
        }
    }

    private void addSymbolsForEntity(List<SymbolInformation> result, String containerName, WEntity e) {
        e.match(new WEntity.MatcherVoid() {
            private void add(String name, SymbolKind kind) {
                result.add(new SymbolInformation(name, kind, Convert.errorLocation(e), containerName));
            }

            @Override
            public void case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
                add(extensionFuncDef.getName(), SymbolKind.Function);
            }

            @Override
            public void case_ClassDef(ClassDef classDef) {
                String name = classDef.getName();
                add(name, SymbolKind.Class);
                for (ClassDef c : classDef.getInnerClasses()) {
                    addSymbolsForEntity(result, containerName + "." + name, c);
                }
                for (FuncDef f : classDef.getMethods()) {
                    addSymbolsForEntity(result, containerName + "." + name, f);
                }
                for (GlobalVarDef v : classDef.getVars()) {
                    addSymbolsForEntity(result, containerName + "." + name, v);
                }
            }

            @Override
            public void case_InterfaceDef(InterfaceDef interfaceDef) {
                String name = interfaceDef.getName();
                add(name, SymbolKind.Interface);
                for (FuncDef f : interfaceDef.getMethods()) {
                    addSymbolsForEntity(result, containerName + "." + name, f);
                }
                for (GlobalVarDef v : interfaceDef.getVars()) {
                    addSymbolsForEntity(result, containerName + "." + name, v);
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
                String name = moduleDef.getName();
                add(name, SymbolKind.Class);
                for (ClassDef c : moduleDef.getInnerClasses()) {
                    addSymbolsForEntity(result, containerName + "." + name, c);
                }
                for (FuncDef f : moduleDef.getMethods()) {
                    addSymbolsForEntity(result, containerName + "." + name, f);
                }
                for (GlobalVarDef v : moduleDef.getVars()) {
                    addSymbolsForEntity(result, containerName + "." + name, v);
                }
            }


        });
    }
}
