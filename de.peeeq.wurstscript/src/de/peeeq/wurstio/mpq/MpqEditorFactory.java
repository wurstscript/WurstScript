package de.peeeq.wurstio.mpq;

import java.io.File;

import de.peeeq.wurstscript.RunArgs;


public class MpqEditorFactory {
	
	static public MpqEditor getEditor(File f, RunArgs args) throws Exception {
		if(args.useJmpq2()){
			return new Jmpq2BasedEditor(f);
		}else{
			return new Jmpq3BasedEditor(f);
		}
	}
}
