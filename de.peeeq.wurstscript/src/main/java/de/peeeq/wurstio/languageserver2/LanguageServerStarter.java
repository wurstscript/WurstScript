package de.peeeq.wurstio.languageserver2;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 *
 */
public class LanguageServerStarter {

    public static void start() {
        System.err.println("Starting ...");
        WurstLanguageServer server = new WurstLanguageServer();
        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server,
                        System.in,
                        System.out);
        System.err.println("Starting to listen ...");
        launcher.startListening();
        System.err.println("Stopping ...");
    }


}
