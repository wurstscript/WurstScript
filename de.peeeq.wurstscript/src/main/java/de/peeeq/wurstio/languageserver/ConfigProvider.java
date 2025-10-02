package de.peeeq.wurstio.languageserver;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.peeeq.wurstscript.WLogger;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides configuration values from the language client with caching and timeout protection.
 * Cache is updated asynchronously in the background to ensure changes are picked up.
 */
public class ConfigProvider {
    private final LanguageClient languageClient;

    // Cache with thread-safe atomic reference
    private final AtomicReference<CachedConfig> cachedConfig = new AtomicReference<>(null);

    // Background refresh executor
    private final ScheduledExecutorService refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ConfigProvider-Refresh");
        t.setDaemon(true);
        return t;
    });

    // Configuration timeouts
    private static final long FETCH_TIMEOUT_MS = 500; // Fast timeout for blocking calls
    private static final long CACHE_DURATION_MS = 5000; // 5 seconds before refresh
    private static final long REFRESH_INTERVAL_MS = 10000; // Check for updates every 10 seconds

    // Track if we've shown warning to avoid spam
    private volatile boolean hasShownTimeoutWarning = false;

    public ConfigProvider(LanguageClient languageClient) {
        this.languageClient = languageClient;

        // Start background refresh task
        refreshExecutor.scheduleAtFixedRate(
            this::refreshCacheAsync,
            REFRESH_INTERVAL_MS,
            REFRESH_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        // Initial fetch (non-blocking)
        refreshCacheAsync();
    }

    /**
     * Get configuration value with caching and timeout protection.
     * Returns cached value if available, otherwise attempts quick fetch with timeout.
     */
    public String getConfig(String key, String defaultValue) {
        CachedConfig cached = cachedConfig.get();

        // Return cached value if valid
        if (cached != null && cached.isValid()) {
            String value = cached.getValue(key);
            return value != null ? value : defaultValue;
        }

        // Try quick fetch if no valid cache
        if (cached == null) {
            String value = fetchConfigWithTimeout(key, defaultValue);
            return value;
        }

        // Return stale cache while refresh happens in background
        String value = cached.getValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Fetch config with timeout protection
     */
    private String fetchConfigWithTimeout(String key, String defaultValue) {
        ConfigurationItem ci = new ConfigurationItem();
        ci.setSection("wurst");
        CompletableFuture<List<Object>> res = languageClient.configuration(
            new ConfigurationParams(Collections.singletonList(ci))
        );

        try {
            List<Object> config = res.get(FETCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            for (Object c : config) {
                if (c instanceof JsonObject) {
                    JsonObject cfg = (JsonObject) c;

                    // Update cache with full config
                    cachedConfig.set(new CachedConfig(cfg));

                    JsonElement result = cfg.get(key);
                    if (result instanceof JsonNull) {
                        return null;
                    } else if (result != null) {
                        return result.getAsString();
                    }
                }
            }
            return defaultValue;
        } catch (TimeoutException e) {
            if (!hasShownTimeoutWarning) {
                WLogger.warning("Config request timed out for " + key + " after " + FETCH_TIMEOUT_MS + "ms, using default: " + defaultValue);
                hasShownTimeoutWarning = true;
            }
            return defaultValue;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            WLogger.warning("Config request interrupted for " + key + ", using default: " + defaultValue);
            return defaultValue;
        } catch (ExecutionException e) {
            WLogger.warning("Could not get config " + key + ", using default value " + defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Asynchronously refresh the cache in background
     */
    private void refreshCacheAsync() {
        ConfigurationItem ci = new ConfigurationItem();
        ci.setSection("wurst");

        languageClient.configuration(new ConfigurationParams(Collections.singletonList(ci)))
            .thenAccept(config -> {
                for (Object c : config) {
                    if (c instanceof JsonObject) {
                        JsonObject cfg = (JsonObject) c;
                        cachedConfig.set(new CachedConfig(cfg));
                        WLogger.trace("Config cache refreshed successfully");
                        return;
                    }
                }
            })
            .exceptionally(e -> {
                WLogger.trace("Background config refresh failed (this is normal if client is busy): " + e.getMessage());
                return null;
            });
    }

    public String getJhcrExe() {
        return getConfig("jhcrExe", "jhcr.exe");
    }

    public Optional<String> getWc3RunArgs() {
        return Optional.ofNullable(getConfig("wc3RunArgs", null));
    }

    /**
     * The path where to put maps before running them
     */
    public Optional<String> getMapDocumentPath() {
        return Optional.ofNullable(getConfig("mapDocumentPath", null));
    }

    /**
     * Shutdown the background refresh executor
     */
    public void shutdown() {
        refreshExecutor.shutdown();
        try {
            if (!refreshExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                refreshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            refreshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Immutable cached configuration with timestamp
     */
    private static class CachedConfig {
        private final JsonObject config;
        private final long timestamp;

        CachedConfig(JsonObject config) {
            this.config = config;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
        }

        String getValue(String key) {
            JsonElement result = config.get(key);
            if (result instanceof JsonNull) {
                return null;
            } else if (result != null) {
                return result.getAsString();
            }
            return null;
        }
    }
}
