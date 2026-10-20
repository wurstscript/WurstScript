package tests.wurstscript.tests;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * {@link TreeShakerTests} relies on the translator naming every function it keeps for later passes. A field which
 * holds one and is not in {@link ImTranslator#pinnedFunctions()} would let the early tree shake remove a function
 * which a later pass calls through the handle, and the Lua translation then fails on a dangling reference.
 */
public class ImTranslatorPinnedFunctionsTests {
    /** The function fields which are roots of every reachability analysis anyway. */
    private static final Set<String> ROOTS = Set.of("mainFunc", "configFunc");

    @Test
    public void everyFunctionFieldOfTheTranslatorIsPinnedOrARoot() throws Exception {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), true, RunArgs.defaults());
        Set<String> notPinned = new TreeSet<>();
        for (Field field : ImTranslator.class.getDeclaredFields()) {
            if (field.getType() != ImFunction.class || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            ImFunction marker = marker(field.getName());
            field.set(translator, marker);
            boolean pinned = containsIdentical(translator.pinnedFunctions(), marker);
            field.set(translator, null);
            if (!pinned && !ROOTS.contains(field.getName())) {
                notPinned.add(field.getName());
            }
        }
        assertEquals(notPinned, Collections.<String>emptySet(),
            "a function the translator keeps for later passes has to be listed in ImTranslator.pinnedFunctions()");
    }

    @Test
    public void pinnedFunctionsOfAFreshTranslatorAreNone() {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), true, RunArgs.defaults());
        assertTrue(translator.pinnedFunctions().isEmpty(), translator.pinnedFunctions().toString());
    }

    private static ImFunction marker(String name) {
        return JassIm.ImFunction(Ast.NoExpr(), name, JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
    }

    private static boolean containsIdentical(List<ImFunction> functions, ImFunction wanted) {
        Set<ImFunction> identity = Collections.newSetFromMap(new IdentityHashMap<>());
        identity.addAll(functions);
        return identity.contains(wanted);
    }
}
