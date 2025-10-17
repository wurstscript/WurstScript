package de.peeeq.wurstio.intermediateLang.interpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.objectreader.ObjectFileType;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.utils.Utils;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.Wc3BinInputStream;
import net.moonlightflower.wc3libs.bin.Wc3BinOutputStream;
import net.moonlightflower.wc3libs.bin.app.objMod.*;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.txt.WTS;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramStateIO extends ProgramState {

    private static final int GENERATED_BY_WURST = 42;
    private static final String OBJECT_CACHE_MANIFEST = "wurst_object_cache.txt";

    private @Nullable ImStmt lastStatement;
    private @Nullable final MpqEditor mpqEditor;
    private final Map<ObjectFileType, ObjMod<? extends ObjMod.Obj>> dataStoreMap = Maps.newLinkedHashMap();
    private final Map<ObjectFileType, String> dataStoreHashes = Maps.newLinkedHashMap();
    private int id = 0;
    private final Map<String, ObjMod.Obj> objDefinitions = Maps.newLinkedHashMap();
    private PrintStream outStream = System.err;
    private @Nullable WTS trigStrings = null;
    private final Optional<File> mapFile;

    /**
     * Tracks which object files have been modified during compiletime
     */
    private static class ObjectCacheManifest {
        Map<String, ObjectFileEntry> objectFiles = new HashMap<>();

        static class ObjectFileEntry {
            String fileType;
            String hash;
            long timestamp;
            int objectCount;

            ObjectFileEntry(String fileType, String hash, long timestamp, int objectCount) {
                this.fileType = fileType;
                this.hash = hash;
                this.timestamp = timestamp;
                this.objectCount = objectCount;
            }
        }

        String serialize() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Wurst Object Cache Manifest v1\n");
            sb.append("# Format: fileType|hash|timestamp|objectCount\n");

            for (Map.Entry<String, ObjectFileEntry> entry : objectFiles.entrySet()) {
                ObjectFileEntry e = entry.getValue();
                sb.append(e.fileType)
                    .append("|")
                    .append(e.hash)
                    .append("|")
                    .append(e.timestamp)
                    .append("|")
                    .append(e.objectCount)
                    .append("\n");
            }
            return sb.toString();
        }

        static ObjectCacheManifest deserialize(String data) {
            ObjectCacheManifest manifest = new ObjectCacheManifest();
            if (data == null || data.isEmpty()) {
                return manifest;
            }

            String[] lines = data.split("\n");
            for (String line : lines) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    try {
                        String fileType = parts[0];
                        String hash = parts[1];
                        long timestamp = Long.parseLong(parts[2]);
                        int objectCount = Integer.parseInt(parts[3]);

                        manifest.objectFiles.put(fileType,
                            new ObjectFileEntry(fileType, hash, timestamp, objectCount));
                    } catch (NumberFormatException e) {
                        WLogger.warning("Invalid object cache manifest line: " + line);
                    }
                }
            }
            return manifest;
        }

        boolean hasEntry(String fileType) {
            return objectFiles.containsKey(fileType);
        }

        boolean hashMatches(String fileType, String hash) {
            ObjectFileEntry entry = objectFiles.get(fileType);
            return entry != null && entry.hash.equals(hash);
        }

        void putEntry(String fileType, String hash, int objectCount) {
            objectFiles.put(fileType, new ObjectFileEntry(fileType, hash, System.currentTimeMillis(), objectCount));
        }
    }

    public ProgramStateIO(Optional<File> mapFile, @Nullable MpqEditor mpqEditor, WurstGui gui, ImProg prog, boolean isCompiletime) {
        super(gui, prog, isCompiletime);
        this.mapFile = mapFile;
        this.mpqEditor = mpqEditor;
    }

    @Override
    public void setLastStatement(ImStmt s) {
        lastStatement = s;
    }

    @Override
    public @Nullable ImStmt getLastStatement() {
        return lastStatement;
    }

    @Override
    public WurstGui getGui() {
        return super.getGui();
    }

    private String getTrigString(int id) {
        return loadTrigStrings().getEntry(id);
    }

    private WTS loadTrigStrings() {
        WTS res = trigStrings;
        if (res != null) {
            return res;
        }
        try {
            byte[] wts = mpqEditor.extractFile("war3map.wts");
            res = new WTS(new ByteArrayInputStream(wts));
        } catch (Exception e) {
            // dummy result
            res = new WTS();
            WLogger.warning("Could not load trigger strings");
            WLogger.info(e);
        }
        trigStrings = res;
        return res;
    }

    ObjMod<? extends ObjMod.Obj> getDataStore(String fileExtension) {
        return getDataStore(ObjectFileType.fromExt(fileExtension));
    }

    private ObjMod<? extends ObjMod.Obj> getDataStore(ObjectFileType filetype) throws Error {
        ObjMod<? extends ObjMod.Obj> dataStore = dataStoreMap.get(filetype);
        if (dataStore != null) {
            return dataStore;
        }
        if (mpqEditor == null) {
            // without a map: create empty object file
            dataStore = filetypeToObjmod(filetype);
            dataStore.setFormat(ObjMod.EncodingFormat.OBJ_0x2);
            dataStoreMap.put(filetype, dataStore);
            return dataStore;
        }

        try {
            // extract specific object file:
            String fileName = "war3map." + filetype.getExt();
            String skinFileName = "war3mapSkin." + filetype.getExt();
            try {
                if (mpqEditor.hasFile(fileName)) {
                    byte[] w3_ = mpqEditor.extractFile(fileName);
                    Wc3BinInputStream in = new Wc3BinInputStream(new ByteArrayInputStream(w3_));
                    switch (filetype) {
                        case UNITS:
                            W3U w3u = new W3U(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3U skin = new W3U(inS);
                                w3u.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3u;
                            break;
                        case ITEMS:
                            W3T w3t = new W3T(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3T skin = new W3T(inS);
                                w3t.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3t;
                            break;
                        case DESTRUCTABLES:
                            W3B w3b = new W3B(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3B skin = new W3B(inS);
                                w3b.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3b;
                            break;
                        case DOODADS:
                            W3D w3d = new W3D(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3D skin = new W3D(inS);
                                w3d.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3d;
                            break;
                        case ABILITIES:
                            W3A w3a = new W3A(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3A skin = new W3A(inS);
                                w3a.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3a;
                            break;
                        case BUFFS:
                            W3H w3h = new W3H(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3H skin = new W3H(inS);
                                w3h.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3h;
                            break;
                        case UPGRADES:
                            W3Q w3q = new W3Q(in);
                            if (mpqEditor.hasFile(skinFileName)) {
                                byte[] w3s_ = mpqEditor.extractFile(skinFileName);
                                Wc3BinInputStream inS = new Wc3BinInputStream(new ByteArrayInputStream(w3s_));
                                W3Q skin = new W3Q(inS);
                                w3q.merge(skin);
                                mpqEditor.deleteFile(skinFileName);
                            }
                            dataStore = w3q;
                            break;
                    }

                    in.close();
                    replaceTrigStrings(dataStore);

                } else {
                    dataStore = filetypeToObjmod(filetype);
                    dataStore.setFormat(ObjMod.EncodingFormat.OBJ_0x2);
                }
            } catch (IOException | InterruptedException e) {
                // TODO maybe tell the user, that something has gone wrong
                WLogger.info("Could not extract file: " + fileName);
                WLogger.info(e);
                dataStore = filetypeToObjmod(filetype);
                dataStore.setFormat(ObjMod.EncodingFormat.OBJ_0x2);
            }
            dataStoreMap.put(filetype, dataStore);

            // clean datastore, remove all objects created by wurst
            deleteWurstObjects(dataStore);
        } catch (Exception e) {
            WLogger.severe(e);
            throw new Error(e);
        }

        return dataStore;
    }

    @NotNull
    private ObjMod<? extends ObjMod.Obj> filetypeToObjmod(ObjectFileType filetype) {
        switch (filetype) {
            case UNITS:
                return new W3U();
            case ITEMS:
                return new W3T();
            case DESTRUCTABLES:
                return new W3B();
            case DOODADS:
                return new W3D();
            case ABILITIES:
                return new W3A();
            case BUFFS:
                return new W3H();
            case UPGRADES:
                return new W3Q();
        }
        return null;
    }

    private void replaceTrigStrings(ObjMod<? extends ObjMod.Obj> dataStore) {
        replaceTrigStringsInTable(dataStore.getOrigObjs());
        replaceTrigStringsInTable(dataStore.getCustomObjs());
    }

    private void replaceTrigStringsInTable(List<? extends ObjMod.Obj> modifiedTable) {
        for (ObjMod.Obj od : modifiedTable) {
            for (ObjMod.Obj.Mod mod : od.getMods()) {
                if (mod.getValType() == ObjMod.ValType.STRING && mod.getVal() instanceof War3String) {
                    War3String stringVal = (War3String) mod.getVal();
                    if (stringVal.getVal().startsWith("TRIGSTR_")) {
                        try {
                            int id = Integer.parseInt(stringVal.getVal().substring("TRIGSTR_".length()), 10);
                            String newVal = getTrigString(id);
                            stringVal.set_val(newVal);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    private void deleteWurstObjects(ObjMod<? extends ObjMod.Obj> dataStore) {
        for (ObjMod.Obj next : dataStore.getCustomObjs()) {
            for (ObjMod.Obj.Mod om : next.getMods()) {
                if (om.getId().getVal().equals("wurs") && om.getVal() instanceof War3Int) {
                    War3Int val = (War3Int) om.getVal();
                    if (val.getVal() == GENERATED_BY_WURST) {
                        dataStore.removeObj(next.getId());
                        break;
                    }
                }
            }
        }
    }

    String addObjectDefinition(ObjMod.Obj objDef) {
        id++;
        String key = "obj" + id;
        objDefinitions.put(key, objDef);
        return key;
    }

    ObjMod.Obj getObjectDefinition(String key) {
        return objDefinitions.get(key);
    }

    /**
     * Calculate hash of an object file's contents
     */
    private String calculateObjectFileHash(ObjMod<? extends ObjMod.Obj> dataStore) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Wc3BinOutputStream out = new Wc3BinOutputStream(baos);
            dataStore.write(out, ObjMod.EncodingFormat.OBJ_0x2);
            out.close();
            byte[] data = baos.toByteArray();
            return ImportFile.calculateHash(data);
        } catch (Exception e) {
            WLogger.warning("Could not calculate object file hash: " + e.getMessage());
            return String.valueOf(System.currentTimeMillis());
        }
    }

    /**
     * Load the object cache manifest from MPQ
     */
    private ObjectCacheManifest loadObjectCacheManifest() {
        if (mpqEditor == null) {
            return new ObjectCacheManifest();
        }

        try {
            if (mpqEditor.hasFile(OBJECT_CACHE_MANIFEST)) {
                byte[] data = mpqEditor.extractFile(OBJECT_CACHE_MANIFEST);
                String content = new String(data, StandardCharsets.UTF_8);
                WLogger.info("Loaded object cache manifest from MPQ");
                return ObjectCacheManifest.deserialize(content);
            }
        } catch (Exception e) {
            WLogger.info("Could not load object cache manifest: " + e.getMessage());
        }
        return new ObjectCacheManifest();
    }

    /**
     * Save the object cache manifest to MPQ
     */
    private void saveObjectCacheManifest(ObjectCacheManifest manifest) {
        if (mpqEditor == null) {
            return;
        }

        try {
            String serialized = manifest.serialize();
            byte[] data = serialized.getBytes(StandardCharsets.UTF_8);
            mpqEditor.deleteFile(OBJECT_CACHE_MANIFEST);
            mpqEditor.insertFile(OBJECT_CACHE_MANIFEST, data);
            WLogger.info("Saved object cache manifest to MPQ");
        } catch (Exception e) {
            WLogger.warning("Could not save object cache manifest: " + e.getMessage());
        }
    }

    @Override
    public void writeBack(boolean inject) {
        gui.sendProgress("Writing back generated objects");
        long startTime = System.currentTimeMillis();

        ObjectCacheManifest oldManifest = loadObjectCacheManifest();
        ObjectCacheManifest newManifest = new ObjectCacheManifest();

        int filesProcessed = 0, filesUpdated = 0, filesSkipped = 0;
        boolean anyFileWritten = false;

        for (ObjectFileType fileType : ObjectFileType.values()) {
            filesProcessed++;
            ObjMod<? extends ObjMod.Obj> dataStore = getDataStore(fileType);

            if (dataStore.getObjsList().isEmpty()) {
                WLogger.info("Object file " + fileType.getExt() + " is empty, skipping");
                continue;
            }

            // Compute hash of what we intend to write
            String currentHash = calculateObjectFileHash(dataStore);
            dataStoreHashes.put(fileType, currentHash);

            boolean mpqHasSame = false;
            if (mpqEditor != null && mpqEditor.hasFile("war3map." + fileType.getExt())) {
                try {
                    byte[] existing = mpqEditor.extractFile("war3map." + fileType.getExt());
                    String existingHash = ImportFile.calculateHash(existing);
                    mpqHasSame = existingHash.equals(currentHash);
                } catch (Exception e) {
                    WLogger.info("Could not validate MPQ content for " + fileType.getExt() + ": " + e.getMessage());
                }
            }

            boolean manifestSaysSame = oldManifest.hasEntry(fileType.getExt())
                && oldManifest.hashMatches(fileType.getExt(), currentHash);

            // Only skip if BOTH the manifest and the actual MPQ content match
            if (manifestSaysSame && mpqHasSame) {
                WLogger.info("Object file " + fileType.getExt() + " unchanged (hash match), skipping writeback");
                filesSkipped++;
                if (inject) {
                    newManifest.putEntry(fileType.getExt(), currentHash, dataStore.getObjsList().size());
                }
                continue;
            }

            WLogger.info("Object file " + fileType.getExt() + " changed or MPQ out of sync, writing back");
            filesUpdated++;
            writebackObjectFile(dataStore, fileType, inject);
            anyFileWritten = anyFileWritten || inject;
            if (inject) {
                newManifest.putEntry(fileType.getExt(), currentHash, dataStore.getObjsList().size());
            }
        }

        // Always write the .w3o (debug aid) – but do NOT touch the manifest for it
//        writeW3oFile();

        // Only persist a new manifest if we actually injected files
        if (inject && anyFileWritten) {
            saveObjectCacheManifest(newManifest);
        } else {
            WLogger.info("Skipping manifest update (inject=" + inject + ", anyFileWritten=" + anyFileWritten + ")");
        }

        long endTime = System.currentTimeMillis();
        WLogger.info(String.format(
            "Object writeback complete in %dms: %d files processed, %d updated, %d skipped",
            endTime - startTime, filesProcessed, filesUpdated, filesSkipped));
    }


    private void writeW3oFile() {
        Optional<File> objFile = getObjectEditingOutputFolder().map(fo -> new File(fo, "wurstCreatedObjects.w3o"));
        try (Wc3BinOutputStream objFileStream = new Wc3BinOutputStream(objFile.get())) {
            objFileStream.writeInt32(1); // version
            for (ObjectFileType fileType : ObjectFileType.values()) {
                ObjMod<? extends ObjMod.Obj> dataStore = getDataStore(fileType);
                if (!dataStore.getObjs().isEmpty()) {
                    objFileStream.writeInt32(1); // exists
                    dataStore.write(objFileStream, ObjMod.EncodingFormat.OBJ_0x2);
                } else {
                    objFileStream.writeInt32(0); // does not exist
                }
            }
        } catch (Error | IOException e) {
            WLogger.severe(e);
        }
    }

    private void writebackObjectFile(ObjMod<? extends ObjMod.Obj> dataStore, ObjectFileType fileType, boolean inject) throws Error {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Wc3BinOutputStream out = new Wc3BinOutputStream(baos);
            Optional<File> folder = getObjectEditingOutputFolder();

            dataStore.write(out, ObjMod.EncodingFormat.OBJ_0x2);

            out.close();
            byte[] w3_ = baos.toByteArray();

            exportToWurst(dataStore, fileType, new File(folder.get(), "WurstExportedObjects_" + fileType.getExt() + ".wurst.txt").toPath());

            if (inject) {
                if (mpqEditor == null) {
                    throw new RuntimeException("Map file must be given with '-injectobjects' option.");
                }
                String filenameInMpq = "war3map." + fileType.getExt();
                mpqEditor.deleteFile(filenameInMpq);
                mpqEditor.insertFile(filenameInMpq, w3_);
                WLogger.info("Injected modified object file: " + filenameInMpq);
            }
        } catch (Exception e) {
            WLogger.severe(e);
            throw new Error(e);
        }
    }

    public void exportToWurst(ObjMod<? extends ObjMod.Obj> dataStore,
                              ObjectFileType fileType, Path outFile) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            out.write("package WurstExportedObjects_" + fileType.getExt() + "\n");
            out.write("import ObjEditingNatives\n\n");

            out.write("// Modified Table (contains all custom objects)\n\n");
            exportToWurst(dataStore.getCustomObjs(), fileType, out);

            out.write("// Original Table (contains all modified default objects)\n" +
                "// Wurst does not support modifying default objects\n" +
                "// but you can copy these functions and replace 'xxxx' with a new, custom id.\n\n");
            exportToWurst(dataStore.getOrigObjs(), fileType, out);
        }
    }

    public void exportToWurst(List<? extends ObjMod.Obj> customObjs, ObjectFileType fileType, Appendable out) throws IOException {
        for (ObjMod.Obj obj : customObjs) {
            String oldId = obj.getBaseId().getVal();
            String newId = (obj.getNewId() != null ? obj.getNewId().getVal() : "xxxx");
            out.append("@compiletime function create_").append(fileType.getExt()).append("_").append(newId)
                .append("()\n");
            out.append("\tlet def = createObjectDefinition(\"").append(fileType.getExt()).append("\", '");
            out.append(newId);
            out.append("', '");
            out.append(oldId);
            out.append("')\n");
            for (ObjMod.Obj.Mod m : obj.getMods()) {
                if (fileType.usesLevels() && m instanceof ObjMod.Obj.ExtendedMod) {
                    ObjMod.Obj.ExtendedMod extendedMod = (ObjMod.Obj.ExtendedMod) m;
                    out.append("\t..setLvlData").append(valTypeToFuncPostfix(extendedMod.getValType())).append("(\"");
                    out.append(m.toString());
                    out.append("\", ")
                        .append(String.valueOf(extendedMod.getLevel()))
                        .append(", ")
                        .append(String.valueOf(extendedMod.getDataPt())).append(", ")
                        .append((extendedMod.getValType() == ObjMod.ValType.STRING) ?
                            Utils.escapeString(extendedMod.getVal().toString()) :
                            extendedMod.getVal().toString())
                        .append(")\n");
                } else {
                    out.append("\t..set").append(valTypeToFuncPostfix(m.getValType())).append("(\"");
                    out.append(m.toString());
                    out.append("\", ").append((m.getValType() == ObjMod.ValType.STRING) ?
                        Utils.escapeString(m.getVal().toString()) :
                        m.getVal().toString()).append(")\n");
                }
            }
            out.append("\n\n");
        }
    }

    private String valTypeToFuncPostfix(ObjMod.ValType valType) {
        switch (valType) {
            case INT:
                return "Int";
            case REAL:
                return "Real";
            case UNREAL:
                return "Unreal";
            case STRING:
                return "String";
        }
        return "Int";
    }

    private Optional<File> getObjectEditingOutputFolder() {
        if (!mapFile.isPresent()) {
            File folder = new File("_build", "objectEditingOutput");
            folder.mkdirs();
            return Optional.of(folder);
        }
        Optional<File> folder = mapFile.map(fi -> new File(fi.getParent(), "objectEditingOutput"));
        if (!folder.get().exists() && !folder.get().mkdirs()) {
            WLogger.info("Could not create folder " + folder.map(File::getAbsoluteFile));
            return Optional.empty();
        }
        return folder;
    }

    @Override
    public PrintStream getOutStream() {
        return outStream;
    }

    @Override
    public void setOutStream(PrintStream os) {
        outStream = os;
    }
}
