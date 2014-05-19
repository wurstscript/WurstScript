package de.peeeq.wurstio.mpq;

import java.io.File;


public class MpqEditorFactory {
	private static String filepath = "";
	private static String tempfolder = "";
	
	static public String getFilepath(){
		return filepath;
	}
	
	static public void setFilepath(String path){
		filepath = path;
	}
	
	static public MpqEditor getEditor(File f) throws Exception {
		MpqEditor ed = new JmpqBasedEditor();
		ed.open(f);
		return ed;
	}

	public static String getTempfolder() {
		return tempfolder;
	}

	public static void setTempfolder(String tempfolder) {
		MpqEditorFactory.tempfolder = tempfolder;
	}
}
