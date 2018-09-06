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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 */
public class WurstCommands {

    public static final String WURST_RESTART = "wurst.restart";
    public static final String WURST_CLEAN = "wurst.clean";
    public static final String WURST_STARTMAP = "wurst.startmap";
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
        String filename = options.has("filename") ? options.get("filename").getAsString() : null;
        int line = options.has("line") ? options.get("line").getAsInt() : -1;
        int column = options.has("column") ? options.get("column").getAsInt() : -1;
        return server.worker().handle(new RunTests(filename, line, column));
    }

    private static CompletableFuture<Object> buildmap(WurstLanguageServer server, ExecuteCommandParams params) {
        WFile workspaceRoot = server.getRootUri();
        if (params.getArguments().isEmpty()) {
            throw new RuntimeException("Missing arguments");
        }
        JsonObject options = (JsonObject) params.getArguments().get(0);
        String mapPath = options.get("mappath").getAsString();
        if (mapPath == null) {
            throw new RuntimeException("No mappath given");
        }

        File map = new File(mapPath);
        List<String> compileArgs = getCompileArgs(workspaceRoot);
        return server.worker().handle(new BuildMap(workspaceRoot, map, compileArgs)).thenApply(x -> x);
    }

    private static CompletableFuture<Object> startmap(WurstLanguageServer server, ExecuteCommandParams params) {
        WFile workspaceRoot = server.getRootUri();
        if (params.getArguments().isEmpty()) {
            throw new RuntimeException("Missing arguments");
        }
        JsonObject options = (JsonObject) params.getArguments().get(0);
        String mapPath = options.get("mappath").getAsString();
        String wc3Path = options.get("wc3path").getAsString();
        if (mapPath == null) {
            throw new RuntimeException("No mappath given");
        }
        if (wc3Path == null) {
            throw new RuntimeException("No wc3path given");
        }

        File map = new File(mapPath);
        List<String> compileArgs = getCompileArgs(workspaceRoot);
        return server.worker().handle(new RunMap(workspaceRoot, wc3Path, map, compileArgs)).thenApply(x -> x);
    }

    private static final List<String> defaultArgs = ImmutableList.of("-runcompiletimefunctions", "-injectobjects", "-stacktraces");

    private static List<String> getCompileArgs(WFile rootPath) {
        try {
            Path configFile = Paths.get(rootPath.toString(), "wurst_run.args");
            if (Files.exists(configFile)) {
                return Files.lines(configFile).filter(s -> s.startsWith("-")).collect(Collectors.toList());
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
