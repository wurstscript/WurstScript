package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;

/**
 *
 */
public class PackageSymbol extends ExportingScopeSymbol {
    private final String name;

    public PackageSymbol(String name) {
        super(null);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public WPackage resolve(WurstModel model) {
        for (CompilationUnit cu : model) {
            for (WPackage p : cu.getPackages()) {
                if (p.getName().equals(name)) {
                    return p;
                }
            }
        }
        throw new CouldNotResolveException();
    }
}
