package tests.prettyprint;

import de.peeeq.wurstscript.attributes.prettyPrint.PrettyUtils;
import org.testng.annotations.Test;
import tests.wurstscript.tests.WurstScriptTest;

import static org.testng.AssertJUnit.fail;

public class PrettyPrintErrorTest extends WurstScriptTest {

    @Test
    public void testPrettyPrintFailsWithCompilationErrors() {
        String invalidCode = "package TestInvalid\n\nfunction test()\n    undefinedFunction()\n    let x = undefinedVariable";
        
        try {
            String result = PrettyUtils.pretty(invalidCode, ".wurst");
            fail("Expected RuntimeException for code with compilation errors, but pretty printing succeeded");
        } catch (RuntimeException e) {
            // Expected behavior - pretty printer should fail with compilation errors
            assert e.getMessage().contains("Cannot format code with compilation errors");
        }
    }
    
    @Test
    public void testPrettyPrintSucceedsWithValidCode() {
        String validCode = "package TestValid\n\nfunction test()\n    print(\"Hello World\")";
        
        try {
            String result = PrettyUtils.pretty(validCode, ".wurst");
            // Should succeed without throwing an exception
            assert result != null;
            assert result.contains("package TestValid");
        } catch (Exception e) {
            fail("Pretty printing valid code should not throw an exception: " + e.getMessage());
        }
    }
}