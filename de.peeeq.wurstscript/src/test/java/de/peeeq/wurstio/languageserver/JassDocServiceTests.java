package de.peeeq.wurstio.languageserver;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class JassDocServiceTests {

    @Test
    public void invalidDownloadDoesNotReplaceExistingDatabase() throws IOException {
        Path dir = Files.createTempDirectory("jassdoc-invalid-download-");
        Path target = dir.resolve("jassdoc-latest.db");
        Path downloaded = dir.resolve("download.tmp");
        Files.writeString(target, "working database", StandardCharsets.UTF_8);
        Files.writeString(downloaded, "proxy error page", StandardCharsets.UTF_8);

        assertThrows(IOException.class,
            () -> new JassDocService().installDownloadedDatabase(downloaded, target));

        assertEquals(Files.readString(target, StandardCharsets.UTF_8), "working database");
        assertFalse(Files.exists(dir.resolve("jassdoc-latest.db.bak")));
    }

    @Test
    public void successfulUpdateKeepsPreviousDatabaseBackup() throws Exception {
        Path dir = Files.createTempDirectory("jassdoc-valid-download-");
        Path target = dir.resolve("jassdoc-latest.db");
        Path downloaded = dir.resolve("download.tmp");
        Files.writeString(target, "previous database", StandardCharsets.UTF_8);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + downloaded.toAbsolutePath());
             Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE docs(name TEXT, documentation TEXT)");
            statement.execute("INSERT INTO docs VALUES ('GetUnitX', 'Returns the x coordinate')");
        }

        new JassDocService().installDownloadedDatabase(downloaded, target);

        assertEquals(Files.readString(dir.resolve("jassdoc-latest.db.bak"), StandardCharsets.UTF_8),
            "previous database");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + target.toAbsolutePath());
             Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery("SELECT documentation FROM docs WHERE name = 'GetUnitX'")) {
            assertTrue(result.next());
            assertEquals(result.getString(1), "Returns the x coordinate");
        }
    }

    @Test
    public void incompleteLegacySchemaDoesNotReplaceExistingDatabase() throws Exception {
        Path dir = Files.createTempDirectory("jassdoc-incomplete-legacy-");
        Path target = dir.resolve("jassdoc-latest.db");
        Path downloaded = dir.resolve("download.tmp");
        Files.writeString(target, "working database", StandardCharsets.UTF_8);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + downloaded.toAbsolutePath());
             Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE parameters(fnname TEXT)");
        }

        assertThrows(IOException.class,
            () -> new JassDocService().installDownloadedDatabase(downloaded, target));
        assertEquals(Files.readString(target, StandardCharsets.UTF_8), "working database");
    }

    @Test
    public void restoreOverwritesAResidualPartialTarget() throws IOException {
        Path dir = Files.createTempDirectory("jassdoc-restore-");
        Path target = dir.resolve("jassdoc-latest.db");
        Path backup = dir.resolve("jassdoc-latest.db.bak");
        Files.writeString(target, "partial replacement", StandardCharsets.UTF_8);
        Files.writeString(backup, "working database", StandardCharsets.UTF_8);
        IOException installFailure = new IOException("simulated interrupted replacement");

        new JassDocService().restoreBackup(backup, target, installFailure);

        assertEquals(Files.readString(target, StandardCharsets.UTF_8), "working database");
        assertEquals(installFailure.getSuppressed().length, 0);
    }

    @Test
    public void automaticUpdatesCanBeDisabled() {
        String previous = System.getProperty("WURST_JASSDOC_DB_AUTO_UPDATE");
        try {
            System.setProperty("WURST_JASSDOC_DB_AUTO_UPDATE", "false");
            assertFalse(new JassDocService().autoUpdateEnabled());
        } finally {
            if (previous == null) {
                System.clearProperty("WURST_JASSDOC_DB_AUTO_UPDATE");
            } else {
                System.setProperty("WURST_JASSDOC_DB_AUTO_UPDATE", previous);
            }
        }
    }

    @Test
    public void proxyBypassSupportsStandardHostForms() {
        assertTrue(JassDocService.shouldBypassProxy(
            "api.github.com", "localhost, .github.com, internal.example:8080"));
        assertTrue(JassDocService.shouldBypassProxy(
            "github.com", "localhost, .github.com, internal.example:8080"));
        assertTrue(JassDocService.shouldBypassProxy(
            "internal.example", "localhost, github.com, internal.example:8080"));
        assertFalse(JassDocService.shouldBypassProxy(
            "github.com", "localhost, example.com"));
    }

    @Test
    public void unsupportedTlsProxyIsRejectedExplicitly() {
        IOException error = expectThrows(IOException.class,
            () -> JassDocService.parseHttpProxyUri("https://proxy.example"));
        assertTrue(error.getMessage().contains("use an http:// proxy URL"));
    }
}
