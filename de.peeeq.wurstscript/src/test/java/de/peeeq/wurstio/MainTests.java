package de.peeeq.wurstio;

import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.RunArgs;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MainTests {

    @Test
    public void noArgumentsReturnsWithoutStartingInteractiveComponents() {
        String output = captureStdout(() -> Main.main(new String[0]));
        assertTrue(output.contains("Usage:"));
    }

    @Test
    public void versionPrintsCompilerVersion() {
        String output = captureStdout(() -> Main.main(new String[]{"-version"}));
        assertEquals(output.trim(), CompileTimeInfo.version);
    }

    @Test
    public void hotReloadBuildTakesTheRunArgumentsNotTheBuildOnlyOnes() throws Exception {
        Path project = Files.createTempDirectory("wurst-hot-reload-build-args");
        Files.writeString(project.resolve("wurst.build"), "projectName: HotReloadArgs\n");
        // What the compiler writes into a new project's wurst_run.args.
        Files.writeString(project.resolve("wurst_run.args"), "-stacktraces\n+opt\n+inline\n+localOptimizations\n");

        List<String> hotReload = Main.workspaceCompileArgs(project.toString(), new RunArgs("-build", "-luaHotReload"));
        assertTrue(hotReload.contains("-stacktraces"));
        assertFalse(hotReload.contains("-opt"), "-luaHotReload refuses -opt, so a hot reload build cannot take it");
        assertFalse(hotReload.contains("-inline"));
        assertFalse(hotReload.contains("-localOptimizations"));

        List<String> release = Main.workspaceCompileArgs(project.toString(), new RunArgs("-build"));
        assertTrue(release.containsAll(List.of("-stacktraces", "-opt", "-inline", "-localOptimizations")));
    }

    @Test
    public void hotReloadInTheWorkspaceFileTakesRunArgumentsForBuilds() throws Exception {
        Path project = Files.createTempDirectory("wurst hot reload workspace args ");
        Files.writeString(project.resolve("wurst.build"), "projectName: HotReloadArgs\n");
        Files.writeString(project.resolve("wurst_run.args"),
            "  -luaHotReload  \r\n-stacktraces\r\n-localOptimizations\r\n+opt\r\n+inline\r\n+localOptimizations\r\n");

        List<String> args = Main.workspaceCompileArgs(project.toString(), new RunArgs("-build"));
        assertTrue(args.containsAll(List.of("-luaHotReload", "-stacktraces", "-localOptimizations")));
        assertFalse(args.contains("-opt"));
        assertFalse(args.contains("-inline"));
        assertEquals(args.stream().filter("-localOptimizations"::equals).count(), 1L);
    }

    private String captureStdout(Runnable action) {
        PrintStream previousOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            action.run();
        } finally {
            System.setOut(previousOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
