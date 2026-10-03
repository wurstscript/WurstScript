package de.peeeq.wurstio.map.importer;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.utils.FileUtils;
import net.moonlightflower.wc3libs.bin.app.IMP;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ImportFileTests {
    private static final String MANIFEST_PATH = "wurst_cache_manifest.txt";

    private Path tempDir;

    @AfterMethod(alwaysRun = true)
    public void cleanup() throws IOException {
        if (tempDir != null) {
            FileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    public void hashesBytesAndFilesWithMd5() throws Exception {
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);
        assertEquals(ImportFile.calculateHash(content), "900150983cd24fb0d6963f7d28e17f72");

        tempDir = Files.createTempDirectory("wurst-import-hash");
        Path file = Files.write(tempDir.resolve("value.bin"), content);
        assertEquals(ImportFile.calculateFileHash(file.toFile()), "900150983cd24fb0d6963f7d28e17f72");
    }

    @Test
    public void manifestRoundTripPreservesConfigsAndImports() {
        ImportFile.CacheManifest manifest = new ImportFile.CacheManifest();
        manifest.setW3iConfig("w3i-hash");
        manifest.setMapConfig("map-hash");
        manifest.importFiles.put("models\\unit.mdx",
            new ImportFile.CacheManifest.FileEntry("file-hash", 123L));

        ImportFile.CacheManifest restored = ImportFile.CacheManifest.deserialize(manifest.serialize());

        assertTrue(restored.hasW3iConfig());
        assertTrue(restored.hasMapConfig());
        assertTrue(restored.w3iConfigMatches("w3i-hash"));
        assertTrue(restored.mapConfigMatches("map-hash"));
        assertEquals(restored.importFiles.get("models\\unit.mdx").hash, "file-hash");
        assertEquals(restored.importFiles.get("models\\unit.mdx").lastModified, 123L);
    }

    @Test
    public void manifestRoundTripKeepsTheSourceOfAnImport() {
        ImportFile.CacheManifest manifest = new ImportFile.CacheManifest();
        manifest.importFiles.put("models\\unit.mdx",
            new ImportFile.CacheManifest.FileEntry("file-hash", 123L, 456L, "imports/my models/unit.mdx"));

        ImportFile.CacheManifest.FileEntry restored =
            ImportFile.CacheManifest.deserialize(manifest.serialize()).importFiles.get("models\\unit.mdx");

        assertEquals(restored.hash, "file-hash");
        assertEquals(restored.lastModified, 123L);
        assertEquals(restored.size, 456L);
        assertEquals(restored.source, "imports/my models/unit.mdx");
    }

    @Test
    public void importEntryFromAnOlderManifestIsNeverTrustedAsTheSameFile() throws Exception {
        ImportFile.CacheManifest restored = ImportFile.CacheManifest.deserialize(
            "IMPORT|models\\unit.mdx|file-hash|123\n");
        ImportFile.CacheManifest.FileEntry entry = restored.importFiles.get("models\\unit.mdx");
        assertEquals(entry.hash, "file-hash");
        assertEquals(entry.lastModified, 123L);

        tempDir = Files.createTempDirectory("wurst-import-legacy");
        Path file = Files.write(tempDir.resolve("unit.mdx"), new byte[3]);
        Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(123L));
        assertFalse(entry.isSameFile("imports/unit.mdx", file.toFile()),
            "an entry without a recorded source must be hashed again");
    }

    @Test
    public void malformedManifestLinesAreIgnored() {
        ImportFile.CacheManifest restored = ImportFile.CacheManifest.deserialize(
            "# comment\ninvalid\nIMPORT|bad|hash|not-a-number\nUNKNOWN|path|hash|1\n");

        assertFalse(restored.hasW3iConfig());
        assertFalse(restored.hasMapConfig());
        assertTrue(restored.importFiles.isEmpty());
    }

    @Test
    public void manifestStorageUsesMpqCacheFile() {
        FakeMpqEditor mpq = new FakeMpqEditor();
        ImportFile.CacheManifest manifest = new ImportFile.CacheManifest();
        manifest.setMapConfig("map-hash");

        ImportFile.saveManifest(mpq, manifest);
        assertTrue(ImportFile.getCachedManifest(mpq).orElseThrow().mapConfigMatches("map-hash"));

        ImportFile.invalidateCache(mpq);
        assertTrue(ImportFile.getCachedManifest(mpq).isEmpty());
    }

    @Test
    public void cachedImportUpdatesAndDeletesOnlyChangedFiles() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-cache");
        Path imports = Files.createDirectories(tempDir.resolve("imports").resolve("nested"));
        Path source = Files.writeString(imports.resolve("unit.txt"), "abc", StandardCharsets.UTF_8);
        FakeMpqEditor mpq = new FakeMpqEditor();

        ImportFile.ImportResult first = ImportFile.importFilesFromImports(tempDir.toFile(), mpq);
        assertEquals(first.filesProcessed, 1);
        assertEquals(first.filesUpdated, 1);
        assertEquals(first.filesDeleted, 0);
        assertFalse(first.cacheUsed);
        assertTrue(mpq.hasFile("nested\\unit.txt"));
        assertTrue(mpq.hasFile(IMP.GAME_PATH));
        assertTrue(mpq.hasFile(MANIFEST_PATH));

        ImportFile.ImportResult cached = ImportFile.importFilesFromImports(tempDir.toFile(), mpq);
        assertEquals(cached.filesProcessed, 1);
        assertEquals(cached.filesUpdated, 0);
        assertEquals(cached.filesDeleted, 0);
        assertTrue(cached.cacheUsed);

        Files.delete(source);
        ImportFile.ImportResult deleted = ImportFile.importFilesFromImports(tempDir.toFile(), mpq);
        assertEquals(deleted.filesProcessed, 0);
        assertEquals(deleted.filesUpdated, 0);
        assertEquals(deleted.filesDeleted, 1);
        assertFalse(deleted.cacheUsed);
        assertFalse(mpq.hasFile("nested\\unit.txt"));
    }

    @Test
    public void importTableListsMapFolderAssetsButNotMapFiles() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-table");
        FakeMpqEditor mpq = new FakeMpqEditor();
        // A map folder's own assets are already in the archive and have no import directory behind them.
        for (String name : new String[] {"biomes\\chaos.dds", "BrutalLord.MDX", "war3mapImported\\x.mdx",
                "war3map.w3u", "war3map.lua", "war3mapMap.blp", "war3mapMisc.txt", "scripts\\war3map.j",
                "(listfile)", "(attributes)"}) {
            mpq.insertFile(name, new byte[] {1});
        }

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);

        assertEquals(readImportTable(mpq.extractFile(IMP.GAME_PATH)),
            List.of("biomes\\chaos.dds", "BrutalLord.MDX", "war3mapImported\\x.mdx"));
    }

    @Test
    public void importTableReplacesStaleEntriesAndIsNotRewrittenWhenUnchanged() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-table-stale");
        FakeMpqEditor mpq = new FakeMpqEditor();
        mpq.insertFile("a\\real.dds", new byte[] {1});
        mpq.insertFile(IMP.GAME_PATH, ImportFile.buildImportTable(List.of("gone\\deleted.mdx"), List.of()));
        mpq.insertCounts.clear();

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);
        assertEquals(readImportTable(mpq.extractFile(IMP.GAME_PATH)), List.of("a\\real.dds"));
        assertEquals(mpq.insertCounts.get(IMP.GAME_PATH), Integer.valueOf(1));

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);
        assertEquals(mpq.insertCounts.get(IMP.GAME_PATH), Integer.valueOf(1));
    }

    @Test
    public void importTableMergesImportDirectoriesWithArchiveFilesWithoutDuplicates() throws Exception {
        byte[] table = ImportFile.buildImportTable(
            List.of("models/Unit.mdx", "tex\\a.blp"),
            List.of("models\\unit.mdx", "tex\\b.blp", "war3map.imp"));

        assertEquals(readImportTable(table), List.of("models\\Unit.mdx", "tex\\a.blp", "tex\\b.blp"));
        assertFalse(ImportFile.isMapSystemFile("war3mapImported\\war3mapThing.mdx"));
        assertFalse(ImportFile.isMapSystemFile("units\\unitdata.slk"));
    }

    @Test
    public void mapSystemFilesAreRecognisedByCompleteKnownName() {
        // Every root file seen in real maps, plus the documented map and campaign files.
        for (String system : new String[] {"war3map.doo", "war3map.imp", "war3map.j", "war3map.lua", "war3map.mmp",
                "war3map.shd", "war3map.w3a", "war3map.w3b", "war3map.w3c", "war3map.w3d", "war3map.w3e", "war3map.w3h",
                "war3map.w3i", "war3map.w3q", "war3map.w3r", "war3map.w3s", "war3map.w3t", "war3map.w3u", "war3map.wct",
                "war3map.wpm", "war3map.wtg", "war3map.wts", "war3mapExtra.txt", "war3mapMap.blp", "war3mapMap.dds",
                "war3mapMap.tga", "war3mapMisc.txt", "war3mapPath.tga", "war3mapPreview.tga", "war3mapSkin.txt",
                "war3mapSkin.w3a", "war3mapSkin.w3b", "war3mapSkin.w3d", "war3mapSkin.w3h", "war3mapSkin.w3q",
                "war3mapSkin.w3t", "war3mapSkin.w3u", "war3mapUnits.doo", "war3campaign.w3f", "WAR3MAP.W3E"}) {
            assertTrue(ImportFile.isMapSystemFile(system), system);
        }
        // A root asset that shares a map file's prefix or stem, or reuses its stem with another extension,
        // is an import and must reach the table.
        for (String asset : new String[] {"war3mapHero.mdx", "war3campaignMusic.mp3", "war3mapGrass.blp",
                "war3map.mdx", "war3mapMap.mdx", "war3mapSkin.mdx", "war3map.w3u.bak", "war3campaign.mp3"}) {
            assertFalse(ImportFile.isMapSystemFile(asset), asset);
        }
    }

    @Test
    public void importTableKeepsRootAssetsThatShareAMapFilePrefix() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-table-prefix");
        FakeMpqEditor mpq = new FakeMpqEditor();
        for (String name : new String[] {"war3mapHero.mdx", "war3campaignMusic.mp3", "war3map.mdx", "war3mapMap.mdx",
                "war3map.w3u", "war3mapMisc.txt"}) {
            mpq.insertFile(name, new byte[] {1});
        }

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);

        assertEquals(readImportTable(mpq.extractFile(IMP.GAME_PATH)),
            List.of("war3campaignMusic.mp3", "war3map.mdx", "war3mapHero.mdx", "war3mapMap.mdx"));
    }

    @Test
    public void importTableKeepsExistingEntriesForFilesTheListingDoesNotShow() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-table-unlisted");
        FakeMpqEditor mpq = new FakeMpqEditor();
        // An archive without a complete (listfile) still holds these files, but cannot enumerate them.
        mpq.insertFile("hidden\\a.mdx", new byte[] {1});
        mpq.insertFile("war3mapImported\\b.mdx", new byte[] {1});
        mpq.insertFile("listed\\c.blp", new byte[] {1});
        mpq.unlisted.add("hidden\\a.mdx");
        mpq.unlisted.add("war3mapImported\\b.mdx");
        // The table written by the editor names them with a custom path (13) and a standard path (5, relative
        // to war3mapImported\), plus one whose file is gone.
        mpq.insertFile(IMP.GAME_PATH, importTableBytes(
            new Object[] {13, "hidden\\a.mdx"}, new Object[] {5, "b.mdx"}, new Object[] {13, "gone\\d.mdx"}));

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);

        assertEquals(readImportTable(mpq.extractFile(IMP.GAME_PATH)),
            List.of("hidden\\a.mdx", "listed\\c.blp", "war3mapImported\\b.mdx"));
    }

    @Test
    public void importTableKeepsExistingNonAsciiEntriesAndWritesThemAsUtf8() throws Exception {
        tempDir = Files.createTempDirectory("wurst-import-table-utf8");
        FakeMpqEditor mpq = new FakeMpqEditor();
        String path = "täxtures\\élève中.blp";
        mpq.insertFile(path, new byte[] {1});
        mpq.unlisted.add(path);
        mpq.insertFile(IMP.GAME_PATH, importTableBytes(new Object[] {13, path}));

        assertEquals(ImportFile.readImportTablePaths(mpq.extractFile(IMP.GAME_PATH)), List.of(path));

        ImportFile.importFilesFromImports(tempDir.toFile(), mpq);

        assertEquals(readImportTable(mpq.extractFile(IMP.GAME_PATH)), List.of(path));
    }

    private static byte[] importTableBytes(Object[]... entries) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(1).putInt(entries.length);
        out.writeBytes(header.array());
        for (Object[] entry : entries) {
            out.write((Integer) entry[0]);
            out.writeBytes(((String) entry[1]).getBytes(StandardCharsets.UTF_8));
            out.write(0);
        }
        return out.toByteArray();
    }

    /** Reads the paths out of a war3map.imp: int version, int count, then a flag byte and a C string each. */
    private static List<String> readImportTable(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buffer.getInt();
        int count = buffer.getInt();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            buffer.get();
            java.io.ByteArrayOutputStream path = new java.io.ByteArrayOutputStream();
            for (byte b = buffer.get(); b != 0; b = buffer.get()) {
                path.write(b);
            }
            paths.add(path.toString(StandardCharsets.UTF_8));
        }
        return paths;
    }

    private static final class FakeMpqEditor implements MpqEditor {
        private final Map<String, byte[]> files = new HashMap<>();
        private final Map<String, Integer> insertCounts = new HashMap<>();
        /** Files that are in the archive but that an incomplete (listfile) leaves out of the enumeration. */
        private final java.util.Set<String> unlisted = new java.util.HashSet<>();

        @Override
        public Collection<String> listFiles() {
            List<String> listed = new ArrayList<>(files.keySet());
            listed.removeAll(unlisted);
            return listed;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public byte[] extractFile(String fileToExtract) throws IOException {
            byte[] result = files.get(fileToExtract);
            if (result == null) {
                throw new IOException("Missing " + fileToExtract);
            }
            return result;
        }

        @Override
        public void insertFile(String filenameInMpq, byte[] contents) {
            insertCounts.merge(filenameInMpq, 1, Integer::sum);
            files.put(filenameInMpq, contents);
        }

        @Override
        public void insertFile(String filenameInMpq, File contents) throws IOException {
            insertCounts.merge(filenameInMpq, 1, Integer::sum);
            files.put(filenameInMpq, Files.readAllBytes(contents.toPath()));
        }

        @Override
        public void deleteFile(String filenameInMpq) {
            files.remove(filenameInMpq);
        }

        @Override
        public boolean hasFile(String fileName) {
            return files.containsKey(fileName);
        }

        @Override
        public void setKeepHeaderOffset(boolean flag) {
        }

        @Override
        public void closeWithCompression() {
        }

        @Override
        public void close() {
        }
    }
}
