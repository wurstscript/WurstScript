package de.peeeq.wurstio.mpq;

import java.io.File;


public class MpqEditorFactory {
	private static String tempfolder = "";
	
	static public MpqEditor getEditor(File f) throws Exception {
		return new Jmpq2BasedEditor(f);
	}

	

	public static void setTempfolder(String tempfolder) {
		MpqEditorFactory.tempfolder = tempfolder;
	}
}
