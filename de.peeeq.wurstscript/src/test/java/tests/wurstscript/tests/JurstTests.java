package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstscript.utils.Utils;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class JurstTests extends WurstScriptTest {

    @Test
    public void multilineString() {
        testJurst(false, false, Utils.string(
                "package test",
                "let s = \"12345678",
                "90\"",
                ""));
    }

    @Test
    public void thisAsVarNameInJass() { // #498
        String jassCode = Utils.string(
                "function foo takes integer this returns integer",
                "	return this",
                "endfunction",
                "function bar takes integer x returns integer",
                "	local integer this = x",
                "	return this",
                "endfunction");


        String jurstCode = Utils.string(
                "package test",
                "	native testSuccess()",
                "	init",
                "		if foo(5) == 5",
                "			testSuccess()",
                "		end",
                "	end",
                "endpackage");

        testJurstWithJass(true, false, jassCode, jurstCode);
    }

    private void testJurstWithJass(boolean executeProg, boolean withStdLib, String jass, String jurst) {
        Map<String, String> inputs = ImmutableMap.of(
                "example.j", jass,
                "test.jurst", jurst
        );
        testScript(Collections.emptyList(), inputs, "JurstJassTest", executeProg, withStdLib, false);
    }

    private void testJurst(boolean executeProg, boolean withStdLib, String jurst) {
        testScript("test.jurst", jurst, "JurstTest", executeProg, withStdLib);
    }

}
