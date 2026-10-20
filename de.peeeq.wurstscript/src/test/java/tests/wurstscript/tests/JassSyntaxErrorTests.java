package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Input Jass which is not valid Jass must be a reported error. The fork protects maps whose script is parsed Jass, so a
 * declaration which the parser accepts but cannot translate, and drops without a word, ships a broken map.
 */
public class JassSyntaxErrorTests extends WurstScriptTest {

    private void assertJassError(String expectedError, String... lines) {
        test().expectError(expectedError).compilationUnits(compilationUnit("u.j", lines));
    }

    @Test
    public void untypedLocal() {
        assertJassError("line 3: missing ID at '='",
            "native Print takes string msg returns nothing",
            "function main takes nothing returns nothing",
            "    local hero = \"h\"",
            "    call Print(hero)",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    @Test
    public void untypedArrayLocal() {
        assertJassError("extraneous input 'array' expecting ID",
            "function main takes nothing returns nothing",
            "    local array a",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    @Test
    public void untypedGlobal() {
        assertJassError("line 2: missing ID at '='",
            "globals",
            "    hero = 1",
            "endglobals",
            "function main takes nothing returns nothing",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    @Test
    public void untypedParameter() {
        assertJassError("missing ID at 'returns'",
            "native Print takes msg returns nothing",
            "function main takes nothing returns nothing",
            "    call Print(\"h\")",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    @Test
    public void missingReturnType() {
        assertJassError("mismatched input '\\n' expecting {'nothing', ID}",
            "function f takes nothing returns",
            "endfunction",
            "function main takes nothing returns nothing",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    @Test
    public void wordBetweenFunctionNameAndArguments() {
        assertJassError("extraneous input 'integer' expecting '('",
            "native Print takes string msg returns nothing",
            "function main takes nothing returns nothing",
            "    call Print integer(\"h\")",
            "endfunction",
            "function config takes nothing returns nothing",
            "endfunction");
    }

    /** More syntax errors than the parser reports: the file is given up, and what it reported fails the compilation. */
    @Test
    public void manySyntaxErrors() {
        List<String> lines = new ArrayList<>();
        lines.add("function main takes nothing returns nothing");
        for (int i = 0; i < 40; i++) {
            lines.add("    call");
        }
        lines.add("endfunction");
        lines.add("function config takes nothing returns nothing");
        lines.add("endfunction");
        assertJassError("line 2: mismatched input '\\n' expecting ID", lines.toArray(new String[0]));
    }

    private static List<CompileError> parseJass(String source) {
        WurstGuiLogger gui = new WurstGuiLogger();
        new WurstParser(new ErrorHandler(gui), gui).parseJass(new StringReader(source), "u.j", false);
        return gui.getErrorList();
    }

    /**
     * An error at the end of the file is reported at the code before it, not at the first character of the file: the
     * start of the error is moved back over the whitespace it is in, and only over that.
     */
    @Test
    public void anErrorAtTheEndOfTheFileIsReportedAtTheLastCode() {
        List<CompileError> errors = parseJass(
            "native A takes nothing returns nothing\n"
                + "function f takes nothing returns nothing\n"
                + "    call A()\n"
                + "\n\n   \n");
        assertFalse(errors.isEmpty(), "the missing endfunction is reported");
        assertEquals(errors.get(0).getSource().getLine(), 3, errors.get(0).toString());
    }
}
