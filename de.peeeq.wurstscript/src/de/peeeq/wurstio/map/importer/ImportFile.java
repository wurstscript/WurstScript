package de.peeeq.wurstio.map.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import de.peeeq.jmpq.JmpqEditor;
import de.peeeq.jmpq.JmpqError;


public class ImportFile {
	private static final int fileVersion = 1;
	
	
	public static void extractImportedFiles(JmpqEditor mpq, File directory) throws JmpqError{
		File temp = null;
		try {
			temp = File.createTempFile("temp", "imp");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		mpq.extractFile("war3map.imp", temp);
		temp = new File("C:\\Users\\Crigges\\Desktop\\war3map.imp");
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
				mpq.extractFile("war3mapImported\\" + mpqpath, out);
			}
		}
		reader.close();
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
	
	

}
