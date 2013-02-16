package de.peeeq.wurstio.intermediateLang.interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.parser.WPos;

public class ProgramStateIO extends ProgramState {

	public static final int GENERATED_BY_WURST = 42;
	private ImStmt lastStatement;
	private WurstGui gui;
	private File mapFile;
	private Map<ObjectFileType, ObjectFile> dataStoreMap = Maps.newHashMap();
	private int id = 0;
	private Map<String, ObjectDefinition> objDefinitions = Maps.newHashMap();
	private PrintStream outStream = System.out;
	private boolean injectObjectFilesIntoMap = false;

	public ProgramStateIO(File mapFile, WurstGui gui) {
		super(mapFile, gui);
		this.gui = gui;
		this.mapFile = mapFile;
	}

	public void setLastStatement(ImStmt s) {
		lastStatement = s;		
	}

	public ImStmt getLastStatement() {
		return lastStatement;
	}

	public WurstGui getGui() {
		return gui;
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
			// extract unit file:
			try {
				LadikMpq editor = MpqEditorFactory.getEditor();
				File w3u = editor.extractFile(mapFile, "war3map."+filetype.getExt());
				dataStore = new ObjectFile(w3u, filetype);
			} catch (Error e) {
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
		try {
			File objFile = new File(mapFile.getParentFile(), "wurstCreatedObjects.w3o");
			BinaryDataOutputStream objFileStream = new BinaryDataOutputStream(objFile, true);
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
		File w3u = new File(mapFile.getParent(), "wurstCreatedObjects." + fileType.getExt());
		if (w3u.exists()) {
			w3u.delete();
		}
		dataStore.writeTo(w3u);
		
		
		try {
			Files.write(dataStore.exportToWurst(),  new File(mapFile.getParent(), "wurstExportedObjects_"+fileType.getExt()+".wurst"), Charsets.UTF_8);
		} catch (IOException e1) {
			WLogger.severe(e1);
		}
		if (injectObjectFilesIntoMap) {
			try {
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
			} catch (Exception e) {
				WLogger.severe(e);
				throw new Error(e);
			}
		}
	}

	public PrintStream getOutStream() {
		return outStream ;
	}
	
	public void setOutStream(PrintStream os) {
		outStream = os;
	}

	

	
}

