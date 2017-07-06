package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.CleanProject;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstio.languageserver.requests.RunTests;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.ExecuteCommandParams;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class WurstCommands {

    public static final String WURST_RESTART = "wurst.restart";
    public static final String WURST_CLEAN = "wurst.clean";
    public static final String WURST_STARTMAP = "wurst.startmap";
    public static final String WURST_STARTLAST = "wurst.startlast";
    public static final String WURST_TESTS = "wurst.tests";
    public static final String WURST_TESTS_FILE = "wurst.tests_file";
    public static final String WURST_TESTS_FUNC = "wurst.tests_func";

    static List<String> providedCommands() {
        return Arrays.asList(
                WURST_CLEAN,
                WURST_STARTMAP,
                WURST_TESTS
        );
    }

    public static CompletableFuture<Object> execute(WurstLanguageServer server, ExecuteCommandParams params) {
        switch (params.getCommand()) {
            case WURST_CLEAN:
                return server.worker().handle(new CleanProject()).thenApply(x -> x);
            case WURST_STARTMAP:
                return startmap(server, params);
            case WURST_TESTS: {
                return testMap(server, params);
            }

        }
        WLogger.info("unhandled command: " + params.getCommand());
        throw new RuntimeException("unhandled command: " + params.getCommand());
    }

    private static CompletableFuture<Object> testMap(WurstLanguageServer server, ExecuteCommandParams params) {
        Map<?, ?> options = (Map<?, ?>) params.getArguments().get(0);
        String filename = (String) options.get("filename");
        int line = Optional.ofNullable(options.get("line")).map(l -> (Double) l).orElse(-1.).intValue();
        int column = Optional.ofNullable(options.get("column")).map(l -> (Double) l).orElse(-1.).intValue();
        return server.worker().handle(new RunTests(server, filename, line, column));
    }

    private static CompletableFuture<Object> startmap(WurstLanguageServer server, ExecuteCommandParams params) {
        WFile workspaceRoot = server.getRootUri();
        if (params.getArguments().isEmpty()) {
            throw new RuntimeException("Missing arguments");
        }
        Map<?, ?> options = (Map<?, ?>) params.getArguments().get(0);
        String mapPath = (String) options.get("mappath");
        String wc3Path = (String) options.get("wc3path");
        if (mapPath == null) {
            throw new RuntimeException("No mappath given");
        }
        if (wc3Path == null) {
            throw new RuntimeException("No wc3path given");
        }

        File map = new File(mapPath);
        List<String> compileArgs = new ArrayList<>();
        return server.worker().handle(new RunMap(workspaceRoot, wc3Path, map, compileArgs)).thenApply(x -> x);
    }
}
