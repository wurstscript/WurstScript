package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.types.WurstType;

/**
 *
 */
public class AttrReturnTyp {
    public static WurstType calculate(FunctionDefinition f) {
        return f.getReturnTyp().attrTyp().dynamic();
    }
}
