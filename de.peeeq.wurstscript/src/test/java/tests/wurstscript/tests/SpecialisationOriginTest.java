package tests.wurstscript.tests;

import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;

/**
 * The specialisation relation on its own.
 * <p>
 * Three passes answer questions from it that they previously answered from names, and each of those
 * name-based answers was wrong in a way that reached a user: fields dropped as dead, a value
 * dispatched through the wrong instance, a slot named after a type argument. So the relation is worth
 * covering directly rather than only through the passes which consume it.
 */
public class SpecialisationOriginTest {

    private static ImVar var(String name) {
        return JassIm.ImVar(de.peeeq.wurstscript.ast.Ast.NoExpr(), JassIm.ImSimpleType("integer"), name, false);
    }

    private static ImTranslator translator() {
        // The relation is a plain side table on the translator and touches nothing else in it, so an
        // instance with no program is enough and keeps this a unit test.
        return new ImTranslator(de.peeeq.wurstscript.ast.Ast.WurstModel(), false, null);
    }

    @Test
    public void aNodeWhichIsNotACopyIsItsOwnOriginal() {
        ImTranslator translator = translator();
        ImVar original = var("count");

        assertSame(translator.canonical(original), original);
        assertNull(translator.specialisationOf(original));
    }

    @Test
    public void aCopyLeadsBackToWhatItWasMadeFrom() {
        ImTranslator translator = translator();
        ImVar original = var("count");
        ImVar copy = var("count");
        translator.recordSpecialisation(copy, original);

        assertSame(translator.canonical(copy), original);
    }

    /** Specialising a specialisation happens, so the relation has to be followed to its root. */
    @Test
    public void aCopyOfACopyLeadsBackToTheRoot() {
        ImTranslator translator = translator();
        ImVar original = var("count");
        ImVar once = var("count");
        ImVar twice = var("count");
        translator.recordSpecialisation(once, original);
        translator.recordSpecialisation(twice, once);

        assertSame(translator.canonical(twice), original);
        assertSame(translator.canonical(once), original);
    }

    /**
     * Two nodes sharing a name are not the same parameter. This is the case the name matching this
     * relation replaced got wrong, and it crashed the interpreter rather than answering differently.
     */
    @Test
    public void sharingANameIsNotSharingAnOrigin() {
        ImTranslator translator = translator();
        ImVar first = var("T");
        ImVar second = var("T");

        assertEquals(first.getName(), second.getName());
        assertSame(translator.canonical(first), first);
        assertSame(translator.canonical(second), second);
    }

    /** The arguments a copy was made for are kept, which is what a name can be composed from. */
    @Test
    public void theTypeArgumentsOfTheCopyAreKept() {
        ImTranslator translator = translator();
        ImVar original = var("keys");
        ImVar copy = var("keys");
        List<ImTypeArgument> arguments = List.of(
            JassIm.ImTypeArgument(JassIm.ImSimpleType("integer"), Collections.emptyMap()));
        translator.recordSpecialisation(copy, original, arguments);

        ImTranslator.Specialisation specialisation = translator.specialisationOf(copy);
        assertSame(specialisation.original(), original);
        assertEquals(specialisation.typeArguments().size(), 1);
    }

    /**
     * A cycle cannot arise from specialising - a copy is always newer than what it was made from - so
     * one means a mistake elsewhere, and the relation says so rather than looping.
     */
    @Test
    public void acycleIsReportedRatherThanFollowedForever() {
        ImTranslator translator = translator();
        ImVar a = var("a");
        ImVar b = var("b");
        translator.recordSpecialisation(a, b);
        translator.recordSpecialisation(b, a);

        assertThrows(IllegalStateException.class, () -> translator.canonical(a));
    }
}
