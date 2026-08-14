package tests.wurstscript.tests;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImTypeClassFunc;
import de.peeeq.wurstscript.jassIm.ImTypeVar;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtojass.TypeSubst;
import io.vavr.control.Either;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Tests for substitution of type variables.
 * <p>
 * The property that matters beyond replacing a variable by a type is that a type argument is a type
 * together with what is known about it. Substituting in an argument position has to carry the whole
 * argument across, because the type class binding is what lets a bound resolve to a direct call.
 */
public class TypeSubstTests {

    @Test
    public void substitutesTypeVariable() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        TypeSubst subst = TypeSubst.of(List.of(t), List.of(argument(integer())));

        assertEquals(typename(subst.apply(JassIm.ImTypeVarRef(t))), "integer");
    }

    @Test
    public void leavesVariablesItDoesNotCoverAlone() {
        ImTypeVar covered = JassIm.ImTypeVar("T");
        ImTypeVar other = JassIm.ImTypeVar("U");
        TypeSubst subst = TypeSubst.of(List.of(covered), List.of(argument(integer())));

        ImTypeVarRef ref = JassIm.ImTypeVarRef(other);
        assertSame(subst.apply((ImType) ref), ref);
    }

    /** A variable stands for one declaration; sharing a name with another does not make it the same. */
    @Test
    public void distinguishesVariablesSharingAName() {
        ImTypeVar declared = JassIm.ImTypeVar("T");
        ImTypeVar unrelated = JassIm.ImTypeVar("T");
        TypeSubst subst = TypeSubst.of(List.of(declared), List.of(argument(integer())));

        ImTypeVarRef ref = JassIm.ImTypeVarRef(unrelated);
        assertSame(subst.apply((ImType) ref), ref);
    }

    /**
     * The regression this exists for: substituting {@code Box<T>} with an argument that carries an
     * instance has to yield {@code Box<integer>} still carrying it. Returning only the type left the
     * argument bare, and the dispatch inside the body then had nothing to resolve against.
     */
    @Test
    public void bindingSurvivesIntoArgumentPosition() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        ImTypeClassFunc requirement = requirement("toIndex");
        ImFunction instance = function("intToIndex");
        TypeSubst subst = TypeSubst.of(List.of(t),
            List.of(JassIm.ImTypeArgument(integer(), binding(requirement, instance))));

        ImClassType boxOfT = classType("Box", argument(JassIm.ImTypeVarRef(t)));
        ImTypeArgument substituted = onlyArgument(subst.apply(boxOfT));

        assertEquals(typename(substituted.getType()), "integer");
        assertEquals(substituted.getTypeClassBinding().size(), 1, "the binding must travel with the type");
        assertSame(substituted.getTypeClassBinding().get(requirement).get(), instance);
    }

    /** Applied directly to an argument rather than reached through a class type. */
    @Test
    public void bindingSurvivesOnADirectArgument() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        ImTypeClassFunc requirement = requirement("toIndex");
        ImFunction instance = function("intToIndex");
        TypeSubst subst = TypeSubst.of(List.of(t),
            List.of(JassIm.ImTypeArgument(integer(), binding(requirement, instance))));

        ImTypeArgument substituted = subst.apply(argument(JassIm.ImTypeVarRef(t)));

        assertEquals(typename(substituted.getType()), "integer");
        assertSame(substituted.getTypeClassBinding().get(requirement).get(), instance);
    }

    /** An argument which already names an instance keeps it: it is the more specific of the two. */
    @Test
    public void bindingAlreadyPresentIsKept() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        ImTypeClassFunc requirement = requirement("toIndex");
        ImFunction fromUse = function("chosenAtUse");
        ImFunction fromSubstitution = function("chosenBySubstitution");

        TypeSubst subst = TypeSubst.of(List.of(t),
            List.of(JassIm.ImTypeArgument(integer(), binding(requirement, fromSubstitution))));
        ImClassType boxOfT = classType("Box",
            JassIm.ImTypeArgument(JassIm.ImTypeVarRef(t), binding(requirement, fromUse)));

        ImTypeArgument substituted = onlyArgument(subst.apply(boxOfT));
        assertSame(substituted.getTypeClassBinding().get(requirement).get(), fromUse);
    }

    /** Nested arguments are reached too, so a bound on an inner position resolves the same way. */
    @Test
    public void bindingSurvivesThroughNesting() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        ImTypeClassFunc requirement = requirement("toIndex");
        ImFunction instance = function("intToIndex");
        TypeSubst subst = TypeSubst.of(List.of(t),
            List.of(JassIm.ImTypeArgument(integer(), binding(requirement, instance))));

        ImClassType inner = classType("Box", argument(JassIm.ImTypeVarRef(t)));
        ImClassType outer = classType("List", argument(inner));

        ImTypeArgument substituted = onlyArgument(onlyArgument(subst.apply(outer)).getType());
        assertEquals(typename(substituted.getType()), "integer");
        assertSame(substituted.getTypeClassBinding().get(requirement).get(), instance);
    }

    /**
     * Fewer arguments than variables is allowed as long as the missing ones are not named, because a
     * use may legitimately leave the arguments off.
     */
    @Test
    public void missingArgumentIsToleratedUntilItIsNamed() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        TypeSubst subst = TypeSubst.of(List.of(t), List.of());

        ImType untouched = integer();
        assertSame(subst.apply(untouched), untouched);
        assertThrows(RuntimeException.class, () -> subst.apply(JassIm.ImTypeVarRef(t)));
    }

    @Test
    public void emptySubstitutionChangesNothing() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        ImClassType boxOfT = classType("Box", argument(JassIm.ImTypeVarRef(t)));

        assertSame(TypeSubst.of(List.of(), List.of()).apply((ImType) boxOfT), boxOfT);
        assertTrue(TypeSubst.empty().isEmpty());
    }

    /** A variable named twice keeps the first argument, which is what positional lookup did. */
    @Test
    public void repeatedVariableKeepsItsFirstArgument() {
        ImTypeVar t = JassIm.ImTypeVar("T");
        TypeSubst subst = TypeSubst.of(List.of(t, t),
            List.of(argument(integer()), argument(JassIm.ImSimpleType("real"))));

        assertEquals(typename(subst.apply(JassIm.ImTypeVarRef(t))), "integer");
    }

    // --- helpers ---------------------------------------------------------------------------------

    private static ImType integer() {
        return JassIm.ImSimpleType("integer");
    }

    private static String typename(ImType type) {
        assertTrue(type instanceof ImSimpleType, "expected a simple type but got " + type);
        return ((ImSimpleType) type).getTypename();
    }

    private static ImTypeArgument argument(ImType type) {
        return JassIm.ImTypeArgument(type, Collections.emptyMap());
    }

    private static ImTypeArgument onlyArgument(ImType type) {
        assertTrue(type instanceof ImClassType, "expected a class type but got " + type);
        ImClassType classType = (ImClassType) type;
        assertEquals(classType.getTypeArguments().size(), 1);
        return classType.getTypeArguments().get(0);
    }

    private static ImClassType classType(String name, ImTypeArgument argument) {
        ImClass classDef = JassIm.ImClass(trace(), name, JassIm.ImTypeVars(JassIm.ImTypeVar(name + "Param")),
            JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), List.of());
        return JassIm.ImClassType(classDef, JassIm.ImTypeArguments(argument));
    }

    private static ImTypeClassFunc requirement(String name) {
        return JassIm.ImTypeClassFunc(trace(), name, JassIm.ImTypeVars(), JassIm.ImVars(), integer());
    }

    private static ImFunction function(String name) {
        return JassIm.ImFunction(trace(), name, JassIm.ImTypeVars(), JassIm.ImVars(), integer(),
            JassIm.ImVars(), JassIm.ImStmts(), List.of());
    }

    /** IM nodes keep a trace back to the source they came from; these have none, so stand one in. */
    private static Element trace() {
        return Ast.NoExpr();
    }

    private static Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding(ImTypeClassFunc requirement,
                                                                              ImFunction instance) {
        Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> binding = new LinkedHashMap<>();
        binding.put(requirement, Either.right(instance));
        return binding;
    }
}
