package de.peeeq.wurstio;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CompilationProcessLuaTests {

    @Test
    public void luaModeDoesNotEmitJassScript() throws Exception {
        Path project = Files.createTempDirectory("wurst-cli-lua");
        Path source = project.resolve("Main.wurst");
        Path requestedJassOutput = project.resolve("output.j");
        Path output = project.resolve("output.lua");
        Files.writeString(source, "package Main\nfunction foo()\nendpackage\n");
        Files.writeString(requestedJassOutput, "stale jass output");

        RunArgs runArgs = new RunArgs("-lua", "-out", requestedJassOutput.toString(), source.toString());
        CompilationProcess process = new CompilationProcess(new WurstGuiCliImpl(true), runArgs);

        CharSequence result = process.doCompilation(null, project.toFile(), false);

        assertTrue(result != null, "Lua compilation should succeed");
        assertTrue(Files.exists(output), "Lua output should be written");
        assertFalse(Files.exists(requestedJassOutput), "Lua compilation must not emit a .j file");
        assertFalse(result.toString().contains("takes nothing returns nothing"),
            "CLI Lua compilation must not use the Jass backend");
    }
}
