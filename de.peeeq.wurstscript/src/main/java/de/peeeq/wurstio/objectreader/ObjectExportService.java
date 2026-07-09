package de.peeeq.wurstio.objectreader;

import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.WLogger;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.app.objMod.*;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.txt.WTS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ObjectExportService {

    private ObjectExportService() {
    }

    public static List<Path> exportObjects(File source, Optional<File> outputDir) throws Exception {
        if (!source.exists()) {
            throw new IOException("Map does not exist: " + source.getAbsolutePath());
        }
        File outDir = outputDir.orElseGet(() -> defaultOutputDir(source));
        Files.createDirectories(outDir.toPath());
        clearStaleExports(outDir.toPath());

        try (ObjectFileSource objectSource = openSource(source)) {
            WTS trigStrings = readTrigStrings(objectSource);
            List<Path> written = new ArrayList<>();
            for (ObjectFileType fileType : ObjectFileType.values()) {
                byte[] main = objectSource.read("war3map." + fileType.getExt()).orElse(null);
                byte[] skin = objectSource.read("war3mapSkin." + fileType.getExt()).orElse(null);
                if (main == null && skin == null) {
                    continue;
                }
                ObjMod<? extends ObjMod.Obj> dataStore = readObjectFile(fileType, main, skin);
                replaceTrigStrings(dataStore, trigStrings);
                if (dataStore.getObjsList().isEmpty()) {
                    continue;
                }

                Path outFile = exportFile(outDir.toPath(), fileType);
                ProgramStateIO.exportToWurstFile(dataStore, fileType, outFile, false);
                written.add(outFile);
                WLogger.info("Exported object file " + fileType.getExt() + " to " + outFile.toAbsolutePath());
            }
            return written;
        }
    }

    private static void clearStaleExports(Path outDir) throws IOException {
        for (ObjectFileType fileType : ObjectFileType.values()) {
            Files.deleteIfExists(exportFile(outDir, fileType));
        }
    }

    private static File defaultOutputDir(File source) {
        File parent = source.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }
        return new File(parent, "objectEditingOutput");
    }

    private static Path exportFile(Path outDir, ObjectFileType fileType) {
        return outDir.resolve("WurstExportedObjects_" + fileType.getExt() + ".wurst");
    }

    private static ObjectFileSource openSource(File source) throws Exception {
        if (source.isDirectory()) {
            return new FolderObjectFileSource(source.toPath());
        }
        if (!source.isFile()) {
            throw new IOException("Map source must be a file or folder: " + source.getAbsolutePath());
        }
        return new MpqObjectFileSource(source);
    }

    private static WTS readTrigStrings(ObjectFileSource source) {
        try {
            Optional<byte[]> data = source.read("war3map.wts");
            if (data.isPresent()) {
                return new WTS(new ByteArrayInputStream(data.get()));
            }
        } catch (Exception e) {
            WLogger.warning("Could not load trigger strings: " + e.getMessage());
        }
        return new WTS();
    }

    private static ObjMod<? extends ObjMod.Obj> readObjectFile(ObjectFileType fileType, byte[] main, byte[] skin) throws Exception {
        switch (fileType) {
            case UNITS: {
                W3U data = main != null ? readW3U(main) : new W3U();
                if (skin != null) {
                    data.merge(readW3U(skin));
                }
                return data;
            }
            case ITEMS: {
                W3T data = main != null ? readW3T(main) : new W3T();
                if (skin != null) {
                    data.merge(readW3T(skin));
                }
                return data;
            }
            case DESTRUCTABLES: {
                W3B data = main != null ? readW3B(main) : new W3B();
                if (skin != null) {
                    data.merge(readW3B(skin));
                }
                return data;
            }
            case DOODADS: {
                W3D data = main != null ? readW3D(main) : new W3D();
                if (skin != null) {
                    data.merge(readW3D(skin));
                }
                return data;
            }
            case ABILITIES: {
                W3A data = main != null ? readW3A(main) : new W3A();
                if (skin != null) {
                    data.merge(readW3A(skin));
                }
                return data;
            }
            case BUFFS: {
                W3H data = main != null ? readW3H(main) : new W3H();
                if (skin != null) {
                    data.merge(readW3H(skin));
                }
                return data;
            }
            case UPGRADES: {
                W3Q data = main != null ? readW3Q(main) : new W3Q();
                if (skin != null) {
                    data.merge(readW3Q(skin));
                }
                return data;
            }
        }
        throw new IOException("Unsupported object file type: " + fileType);
    }

    private static W3U readW3U(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3U(in);
        }
    }

    private static W3T readW3T(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3T(in);
        }
    }

    private static W3B readW3B(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3B(in);
        }
    }

    private static W3D readW3D(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3D(in);
        }
    }

    private static W3A readW3A(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3A(in);
        }
    }

    private static W3H readW3H(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3H(in);
        }
    }

    private static W3Q readW3Q(byte[] data) throws IOException, InterruptedException {
        try (Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(data))) {
            return new W3Q(in);
        }
    }

    private static void replaceTrigStrings(ObjMod<? extends ObjMod.Obj> dataStore, WTS trigStrings) {
        replaceTrigStrings(dataStore.getOrigObjs(), trigStrings);
        replaceTrigStrings(dataStore.getCustomObjs(), trigStrings);
    }

    private static void replaceTrigStrings(List<? extends ObjMod.Obj> objects, WTS trigStrings) {
        for (ObjMod.Obj obj : objects) {
            for (ObjMod.Obj.Mod mod : obj.getMods()) {
                if (mod.getValType() == ObjMod.ValType.STRING && mod.getVal() instanceof War3String) {
                    War3String stringVal = (War3String) mod.getVal();
                    if (stringVal.getVal().startsWith("TRIGSTR_")) {
                        String replacement = resolveTrigString(trigStrings, stringVal.getVal());
                        if (replacement != null) {
                            stringVal.set_val(replacement);
                        }
                    }
                }
            }
        }
    }

    private static String resolveTrigString(WTS trigStrings, String value) {
        try {
            int id = Integer.parseInt(value.substring("TRIGSTR_".length()), 10);
            return trigStrings.getEntry(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private interface ObjectFileSource extends AutoCloseable {
        Optional<byte[]> read(String name) throws Exception;

        @Override
        default void close() throws Exception {
        }
    }

    private static final class FolderObjectFileSource implements ObjectFileSource {
        private final Path root;

        private FolderObjectFileSource(Path root) {
            this.root = root;
        }

        @Override
        public Optional<byte[]> read(String name) throws IOException {
            Path file = root.resolve(name);
            if (!Files.exists(file)) {
                return Optional.empty();
            }
            return Optional.of(Files.readAllBytes(file));
        }
    }

    private static final class MpqObjectFileSource implements ObjectFileSource {
        private final MpqEditor mpqEditor;

        private MpqObjectFileSource(File source) throws Exception {
            this.mpqEditor = MpqEditorFactory.getEditor(Optional.of(source), true);
        }

        @Override
        public Optional<byte[]> read(String name) throws Exception {
            if (!mpqEditor.hasFile(name)) {
                return Optional.empty();
            }
            return Optional.of(mpqEditor.extractFile(name));
        }

        @Override
        public void close() throws IOException {
            mpqEditor.close();
        }
    }
}
