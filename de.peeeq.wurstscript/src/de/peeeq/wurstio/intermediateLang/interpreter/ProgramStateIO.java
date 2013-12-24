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

import de.peeeq.wurstio.mpq.LadikMpq;
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
	private WurstGui gui;
	private File mapFile;
	private Map<ObjectFileType, ObjectFile> dataStoreMap = Maps.newLinkedHashMap();
	private int id = 0;
	private Map<String, ObjectDefinition> objDefinitions = Maps.newLinkedHashMap();
	private PrintStream outStream = System.out;
	private boolean injectObjectFilesIntoMap = false;
	private Map<Integer, String> trigStrings = null;

	public ProgramStateIO(File mapFile, WurstGui gui) {
		super(mapFile, gui);
		this.gui = gui;
		this.mapFile = mapFile;
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
		return gui;
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
			LadikMpq editor = MpqEditorFactory.getEditor();
			File wts = editor.extractFile(mapFile, "war3map.wts");
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
		if (mapFile == null) {
			throw new Error("Mapfile not set.");
		}

		try {
			// extract specific object file:
			try {
				LadikMpq editor = MpqEditorFactory.getEditor();
				File w3_ = editor.extractFile(mapFile, "war3map."+filetype.getExt());
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
	public void writeBack() {
		gui.sendProgress("Writing back generated objects", 0.9);

		for (ObjectFileType fileType : ObjectFileType.values()) {
			ObjectFile dataStore = getDataStore(fileType);
			if (!dataStore.isEmpty()) {
				writebackObjectFile(dataStore, fileType);
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

	private void writebackObjectFile(ObjectFile dataStore, ObjectFileType fileType) throws Error {


		try {
			File folder = getObjectEditingOutputFolder();

			File w3u = new File(folder, "wurstCreatedObjects." + fileType.getExt());
			if (w3u.exists()) {
				w3u.delete();
			}
			dataStore.writeTo(w3u);
			Files.write(dataStore.exportToWurst(fileType),  new File(folder, "WurstExportedObjects_"+fileType.getExt()+".wurst.txt"), Charsets.UTF_8);

			if (injectObjectFilesIntoMap) {
				LadikMpq editor = MpqEditorFactory.getEditor();
				String filenameInMpq = "war3map." + fileType.getExt();
				editor.deleteFile(mapFile, filenameInMpq);
				int tries = 1;
				while (tries < 20) {
					editor.insertFile(mapFile, filenameInMpq, w3u);

					File extr;
					try {
						extr = editor.extractFile(mapFile, filenameInMpq);
					} catch (Error e) {
						extr = null;
					}
					if (extr != null && extr.exists()) {
						break;
					}
					System.gc();
					tries++;
				}
				if (tries >= 20) {
					JOptionPane.showMessageDialog(null, "Could not insert " + fileType.getExt());
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

