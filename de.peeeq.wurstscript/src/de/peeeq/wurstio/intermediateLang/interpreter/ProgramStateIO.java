package de.peeeq.wurstio.intermediateLang.interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.objectreader.BinaryDataOutputStream;
import de.peeeq.wurstio.objectreader.ObjectDefinition;
import de.peeeq.wurstio.objectreader.ObjectFile;
import de.peeeq.wurstio.objectreader.ObjectFileType;
import de.peeeq.wurstio.objectreader.ObjectModification;
import de.peeeq.wurstio.objectreader.ObjectModificationInt;
import de.peeeq.wurstio.objectreader.ObjectModificationString;
import de.peeeq.wurstio.objectreader.ObjectTable;
import de.peeeq.wurstio.objectreader.WTSFile;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImStmt;

public class ProgramStateIO extends ProgramState {

	public static final int GENERATED_BY_WURST = 42;
	private ImStmt lastStatement;
	private final MpqEditor mpqEditor;
	private final Map<ObjectFileType, ObjectFile> dataStoreMap = Maps.newLinkedHashMap();
	private int id = 0;
	private final Map<String, ObjectDefinition> objDefinitions = Maps.newLinkedHashMap();
	private PrintStream outStream = System.out;
	private Map<Integer, String> trigStrings = null;
	private final File mapFile;

	public ProgramStateIO(File mapFile, MpqEditor mpqEditor, WurstGui gui) {
		super(gui);
		this.mapFile = mapFile;
		this.mpqEditor = mpqEditor;
	}

	@Override
	public void setLastStatement(ImStmt s) {
		lastStatement = s;		
	}

	@Override
	public ImStmt getLastStatement() {
		return lastStatement;
	}

	@Override
	public WurstGui getGui() {
		return super.getGui();
	}

	public String getTrigString(int id) {
		loadTrigStrings();
		String r = trigStrings.get(id);
		return r == null ? "" : r;
	}

	private void loadTrigStrings() {
		if (trigStrings != null) {
			return;
		}
		try {
			byte[] wts = mpqEditor.extractFile("war3map.wts");
			trigStrings = WTSFile.parse(wts);
		} catch (Exception e) {
			// dummy result
			trigStrings = Maps.newLinkedHashMap();
			WLogger.warning("Could not load trigger strings");
			WLogger.info(e);
		}
	}

	public ObjectFile getDataStore(String fileExtension) {
		return getDataStore(ObjectFileType.fromExt(fileExtension));
	}

	private ObjectFile getDataStore(ObjectFileType filetype) throws Error {
		ObjectFile dataStore = dataStoreMap.get(filetype);
		if (dataStore != null) {
			return dataStore;
		}
		if (mpqEditor == null) {
			throw new Error("Mapfile not set.");
		}

		try {
			// extract specific object file:
			try {
				byte[] w3_ = mpqEditor.extractFile("war3map."+filetype.getExt());
				dataStore = new ObjectFile(w3_, filetype);
				replaceTrigStrings(dataStore);
			} catch (IOException | InterruptedException e) {
				// TODO maybe tell the user, that something has gone wrong
				WLogger.info("Could not extract file war3map."+filetype.getExt());
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
					WLogger.info("mod: " + mod);
					if (modS.getData().startsWith("TRIGSTR_")) {
						try {
							int id = Integer.parseInt(modS.getData().substring("TRIGSTR_".length()), 10);
							String newVal = getTrigString(id);
							WLogger.info("replace id: " + id + " with '" + newVal + "'");
							modS.setData(newVal);
						} catch (NumberFormatException e) {
							// ignore
							WLogger.info("number format in trigstr: " + modS.getData());
						}
					}
				}
			}
		}
	}

	private void deleteWurstObjects(ObjectFile unitStore) {
		WLogger.info("deleteWurstObjects start " + unitStore.getFileType() + "\n\n" + unitStore);
		Iterator<ObjectDefinition> it = unitStore.getModifiedTable().getObjectDefinitions().iterator();
		while (it.hasNext()) {
			ObjectDefinition od = it.next();
			for (ObjectModification<?> om : od.getModifications()) {
				if (om.getModificationId().equals("wurs") && om instanceof ObjectModificationInt) {
					ObjectModificationInt om2 = (ObjectModificationInt) om;
					if (om2.getData() == GENERATED_BY_WURST) {
						WLogger.info( "Removing " + od + " from " + unitStore.getFileType());
						it.remove();
						break;
					}
				}
			}
		}
		WLogger.info("deleteWurstObjects end " + unitStore.getFileType() + "\n\n" + unitStore);
	}



	public String addObjectDefinition(ObjectDefinition objDef) {
		id++;
		String key = "obj"+id;
		objDefinitions.put(key, objDef);
		return key;
	}

	public ObjectDefinition getObjectDefinition(String key) {
		return objDefinitions.get(key);
	}

	@Override
	public void writeBack(boolean inject) {
		gui.sendProgress("Writing back generated objects", 0.9);

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
		File objFile = new File(getObjectEditingOutputFolder(), "wurstCreatedObjects.w3o");
		try (BinaryDataOutputStream objFileStream = new BinaryDataOutputStream(objFile, true)) {
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
		} catch (FileNotFoundException e) {
			WLogger.severe(e);
		} catch (IOException e) {
			WLogger.severe(e);
		} catch (Error e) {
			WLogger.severe(e);
		}
	}

	private void writebackObjectFile(ObjectFile dataStore, ObjectFileType fileType, boolean inject) throws Error {


		try {
			File folder = getObjectEditingOutputFolder();

			byte[] w3u = dataStore.writeToByteArray();
			
			// wurst exported objects
			Files.write(dataStore.exportToWurst(fileType),  new File(folder, "WurstExportedObjects_"+fileType.getExt()+".wurst.txt"), Charsets.UTF_8);

			if (inject) {
				String filenameInMpq = "war3map." + fileType.getExt();
				
				File mapFileTemp = new File("inject_tempmap.w3x");
				
				try {
					mpqEditor.deleteFile(filenameInMpq);
				} catch (Throwable e) {
					WLogger.info(e);
				}
				mpqEditor.insertFile(filenameInMpq, w3u);

				
				byte[] extr;
				try {
					extr = mpqEditor.extractFile(filenameInMpq);
				} catch (Throwable e) {
					WLogger.info(e);
					extr = null;
				}
				if (extr == null) {
					JOptionPane.showMessageDialog(null, "Could not inject " + filenameInMpq + ".");
				}
				
				
				
				
			}
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		} catch (Exception e) {
			WLogger.severe(e);
			throw new Error(e);
		}

	}

	private File getObjectEditingOutputFolder() {
		File folder = new File(mapFile.getParent(), "objectEditingOutput");
		folder.mkdirs();
		return folder;
	}

	@Override
	public PrintStream getOutStream() {
		return outStream ;
	}

	@Override
	public void setOutStream(PrintStream os) {
		outStream = os;
	}




}

