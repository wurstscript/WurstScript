package de.peeeq.wurstscript;

import com.google.common.collect.Lists;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RunArgs {


    private final String[] args;
    private final RunOption optionLua;
    private final RunOption optionCompiletimeCache;
    private final List<String> files = Lists.newArrayList();
    private @Nullable String mapFile = null;
    private @Nullable String exportObjectsFile = null;
    private @Nullable String exportObjectsOut = null;
    private @Nullable String outFile = null;
    private @Nullable String workspaceroot = null;
    private @Nullable String inputmap = null;
    private @Nullable int testTimeout = 20;
    private @Nullable String testFilter = null;
    private @Nullable String benchmarkFilter = null;
    private @Nullable String benchmarkName = null;
    private @Nullable String benchmarkOutput = null;
    private int benchmarkWarmup = 0;
    private int benchmarkIterations = 1;
    private final List<RunOption> options = Lists.newArrayList();
    private final List<File> libDirs = Lists.newArrayList();
    private final RunOption optionHelp;
    private final RunOption optionOpt;
    private final RunOption optionInline;
    private final RunOption optionLocalOptimizations;
    private final RunOption optionRuntests;
    private final RunOption optionRunBenchmarks;
    private final RunOption optionBenchmarkList;
    private final RunOption optionBenchmarkFilter;
    private final RunOption optionBenchmarkName;
    private final RunOption optionBenchmarkWarmup;
    private final RunOption optionBenchmarkIterations;
    private final RunOption optionBenchmarkOutput;
    private final RunOption optionGui;
    private final RunOption optionAbout;
    private final RunOption optionShowErrors;
    private final RunOption optionRunCompileTimeFunctions;
    private final RunOption optionStacktraces;
    private final RunOption uncheckedDispatch;
    private final RunOption optionNodebug;
    private final RunOption optionInjectCompiletimeObjects;
    private final RunOption optionExtractImports;
    private final RunOption optionExportObjects;
    private final RunOption optionStartServer;
    private final RunOption optionLanguageServer;
    private final RunOption optionLanguageServerAppCdsTrain;
    private final RunOption optionNoExtractMapScript;
    private final RunOption optionFixInstall;
    private final RunOption optionCopyMap;
    private final RunOption optionDisablePjass;
    private final RunOption optionLegacyJassChecks;
    private final RunOption optionShowVersion;
    private final RunOption optionPrettyPrint;
    private final RunOption optionMeasureTimes;
    private final RunOption optionCompactOutput;
    private final RunOption optionHotStartmap;
    private final RunOption optionHotReload;
    private final RunOption optionTestTimeout;
    private final RunOption optionTestFilter;
    private final RunOption optionDevBuild;
    private int functionSplitLimit = 10000;

    /**
     * When set, the build targets a legacy patch (pre-1.24) whose Blizzard-provided
     * common.j/blizzard.j contain type mismatches the weakly-typed Jass VM tolerates
     * (e.g. returning a real where an integer is declared). For such targets, Jass
     * return-type mismatches are downgraded to warnings and PJass is skipped on the
     * generated script, since PJass would reject the same Blizzard code.
     */
    private boolean legacyJassTypeChecks = false;

    private final RunOption optionBuild;

    public RunArgs with(String... additionalArgs) {
        RunArgs result = new RunArgs(Stream.concat(Stream.of(args), Stream.of(additionalArgs))
                .toArray(String[]::new));
        result.legacyJassTypeChecks = this.legacyJassTypeChecks;
        return result;
    }

    private static class RunOption {

        final String name;
        final String descr;
        final @Nullable Consumer<String> argHandler;
        boolean isSet;
        RunOption(String name, String descr) {
            this.name = name;
            this.descr = descr;
            this.argHandler = null;
        }

        RunOption(String name, String descr, Consumer<String> argHandler2) {
            this.name = name;
            this.descr = descr;
            this.argHandler = argHandler2;
        }

    }

    public static RunArgs defaults() {
        return new RunArgs();
    }

    public RunArgs(String... args) {
        this.args = args;
        // interpreter
        optionRuntests = addOption("runtests", "Run all test functions found in the scripts.");
        optionRunBenchmarks = addOption("runbenchmarks", "Run the hidden benchmark worker mode.");
        optionBenchmarkList = addOption("benchmarkList", "List benchmark functions instead of executing one.");
        optionTestTimeout = addOptionWithArg("testTimeout", "Timeout in seconds after which tests will be cancelled and considered failed, if they did not yet succeed.", arg -> testTimeout = Integer.parseInt(arg));
        optionTestFilter = addOptionWithArg("testFilter", "Only run tests whose qualified name (Package.function) contains this string (case-insensitive).", arg -> testFilter = arg);
        optionBenchmarkFilter = addOptionWithArg("benchmarkFilter", "Filter benchmark names during benchmark discovery.", arg -> benchmarkFilter = arg);
        optionBenchmarkName = addOptionWithArg("benchmarkName", "Execute one exact qualified benchmark name.", arg -> benchmarkName = arg);
        optionBenchmarkWarmup = addOptionWithArg("benchmarkWarmup", "Number of unmeasured benchmark warmup iterations.", arg -> benchmarkWarmup = parseBenchmarkInteger("benchmarkWarmup", arg));
        optionBenchmarkIterations = addOptionWithArg("benchmarkIterations", "Number of measured benchmark iterations.", arg -> benchmarkIterations = parseBenchmarkInteger("benchmarkIterations", arg));
        optionBenchmarkOutput = addOptionWithArg("benchmarkOutput", "Output path for benchmark worker JSON.", arg -> benchmarkOutput = arg);
        optionRunCompileTimeFunctions = addOption("runcompiletimefunctions", "Run all compiletime functions found in the scripts.");
        optionInjectCompiletimeObjects = addOption("injectobjects", "Injects the objects generated by compiletime functions into the map.");
        // optimization
        optionOpt = addOption("opt", "Enables identifier name compression and whitespace removal.");
        optionInline = addOption("inline", "Enables function inlining.");
        optionLocalOptimizations = addOption("localOptimizations", "Enables local optimizations (cpu and ram extensive, recommended for release)");
        // debug options
        optionStacktraces = addOption("stacktraces", "Generate stacktrace information in the script (useful for debugging).");
        optionNodebug = addOption("nodebug", "Remove all error messages from the script. (Not recommended)");
        uncheckedDispatch = addOption("uncheckedDispatch", "(dangerous) Removes checks from method-dispatch code. With unchecked dispatch "
                + "some programming errors like null-pointer-dereferences or accessing of destroyed objects can no longer be detected. "
                + "It is strongly recommended to not use this option, but it can give some performance benefits.");
        optionMeasureTimes = addOption("measure", "Measure how long each step of the translation process takes.");
        optionCompactOutput = addOption("compactOutput", "Print compact CLI output for automated agents.");
        // tools
        optionAbout = addOption("-about", "Show the 'about' window.");
        optionFixInstall = addOption("-fixInstallation", "Checks your wc3 installation and applies compatibility fixes");
        optionCopyMap = addOption("-copyMap", "copies map");
        optionStartServer = addOption("-startServer", "Starts the compilation server.");
        optionShowErrors = addOption("-showerrors", "(currently not implemented.) Show errors generated by last compile.");
        optionExtractImports = addOptionWithArg("-extractImports", "Extract all files from a map into a folder next to the mapp.", arg -> mapFile = arg);
        optionExportObjects = addOptionWithArg("exportobjects", "Export object editor data from a map file or map folder to Wurst source.", arg -> exportObjectsFile = arg);
        addOptionWithArg("exportobjectsOut", "Output folder for -exportobjects.", arg -> exportObjectsOut = arg);
        optionShowVersion = addOption("-version", "Shows the version of the compiler");

        // other
        optionNoExtractMapScript = addOption("noExtractMapScript", "Do not extract the map script from the map and use the one from the Wurst folder instead.");
        optionGui = addOption("gui", "Show a graphical user interface (progress bar and error window).");
        addOptionWithArg("lib", "The next argument should be a library folder which is lazily added to the build.", arg -> libDirs.add(new File(arg)));
        addOptionWithArg("out", "Outputs the compiled script to this file.", arg -> outFile = arg);

        optionLanguageServer = addOption("languageServer", "Starts a language server which can be used by editors to get services "
                + "like code completion, validations, and find declaration. The communication to the language server is via standard input output.");
        optionLanguageServerAppCdsTrain = addOption("languageServerAppCdsTrain", "Starts and immediately stops a lightweight language-server startup path for AppCDS training.");

        optionHelp = addOption("help", "Prints this help message.");
        optionDisablePjass = addOption("noPJass", "Disables PJass checks for the generated code.");
        optionLegacyJassChecks = addOption("legacyJassChecks", "Relax Jass type checks for legacy (pre-1.24) targets whose Blizzard-provided "
                + "common.j/blizzard.j contain return-type mismatches the Jass VM tolerates. Also skips PJass on the generated script.");
        optionHotStartmap = addOption("hotstart", "Uses Jass Hot Code Reload (JHCR) to start the map.");
        optionHotReload = addOption("hotreload", "Reloads the mapscript after running the map with Jass Hot Code Reload (JHCR).");

        optionBuild = addOption("build", "Builds an output map from the input map and library directories.");
        optionDevBuild = addOption("dev", "Builds an output map in development/run mode, so compiletime isProductionBuild() is false.");
        addOptionWithArg("workspaceroot", "The next argument should be the root folder of the project to build.", arg -> workspaceroot = arg);
        addOptionWithArg("inputmap", "The next argument should be the input map.", arg -> inputmap = arg);
        optionLua = addOption("lua", "Choose Lua as the compilation target.");
        optionCompiletimeCache = addOption("compiletimeCache", "(Experimental) Cache results of compiletime invocations without side effects");

        addOptionWithArg("functionSplitLimit", "The maximum number of operations in a function before it is split by the function splitter (used for compiletime functions)",
            s -> functionSplitLimit = Integer.parseInt(s, 10));
        optionPrettyPrint = addOption("prettyPrint", "Pretty print the input file, or all sub-directory if the given path is: '...'");

        nextArg:
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                for (RunOption o : options) {
                    if (("-" + o.name).equals(a)) {
                        Consumer<String> argHandler = o.argHandler;
                        if (argHandler != null) {
                            i++;
                            argHandler.accept(args[i]);
                        }
                        o.isSet = true;
                        continue nextArg;
                    } else if ((o.argHandler != null && isDoubleArg(a, o))) {
                        String value = a.substring(a.indexOf(" ") + 1).trim();
                        if (value.isEmpty()) {
                            throw new RuntimeException("Missing value for option: -" + o.name);
                        }
                        o.argHandler.accept(value);
                        o.isSet = true;
                        continue nextArg;
                    } else if (o.argHandler != null && isEqualsArg(a, o)) {
                        String value = a.substring(a.indexOf("=") + 1).trim();
                        if (value.isEmpty()) {
                            throw new RuntimeException("Missing value for option: -" + o.name);
                        }
                        o.argHandler.accept(value);
                        o.isSet = true;
                        continue nextArg;
                    }
                }
                throw new RuntimeException("Unknown option: " + a);
            } else {
                files.add(a);
                if (a.endsWith(".w3x") || a.endsWith(".w3m")) {
                    mapFile = a;
                }
            }
        }

        if (optionHelp.isSet) {
            printHelpAndExit();
        }

        validateBenchmarkOptions();
    }

    private static int parseBenchmarkInteger(String option, String value) {
        try {
            return Integer.parseInt(value, 10);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid integer for -" + option + ": " + value, e);
        }
    }

    private void validateBenchmarkOptions() {
        boolean hasBenchmarkOption = optionRunBenchmarks.isSet
            || optionBenchmarkList.isSet
            || optionBenchmarkFilter.isSet
            || optionBenchmarkName.isSet
            || optionBenchmarkWarmup.isSet
            || optionBenchmarkIterations.isSet
            || optionBenchmarkOutput.isSet;
        if (!hasBenchmarkOption) {
            return;
        }
        if (!optionRunBenchmarks.isSet) {
            throw new RuntimeException("Benchmark options require -runbenchmarks.");
        }
        StringBuilder conflictingModes = new StringBuilder();
        if (optionRuntests.isSet) conflictingModes.append(" -runtests");
        if (optionRunCompileTimeFunctions.isSet) conflictingModes.append(" -runcompiletimefunctions");
        if (optionBuild.isSet) conflictingModes.append(" -build");
        if (optionDevBuild.isSet) conflictingModes.append(" -dev");
        if (outFile != null) conflictingModes.append(" -out");
        if (optionInjectCompiletimeObjects.isSet) conflictingModes.append(" -injectobjects");
        if (optionTestFilter.isSet) conflictingModes.append(" -testFilter");
        if (optionTestTimeout.isSet) conflictingModes.append(" -testTimeout");
        if (conflictingModes.length() > 0) {
            throw new RuntimeException(
                "-runbenchmarks cannot be combined with ordinary execution/output modes:" + conflictingModes);
        }
        if (benchmarkOutput == null || benchmarkOutput.isBlank()) {
            throw new RuntimeException("-runbenchmarks requires -benchmarkOutput.");
        }
        if (benchmarkWarmup < 0) {
            throw new RuntimeException("-benchmarkWarmup must be non-negative.");
        }
        if (benchmarkIterations <= 0) {
            throw new RuntimeException("-benchmarkIterations must be positive.");
        }
        if (optionBenchmarkList.isSet && benchmarkName != null) {
            throw new RuntimeException("-benchmarkList cannot be combined with -benchmarkName.");
        }
        if (optionBenchmarkList.isSet) {
            if (optionBenchmarkWarmup.isSet || optionBenchmarkIterations.isSet) {
                throw new RuntimeException("-benchmarkWarmup and -benchmarkIterations require -benchmarkName.");
            }
        } else if (benchmarkName == null) {
            throw new RuntimeException("-runbenchmarks requires either -benchmarkList or -benchmarkName.");
        }
        if (benchmarkName != null && benchmarkFilter != null) {
            throw new RuntimeException("-benchmarkFilter cannot be combined with -benchmarkName.");
        }
    }

    private boolean isDoubleArg(String arg, RunOption option) {
        return (arg.contains(" ") && ("-" + option.name).equals(arg.substring(0, arg.indexOf(" "))));
    }

    private boolean isEqualsArg(String arg, RunOption option) {
        return arg.startsWith("-" + option.name + "=");
    }

    private RunOption addOption(String name, String descr) {
        RunOption opt = new RunOption(name, descr);
        options.add(opt);
        return opt;
    }

    private RunOption addOptionWithArg(String name, String descr, Consumer<String> argHandler) {
        RunOption opt = new RunOption(name, descr, argHandler);
        options.add(opt);
        return opt;
    }

    public RunArgs(List<String> runArgs) {
        this(runArgs.toArray(new String[0]));
    }

    public void printHelpAndExit() {
        System.out.println("Usage: ");
        System.out.println("wurst <options> <files>");
        System.out.println();
        System.out.println("Example: wurst -opt common.j Blizzard.j myMap.w3x");
        System.out.println("Compiles the given map with the two script files and optimizations enabled.");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        for (RunOption opt : options) {
            System.out.println("-" + opt.name);
            System.out.println("	" + opt.descr);
            System.out.println();
        }
    }

    public List<String> getFiles() {
        return files;
    }

    public boolean isOptimize() {
        return optionOpt.isSet;
    }

    public boolean isGui() {
        return optionGui.isSet;
    }

    public @Nullable String getMapFile() {
        return mapFile;
    }

    public void setMapFile(String file) {
        mapFile = file;
    }

    /** See {@link #legacyJassTypeChecks}. Set via the {@code -legacyJassChecks} flag or programmatically. */
    public boolean isLegacyJassTypeChecks() {
        return legacyJassTypeChecks || optionLegacyJassChecks.isSet;
    }

    public void setLegacyJassTypeChecks(boolean legacyJassTypeChecks) {
        this.legacyJassTypeChecks = legacyJassTypeChecks;
    }

    public @Nullable String getOutFile() {
        return outFile;
    }

    public boolean showAbout() {
        return optionAbout.isSet;
    }

    public boolean isFixInstall() {
        return optionFixInstall.isSet;
    }

    public boolean isStartServer() {
        return optionStartServer.isSet;
    }

    public boolean showLastErrors() {
        return optionShowErrors.isSet;
    }

    public boolean isInline() {
        return optionInline.isSet;
    }

    public boolean runCompiletimeFunctions() {
        return optionRunCompileTimeFunctions.isSet;
    }


    public boolean isNullsetting() {
        return isOptimize();
    }

    public boolean isLocalOptimizations() {
        return optionLocalOptimizations.isSet;
    }

    public boolean isIncludeStacktraces() {
        return optionStacktraces.isSet;
    }

    public boolean isNoDebugMessages() {
        return optionNodebug.isSet;
    }

    public boolean isInjectObjects() {
        return !isHotReload() && optionInjectCompiletimeObjects.isSet;
    }

    public List<File> getAdditionalLibDirs() {
        return Collections.unmodifiableList(libDirs);
    }

    public void addLibs(Set<String> dependencies) {
        for (String dep : dependencies) {
            libDirs.add(new File(dep));
        }
    }

    public void addLibDirs(Set<File> dependencies) {
        libDirs.addAll(dependencies);
    }

    public boolean showHelp() {
        return optionHelp.isSet;
    }

    public boolean isExtractImports() {
        return optionExtractImports.isSet;
    }

    public boolean isExportObjects() {
        return optionExportObjects.isSet;
    }

    public @Nullable String getExportObjectsFile() {
        return exportObjectsFile;
    }

    public Optional<String> getExportObjectsOut() {
        return Optional.ofNullable(exportObjectsOut);
    }

    public boolean isShowVersion() {
        return optionShowVersion.isSet;
    }

    public boolean isUncheckedDispatch() {
        return uncheckedDispatch.isSet;
    }

    public boolean isLanguageServer() {
        return optionLanguageServer.isSet;
    }

    public boolean isLanguageServerAppCdsTrain() {
        return optionLanguageServerAppCdsTrain.isSet;
    }

    public boolean isNoExtractMapScript() {
        return optionNoExtractMapScript.isSet;
    }

    public boolean isCopyMap() {
        return optionCopyMap.isSet;
    }

    public boolean isDisablePjass() {
        return optionDisablePjass.isSet;
    }

    public boolean isRunTests() {
        return optionRuntests.isSet;
    }

    public boolean isRunBenchmarks() {
        return optionRunBenchmarks.isSet;
    }

    public boolean isBenchmarkList() {
        return optionBenchmarkList.isSet;
    }

    public @Nullable String getBenchmarkFilter() {
        return benchmarkFilter;
    }

    public @Nullable String getBenchmarkName() {
        return benchmarkName;
    }

    public int getBenchmarkWarmup() {
        return benchmarkWarmup;
    }

    public int getBenchmarkIterations() {
        return benchmarkIterations;
    }

    public @Nullable String getBenchmarkOutput() {
        return benchmarkOutput;
    }

    public boolean isPrettyPrint() {
        return optionPrettyPrint.isSet;
    }

    public int getTestTimeout() {
        return testTimeout;
    }

    public Optional<String> getTestFilter() {
        return Optional.ofNullable(testFilter);
    }

    public boolean isMeasureTimes() {
        return optionMeasureTimes.isSet;
    }

    public boolean isCompactOutput() {
        return optionCompactOutput.isSet;
    }

    public boolean isHotStartmap() {
        return optionHotStartmap.isSet;
    }

    public boolean isHotReload() {
        return optionHotReload.isSet;
    }

    public boolean isBuild() {
        return optionBuild.isSet;
    }

    public boolean isDevBuild() {
        return optionDevBuild.isSet;
    }

    public String getWorkspaceroot() {
        return workspaceroot;
    }

    public String getInputmap() {
        return inputmap;
    }

    public boolean isLua() {
        return optionLua.isSet;
    }

    public boolean isCompiletimeCache() {
        return optionCompiletimeCache.isSet;
    }


    public int getFunctionSplitLimit() {
        return functionSplitLimit;
    }

}
