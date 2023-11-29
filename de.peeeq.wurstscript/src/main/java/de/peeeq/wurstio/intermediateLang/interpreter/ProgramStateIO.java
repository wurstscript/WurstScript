package de.peeeq.wurstio.intermediateLang.interpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.objectreader.ObjectFileType;
import de.peeeq.wurstio.utils.FileUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProgramStateIO extends ProgramState {

    private static final int GENERATED_BY_WURST = 42;
    private @Nullable ImStmt lastStatement;
    private @Nullable
    final MpqEditor mpqEditor;
    private final Map<ObjectFileType, ObjMod<? extends ObjMod.Obj>> dataStoreMap = Maps.newLinkedHashMap();
    private int id = 0;
    private final Map<String, ObjMod.Obj> objDefinitions = Maps.newLinkedHashMap();
    private PrintStream outStream = System.err;
    private @Nullable WTS trigStrings = null;
    private final Optional<File> mapFile;

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

    @Override
    public void writeBack(boolean inject) {
        gui.sendProgress("Writing back generated objects");

        for (ObjectFileType fileType : ObjectFileType.values()) {
            WLogger.info("Writing back " + fileType);
            ObjMod<? extends ObjMod.Obj> dataStore = getDataStore(fileType);
            if (!dataStore.getObjsList().isEmpty()) {
                WLogger.info("Writing back filetype " + fileType);
                writebackObjectFile(dataStore, fileType, inject);
            } else {
                WLogger.info("Writing back empty for " + fileType);
            }
        }
        writeW3oFile();
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

            // TODO  wurst exported objects
            FileUtils.write(
                exportToWurst(dataStore, fileType),
                new File(folder.get(), "WurstExportedObjects_" + fileType.getExt() + ".wurst.txt"));

            if (inject) {
                if (mpqEditor == null) {
                    throw new RuntimeException("Map file must be given with '-injectobjects' option.");
                }
                String filenameInMpq = "war3map." + fileType.getExt();
                mpqEditor.deleteFile(filenameInMpq);
                mpqEditor.insertFile(filenameInMpq, w3_);
            }
        } catch (Exception e) {
            WLogger.severe(e);
            throw new Error(e);
        }

    }

    public String exportToWurst(ObjMod<? extends ObjMod.Obj> dataStore, ObjectFileType fileType) throws IOException {

        Appendable out = new StringBuilder();
        out.append("package WurstExportedObjects_").append(fileType.getExt()).append("\n");
        out.append("import ObjEditingNatives\n\n");

        out.append("// Modified Table (contains all custom objects)\n\n");
        exportToWurst(dataStore.getCustomObjs(), fileType, out);

        out.append("// Original Table (contains all modified default objects)\n" +
            "// Wurst does not support modifying default objects\n" +
            "// but you can copy these functions and replace 'xxxx' with a new, custom id.\n\n");
        exportToWurst(dataStore.getOrigObjs(), fileType, out);

        return out.toString();
    }

    public void exportToWurst(List<? extends ObjMod.Obj> customObjs, ObjectFileType fileType, Appendable out) throws IOException {
        for (ObjMod.Obj obj : customObjs) {
            String oldId = obj.getBaseId().getVal();
            String newId = (obj.getNewId() != null ? obj.getNewId().getVal() : "xxxx");
            out.append("@compiletime function create_").append(fileType.getExt()).append("_").append(newId)
                .append("()\n");
            out.append("	let def = createObjectDefinition(\"").append(fileType.getExt()).append("\", '");
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

