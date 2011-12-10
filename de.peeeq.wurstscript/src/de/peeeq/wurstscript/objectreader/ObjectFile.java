package de.peeeq.wurstscript.objectreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.peeeq.wurstscript.WLogger;

public class ObjectFile {

	private ObjectFileType fileType;
	private ObjectTable origTable;
	private ObjectTable modifiedTable;

	public ObjectFileType getFileType() {
		return fileType;
	}

	public ObjectTable getOrigTable() {
		return origTable;
	}

	public ObjectTable getModifiedTable() {
		return modifiedTable;
	}

	public ObjectFile(File file, ObjectFileType fileType) {
		this.fileType = fileType;
		FileInputStream fis = null;
		try {
			BinaryDataInputStream in = new BinaryDataInputStream(file, true);

			int version = in.readInt();

			this.origTable = ObjectTable.readFromStream(in, fileType);
			this.modifiedTable = ObjectTable.readFromStream(in, fileType);

		} catch (FileNotFoundException e) {
			WLogger.severe(e);
			throw new Error(e);
		} catch (IOException e) {
			WLogger.severe(e);
			throw new Error(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new Error(e);
				}
			}
		}

	}

	


	
}
