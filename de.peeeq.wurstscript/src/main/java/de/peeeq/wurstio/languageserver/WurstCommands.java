package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstio.languageserver.requests.CleanProject;
import de.peeeq.wurstio.languageserver.requests.RunMap;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.ExecuteCommandParams;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
                WURST_RESTART,
                WURST_CLEAN,
                WURST_STARTMAP,
                WURST_STARTLAST,
                WURST_TESTS,
                WURST_TESTS_FILE,
                WURST_TESTS_FUNC
        );
    }

    public static CompletableFuture<Object> execute(WurstLanguageServer server, ExecuteCommandParams params) {
        switch (params.getCommand()) {
            case WURST_CLEAN:
                return server.worker().handle(new CleanProject()).thenApply(x -> x);
            case WURST_STARTMAP:
                String workspaceRoot = server.getRootUri();
                String wc3Path = (String) params.getArguments().get(0);
                File map = new File((String) params.getArguments().get(1));
                List<String> compileArgs = (List<String>) params.getArguments().get(2);
                return server.worker().handle(new RunMap(workspaceRoot, wc3Path, map, compileArgs)).thenApply(x -> x);


        }
        WLogger.info("unhandled command: " + params.getCommand());
        throw new RuntimeException("unhandled command: " + params.getCommand());
    }
}
