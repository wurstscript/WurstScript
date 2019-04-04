package de.peeeq.wurstio.mpq;

import java.io.File;


public class MpqEditorFactory {

    static public MpqEditor getEditor(File f) throws Exception {
        if (f == null) {
            return null;
        }
        return new Jmpq3BasedEditor(f);
    }
}
