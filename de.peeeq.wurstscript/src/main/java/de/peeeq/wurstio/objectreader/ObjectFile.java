package de.peeeq.wurstio.objectreader;

import de.peeeq.wurstscript.WLogger;

import java.io.*;

public class ObjectFile {

    private ObjectFileType fileType;
    private ObjectTable origTable;
    private ObjectTable modifiedTable;
    private int version;

    public ObjectFileType getFileType() {
        return fileType;
    }

    public ObjectTable getOrigTable() {
        return origTable;
    }

    public ObjectTable getModifiedTable() {
        return modifiedTable;
    }

    @SuppressWarnings("resource") // closed in constructor
    public ObjectFile(File file, ObjectFileType fileType) throws IOException {
        this(new BinaryDataInputStream(file, true), fileType);
    }

    @SuppressWarnings("resource") // closed in constructor
    public ObjectFile(byte[] w3_, ObjectFileType filetype) {
        this(new BinaryDataInputStream(new ByteArrayInputStream(w3_), true), filetype);
    }

    private ObjectFile(BinaryDataInputStream in, ObjectFileType fileType) {
        this.fileType = fileType;
        try {
            version = in.readInt();

            this.origTable = ObjectTable.readFromStream(in, fileType);
            this.modifiedTable = ObjectTable.readFromStream(in, fileType);
        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public ObjectFile(ObjectFileType fileType) {
        this.fileType = fileType;
        version = 2;
        origTable = new ObjectTable(fileType);
        modifiedTable = new ObjectTable(fileType);
    }


    public void writeTo(File file) {
        try (BinaryDataOutputStream out = new BinaryDataOutputStream(file, true)) {
            writeTo(out);
        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        }
    }

    public byte[] writeToByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (BinaryDataOutputStream out = new BinaryDataOutputStream(os, true)) {
            writeTo(out);
            return os.toByteArray();
        } catch (IOException e) {
            WLogger.severe(e);
            throw new Error(e);
        }
    }

    public void writeTo(BinaryDataOutputStream out) throws IOException {
        out.writeInt(version);
        this.origTable.writeToStream(out, fileType);
        this.modifiedTable.writeToStream(out, fileType);
        out.flush();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("version = ").append(version).append("\n");
        sb.append(" original Table(modified standard units):\n");
        sb.append("##################\n");
        origTable.prettyPrint(sb);
        sb.append(" modified Table(newly created unit):\n");
        sb.append("##################\n");
        modifiedTable.prettyPrint(sb);

        return sb.toString();
    }


    public void exportToWurst(Appendable out, ObjectFileType fileType) throws IOException {
        out.append("package WurstExportedObjects_").append(fileType.getExt()).append("\n");
        out.append("import ObjEditingNatives\n\n");

        out.append("// Modified Table (contains all custom objects)\n\n");
        modifiedTable.exportToWurst(out);

        out.append( "// Original Table (contains all modified default objects)\n" +
                    "// Wurst does not support modifying default objects\n" +
                    "// but you can copy these functions and replace 'xxxx' with a new, custom id.\n\n");
        origTable.exportToWurst(out);
    }


    public String exportToWurst(ObjectFileType fileType) {
        StringBuilder sb = new StringBuilder();
        try {
            exportToWurst(sb, fileType);
        } catch (IOException e) {
            WLogger.severe(e);
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return origTable.isEmpty() && modifiedTable.isEmpty();
    }

}
