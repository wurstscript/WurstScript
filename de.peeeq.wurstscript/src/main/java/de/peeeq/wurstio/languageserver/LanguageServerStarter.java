package de.peeeq.wurstio.languageserver;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 *
 */
public class LanguageServerStarter {

    public static void start() {
        WurstLanguageServer server = new WurstLanguageServer();
        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server,
                        System.in,
                        System.out);
        // redirect all other output to StdErr, just to be sure:
        System.setOut(System.err);
        server.connect(launcher.getRemoteProxy());
        launcher.startListening();
        server.setRemoteEndpoint(launcher.getRemoteEndpoint());
    }


}
