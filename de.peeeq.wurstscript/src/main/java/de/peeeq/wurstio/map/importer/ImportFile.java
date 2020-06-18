package de.peeeq.wurstio.map.importer;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.TempDir;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;

public class ImportFile {
    private static final String DEFAULT_IMPORT_PATH = "war3mapImported\\";
    private static final int FILE_VERSION = 1;

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
        try (MpqEditor editor = MpqEditorFactory.getEditor(Optional.of(tempMap))) {
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
            JOptionPane.showMessageDialog(null, "No vaild war3map.imp was found, or there are no imports");
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
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                getFilesOfDirectory(f, addTo);
            } else {
                addTo.add(f);
            }
        }
        return addTo;

    }

    private static void insertImportedFiles(MpqEditor mpq, File directory) throws Exception {
        LinkedList<File> files = new LinkedList<>();
        getFilesOfDirectory(directory, files);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(FILE_VERSION).array());
        dataOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(files.size()).array());
        for (File f : files) {
            Path p = f.toPath();
            p = directory.toPath().relativize(p);
            String normalizedWc3Path = p.toString().replaceAll("/", "\\\\");
            dataOut.writeByte((byte) 13);
            dataOut.write(normalizedWc3Path.getBytes("UTF-8"));
            dataOut.write((byte) 0);
            WLogger.info("importing file: " + normalizedWc3Path);
            mpq.deleteFile(normalizedWc3Path);
            mpq.insertFile(normalizedWc3Path, f);
        }
        dataOut.flush();
        mpq.deleteFile("war3map.imp");
        mpq.insertFile("war3map.imp", byteOut.toByteArray());
    }

    public static void importFilesFromImportDirectory(File projectFolder, MpqEditor ed) {
        File importDirectory = getImportDirectory(projectFolder);
        if (importDirectory.exists() && importDirectory.isDirectory()) {
            WLogger.info("importing from: " + importDirectory.getAbsolutePath());
            WLogger.info("projectFolder: " + projectFolder.getAbsolutePath());
            try {
                insertImportedFiles(ed, importDirectory);
            } catch (Exception e) {
                WLogger.severe(e);
                JOptionPane.showMessageDialog(null, "Couldn't import resources from " + importDirectory + ": " + e.getMessage());
            }
        }
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

}
