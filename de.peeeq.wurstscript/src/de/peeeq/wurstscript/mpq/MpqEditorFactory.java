package de.peeeq.wurstscript.mpq;

public class MpqEditorFactory {

	static public MpqEditor getEditor() {
		return new WinMpq();
	}
}
