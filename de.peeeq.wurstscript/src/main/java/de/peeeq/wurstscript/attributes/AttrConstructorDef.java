package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;


/**
 * find the constructor for a "new" call
 */
public class AttrConstructorDef {

    public static @Nullable ConstructorDef calculate(final ExprNewObject node) {
        @Nullable FunctionDefinition functionDefinition = node.attrFuncDef();
        if (functionDefinition instanceof ConstructorDef) {
            return (ConstructorDef) functionDefinition;
        }
        return null;
    }


}
