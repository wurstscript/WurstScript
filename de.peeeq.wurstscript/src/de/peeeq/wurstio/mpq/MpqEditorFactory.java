package de.peeeq.wurstio.mpq;


public class MpqEditorFactory {
	private static String filepath = "";
	private static String tempfolder = "";
	
	static public String getFilepath(){
		return filepath;
	}
	
	static public void setFilepath(String path){
		filepath = path;
	}
	
	static public LadikMpq getEditor() throws Exception {
		if ( filepath.equals("")){
			throw new Exception("mpqeditor filepath not set");
		}else {
			return new LadikMpq();
		}
	}

	public static String getTempfolder() {
		return tempfolder;
	}

	public static void setTempfolder(String tempfolder) {
		MpqEditorFactory.tempfolder = tempfolder;
	}
}
