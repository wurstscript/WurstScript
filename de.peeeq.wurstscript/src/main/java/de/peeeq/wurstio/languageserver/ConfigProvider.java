package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class ConfigProvider {
private final LanguageClient languageClient;

    public ConfigProvider(LanguageClient languageClient) {
        this.languageClient = languageClient;
    }

    public String getConfig(String key) {
        ConfigurationItem ci = new ConfigurationItem();
        ci.setSection("wurst");
        CompletableFuture<List<Object>> res = languageClient.configuration(new ConfigurationParams(Collections.singletonList(ci)));
        try {
            List<Object> config = res.get();
            return "ok";
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJhcrExe() {
        try {
            return getConfig("wurst.jhcrExe");
        } catch (Exception e) {
            WLogger.info(e);
            return "jhcr.exe";
        }
    }
}


