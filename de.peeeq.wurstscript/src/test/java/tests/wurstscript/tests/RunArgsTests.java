package tests.wurstscript.tests;

import de.peeeq.wurstscript.RunArgs;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Set;

public class RunArgsTests {

    @Test
    public void parsesFlagsAndValues() {
        RunArgs args = new RunArgs(
            "-lua", "-opt", "-inline", "-localOptimizations", "-runcompiletimefunctions",
            "-testTimeout=7", "-testFilter", "Foo", "-functionSplitLimit", "42",
            "-workspaceroot", "project", "-inputmap=source.w3x", "-out", "output.j",
            "-lib", "lib", "source.w3x", "helper.wurst");

        Assert.assertTrue(args.isLua());
        Assert.assertTrue(args.isOptimize());
        Assert.assertTrue(args.isInline());
        Assert.assertTrue(args.isLocalOptimizations());
        Assert.assertTrue(args.runCompiletimeFunctions());
        Assert.assertEquals(args.getTestTimeout(), 7);
        Assert.assertEquals(args.getTestFilter().orElseThrow(), "Foo");
        Assert.assertEquals(args.getFunctionSplitLimit(), 42);
        Assert.assertEquals(args.getWorkspaceroot(), "project");
        Assert.assertEquals(args.getInputmap(), "source.w3x");
        Assert.assertEquals(args.getOutFile(), "output.j");
        Assert.assertEquals(args.getMapFile(), "source.w3x");
        Assert.assertEquals(args.getFiles(), java.util.List.of("source.w3x", "helper.wurst"));
        Assert.assertEquals(args.getAdditionalLibDirs(), java.util.List.of(new File("lib")));
    }

    @Test
    public void exposesModeAndSafetyFlags() {
        RunArgs args = new RunArgs("-build", "-dev", "-stacktraces", "-nodebug", "-uncheckedDispatch",
            "-injectobjects", "-hotreload", "-hotstart", "-noPJass", "-legacyJassChecks",
            "-compactOutput", "-measure", "-compiletimeCache", "-noExtractMapScript", "-copyMap",
            "-prettyPrint", "-languageServer", "-languageServerAppCdsTrain");

        Assert.assertTrue(args.isBuild());
        Assert.assertTrue(args.isDevBuild());
        Assert.assertTrue(args.isIncludeStacktraces());
        Assert.assertTrue(args.isNoDebugMessages());
        Assert.assertTrue(args.isUncheckedDispatch());
        Assert.assertFalse(args.isInjectObjects(), "hot reload suppresses object injection");
        Assert.assertTrue(args.isHotReload());
        Assert.assertTrue(args.isHotStartmap());
        Assert.assertTrue(args.isDisablePjass());
        Assert.assertTrue(args.isLegacyJassTypeChecks());
        Assert.assertTrue(args.isCompactOutput());
        Assert.assertTrue(args.isMeasureTimes());
        Assert.assertTrue(args.isCompiletimeCache());
        Assert.assertTrue(args.isNoExtractMapScript());
        Assert.assertTrue(args.isCopyMap());
        Assert.assertTrue(args.isPrettyPrint());
        Assert.assertTrue(args.isLanguageServer());
        Assert.assertTrue(args.isLanguageServerAppCdsTrain());
    }

    @Test
    public void supportsProgrammaticMutationAndAdditionalLibraries() {
        RunArgs args = new RunArgs();
        args.setMapFile("map.w3x");
        args.setLegacyJassTypeChecks(true);
        args.addLibs(Set.of("one", "two"));
        args.addLibDirs(Set.of(new File("three")));

        Assert.assertEquals(args.getMapFile(), "map.w3x");
        Assert.assertTrue(args.isLegacyJassTypeChecks());
        Assert.assertEquals(args.getAdditionalLibDirs().size(), 3);
        Assert.assertThrows(UnsupportedOperationException.class, () -> args.getAdditionalLibDirs().add(new File("four")));
    }

    @Test
    public void rejectsUnknownAndMissingArguments() {
        Assert.assertThrows(RuntimeException.class, () -> new RunArgs("-doesNotExist"));
        Assert.assertThrows(RuntimeException.class, () -> new RunArgs("-out="));
        Assert.assertThrows(ArrayIndexOutOfBoundsException.class, () -> new RunArgs("-out"));
    }
}
