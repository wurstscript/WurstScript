package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.jassIm.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeRewriter {

    /**
     * Visits all elements where a type can be used and
     * applies the given function to rewrite the type.
     */
    public static void rewriteTypes(Element e, Function<ImType, ImType> rewriteType) {
        e.accept(new Element.DefaultVisitor() {

            private ImType rewriteType(ImType type) {
                return rewriteType.apply(type);
            }


            @Override
            public void visit(ImVar e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImFunction e) {
                super.visit(e);
                e.setReturnType(rewriteType(e.getReturnType()));
            }

            @Override
            public void visit(ImNull e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImTypeArgument e) {
                super.visit(e);
                e.setType(rewriteType(e.getType()));
            }

            @Override
            public void visit(ImClass e) {
                super.visit(e);
                List<ImClassType> newSuperClasses = e.getSuperClasses().stream()
                        .map(tt -> (ImClassType) rewriteType(tt))
                        .collect(Collectors.toList());
                e.setSuperClasses(newSuperClasses);
            }

            @Override
            public void visit(ImMethod e) {
                super.visit(e);
                e.setMethodClass((ImClassType) rewriteType(e.getMethodClass()));
            }

            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImDealloc e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                super.visit(e);
                e.setClazz((ImClassType) rewriteType(e.getClazz()));
            }

            @Override
            public void visit(ImCast e) {
                super.visit(e);
                e.setToType(rewriteType(e.getToType()));
            }
        });
    }

}
