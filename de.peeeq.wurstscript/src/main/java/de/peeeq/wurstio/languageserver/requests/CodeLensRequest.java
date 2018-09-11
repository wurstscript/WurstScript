package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.ExprStringVal;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CodeLensRequest extends UserRequest<List<? extends CodeLens>> {
    private CodeLensParams params;

    public CodeLensRequest(CodeLensParams params) {
        this.params = params;
    }

    @Override
    public List<? extends CodeLens> execute(ModelManager modelManager) throws IOException {
        ArrayList<CodeLens> result = new ArrayList<>();
        CompilationUnit cu = modelManager.getCompilationUnit(WFile.create(params.getTextDocument()));
        cu.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ExprStringVal exprStringVal) {
                super.visit(exprStringVal);
                String valS = exprStringVal.getValS();
                if(valS.endsWith(".blp")) {
                    if(valS.contains("CommandButtons")) {
                        result.add(new CodeLens(Convert.range(exprStringVal), new Command("Browse Icon", "wurst.browseIcon"), valS));
                    } else {
                        result.add(new CodeLens(Convert.range(exprStringVal), new Command("Browse Texture", "wurst.browseTexture"), valS));
                    }
                }
            }
        });
        return result;
    }
}
