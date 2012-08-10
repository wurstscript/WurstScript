package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.mpq.LadikMpq;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.objectreader.ObjectDefinition;
import de.peeeq.wurstscript.objectreader.ObjectFile;
import de.peeeq.wurstscript.objectreader.ObjectFileType;
import de.peeeq.wurstscript.objectreader.ObjectModification;
import de.peeeq.wurstscript.objectreader.ObjectModificationInt;
import de.peeeq.wurstscript.objectreader.ObjectModificationString;

public class ProgramState extends State {

	public static final int GENERATED_BY_WURST = 42;
	private ImStmt lastStatement;
	private WurstGui gui;
	private File mapFile;
	private ObjectFile unitStore;
	private int id = 0;
	private Map<String, ObjectDefinition> objDefinitions = Maps.newHashMap();

	public ProgramState(File mapFile, WurstGui gui) {
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

	public ObjectFile getUnitStore() {
		if (unitStore != null) {
			return unitStore;
		}
		if (mapFile == null) {
			throw new Error("Mapfile not set.");
		}
		
		try {
			// extract unit file:
			LadikMpq editor = MpqEditorFactory.getEditor();
			File w3u = editor.extractFile(mapFile, "war3map.w3u");
			unitStore = new ObjectFile(w3u, ObjectFileType.UNITS);
			
			// clean unitstore:
			Iterator<ObjectDefinition> it = unitStore.getModifiedTable().getObjectDefinitions().iterator();
			while (it.hasNext()) {
				ObjectDefinition od = it.next();
				for (ObjectModification om : od.getModifications()) {
//					gui.sendError(new CompileError(lastStatement.attrTrace().attrSource(), 
//							od.getNewObjectId() + " -- " + om.getModificationId() + " : " + om));
					if (om.getModificationId().equals("wurs") && om instanceof ObjectModificationInt) {
						ObjectModificationInt om2 = (ObjectModificationInt) om;
						if (om2.getData() == GENERATED_BY_WURST) {
							it.remove();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			WLogger.severe(e);
			throw new Error(e);
		}
		
		return unitStore;
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
		if (unitStore != null) {
			File w3u = new File("./temp/new_war3map.w3u");
			if (w3u.exists()) {
				w3u.delete();
			}
			unitStore.writeTo(w3u);
			
			try {
				LadikMpq editor = MpqEditorFactory.getEditor();
				editor.deleteFile(mapFile, "war3map.w3u");
				int tries = 1;
				while (tries < 20) {
					editor.insertFile(mapFile, "war3map.w3u", w3u);
					
					File extr;
					try {
						extr = editor.extractFile(mapFile, "war3map.w3u");
					} catch (Error e) {
						extr = null;
					}
					if (extr != null && extr.exists()) {
						break;
					}
					tries++;
				}
				if (tries >= 20) {
					JOptionPane.showMessageDialog(null, "Could not insert w3u.");
				}
			} catch (Exception e) {
				WLogger.severe(e);
				throw new Error(e);
			}
		}
	}

	

	
}

