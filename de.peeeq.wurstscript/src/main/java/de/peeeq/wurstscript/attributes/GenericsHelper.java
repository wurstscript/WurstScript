package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class GenericsHelper {
    /**
     * Return the type parameter binding given by the user (if any)
     */
    public static VariableBinding givenBinding(AstElementWithTypeArgs e, List<TypeParamDef> typeParams) {
        VariableBinding res = VariableBinding.emptyMapping().withTypeVariables(typeParams);
        TypeExprList typeArgs = e.getTypeArgs();
        for (int i = 0; i < typeArgs.size() && i < typeParams.size(); i++) {
            TypeParamDef tp = typeParams.get(i);
            TypeExpr te = typeArgs.get(i);
            res = res.set(tp, new WurstTypeBoundTypeParam(tp, te.attrTyp().dynamic(), e));
        }
        if (typeArgs.size() > typeParams.size()) {
            res = res.withError(new CompileError(typeArgs.get(typeParams.size()).attrErrorPos(),
                    "Too many type arguments given"));
        }
        return res;
    }

    public static List<TypeParamDef> typeParameters(StructureDef struct) {
        if (struct instanceof AstElementWithTypeParameters) {
            return ((AstElementWithTypeParameters) struct).getTypeParameters();
        }
        return Collections.emptyList();
    }
}
