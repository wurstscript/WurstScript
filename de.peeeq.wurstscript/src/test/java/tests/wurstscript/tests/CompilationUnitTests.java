package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Tests using using more than one compilation unit
 */
public class CompilationUnitTests extends WurstScriptTest {


    @Test
    public void packages() {
        testAssertOk(false, false,
                compilationUnit("A.wurst",
                        "package A",
                        "endpackage"
                ),
                compilationUnit("B.wurst",
                        "package B",
                        "	import A",
                        "endpackage"
                ));
    }

    @Test
    public void jass() {
        testAssertOk(false, false,
                compilationUnit("A.wurst",
                        "function foo takes nothing returns nothing",
                        "endfunction",
                        ""
                ),
                compilationUnit("B.wurst",
                        "package B",
                        "	init",
                        "		foo()",
                        "endpackage"
                ));
    }

    @Test
    public void jassLocalHandleDefaultsToNull() {
        testAssertOkLinesWithStdLib(false,
            "function tiw takes nothing returns nothing",
            "local location uiw",
            "set uiw = null",
            "endfunction",
            "package B",
            "    init",
            "        tiw()",
            "endpackage"
        );
    }

    @Test
    public void jassLocalHandleReadWithoutExplicitInitReportsEarly() {
        testAssertWarningsLinesWithStdLib(false, "read before explicit initialization in input JASS",
            "function tiw takes nothing returns nothing",
            "local location uiw",
            "call RemoveLocation(uiw)",
            "endfunction",
            "package B",
            "    init",
            "        tiw()",
            "endpackage"
        );
    }

    @Test
    public void war3mapJassLocalHandleReadWithoutExplicitInitReportsEarly() {
        test()
            .withStdLib()
            .setStopOnFirstError(false)
            .executeProg(false)
            .expectWarning("read before explicit initialization in input JASS")
            .compilationUnits(
                compilationUnit("war3map.j",
                    "function showUnitTextAlliesWithZ takes nothing returns nothing",
                    "local location p2",
                    "call RemoveLocation(p2)",
                    "endfunction"
                )
            );
    }

    @Test
    public void jassLocalHandleReadBeforeLaterWriteStillWarns() {
        testAssertWarningsLinesWithStdLib(false, "read before explicit initialization in input JASS",
            "function tiw takes nothing returns nothing",
            "local location uiw",
            "call RemoveLocation(uiw)",
            "set uiw = GetRectCenter(GetPlayableMapRect())",
            "call RemoveLocation(uiw)",
            "set uiw = null",
            "endfunction",
            "package B",
            "    init",
            "        tiw()",
            "endpackage"
        );
    }

    /** Jass code calling a Wurst-defined top-level function must produce a validator error. */
    @Test
    public void jassCallingWurstFunctionIsError() {
        test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .expectError("Jass code cannot access Wurst symbol")
            .compilationUnits(
                compilationUnit("mylib.wurst",
                    "// Jass-compat function defined in a .wurst file",
                    "function wurstHelper takes nothing returns nothing",
                    "endfunction"
                ),
                compilationUnit("war3map.j",
                    "function jassFunc takes nothing returns nothing",
                    "    call wurstHelper()",
                    "endfunction"
                )
            );
    }

    /** Wurst code calling a plain Jass function is valid (one-way relationship). */
    @Test
    public void wurstCallingJassFunctionIsOk() {
        testAssertOk(false, false,
            compilationUnit("mymap.j",
                "function jassHelper takes nothing returns nothing",
                "endfunction"
            ),
            compilationUnit("mylib.wurst",
                "package mylib",
                "init",
                "    jassHelper()",
                "endpackage"
            )
        );
    }

    /**
     * Pre-1.24 Blizzard common.j/blizzard.j contain return-type mismatches that the
     * weakly-typed Jass VM tolerates (e.g. returning a real where an integer is declared).
     * For legacy targets these must not fail the build for input Jass; they are reported
     * as warnings instead.
     */
    @Test
    public void jassReturnTypeMismatchIsWarningForLegacyTarget() {
        test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .legacyJassTypeChecks()
            .expectWarning("Cannot return")
            .compilationUnits(
                compilationUnit("blizzard.j",
                    // real returned where integer is declared (cf. GetFadeFromSeconds)
                    "function getFadeFromSeconds takes real seconds returns integer",
                    "    return 128 / seconds",
                    "endfunction"
                )
            );
    }

    /** Without a legacy target, the same Jass mismatch must remain a hard error. */
    @Test
    public void jassReturnTypeMismatchIsErrorByDefault() {
        test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .expectError("Cannot return")
            .compilationUnits(
                compilationUnit("blizzard.j",
                    "function getFadeFromSeconds takes real seconds returns integer",
                    "    return 128 / seconds",
                    "endfunction"
                )
            );
    }

    /** The same return-type mismatch in user Wurst code must always be an error. */
    @Test
    public void wurstReturnTypeMismatchIsError() {
        test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .legacyJassTypeChecks()
            .expectError("Cannot return")
            .compilationUnits(
                compilationUnit("mylib.wurst",
                    "package mylib",
                    "function badReturn() returns int",
                    "    return 1.5",
                    "endpackage"
                )
            );
    }

}
