package de.peeeq.wurstio.languageserver;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.ExecuteCommandParams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class WurstCommands {

    public static final String WURST_RESTART = "wurst.restart";
    public static final String WURST_CLEAN = "wurst.clean";
    public static final String WURST_STARTMAP = "wurst.startmap";
    public static final String WURST_HOTSTARTMAP = "wurst.hotstartmap";
    public static final String WURST_HOTRELOAD = "wurst.hotreload";
    public static final String WURST_BUILDMAP = "wurst.buildmap";
    public static final String WURST_STARTLAST = "wurst.startlast";
    public static final String WURST_TESTS = "wurst.tests";
    public static final String WURST_TESTS_FILE = "wurst.tests_file";
    public static final String WURST_TESTS_FUNC = "wurst.tests_func";
    public static final String WURST_PERFORM_CODE_ACTION = "wurst.perform_code_action";

    static List<String> providedCommands() {
        return Arrays.asList(
                WURST_CLEAN
        );
    }

    public static CompletableFuture<Object> execute(WurstLanguageServer server, ExecuteCommandParams params) {
        switch (params.getCommand()) {
            case WURST_CLEAN:
                return server.worker().handle(new CleanProject()).thenApply(x -> x);
            case WURST_STARTMAP:
                return startmap(server, params);
            case WURST_HOTSTARTMAP:
                return startmap(server, params, "-hotstart");
            case WURST_HOTRELOAD:
                return startmap(server, params, "-hotreload");
            case WURST_TESTS:
                return testMap(server, params);
            case WURST_PERFORM_CODE_ACTION:
                return server.worker().handle(new PerformCodeActionRequest(server, params));
            case WURST_BUILDMAP:
                return buildmap(server, params);
        }
        WLogger.info("unhandled command: " + params.getCommand());
        throw new RuntimeException("unhandled command: " + params.getCommand());
    }


    private static CompletableFuture<Object> testMap(WurstLanguageServer server, ExecuteCommandParams params) {
        JsonObject options = (JsonObject) params.getArguments().get(0);
        Optional<String> filename = getString(options, "filename");
        int line = options.has("line") ? options.get("line").getAsInt() : -1;
        int column = options.has("column") ? options.get("column").getAsInt() : -1;
        Optional<String> testName = getString(options, "testName");
        return server.worker().handle(new RunTests(filename, line, column, testName));
    }

    private static CompletableFuture<Object> buildmap(WurstLanguageServer server, ExecuteCommandParams params) {
        WFile workspaceRoot = server.getRootUri();
        if (params.getArguments().isEmpty()) {
            throw new RuntimeException("Missing arguments");
        }
        JsonObject options = (JsonObject) params.getArguments().get(0);
        Optional<String> mapPath = getString(options, "mappath");
        Optional<String> wc3Path = getString(options, "wc3path");
        if (!mapPath.isPresent()) {
            throw new RuntimeException("No mappath given");
        }

        Optional<File> map = mapPath.map(path -> new File(path));
        List<String> compileArgs = getCompileArgs(workspaceRoot);
        return server.worker().handle(new BuildMap(server.getConfigProvider(), workspaceRoot, wc3Path, map, compileArgs)).thenApply(x -> x);
    }

    private static CompletableFuture<Object> startmap(WurstLanguageServer server, ExecuteCommandParams params, String... additionalArgs) {
        WFile workspaceRoot = server.getRootUri();
        if (params.getArguments().isEmpty()) {
            throw new RuntimeException("Missing arguments");
        }
        JsonObject options = (JsonObject) params.getArguments().get(0);
        String key = "mappath";
        Optional<String> mapPath = getString(options, key);
        Optional<String> wc3Path = getString(options, "wc3path");

        Optional<File> map = mapPath.map(mp -> new File(mp));
        List<String> compileArgs = getCompileArgs(workspaceRoot, additionalArgs);
        return server.worker().handle(new RunMap(server.getConfigProvider(), workspaceRoot, wc3Path, map, compileArgs)).thenApply(x -> x);
    }

    private static Optional<String> getString(JsonObject options, String key) {
        try {
            return Optional.ofNullable(options.get(key)).map(jsonElement -> jsonElement.getAsString());
        } catch (ClassCastException | IllegalStateException e) {
            WLogger.warning("Invalid configuration", e);
            return Optional.empty();
        }
    }

    private static final List<String> defaultArgs = ImmutableList.of("-runcompiletimefunctions", "-injectobjects", "-stacktraces");

    public static List<String> getCompileArgs(WFile rootPath, String... additionalArgs) {
        try {
            Path configFile = Paths.get(rootPath.toString(), "wurst_run.args");
            if (Files.exists(configFile)) {
                return Stream.concat(
                        Files.lines(configFile)
                                .filter(s -> s.startsWith("-")),
                        Stream.of(additionalArgs)
                ).collect(Collectors.toList());
            } else {

                String cfg = String.join("\n", defaultArgs) + "\n";
                Files.write(configFile, cfg.getBytes(Charsets.UTF_8));
                return defaultArgs;
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not access wurst run config file", e);
        }
    }

}
