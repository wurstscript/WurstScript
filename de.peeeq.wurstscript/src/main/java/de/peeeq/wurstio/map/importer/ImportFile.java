package de.peeeq.wurstio.map.importer;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.TempDir;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.IMP;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Stream;

/**
 * Handles import file management with intelligent caching.
 *
 * Public API:
 * - importFilesFromImports() - Smart import with caching
 * - extractImportsFromMap() - Extract imports to project folder
 * - getCachedManifest() - Get the current manifest state
 * - invalidateCache() - Force a full rebuild on next import
 */
public class ImportFile {
    private static final String DEFAULT_IMPORT_PATH = "war3mapImported\\";
    private static final int FILE_VERSION = 1;
    private static final String MANIFEST_MPQ_PATH = "wurst_cache_manifest.txt";

    /**
     * Result of an import operation
     */
    public static class ImportResult {
        public final int filesProcessed;
        public final int filesUpdated;
        public final int filesDeleted;
        public final long durationMs;
        public final boolean cacheUsed;

        ImportResult(int filesProcessed, int filesUpdated, int filesDeleted, long durationMs, boolean cacheUsed) {
            this.filesProcessed = filesProcessed;
            this.filesUpdated = filesUpdated;
            this.filesDeleted = filesDeleted;
            this.durationMs = durationMs;
            this.cacheUsed = cacheUsed;
        }

        @Override
        public String toString() {
            return String.format("ImportResult{processed=%d, updated=%d, deleted=%d, duration=%dms, cached=%s}",
                filesProcessed, filesUpdated, filesDeleted, durationMs, cacheUsed);
        }
    }

    /**
     * Represents the cached state of all map data (imports + config)
     */
    public static class CacheManifest {
        Map<String, FileEntry> importFiles = new HashMap<>();
        ConfigEntry w3iConfig = null;
        ConfigEntry mapConfig = null;

        static class FileEntry {
            String hash;
            long lastModified;

            FileEntry(String hash, long lastModified) {
                this.hash = hash;
                this.lastModified = lastModified;
            }
        }

        static class ConfigEntry {
            String hash;
            long timestamp;

            ConfigEntry(String hash, long timestamp) {
                this.hash = hash;
                this.timestamp = timestamp;
            }
        }

        String serialize() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Wurst Cache Manifest v2\n");
            sb.append("# Format: TYPE|path|hash|lastModified\n");

            // Serialize w3i config
            if (w3iConfig != null) {
                sb.append("W3I|config|")
                    .append(w3iConfig.hash)
                    .append("|")
                    .append(w3iConfig.timestamp)
                    .append("\n");
            }

            // Serialize map config
            if (mapConfig != null) {
                sb.append("MAPCONFIG|config|")
                    .append(mapConfig.hash)
                    .append("|")
                    .append(mapConfig.timestamp)
                    .append("\n");
            }

            // Serialize import files
            for (Map.Entry<String, FileEntry> entry : importFiles.entrySet()) {
                sb.append("IMPORT|")
                    .append(entry.getKey())
                    .append("|")
                    .append(entry.getValue().hash)
                    .append("|")
                    .append(entry.getValue().lastModified)
                    .append("\n");
            }
            return sb.toString();
        }

        static CacheManifest deserialize(String data) {
            CacheManifest manifest = new CacheManifest();
            if (data == null || data.isEmpty()) {
                return manifest;
            }

            String[] lines = data.split("\n");
            for (String line : lines) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    continue;
                }

                try {
                    String type = parts[0];
                    String path = parts[1];
                    String hash = parts[2];
                    long timestamp = Long.parseLong(parts[3]);

                    switch (type) {
                        case "W3I":
                            manifest.w3iConfig = new ConfigEntry(hash, timestamp);
                            break;
                        case "MAPCONFIG":
                            manifest.mapConfig = new ConfigEntry(hash, timestamp);
                            break;
                        case "IMPORT":
                            manifest.importFiles.put(path, new FileEntry(hash, timestamp));
                            break;
                    }
                } catch (NumberFormatException e) {
                    WLogger.warning("Invalid manifest line: " + line);
                }
            }
            return manifest;
        }

        public boolean hasW3iConfig() {
            return w3iConfig != null;
        }

        public boolean hasMapConfig() {
            return mapConfig != null;
        }

        public boolean w3iConfigMatches(String hash) {
            return w3iConfig != null && w3iConfig.hash.equals(hash);
        }

        public boolean mapConfigMatches(String hash) {
            return mapConfig != null && mapConfig.hash.equals(hash);
        }

        public void setW3iConfig(String hash) {
            this.w3iConfig = new ConfigEntry(hash, System.currentTimeMillis());
        }

        public void setMapConfig(String hash) {
            this.mapConfig = new ConfigEntry(hash, System.currentTimeMillis());
        }
    }

    /**
     * PUBLIC API: Get the current cached manifest from an MPQ
     */
    public static Optional<CacheManifest> getCachedManifest(MpqEditor mpq) {
        try {
            if (mpq.hasFile(MANIFEST_MPQ_PATH)) {
                byte[] data = mpq.extractFile(MANIFEST_MPQ_PATH);
                String content = new String(data, StandardCharsets.UTF_8);
                return Optional.of(CacheManifest.deserialize(content));
            }
        } catch (Exception e) {
            WLogger.info("Could not load manifest from MPQ: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * PUBLIC API: Invalidate cache by removing manifest from MPQ
     */
    public static void invalidateCache(MpqEditor mpq) {
        try {
            mpq.deleteFile(MANIFEST_MPQ_PATH);
            WLogger.info("Cache invalidated - next build will be full rebuild");
        } catch (Exception e) {
            WLogger.warning("Could not invalidate cache: " + e.getMessage());
        }
    }

    /**
     * PUBLIC API: Save manifest to MPQ
     */
    public static void saveManifest(MpqEditor mpq, CacheManifest manifest) {
        try {
            String serialized = manifest.serialize();
            byte[] data = serialized.getBytes(StandardCharsets.UTF_8);
            mpq.insertFile(MANIFEST_MPQ_PATH, data);
            WLogger.info("Saved cache manifest to MPQ");
        } catch (Exception e) {
            WLogger.warning("Could not save manifest to MPQ: " + e.getMessage());
        }
    }

    /**
     * PUBLIC API: Calculate hash of any data
     */
    public static String calculateHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    /**
     * PUBLIC API: Calculate hash of a file
     */
    public static String calculateFileHash(File file) throws Exception {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    /**
     * PUBLIC API: Main entry point for importing files with intelligent caching
     */
    public static ImportResult importFilesFromImports(File projectFolder, MpqEditor ed) {
        LinkedList<File> folders = new LinkedList<>();
        folders.add(getImportDirectory(projectFolder));
        folders.addAll(Arrays.asList(getTransientImportDirectories(projectFolder)));

        folders.removeIf(folder -> !folder.exists());

        try {
            return insertImportedFiles_Cached(ed, folders);
        } catch (Exception e) {
            WLogger.severe(e);
            throw new RuntimeException("Failed to import resources: " + e.getMessage(), e);
        }
    }

    /**
     * Use this to start the extraction process
     */
    public static void extractImportsFromMap(File mapFile, RunArgs runArgs) {
        WLogger.info("Extracting all imported Files...");
        if (!mapFile.exists() || !mapFile.isFile() || mapFile.getName().endsWith("w3m")) {
            JOptionPane.showMessageDialog(null, "Map " + mapFile.getAbsolutePath() + " does not exist or is of wrong format (w3x only)");
            return;
        }

        try {
            File projectFolder = mapFile.getParentFile();
            File importDirectory = getImportDirectory(projectFolder);
            File tempMap = getCopyOfMap(mapFile);

            extractImportsFrom(importDirectory, tempMap, runArgs);
        } catch (Exception e) {
            WLogger.severe(e);
            JOptionPane.showMessageDialog(null, "Could not export objects (2): " + e.getMessage());
        }
    }

    private static void extractImportsFrom(File importDirectory, File tempMap, RunArgs runArgs) throws Exception {
        try (MpqEditor editor = MpqEditorFactory.getEditor(Optional.of(tempMap), true)) {
            LinkedList<String> failed = extractImportedFiles(editor, importDirectory);

            if (failed.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All imports were extracted to " + importDirectory.getAbsolutePath());
            } else {
                String message = "Following files could not be extracted:\n" + String.join(",", failed);
                WLogger.info(message);
                JOptionPane.showMessageDialog(null, message);
            }
        }
    }

    private static LinkedList<String> extractImportedFiles(MpqEditor mpq, File directory) {
        LinkedList<String> failed = new LinkedList<>();
        byte[] temp;
        try {
            temp = mpq.extractFile("war3map.imp");
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(null, "No valid war3map.imp was found, or there are no imports");
            return failed;
        }
        try (LittleEndianDataInputStream reader = new LittleEndianDataInputStream(new ByteArrayInputStream(temp))) {
            @SuppressWarnings("unused")
            int fileFormatVersion = reader.readInt(); // Not needed
            int fileCount = reader.readInt();
            WLogger.info("Imported FileCount: " + fileCount);

            for (int i = 1; i <= fileCount; i++) {
                extractImportedFile(mpq, directory, failed, reader);
            }

            return failed;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void extractImportedFile(MpqEditor mpq, File directory, LinkedList<String> failed, LittleEndianDataInputStream reader) throws Exception {
        byte b = reader.readByte();
        String path = directory.getPath() + "\\";
        String mpqpath = "";
        if (isStandardPath(b)) {
            path += DEFAULT_IMPORT_PATH;
            mpqpath += DEFAULT_IMPORT_PATH;
        }

        String filename = readString(reader);
        WLogger.info("Extracting file: " + filename);

        filename = filename.trim();
        mpqpath += filename;
        path += filename;

        extractFile(mpq, directory, failed, path, mpqpath, filename);
    }

    private static void extractFile(MpqEditor mpq, File directory, LinkedList<String> failed, String path, String mpqpath, String filename) throws Exception {
        File out = new File(path);
        out.getParentFile().mkdirs();
        try {
            byte[] xx = mpq.extractFile(mpqpath);
            Files.write(xx, out);
        } catch (IOException e) {
            out.delete();
            out = new File(directory.getPath() + "\\" + DEFAULT_IMPORT_PATH + filename);
            out.getParentFile().mkdirs();
            try {
                byte[] xx = mpq.extractFile(DEFAULT_IMPORT_PATH + mpqpath);
                Files.write(xx, out);
            } catch (IOException e1) {
                failed.add(mpqpath);
            }
        }
    }

    /**
     * Blizzard magic numbers for standard path
     */
    private static boolean isStandardPath(byte b) {
        return b == 5 || b == 8;
    }

    /**
     * Reads chars from the inputstream until it hits a 0-char
     */
    private static String readString(LittleEndianDataInputStream reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                char c = (char) reader.readByte();
                if (c == 0) {
                    return sb.toString();
                }
                sb.append(c);
            }
        } catch (EOFException e) {
            return sb.toString();
        }
    }

    private static LinkedList<File> getFilesOfDirectory(File dir, LinkedList<File> addTo) {
        File[] listFiles = dir.listFiles();
        if (listFiles == null) {
            return addTo;
        }
        for (File f : listFiles) {
            if (f.isDirectory()) {
                getFilesOfDirectory(f, addTo);
            } else {
                addTo.add(f);
            }
        }
        return addTo;
    }

    /**
     * Cached version that only updates changed files
     */
    /**
     * Cached version that only updates changed files
     */
    private static ImportResult insertImportedFiles_Cached(MpqEditor mpq, List<File> directories) throws Exception {
        long startTime = System.currentTimeMillis();

        // Load the old manifest from the MPQ
        CacheManifest oldManifest = getCachedManifest(mpq).orElse(new CacheManifest());
        CacheManifest newManifest = new CacheManifest();

        // Copy over config entries (they're managed separately)
        newManifest.w3iConfig = oldManifest.w3iConfig;
        newManifest.mapConfig = oldManifest.mapConfig;

        boolean importsChanged = false;
        int filesProcessed = 0;
        int filesUpdated = 0;
        int filesDeleted = 0;

        // 1. Gather all current files and their info
        Map<String, File> allFiles = new HashMap<>();
        for (File directory : directories) {
            LinkedList<File> filesInDir = new LinkedList<>();
            getFilesOfDirectory(directory, filesInDir);

            for (File f : filesInDir) {
                Path relativePath = directory.toPath().relativize(f.toPath());
                String normalizedWc3Path = relativePath.toString().replace("/", "\\");
                allFiles.put(normalizedWc3Path, f);
            }
        }

        WLogger.info("Found " + allFiles.size() + " files in import directories");

        // 2. Process current files (check for new/modified)
        for (Map.Entry<String, File> entry : allFiles.entrySet()) {
            String path = entry.getKey();
            File file = entry.getValue();
            filesProcessed++;

            long lastModified = file.lastModified();

            // Quick check: if file hasn't been modified, assume it's the same
            CacheManifest.FileEntry oldEntry = oldManifest.importFiles.get(path);
            if (oldEntry != null && oldEntry.lastModified == lastModified) {
                // File hasn't changed, but verify it exists in MPQ
                if (!mpq.hasFile(path)) {
                    WLogger.info("File in manifest but missing from MPQ, re-adding: " + path);
                    mpq.insertFile(path, file);
                    importsChanged = true;
                    filesUpdated++;
                }
                newManifest.importFiles.put(path, oldEntry);
                continue;
            }

            // File is new or modified, calculate hash
            String newHash = calculateFileHash(file);
            newManifest.importFiles.put(path, new CacheManifest.FileEntry(newHash, lastModified));

            if (oldEntry == null) {
                WLogger.info("New import: " + path);
                importsChanged = true;
                filesUpdated++;
                mpq.insertFile(path, file);
            } else if (!oldEntry.hash.equals(newHash)) {
                WLogger.info("Modified import: " + path);
                importsChanged = true;
                filesUpdated++;
                // Delete first to ensure clean update
                if (mpq.hasFile(path)) {
                    mpq.deleteFile(path);
                }
                mpq.insertFile(path, file);
            }
        }

        // 3. Process deletions (files in old manifest but not in current file list)
        Set<String> deletedFiles = new HashSet<>(oldManifest.importFiles.keySet());
        deletedFiles.removeAll(allFiles.keySet());

        for (String deletedPath : deletedFiles) {
            WLogger.info("Deleting import: " + deletedPath);
            importsChanged = true;
            filesDeleted++;
            if (mpq.hasFile(deletedPath)) {
                mpq.deleteFile(deletedPath);
            }
        }

        // 4. Always rebuild war3map.imp to ensure it's in sync
        if (importsChanged || !mpq.hasFile(IMP.GAME_PATH)) {
            WLogger.info("Rebuilding war3map.imp");
            IMP importFile = new IMP();
            for (String path : allFiles.keySet()) {
                IMP.Obj importObj = new IMP.Obj();
                importObj.setPath(path);
                importObj.setStdFlag(IMP.StdFlag.CUSTOM);
                importFile.addObj(importObj);
            }

            if (mpq.hasFile(IMP.GAME_PATH)) {
                mpq.deleteFile(IMP.GAME_PATH);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (Wc3BinOutputStream out = new Wc3BinOutputStream(baos)) {
                importFile.write(out);
            }
            baos.flush();
            byte[] byteArray = baos.toByteArray();
            mpq.insertFile(IMP.GAME_PATH, byteArray);
        }

        // 5. Save the new manifest AFTER all changes are made
        saveManifest(mpq, newManifest);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        WLogger.info(String.format("Import processing complete in %dms: %d files processed, %d updated, %d deleted",
            duration, filesProcessed, filesUpdated, filesDeleted));

        return new ImportResult(filesProcessed, filesUpdated, filesDeleted, duration, !importsChanged);
    }

    private static File getCopyOfMap(File mapFile) throws IOException {
        File mapTemp = File.createTempFile("temp", "w3x", TempDir.get());
        mapTemp.deleteOnExit();
        Files.copy(mapFile, mapTemp);
        return mapTemp;
    }

    private static File getImportDirectory(File projectFolder) {
        return new File(projectFolder, "imports");
    }

    private static File[] getTransientImportDirectories(File projectFolder) {
        ArrayList<Path> paths = new ArrayList<>();
        Path dependencies = projectFolder.toPath().resolve("_build").resolve("dependencies");
        try (Stream<Path> spaths = java.nio.file.Files.list(dependencies)) {
            spaths.forEach(dependency -> {
                if (java.nio.file.Files.exists(dependency.resolve("imports"))) {
                    paths.add(dependency.resolve("imports"));
                }
            });
        } catch (IOException e) {
            // Dependencies folder doesn't exist or can't be read
        }
        File[] arr = new File[paths.size()];
        List<File> list = new ArrayList<>();
        for (Path path : paths) {
            File file = path.toFile();
            list.add(file);
        }
        return list.toArray(arr);
    }
}
