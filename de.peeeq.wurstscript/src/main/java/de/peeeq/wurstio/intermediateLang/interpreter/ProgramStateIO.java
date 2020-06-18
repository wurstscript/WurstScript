package de.peeeq.wurstio.intermediateLang.interpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.objectreader.*;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmt;
import net.moonlightflower.wc3libs.txt.WTS;
import org.eclipse.jdt.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ProgramStateIO extends ProgramState {

    private static final int GENERATED_BY_WURST = 42;
    private @Nullable ImStmt lastStatement;
    private @Nullable final MpqEditor mpqEditor;
    private final Map<ObjectFileType, ObjectFile> dataStoreMap = Maps.newLinkedHashMap();
    private int id = 0;
    private final Map<String, ObjectDefinition> objDefinitions = Maps.newLinkedHashMap();
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

    ObjectFile getDataStore(String fileExtension) {
        return getDataStore(ObjectFileType.fromExt(fileExtension));
    }

    private ObjectFile getDataStore(ObjectFileType filetype) throws Error {
        ObjectFile dataStore = dataStoreMap.get(filetype);
        if (dataStore != null) {
            return dataStore;
        }
        if (mpqEditor == null) {
            // without a map: create empty object file
            dataStore = new ObjectFile(filetype);
            dataStoreMap.put(filetype, dataStore);
            return dataStore;
        }

        try {
            // extract specific object file:
            String fileName = "war3map." + filetype.getExt();
            try {
                if (mpqEditor.hasFile(fileName)) {
                    byte[] w3_ = mpqEditor.extractFile(fileName);
                    dataStore = new ObjectFile(w3_, filetype);
                    replaceTrigStrings(dataStore);
                } else {
                    dataStore = new ObjectFile(filetype);
                }
            } catch (IOException | InterruptedException e) {
                // TODO maybe tell the user, that something has gone wrong
                WLogger.info("Could not extract file: " + fileName);
                WLogger.info(e);
                dataStore = new ObjectFile(filetype);
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

    private void replaceTrigStrings(ObjectFile dataStore) {
        replaceTrigStringsInTable(dataStore.getOrigTable());
        replaceTrigStringsInTable(dataStore.getModifiedTable());


    }

    private void replaceTrigStringsInTable(ObjectTable modifiedTable) {
        for (ObjectDefinition od : modifiedTable.getObjectDefinitions()) {
            for (ObjectModification<?> mod : od.getModifications()) {
                if (mod instanceof ObjectModificationString) {
                    ObjectModificationString modS = (ObjectModificationString) mod;
                    if (modS.getData().startsWith("TRIGSTR_")) {
                        try {
                            int id = Integer.parseInt(modS.getData().substring("TRIGSTR_".length()), 10);
                            String newVal = getTrigString(id);
                            modS.setData(newVal);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    private void deleteWurstObjects(ObjectFile unitStore) {
        Iterator<ObjectDefinition> it = unitStore.getModifiedTable().getObjectDefinitions().iterator();
        while (it.hasNext()) {
            ObjectDefinition od = it.next();
            for (ObjectModification<?> om : od.getModifications()) {
                if (om.getModificationId().equals("wurs") && om instanceof ObjectModificationInt) {
                    ObjectModificationInt om2 = (ObjectModificationInt) om;
                    if (om2.getData() == GENERATED_BY_WURST) {
                        it.remove();
                        break;
                    }
                }
            }
        }
    }


    String addObjectDefinition(ObjectDefinition objDef) {
        id++;
        String key = "obj" + id;
        objDefinitions.put(key, objDef);
        return key;
    }

    ObjectDefinition getObjectDefinition(String key) {
        return objDefinitions.get(key);
    }

    @Override
    public void writeBack(boolean inject) {
        gui.sendProgress("Writing back generated objects");

        for (ObjectFileType fileType : ObjectFileType.values()) {
            WLogger.info("Writing back " + fileType);
            ObjectFile dataStore = getDataStore(fileType);
            if (!dataStore.isEmpty()) {
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
        try (BinaryDataOutputStream objFileStream = new BinaryDataOutputStream(objFile.get(), true)) {
            objFileStream.writeInt(1); // version
            for (ObjectFileType fileType : ObjectFileType.values()) {
                ObjectFile dataStore = getDataStore(fileType);
                if (!dataStore.isEmpty()) {
                    objFileStream.writeInt(1); // exists
                    dataStore.writeTo(objFileStream);
                } else {
                    objFileStream.writeInt(0); // does not exist
                }
            }
        } catch (Error | IOException e) {
            WLogger.severe(e);
        }
    }

    private void writebackObjectFile(ObjectFile dataStore, ObjectFileType fileType, boolean inject) throws Error {
        try {
            Optional<File> folder = getObjectEditingOutputFolder();

            byte[] w3u = dataStore.writeToByteArray();

            // wurst exported objects
            FileUtils.write(
                dataStore.exportToWurst(fileType),
                new File(folder.get(), "WurstExportedObjects_" + fileType.getExt() + ".wurst.txt"));

            if (inject) {
                if (mpqEditor == null) {
                    throw new RuntimeException("Map file must be given with '-injectobjects' option.");
                }
                String filenameInMpq = "war3map." + fileType.getExt();
                mpqEditor.deleteFile(filenameInMpq);
                mpqEditor.insertFile(filenameInMpq, w3u);
            }
        } catch (Exception e) {
            WLogger.severe(e);
            throw new Error(e);
        }

    }

    private Optional<File> getObjectEditingOutputFolder() {
        if (!mapFile.isPresent()) {
            File folder = new File("_build", "objectEditingOutput");
            folder.mkdirs();
            return Optional.of(folder);
        }
        Optional<File> folder = mapFile.map(fi -> new File(fi.getParent(), "objectEditingOutput"));
        if (!folder.isPresent() || (!folder.get().exists() && !folder.get().mkdirs())) {
            WLogger.info("Could not create folder " + folder.map(fo -> fo.getAbsoluteFile()));
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

