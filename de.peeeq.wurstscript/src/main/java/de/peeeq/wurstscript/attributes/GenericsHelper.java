package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import fj.data.TreeMap;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class GenericsHelper {
    /**
     * Return the type parameter binding given by the user (if any)
     */
    public static TreeMap<TypeParamDef, WurstTypeBoundTypeParam> givenBinding(AstElementWithTypeArgs e, List<TypeParamDef> typeParams) {
        TreeMap<TypeParamDef, WurstTypeBoundTypeParam> res = WurstType.emptyMapping();
        TypeExprList typeArgs = e.getTypeArgs();
        for (int i = 0; i < typeArgs.size() && i < typeParams.size(); i++) {
            TypeParamDef tp = typeParams.get(i);
            TypeExpr te = typeArgs.get(i);
            res = res.set(tp, new WurstTypeBoundTypeParam(tp, te.attrTyp(), e));
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
