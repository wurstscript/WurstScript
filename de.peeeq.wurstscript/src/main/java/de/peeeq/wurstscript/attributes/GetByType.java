package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;

public class GetByType {

    public static ByTypes calculate(CompilationUnit cu) {
        final ByTypes result = new ByTypes();
        cu.accept(new Element.DefaultVisitor() {

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
