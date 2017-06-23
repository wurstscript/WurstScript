package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WPackage;

public class GetByType {

    public static ByTypes calculate(CompilationUnit cu) {
        final ByTypes result = new ByTypes();
        cu.accept(new CompilationUnit.DefaultVisitor() {

            @Override
            public void visit(@SuppressWarnings("null") ClassDef classDef) {
                result.classes.add(classDef);
            }


            @Override
            public void visit(@SuppressWarnings("null") WPackage wPackage) {
                result.packageDefs.add(wPackage);
            }
        });

        return result;
    }

}
