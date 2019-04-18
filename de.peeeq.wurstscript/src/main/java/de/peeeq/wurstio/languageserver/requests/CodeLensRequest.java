package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.JsonObject;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class CodeLensRequest {
    public static class GetCodeLens extends UserRequest<List<? extends CodeLens>> {
        private final CodeLensParams params;
        private final BufferManager bufferManager;

        public GetCodeLens(CodeLensParams params, BufferManager bufferManager) {
            this.params = params;
            this.bufferManager = bufferManager;
        }

        @Override
        public List<CodeLens> execute(ModelManager modelManager) {
            WFile wFile = WFile.create(params.getTextDocument());
            String buffer = bufferManager.getBuffer(wFile);
            CompilationUnit cu = modelManager.replaceCompilationUnitContent(wFile, buffer, true);
            if (cu == null) {
                return Collections.emptyList();
            }
            List<CodeLens> result = new ArrayList<>();
            for (WPackage p : cu.getPackages()) {
                for (WEntity element : p.getElements()) {
                    if (element instanceof FuncDef) {
                        FuncDef f = (FuncDef) element;
                        if (f.hasAnnotation("@test")) {
                            Range range = Convert.range(f);
                            JsonObject options = new JsonObject();
                            options.addProperty("filename", params.getTextDocument().getUri());
                            String testName = p.getName() + "." + f.getName();
                            options.addProperty("testName", testName);
                            result.add(new CodeLens(range, null, options));
                        }
                    }
                }
            }
            return result;
        }
    }

    public static class Resolve extends UserRequest<CodeLens> {
        private final CodeLens unresolved;

        public Resolve(CodeLens unresolved) {
            this.unresolved = unresolved;
        }

        @Override
        public CodeLens execute(ModelManager modelManager) throws IOException {
            Object data = unresolved.getData();
            Command cmd = new Command("Run Wurst unit test", "wurst.tests", Collections.singletonList(data));
            unresolved.setCommand(cmd);
            return unresolved;
        }
    }
}
