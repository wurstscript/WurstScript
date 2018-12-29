package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class GenericsHelper {
    /**
     * Return the type parameter binding given by the user (if any)
     */
    public static VariableBinding givenBinding(AstElementWithTypeArgs e, List<TypeParamDef> typeParams) {
        VariableBinding res = VariableBinding.emptyMapping().withTypeVariables(fj.data.List.iterableList(typeParams));
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
