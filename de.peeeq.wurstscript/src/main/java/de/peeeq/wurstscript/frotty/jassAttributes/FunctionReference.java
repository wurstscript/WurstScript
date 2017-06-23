package de.peeeq.wurstscript.frotty.jassAttributes;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.Element;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProgs;
import org.eclipse.jdt.annotation.Nullable;

public class FunctionReference {

    public static @Nullable JassFunction get(JassExprFuncRef ref) {
        String funcName = ref.getFuncName();
        Element node = ref.getParent();
        while (node != null) {
            if (node instanceof JassProgs) {
                JassProgs jassProgs = (JassProgs) node;
                JassFunction v = jassProgs.getFunction(funcName);
                if (v != null) {
                    return v;
                }
            }
            node = node.getParent();
        }
        JassErrors.addError("Could not find function '" + funcName + "'.", ref.getLine());
        return null;
    }

}
