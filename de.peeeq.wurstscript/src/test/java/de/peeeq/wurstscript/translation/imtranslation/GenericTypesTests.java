package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImTypeClassFunc;
import de.peeeq.wurstscript.jassIm.JassIm;
import io.vavr.control.Either;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class GenericTypesTests {

    @Test
    public void nestedTypeClassBindingsDoNotSplitSpecializationKeys() {
        ImClass box = genericClass("Box");
        ImClass list = genericClass("List");
        ImSimpleType integer = JassIm.ImSimpleType("integer");
        ImTypeClassFunc requirement = JassIm.ImTypeClassFunc(Ast.NoExpr(), "toIndex",
            JassIm.ImTypeVars(), JassIm.ImVars(), integer);
        ImFunction instance = JassIm.ImFunction(Ast.NoExpr(), "intToIndex", JassIm.ImTypeVars(),
            JassIm.ImVars(), integer, JassIm.ImVars(), JassIm.ImStmts(), List.of());

        Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding = new LinkedHashMap<>();
        binding.put(requirement, Either.right(instance));
        ImClassType unboundBox = JassIm.ImClassType(box,
            JassIm.ImTypeArguments(argument(integer, Collections.emptyMap())));
        ImClassType boundBox = JassIm.ImClassType(box,
            JassIm.ImTypeArguments(argument(integer, binding)));

        GenericTypes unbound = key(list, unboundBox);
        GenericTypes bound = key(list, boundBox);

        assertEquals(bound, unbound,
            "type-class dispatch metadata must not change a structural specialization key");
        assertEquals(bound.hashCode(), unbound.hashCode());
    }

    private static GenericTypes key(ImClass list, ImClassType nestedType) {
        ImClassType listType = JassIm.ImClassType(list,
            JassIm.ImTypeArguments(argument(nestedType, Collections.emptyMap())));
        return new GenericTypes(List.of(argument(listType, Collections.emptyMap())));
    }

    private static ImTypeArgument argument(de.peeeq.wurstscript.jassIm.ImType type,
                                           Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding) {
        return JassIm.ImTypeArgument(type, binding);
    }

    private static ImClass genericClass(String name) {
        return JassIm.ImClass(Ast.NoExpr(), name, JassIm.ImTypeVars(JassIm.ImTypeVar("T")),
            JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), List.of());
    }
}
