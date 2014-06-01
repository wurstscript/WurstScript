package de.peeeq.wurstio.map.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.google.common.io.Files;

import de.peeeq.jmpq.JmpqEditor;
import de.peeeq.jmpq.JmpqError;
import de.peeeq.wurstscript.WLogger;


public class ImportFile {
	private static final int fileVersion = 1;
	
	
	public static LinkedList<String> extractImportedFiles(JmpqEditor mpq, File directory){
		File temp = null;
		LinkedList<String> failed = new LinkedList<String>();
		try {
			temp = File.createTempFile("temp", "imp");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			mpq.extractFile("war3map.imp", temp);
		} catch (JmpqError e1) {
			JOptionPane.showMessageDialog(null, "No vaild war3map.imp was found, or there are no imports");
		}
		BinFileReader reader = new BinFileReader(temp);
		reader.readInt();
		int fileCount =  reader.readInt();
		for(int i = 1; i <= fileCount; i++){
			byte b = reader.readByte();
			String path = directory.getPath() + "\\";
			String mpqpath = "";
			if(b == 5 || b == 8){
				path += "war3mapImported\\";
				mpqpath += "war3mapImported\\";
			}
			String filename = reader.readString();
			filename = filename.trim();
			mpqpath += filename;
			path += filename;
			File out = new File(path);
			out.getParentFile().mkdirs();
			try {
				mpq.extractFile(mpqpath, out);
			} catch (JmpqError e) {
				out.delete();
				out = new File(directory.getPath() + "\\" + "war3mapImported\\" + filename);
				out.getParentFile().mkdirs();
				try {
					mpq.extractFile("war3mapImported\\" + mpqpath, out);
				} catch (JmpqError e1) {
					failed.add(mpqpath);
				}
			}
		}
		reader.close();
		return failed;
	}
	
	private static LinkedList<File> getFilesOfDirectory(File dir, LinkedList<File> addTo){
		for(File f : dir.listFiles()){
			if(f.isDirectory()){
				getFilesOfDirectory(f, addTo);
			}else{
				addTo.add(f);
			}
		}
		return addTo;
		
	}
	
	public static void insertImportedFiles(JmpqEditor mpq, File directory) throws JmpqError{
		LinkedList<File> files = new LinkedList<File>();
		getFilesOfDirectory(directory, files);
		File temp = null;
		try {
			temp = File.createTempFile("import", "imp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		BinFileWriter writer = new BinFileWriter(temp);
		writer.writeInt(fileVersion);
		writer.writeInt(files.size());
		for(File f : files){
			Path p = f.toPath();
			p = directory.toPath().relativize(p);
			writer.writeByte((byte) 13);
			writer.writeString(p.toString());
			try {
				mpq.injectFile(f, p.toString());
			} catch (JmpqError e) {
				e.printStackTrace();
			}
		}
		writer.close();
		mpq.injectFile(temp, "war3map.imp");	
	}
	
	public static void main(String[] args){
		try {
			//test
			JmpqEditor editor = new JmpqEditor(new File("C:\\Users\\Crigges\\Desktop\\ter.w3x"));
			//extractImportedFiles(editor, new File("C:\\Users\\Crigges\\Desktop\\imports"));
			insertImportedFiles(editor, new File("C:\\Users\\Crigges\\Desktop\\imports"));
			editor.compact();
			editor.close();
		} catch (JmpqError e) {
			e.printStackTrace();
		}
	}

	public static void extractImportedFiles(File mapFile) {
		if (!mapFile.exists() || !mapFile.isFile()) {
			JOptionPane.showMessageDialog(null, "Map " + mapFile.getAbsolutePath() + " does not exist.");
			return;
		}
		try {
			File mapTemp = File.createTempFile("temp", "w3x");
			Files.copy(mapFile, mapTemp);
			try (JmpqEditor ed = new JmpqEditor(mapTemp)) {
				File importDirectory = getImportDirectory(mapFile);
				LinkedList<String> failed = extractImportedFiles(ed, importDirectory);
				if (failed.isEmpty()){
					JOptionPane.showMessageDialog(null, "All imports were extracted to " + importDirectory.getAbsolutePath());
				}else{
					String message = "Following files could not be extracted:" + "\n";
					for(String s: failed){
						message = message + s + "\n";
					}
					WLogger.info(message);
					JOptionPane.showMessageDialog(null, message);
				}
			}
		} catch (IOException e) {
			WLogger.severe(e);
			JOptionPane.showMessageDialog(null, "Could not export objects (2): " + e.getMessage());
		}
	}
	
	public static void importFilesFromImportDirectory(File mapFile) {
		File importDirectory = getImportDirectory(mapFile);
		if (importDirectory.exists() && importDirectory.isDirectory()) {
			WLogger.info("importing from: " + importDirectory.getAbsolutePath());
			try (JmpqEditor ed = new JmpqEditor(mapFile)) {
				insertImportedFiles(ed, importDirectory);
			} catch (JmpqError e) {
				WLogger.severe(e);
				JOptionPane.showMessageDialog(null, "Could import objects from "+importDirectory+": " + e.getMessage());
			}
		}
	}

	private static File getImportDirectory(File mapFile) {
		return new File(mapFile.getParentFile(), "imports");
	}
	
	

}
