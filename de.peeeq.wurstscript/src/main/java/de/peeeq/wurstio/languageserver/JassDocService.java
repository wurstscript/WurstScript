package de.peeeq.wurstio.languageserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public final class JassDocService {

    public enum SymbolKind {
        FUNCTION, VARIABLE
    }

    public static final class LookupKey {
        private final String symbolName;
        private final SymbolKind symbolKind;
        private final String sourceFile;

        public LookupKey(String symbolName, SymbolKind symbolKind, String sourceFile) {
            this.symbolName = symbolName;
            this.symbolKind = symbolKind;
            this.sourceFile = sourceFile;
        }

        public String symbolName() {
            return symbolName;
        }

        public SymbolKind symbolKind() {
            return symbolKind;
        }

        public String sourceFile() {
            return sourceFile;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LookupKey)) {
                return false;
            }
            LookupKey other = (LookupKey) obj;
            return symbolName.equals(other.symbolName)
                && symbolKind == other.symbolKind
                && sourceFile.equals(other.sourceFile);
        }

        @Override
        public int hashCode() {
            int result = symbolName.hashCode();
            result = 31 * result + symbolKind.hashCode();
            result = 31 * result + sourceFile.hashCode();
            return result;
        }
    }

    private static final JassDocService INSTANCE = new JassDocService();
    private static final Duration DEFAULT_LATEST_MAX_AGE = Duration.ofHours(24);
    private static final String RELEASES_LATEST_API = "https://api.github.com/repos/wurstscript/wurst-jassdoc-build/releases/latest";
    private static final String RELEASES_API = "https://api.github.com/repos/wurstscript/wurst-jassdoc-build/releases?per_page=20";
    private static final String RELEASES_DOWNLOAD_BASE = "https://github.com/wurstscript/wurst-jassdoc-build/releases";

    private final Map<LookupKey, Optional<String>> lookupCache = new ConcurrentHashMap<>();
    private volatile @Nullable CachedDb cachedDb;
    private volatile boolean triedInit;
    private volatile boolean initFailed;
    private volatile boolean missingSourceWarningShown;
    private final AtomicBoolean initRequested = new AtomicBoolean(false);
    private volatile @Nullable Connection sharedConnection;
    private volatile @Nullable Path sharedConnectionPath;
    private static volatile @Nullable Function<LookupKey, @Nullable String> testLookup;

    private static final class CachedDb {
        private final Path dbPath;
        private final List<TableSchema> schemas;

        private CachedDb(Path dbPath, List<TableSchema> schemas) {
            this.dbPath = dbPath;
            this.schemas = schemas;
        }
    }

    private static final class TableSchema {
        private final String table;
        private final String nameColumn;
        private final String docColumn;
        private final @Nullable String kindColumn;
        private final @Nullable String patchColumn;

        private TableSchema(String table, String nameColumn, String docColumn, @Nullable String kindColumn, @Nullable String patchColumn) {
            this.table = table;
            this.nameColumn = nameColumn;
            this.docColumn = docColumn;
            this.kindColumn = kindColumn;
            this.patchColumn = patchColumn;
        }
    }

    public static JassDocService getInstance() {
        return INSTANCE;
    }

    public static void setTestLookup(@Nullable Function<LookupKey, @Nullable String> testLookupFn) {
        testLookup = testLookupFn;
    }

    public @Nullable String documentationForFunction(FunctionDefinition f) {
        return documentationFor(f.getName(), SymbolKind.FUNCTION, f.getSource().getFile());
    }

    public @Nullable String documentationForFunctionQuick(FunctionDefinition f) {
        return documentationForQuick(f.getName(), SymbolKind.FUNCTION, f.getSource().getFile());
    }

    public @Nullable String documentationForVariable(NameDef n) {
        return documentationFor(n.getName(), SymbolKind.VARIABLE, n.getSource().getFile());
    }

    public @Nullable String documentationForVariableQuick(NameDef n) {
        return documentationForQuick(n.getName(), SymbolKind.VARIABLE, n.getSource().getFile());
    }

    public @Nullable String documentationFor(String symbolName, SymbolKind symbolKind, String sourceFile) {
        Function<LookupKey, @Nullable String> override = testLookup;
        LookupKey key = new LookupKey(symbolName, symbolKind, sourceFile);
        if (override != null) {
            Optional<String> computed = Optional.ofNullable(trimToNull(override.apply(key)));
            Optional<String> prev = lookupCache.putIfAbsent(key, computed);
            return (prev != null ? prev : computed).orElse(null);
        }
        if (!isJassBuiltinSource(sourceFile)) {
            return null;
        }
        Optional<String> cached = lookupCache.computeIfAbsent(key, this::lookupDocumentation);
        return cached.orElse(null);
    }

    /**
     * Non-blocking lookup for latency-sensitive paths (autocomplete):
     * if DB is not ready, triggers async init and returns null immediately.
     */
    public @Nullable String documentationForQuick(String symbolName, SymbolKind symbolKind, String sourceFile) {
        LookupKey key = new LookupKey(symbolName, symbolKind, sourceFile);
        Function<LookupKey, @Nullable String> override = testLookup;
        if (override != null) {
            Optional<String> computed = Optional.ofNullable(trimToNull(override.apply(key)));
            Optional<String> prev = lookupCache.putIfAbsent(key, computed);
            return (prev != null ? prev : computed).orElse(null);
        }
        if (!isJassBuiltinSource(sourceFile)) {
            return null;
        }
        Optional<String> cached = lookupCache.get(key);
        if (cached != null) {
            return cached.orElse(null);
        }
        CachedDb db = cachedDb;
        if (db == null) {
            triggerAsyncInit();
            return null;
        }
        Optional<String> computed = Optional.ofNullable(lookupFromDb(db, key));
        Optional<String> prev = lookupCache.putIfAbsent(key, computed);
        return (prev != null ? prev : computed).orElse(null);
    }

    public void clearCacheForTests() {
        lookupCache.clear();
        closeSharedConnection();
        cachedDb = null;
        triedInit = false;
        initFailed = false;
        missingSourceWarningShown = false;
        initRequested.set(false);
    }

    private Optional<String> lookupDocumentation(LookupKey key) {
        Function<LookupKey, @Nullable String> override = testLookup;
        if (override != null) {
            return Optional.ofNullable(trimToNull(override.apply(key)));
        }

        CachedDb db = getOrInitDb();
        if (db == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(lookupFromDb(db, key));
    }

    private @Nullable CachedDb getOrInitDb() {
        if (initFailed) {
            return null;
        }
        CachedDb existing = cachedDb;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (cachedDb != null) {
                return cachedDb;
            }
            if (initFailed) {
                return null;
            }
            triedInit = true;
            try {
                Optional<Path> dbPath = ensureDbAvailable();
                if (!dbPath.isPresent()) {
                    initFailed = true;
                    return null;
                }
                try (Connection conn = open(dbPath.get())) {
                    List<TableSchema> schemas = discoverSchemas(conn);
                    boolean hasLegacySchema = hasLegacyJassdocSchema(conn);
                    if (schemas.isEmpty() && !hasLegacySchema) {
                        WLogger.warning("JassDoc DB found, but no compatible documentation tables were detected.");
                        initFailed = true;
                        return null;
                    }
                    cachedDb = new CachedDb(dbPath.get(), schemas);
                    return cachedDb;
                }
            } catch (Exception e) {
                initFailed = true;
                WLogger.warning("Could not initialize JassDoc DB: " + e.getMessage());
                return null;
            }
        }
    }

    private @Nullable String lookupFromDb(CachedDb db, LookupKey key) {
        try {
            Connection conn = getSharedConnection(db.dbPath);
            String legacyDoc = lookupFromLegacyJassdocTables(conn, key);
            if (legacyDoc != null) {
                return legacyDoc;
            }
            String bestDoc = null;
            int bestScore = Integer.MIN_VALUE;
            for (TableSchema schema : db.schemas) {
                String sql = "SELECT "
                    + q(schema.docColumn)
                    + (schema.kindColumn == null ? "" : ", " + q(schema.kindColumn))
                    + (schema.patchColumn == null ? "" : ", " + q(schema.patchColumn))
                    + " FROM " + q(schema.table)
                    + " WHERE lower(" + q(schema.nameColumn) + ") = lower(?) LIMIT 10";
                try (PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setString(1, key.symbolName());
                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            String doc = trimToNull(rs.getString(1));
                            if (doc == null) {
                                continue;
                            }
                            String kind = schema.kindColumn == null ? null : rs.getString(2);
                            String patch = schema.patchColumn == null
                                ? null
                                : rs.getString(schema.kindColumn == null ? 2 : 3);
                            int score = score(kind, key.symbolKind());
                            if (score > bestScore) {
                                bestScore = score;
                                bestDoc = formatDoc(doc, patch);
                            }
                        }
                    }
                }
            }
            return bestDoc;
        } catch (SQLException e) {
            WLogger.warning("JassDoc lookup failed for '" + key.symbolName() + "': " + e.getMessage());
            return null;
        }
    }

    private Connection getSharedConnection(Path dbPath) throws SQLException {
        synchronized (this) {
            if (sharedConnection != null) {
                try {
                    if (!sharedConnection.isClosed() && dbPath.equals(sharedConnectionPath)) {
                        return sharedConnection;
                    }
                } catch (SQLException ignored) {
                    // reconnect below
                }
                closeSharedConnection();
            }
            sharedConnection = open(dbPath);
            sharedConnectionPath = dbPath;
            return sharedConnection;
        }
    }

    private void closeSharedConnection() {
        synchronized (this) {
            if (sharedConnection != null) {
                try {
                    sharedConnection.close();
                } catch (SQLException ignored) {
                }
            }
            sharedConnection = null;
            sharedConnectionPath = null;
        }
    }

    private void triggerAsyncInit() {
        if (!initRequested.compareAndSet(false, true)) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                getOrInitDb();
            } finally {
                initRequested.set(false);
            }
        }, "JassDocInit");
        t.setDaemon(true);
        t.start();
    }

    private @Nullable String lookupFromLegacyJassdocTables(Connection conn, LookupKey key) throws SQLException {
        if (!tableExists(conn, "parameters")) {
            return null;
        }
        Map<String, String> params = readKeyValueRows(conn, "parameters", "fnname", "param", "value", key.symbolName());
        Map<String, String> anns = tableExists(conn, "annotations")
            ? readKeyValueRows(conn, "annotations", "fnname", "anname", "value", key.symbolName())
            : java.util.Collections.emptyMap();
        String sourceFile = firstValue(params, anns, "source-file");
        if (!matchesRequestedSource(sourceFile, key.sourceFile())) {
            return null;
        }

        Map<String, String> paramDescriptions = new LinkedHashMap<>(params);
        if (tableExists(conn, "params_extra")) {
            mergeParamDetails(conn, key.symbolName(), paramDescriptions);
        }

        String doc = firstValue(params, anns,
            "description", "desc", "documentation", "doc", "comment", "details", "notes", "help", "summary");
        String note = firstValue(params, anns, "note");
        String patch = firstValue(params, anns,
            "patch", "since", "introduced", "introduced_patch", "version");

        String paramDoc = renderParameterDocs(paramDescriptions);
        if (doc == null && note == null && paramDoc == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        appendBlock(sb, doc);
        appendBlock(sb, note);
        appendBlock(sb, paramDoc);
        String result = trimToNull(sb.toString());
        if (result == null) {
            return null;
        }
        return formatDoc(result, patch);
    }

    private void mergeParamDetails(Connection conn, String fnName, Map<String, String> paramDescriptions) throws SQLException {
        String sql = "SELECT param, anname, value FROM params_extra WHERE lower(fnname) = lower(?)";
        Map<String, ParamDetails> details = new LinkedHashMap<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, fnName);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String param = trimToNull(rs.getString(1));
                    String key = trimToNull(rs.getString(2));
                    String value = trimToNull(rs.getString(3));
                    if (param == null || key == null || value == null) {
                        continue;
                    }
                    ParamDetails pd = details.computeIfAbsent(param, k -> new ParamDetails(param));
                    String k = key.toLowerCase(Locale.ROOT);
                    if (k.equals("param_order")) {
                        try {
                            pd.order = Integer.parseInt(value);
                        } catch (NumberFormatException ignored) {
                            // ignore malformed order
                        }
                    } else if (k.equals("param_type") || k.equals("type")) {
                        pd.type = value;
                    }
                }
            }
        }

        List<ParamDetails> ordered = new ArrayList<>(details.values());
        ordered.sort(Comparator.comparingInt(p -> p.order == null ? Integer.MAX_VALUE : p.order));
        LinkedHashMap<String, String> reordered = new LinkedHashMap<>();
        for (ParamDetails p : ordered) {
            String desc = paramDescriptions.get(p.name);
            if (desc != null) {
                String combined = p.type == null ? desc : "(" + p.type + ") " + desc;
                reordered.put(p.name, combined);
            }
        }
        for (Map.Entry<String, String> e : paramDescriptions.entrySet()) {
            reordered.putIfAbsent(e.getKey(), e.getValue());
        }
        paramDescriptions.clear();
        paramDescriptions.putAll(reordered);
    }

    private @Nullable String renderParameterDocs(Map<String, String> paramDescriptions) {
        if (paramDescriptions.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("Parameters:");
        for (Map.Entry<String, String> e : paramDescriptions.entrySet()) {
            String name = trimToNull(e.getKey());
            String value = trimToNull(e.getValue());
            if (name == null || value == null) {
                continue;
            }
            sb.append("\n- ").append(name).append(": ").append(value);
        }
        return trimToNull(sb.toString());
    }

    private boolean matchesRequestedSource(@Nullable String dbSourceFile, String requestedSourceFile) {
        String db = basename(dbSourceFile);
        if (db == null) {
            return true;
        }
        String req = basename(requestedSourceFile);
        if (req == null) {
            return true;
        }
        return db.equalsIgnoreCase(req);
    }

    private @Nullable String basename(@Nullable String path) {
        String p = trimToNull(path);
        if (p == null) {
            return null;
        }
        p = p.replace('\\', '/');
        int i = p.lastIndexOf('/');
        return i >= 0 ? p.substring(i + 1) : p;
    }

    private void appendBlock(StringBuilder sb, @Nullable String text) {
        String t = trimToNull(text);
        if (t == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("\n\n");
        }
        sb.append(t);
    }

    private static final class ParamDetails {
        private final String name;
        private @Nullable Integer order;
        private @Nullable String type;

        private ParamDetails(String name) {
            this.name = name;
        }
    }

    private Optional<Path> ensureDbAvailable() throws IOException {
        Optional<String> explicitPath = Utils.getEnvOrConfig("WURST_JASSDOC_DB_PATH");
        if (explicitPath.isPresent()) {
            Path p = Path.of(explicitPath.get());
            if (Files.exists(p)) {
                return Optional.of(p);
            }
            WLogger.warning("WURST_JASSDOC_DB_PATH points to a missing file: " + p);
        }

        Path userHome = Path.of(System.getProperty("user.home"));
        Path dbDir = userHome.resolve(".wurst").resolve("jassdoc");
        Files.createDirectories(dbDir);

        String revision = sanitizeRevision(Utils.getEnvOrConfig("WURST_JASSDOC_DB_REV").orElse("latest"));
        Path dbPath = dbDir.resolve("jassdoc-" + revision + ".db");
        if (!Files.exists(dbPath)) {
            Optional<Path> fallback = findFirstExisting(dbDir,
                "jassdoc-" + revision + ".db",
                "jassdoc-" + revision + ".sqlite",
                "jass.db",
                "jassdoc.db",
                "jassdoc.sqlite");
            if (fallback.isPresent()) {
                return fallback;
            }
        }

        boolean needsDownload = !Files.exists(dbPath);
        if (!needsDownload && "latest".equals(revision)) {
            needsDownload = isStaleLatest(dbPath);
        }

        if (!needsDownload) {
            return Optional.of(dbPath);
        }

        List<String> urls = Utils.getEnvOrConfig("WURST_JASSDOC_DB_URL")
            .map(List::of)
            .orElseGet(() -> defaultDbUrlsForRevision(revision));

        if (urls.isEmpty()) {
            showMissingSourceWarning();
            return Optional.empty();
        }

        List<String> errors = new ArrayList<>();
        for (String url : urls) {
            try {
                download(url, dbPath);
                WLogger.info("Downloaded JassDoc DB from " + url + " to " + dbPath);
                return Optional.of(dbPath);
            } catch (IOException e) {
                errors.add(url + " -> " + e.getMessage());
            }
        }

        if (Files.exists(dbPath)) {
            return Optional.of(dbPath);
        }
        if (!errors.isEmpty()) {
            throw new IOException("Could not download JassDoc DB. Tried: " + String.join("; ", errors));
        }
        return Optional.empty();
    }

    private List<String> defaultDbUrlsForRevision(String revision) {
        List<String> urls = new ArrayList<>();
        if ("latest".equals(revision)) {
            urls.addAll(resolveLatestReleaseAssetUrls());
            urls.add(RELEASES_DOWNLOAD_BASE + "/latest/download/jass.db");
        } else {
            urls.add(RELEASES_DOWNLOAD_BASE + "/download/" + revision + "/jass.db");
            urls.add(RELEASES_DOWNLOAD_BASE + "/download/" + revision + "/jassdoc.db");
            urls.add(RELEASES_DOWNLOAD_BASE + "/download/" + revision + "/jassdoc.sqlite");
        }
        return urls;
    }

    private List<String> resolveLatestReleaseAssetUrls() {
        List<String> urls = new ArrayList<>();
        urls.addAll(readReleaseAssetUrls(RELEASES_LATEST_API));
        if (!urls.isEmpty()) {
            return urls;
        }
        urls.addAll(readNewestReleaseAssetUrlsFromList());
        return urls;
    }

    private List<String> readNewestReleaseAssetUrlsFromList() {
        List<String> urls = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(RELEASES_API).openConnection();
            con.setConnectTimeout(10_000);
            con.setReadTimeout(20_000);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github+json");
            con.setRequestProperty("User-Agent", "WurstScript-LSP");
            int code = con.getResponseCode();
            if (code >= 200 && code < 300) {
                String body;
                try (InputStream in = con.getInputStream()) {
                    body = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                JsonArray releases = JsonParser.parseString(body).getAsJsonArray();
                for (JsonElement e : releases) {
                    if (!e.isJsonObject()) {
                        continue;
                    }
                    JsonObject rel = e.getAsJsonObject();
                    List<String> relUrls = extractDbAssetUrls(rel);
                    if (!relUrls.isEmpty()) {
                        urls.addAll(relUrls);
                        break;
                    }
                }
            }
            con.disconnect();
        } catch (Exception e) {
            WLogger.info("Could not query releases list for JassDoc DB: " + e.getMessage());
        }
        return urls;
    }

    private List<String> readReleaseAssetUrls(String apiUrl) {
        List<String> urls = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
            con.setConnectTimeout(10_000);
            con.setReadTimeout(20_000);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github+json");
            con.setRequestProperty("User-Agent", "WurstScript-LSP");
            int code = con.getResponseCode();
            if (code >= 200 && code < 300) {
                String body;
                try (InputStream in = con.getInputStream()) {
                    body = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                urls.addAll(extractDbAssetUrls(obj));
            }
            con.disconnect();
        } catch (Exception e) {
            WLogger.info("Could not query latest JassDoc release API: " + e.getMessage());
        }
        return urls;
    }

    private List<String> extractDbAssetUrls(JsonObject releaseObj) {
        List<String> urls = new ArrayList<>();
        JsonArray assets = releaseObj.getAsJsonArray("assets");
        if (assets == null) {
            return urls;
        }
        for (JsonElement e : assets) {
            if (!e.isJsonObject()) {
                continue;
            }
            JsonObject asset = e.getAsJsonObject();
            String name = getString(asset, "name");
            String browserDownload = getString(asset, "browser_download_url");
            if (browserDownload == null || name == null) {
                continue;
            }
            String n = name.toLowerCase(Locale.ROOT);
            if (n.endsWith(".db") || n.endsWith(".sqlite")) {
                urls.add(browserDownload);
            }
        }
        return urls;
    }

    private @Nullable String getString(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void showMissingSourceWarning() {
        if (missingSourceWarningShown) {
            return;
        }
        missingSourceWarningShown = true;
        WLogger.warning(
            "No JassDoc DB source configured. "
                + "Set WURST_JASSDOC_DB_PATH to a local jass.db/sqlite file, "
                + "or WURST_JASSDOC_DB_URL to a downloadable DB artifact."
        );
    }

    private Optional<Path> findFirstExisting(Path dir, String... names) {
        return Stream.of(names)
            .map(dir::resolve)
            .filter(Files::exists)
            .findFirst();
    }

    private boolean isStaleLatest(Path dbPath) throws IOException {
        FileTime modified = Files.getLastModifiedTime(dbPath);
        Duration maxAge = Utils.getEnvOrConfig("WURST_JASSDOC_DB_MAX_AGE")
            .map(this::parseDurationOrDefault)
            .orElse(DEFAULT_LATEST_MAX_AGE);
        Instant cutoff = Instant.now().minus(maxAge);
        return modified.toInstant().isBefore(cutoff);
    }

    private Duration parseDurationOrDefault(String text) {
        try {
            return Duration.parse(text);
        } catch (Exception e) {
            return DEFAULT_LATEST_MAX_AGE;
        }
    }

    private void download(String urlString, Path target) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10_000);
        con.setReadTimeout(20_000);
        con.setInstanceFollowRedirects(true);
        con.setRequestMethod("GET");
        int code = con.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + " for " + urlString);
        }
        Path tmp = Files.createTempFile(target.getParent(), "jassdoc-", ".tmp");
        try (InputStream in = con.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            con.disconnect();
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private Connection open(Path dbPath) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // Driver may still be auto-loaded by JDBC.
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
    }

    private List<TableSchema> discoverSchemas(Connection conn) throws SQLException {
        List<TableSchema> result = new ArrayList<>();
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet tables = md.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                if (table == null) {
                    continue;
                }
                Map<String, String> cols = new ConcurrentHashMap<>();
                try (ResultSet columns = md.getColumns(null, null, table, "%")) {
                    while (columns.next()) {
                        String c = columns.getString("COLUMN_NAME");
                        if (c != null) {
                            cols.put(c.toLowerCase(Locale.ROOT), c);
                        }
                    }
                }
                String nameCol = first(cols, "name", "native", "identifier", "symbol");
                String docCol = first(cols, "documentation", "description", "doc", "comment", "notes", "help");
                if (nameCol == null || docCol == null) {
                    continue;
                }
                String kindCol = first(cols, "kind", "type", "symbol_type", "category");
                String patchCol = first(cols, "patch", "introduced", "since", "version", "introduced_patch");
                result.add(new TableSchema(table, nameCol, docCol, kindCol, patchCol));
            }
        }
        return result;
    }

    private boolean hasLegacyJassdocSchema(Connection conn) throws SQLException {
        return tableExists(conn, "parameters");
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet tables = md.getTables(null, null, tableName, new String[]{"TABLE"})) {
            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                if (name != null && name.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> readKeyValueRows(Connection conn, String table, String symbolCol, String keyCol, String valueCol, String symbolName) throws SQLException {
        Map<String, String> result = new ConcurrentHashMap<>();
        String sql = "SELECT " + q(keyCol) + ", " + q(valueCol)
            + " FROM " + q(table)
            + " WHERE lower(" + q(symbolCol) + ") = lower(?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, symbolName);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(1);
                    String value = trimToNull(rs.getString(2));
                    if (key == null || value == null) {
                        continue;
                    }
                    result.putIfAbsent(key.toLowerCase(Locale.ROOT), value);
                }
            }
        }
        return result;
    }

    private @Nullable String firstValue(Map<String, String> primary, Map<String, String> secondary, String... keys) {
        for (String key : keys) {
            String k = key.toLowerCase(Locale.ROOT);
            String value = trimToNull(primary.get(k));
            if (value != null) {
                return value;
            }
            value = trimToNull(secondary.get(k));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private @Nullable String first(Map<String, String> cols, String... names) {
        for (String name : names) {
            String found = cols.get(name.toLowerCase(Locale.ROOT));
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private int score(@Nullable String dbKind, SymbolKind requested) {
        if (dbKind == null || dbKind.isBlank()) {
            return 0;
        }
        String kind = dbKind.toLowerCase(Locale.ROOT);
        if (requested == SymbolKind.FUNCTION && (kind.contains("func") || kind.contains("native"))) {
            return 2;
        }
        if (requested == SymbolKind.VARIABLE && (kind.contains("var") || kind.contains("global") || kind.contains("const"))) {
            return 2;
        }
        return -1;
    }

    private String formatDoc(String doc, @Nullable String patch) {
        String p = trimToNull(patch);
        if (p == null) {
            return doc;
        }
        return doc + "\n\n_Since " + p + "_";
    }

    private static boolean isJassBuiltinSource(String sourceFile) {
        String s = sourceFile.replace('\\', '/').toLowerCase(Locale.ROOT);
        return s.endsWith("/common.j") || s.endsWith("/blizzard.j") || s.equals("common.j") || s.equals("blizzard.j");
    }

    private static @Nullable String trimToNull(@Nullable String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String sanitizeRevision(String revision) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < revision.length(); i++) {
            char c = revision.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.') {
                sb.append(c);
            }
        }
        if (sb.length() == 0) {
            return "latest";
        }
        return sb.toString();
    }

    private static String q(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
