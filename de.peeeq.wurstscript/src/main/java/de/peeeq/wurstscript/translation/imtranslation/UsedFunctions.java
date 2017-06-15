package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;

import java.util.Set;

public class UsedFunctions {


    public static Set<ImFunction> calculate(ImFunction imFunction) {
        final Set<ImFunction> result = Sets.newLinkedHashSet();
        imFunction.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall e) {
                result.add(e.getFunc());
            }

            @Override
            public void visit(ImFuncRef e) {
                result.add(e.getFunc());
            }
        });
        return result;
    }

}
