package de.peeeq.wurstio.mpq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.google.common.base.Preconditions;

import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.JMpqException;

class Jmpq3BasedEditor implements MpqEditor {

    private JMpqEditor editor;

    private JMpqEditor getEditor() {
        if (editor == null) {
            throw new RuntimeException("editor already closed");
        }
        return editor;
    }

    public Jmpq3BasedEditor(File mpqArchive) throws Exception {
        Preconditions.checkNotNull(mpqArchive);
        if (!mpqArchive.exists()) {
            throw new FileNotFoundException("not found: " + mpqArchive);
        }
        this.editor = new JMpqEditor(mpqArchive);
    }

    @Override
    public void insertFile(String filenameInMpq, byte[] contents) throws Exception {
        File temp = File.createTempFile("peq", "wurst");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(contents);
        fos.close();
        getEditor().insertFile(filenameInMpq, temp, false);
    }

    @Override
    public byte[] extractFile(String fileToExtract) throws Exception {
        File temp = File.createTempFile("peq", "wurst");
        temp.deleteOnExit();
        getEditor().extractFile(fileToExtract, temp);
        return Files.readAllBytes(temp.toPath());
    }

    @Override
    public void deleteFile(String filenameInMpq) throws Exception {
        getEditor().deleteFile(filenameInMpq);
    }

    @Override
    public void close() throws IOException {
        if (editor != null) {
            try {
                editor.close();
            } catch (JMpqException e) {
                throw new IOException(e);
            }
            editor = null;
        }
    }

}
