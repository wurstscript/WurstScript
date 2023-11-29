package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WPackage;

public class GetByType {

    public static ByTypes calculate(CompilationUnit cu) {
        final ByTypes result = new ByTypes();
        cu.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(@SuppressWarnings("null") ClassDef classDef) {
                super.visit(classDef);
                result.classes.add(classDef);
            }


            @Override
            public void visit(@SuppressWarnings("null") WPackage wPackage) {
                super.visit(wPackage);
                result.packageDefs.add(wPackage);
            }
        });

        return result;
    }

}
