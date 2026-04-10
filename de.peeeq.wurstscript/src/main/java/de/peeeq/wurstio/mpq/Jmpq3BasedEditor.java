package de.peeeq.wurstio.mpq;

import com.google.common.base.Preconditions;
import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.JMpqException;
import systems.crigges.jmpq3.MPQOpenOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class Jmpq3BasedEditor implements MpqEditor {
    private final JMpqEditor editor;

    private JMpqEditor getEditor() {
        return editor;
    }

    public Jmpq3BasedEditor(File mpqArchive, boolean readonly) throws Exception {
        Preconditions.checkNotNull(mpqArchive);
        if (!mpqArchive.exists()) {
            throw new FileNotFoundException("not found: " + mpqArchive);
        }
        this.editor = new JMpqEditor(mpqArchive, readonly ? MPQOpenOption.READ_ONLY : MPQOpenOption.FORCE_V0);

    }

    static void createEmptyArchive(File mpqArchive) throws IOException {
        try {
            JMpqEditor.class.getMethod("createEmptyArchive", File.class).invoke(null, mpqArchive);
        } catch (NoSuchMethodException e) {
            throw new IOException("JMPQ3 is missing createEmptyArchive(File); update the JMPQ3 dependency.", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Cannot access JMPQ3 createEmptyArchive(File).", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("JMPQ3 could not create an empty MPQ archive.", cause);
        }
    }

    @Override
    public void insertFile(String filenameInMpq, byte[] contents) {
        getEditor().deleteFile(filenameInMpq);
        getEditor().insertByteArray(filenameInMpq, contents);
    }

    @Override
    public void insertFile(String filenameInMpq, File contents) throws Exception {
        getEditor().deleteFile(filenameInMpq);
        getEditor().insertFile(filenameInMpq, contents);
    }

    @Override
    public boolean canWrite() {
        return editor.isCanWrite();
    }

    @Override
    public byte[] extractFile(String fileToExtract) throws Exception {
        return getEditor().extractFileAsBytes(fileToExtract);
    }

    @Override
    public void deleteFile(String filenameInMpq) {
        getEditor().deleteFile(filenameInMpq);
    }

    @Override
    public void close() throws IOException {
        try {
            editor.close();
        } catch (JMpqException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean hasFile(String fileName) {
        return getEditor().hasFile(fileName);
    }

    @Override
    public void setKeepHeaderOffset(boolean flag) {
        editor.setKeepHeaderOffset(flag);
    }

    @Override
    public void closeWithCompression() throws IOException {
        try {
            editor.close(true, false, true);
        } catch (JMpqException e) {
            throw new IOException(e);
        }
    }

}
