package de.peeeq.wurstio.map.importer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.WLogger;


public class ImportFile {
	private static final int fileVersion = 1;
	
	
	public static LinkedList<String> extractImportedFiles(MpqEditor mpq, File directory){
		LinkedList<String> failed = new LinkedList<String>();
		byte[] temp;
		try {
			temp = mpq.extractFile("war3map.imp");
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "No vaild war3map.imp was found, or there are no imports");
			return failed;
		}
//		BinFileReader reader = new BinFileReader(temp);
		try {
			LittleEndianDataInputStream reader = new LittleEndianDataInputStream(new ByteArrayInputStream(temp));
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
				String filename = readString(reader);
				filename = filename.trim();
				mpqpath += filename;
				path += filename;
				File out = new File(path);
				out.getParentFile().mkdirs();
				try {
					byte[] xx = mpq.extractFile(mpqpath);
					Files.write(xx, out);
				} catch (IOException e) {
					out.delete();
					out = new File(directory.getPath() + "\\" + "war3mapImported\\" + filename);
					out.getParentFile().mkdirs();
					try {
						byte[] xx = mpq.extractFile("war3mapImported\\" + mpqpath);
						Files.write(xx, out);
					} catch (IOException e1) {
						failed.add(mpqpath);
					}
				}
			}
			reader.close();
			return failed;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String readString(LittleEndianDataInputStream reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			char c = reader.readChar();
			if (c==0) {
				return sb.toString();
			}
			sb.append(c);
		}
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
	
	public static void insertImportedFiles(MpqEditor mpq, File directory) throws Exception{
		LinkedList<File> files = new LinkedList<File>();
		getFilesOfDirectory(directory, files);
		File temp = null;
		try {
			temp = File.createTempFile("import", "imp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO directly write to byte array instead of temp file 
		BinFileWriter writer = new BinFileWriter(temp);
		writer.writeInt(fileVersion);
		writer.writeInt(files.size());
		for(File f : files){
			Path p = f.toPath();
			p = directory.toPath().relativize(p);
			writer.writeByte((byte) 13);
			writer.writeString(p.toString());
			WLogger.info("importing file: " + p.toString());
//			try {
				mpq.insertFile(p.toString(), Files.toByteArray(f));
//			} catch (IOException e) {
//				WLogger.info(e);
//				e.printStackTrace();
//			}
		}
		writer.close();
		mpq.insertFile("war3map.imp",Files.toByteArray(temp));	
	}
	
	public static void extractImportedFiles(File mapFile) {
		if (!mapFile.exists() || !mapFile.isFile()) {
			JOptionPane.showMessageDialog(null, "Map " + mapFile.getAbsolutePath() + " does not exist.");
			return;
		}
		try {
			File mapTemp = File.createTempFile("temp", "w3x");
			Files.copy(mapFile, mapTemp);
			try (MpqEditor ed = MpqEditorFactory.getEditor(mapTemp)) {
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
		} catch (Exception e) {
			WLogger.severe(e);
			JOptionPane.showMessageDialog(null, "Could not export objects (2): " + e.getMessage());
		}
	}
	
	public static void importFilesFromImportDirectory(File mapFile, MpqEditor ed) {
		File importDirectory = getImportDirectory(mapFile);
		if (importDirectory.exists() && importDirectory.isDirectory()) {
			WLogger.info("importing from: " + importDirectory.getAbsolutePath());
			WLogger.info("mapfile: " + mapFile.getAbsolutePath());
			try {
				insertImportedFiles(ed, importDirectory);
			} catch (Exception e) {
				WLogger.severe(e);
				JOptionPane.showMessageDialog(null, "Could import objects from "+importDirectory+": " + e.getMessage());
			}
		}
	}

	private static File getImportDirectory(File mapFile) {
		return new File(mapFile.getParentFile(), "imports");
	}
	

}
