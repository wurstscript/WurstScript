package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstscript.ast.*;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbolParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class SymbolInformationRequest extends UserRequest<List<? extends SymbolInformation>> {

    private String query;

    public SymbolInformationRequest(WorkspaceSymbolParams params) {
        query = params.getQuery().toLowerCase();
    }

    @Override
    public List<SymbolInformation> execute(ModelManager modelManager) {
        return symbolsFromModel(modelManager.getModel());
    }

    private List<SymbolInformation> symbolsFromModel(WurstModel model) {
        return model.stream()
                .flatMap(cu -> symbolsFromCu(cu).stream())
                .filter(si -> (si.getContainerName() + "." + si.getName()).toLowerCase().contains(query))
                .collect(Collectors.toList());
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
