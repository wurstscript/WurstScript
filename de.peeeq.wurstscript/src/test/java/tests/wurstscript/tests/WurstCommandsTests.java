package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.WurstCommands;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class WurstCommandsTests {

    @Test
    public void buildOnlyRunArgsAreExcludedFromRunAndEnabledForBuild() throws Exception {
        Path project = Files.createTempDirectory("wurst-run-args-policy");
        Files.writeString(project.resolve("wurst.build"), "projectName: PolicyTest\n");
        Files.writeString(project.resolve("wurst_run.args"), """
            -stacktraces
            +inline
            +localOptimizations
            # comments and blank lines are ignored

            """);

        WFile root = WFile.create(project.toFile());
        List<String> runArgs = WurstCommands.getCompileArgs(root);
        List<String> buildArgs = WurstCommands.getCompileArgs(root, true);

        assertTrue(runArgs.contains("-stacktraces"));
        assertFalse(runArgs.contains("-inline"));
        assertFalse(runArgs.contains("-localOptimizations"));
        assertTrue(buildArgs.contains("-stacktraces"));
        assertTrue(buildArgs.contains("-inline"));
        assertTrue(buildArgs.contains("-localOptimizations"));
        assertFalse(buildArgs.contains("-opt"), "build options require their corresponding '+' entry");
    }

    @Test
    public void missingRunArgsFileDocumentsBuildOnlyDefaults() throws Exception {
        Path project = Files.createTempDirectory("wurst-run-args-defaults");
        Files.writeString(project.resolve("wurst.build"), "projectName: DefaultsTest\n");

        WFile root = WFile.create(project.toFile());
        WurstCommands.getCompileArgs(root);

        String config = Files.readString(project.resolve("wurst_run.args"));
        assertTrue(config.contains("+opt"));
        assertTrue(config.contains("+inline"));
        assertTrue(config.contains("+localOptimizations"));
    }

    @Test
    public void explicitHotReloadOmitsBuildOnlyOptionsWhenCreatingDefaults() throws Exception {
        Path project = Files.createTempDirectory("wurst-hot-reload-defaults");
        Files.writeString(project.resolve("wurst.build"), "projectName: HotReloadDefaults\n");

        WFile root = WFile.create(project.toFile());
        List<String> args = WurstCommands.getCompileArgs(root, true, "-luaHotReload");
        assertTrue(args.containsAll(List.of("-luaHotReload", "-stacktraces")));
        assertFalse(args.contains("-opt"));
        assertFalse(args.contains("-inline"));
        assertFalse(args.contains("-localOptimizations"));

        List<String> release = WurstCommands.getCompileArgs(root, true);
        assertTrue(release.containsAll(List.of("-opt", "-inline", "-localOptimizations")));
    }

    @Test
    public void explicitHotReloadOmitsExistingBuildOnlyOptions() throws Exception {
        Path project = Files.createTempDirectory("wurst-hot-reload-explicit-args");
        Files.writeString(project.resolve("wurst.build"), "projectName: HotReloadExplicit\n");
        Files.writeString(project.resolve("wurst_run.args"), "-inline\n+opt\n+localOptimizations\n");

        List<String> args = WurstCommands.getCompileArgs(WFile.create(project.toFile()), true, "-luaHotReload");
        assertTrue(args.containsAll(List.of("-luaHotReload", "-inline")),
            "shared options remain explicit user choices, including incompatible ones");
        assertFalse(args.contains("-opt"));
        assertFalse(args.contains("-localOptimizations"));
    }
}
