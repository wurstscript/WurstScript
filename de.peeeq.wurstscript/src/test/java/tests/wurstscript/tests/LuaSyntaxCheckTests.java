package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class LuaSyntaxCheckTests {

    @Test
    public void luacSyntaxErrorsAreDetected() throws Exception {
        WurstScriptTest helper = new WurstScriptTest();
        String luacExecutable = invokeGetLuacExecutable(helper);

        File invalidLua = File.createTempFile("wurst-invalid-lua-", ".lua");
        invalidLua.deleteOnExit();
        java.nio.file.Files.writeString(invalidLua.toPath(), "function broken(\n", StandardCharsets.UTF_8);

        Method checkLuaSyntax = WurstScriptTest.class.getDeclaredMethod("checkLuaSyntax", String.class, File.class);
        checkLuaSyntax.setAccessible(true);
        try {
            checkLuaSyntax.invoke(helper, luacExecutable, invalidLua);
            fail("Expected syntax check to fail for invalid Lua source.");
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            assertTrue(cause instanceof IOException,
                "Expected IOException but got: " + (cause == null ? "null" : cause.getClass().getName()));
            assertTrue(cause.getMessage() != null && cause.getMessage().contains("Lua syntax check failed"),
                "Expected syntax check failure message, but got: " + cause.getMessage());
        }
    }

    private String invokeGetLuacExecutable(WurstScriptTest helper) throws Exception {
        Method getLuacExecutable = WurstScriptTest.class.getDeclaredMethod("getLuacExecutable");
        getLuacExecutable.setAccessible(true);
        Object result = getLuacExecutable.invoke(helper);
        return (String) result;
    }
}

