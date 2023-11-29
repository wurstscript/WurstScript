package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.*;

import java.util.Set;

public class UsedFunctions {


    public static Set<ImFunction> calculate(ImFunction imFunction) {
        final Set<ImFunction> result = Sets.newLinkedHashSet();
        imFunction.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                result.add(e.getFunc());
            }

            @Override
            public void visit(ImFuncRef e) {
                super.visit(e);
                result.add(e.getFunc());
            }

            @Override
            public void visit(ImMethodCall e) {
                super.visit(e);
                for(ImMethod sub : e.getMethod().getSubMethods()) {
                    result.add(sub.getImplementation());
                }
                result.add(e.getMethod().getImplementation());
            }
        });
        return result;
    }

}
