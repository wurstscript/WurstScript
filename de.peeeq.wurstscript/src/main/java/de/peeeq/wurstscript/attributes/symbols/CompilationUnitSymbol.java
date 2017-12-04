package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;

/**
 *
 */
public class CompilationUnitSymbol extends ExportingScopeSymbol {

    private String fileName;

    public CompilationUnitSymbol(String fileName) {
        super(null);
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return "compilation unit '" + fileName + "'";
    }

    @Override
    public CompilationUnit resolve(WurstModel model) {
        for (CompilationUnit cu : model) {
            if (cu.getFile().equals(fileName)) {
                return cu;
            }
        }
        throw new CouldNotResolveException();
    }
}
