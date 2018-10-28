package de.peeeq.wurstio.objectreader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class BinaryDataOutputStream implements Closeable {

    private BufferedOutputStream out;
    private boolean littleEndian;

    @SuppressWarnings("resource")
    public BinaryDataOutputStream(File file, boolean littleEndian) throws IOException {
        this(Files.newOutputStream(file.toPath()), littleEndian);
    }


    public BinaryDataOutputStream(OutputStream out, boolean littleEndian) {
        this.out = new BufferedOutputStream(out);
        this.littleEndian = littleEndian;
    }


    public void flush() throws IOException {
        out.flush();
    }

    public void writeInt(int i, boolean littleEndian) throws IOException {
//		byte[] data = new byte[4];
//		data[3] = (byte) (i >> 24);
//		data[2] = (byte) (i >> 16);
//		data[1] = (byte) (i >> 8);
//		data[0] = (byte) (i >> 0);

        byte[] data = ByteBuffer.allocate(4).putInt(i).array();
        for (int j = 0; j < 4; j++) {
            byte b = data[littleEndian ? (3 - j) : j];
            out.write(b);
        }
    }

    public void writeInt(int i) throws IOException {
        writeInt(i, littleEndian);
    }

    public void writeIntReverse(int i) throws IOException {
        writeInt(i, !littleEndian);
    }

    public void writeString(String s, int len) throws IOException {
        byte[] bytes = new byte[len];
        byte[] s_bytes = s.getBytes();
        System.arraycopy(s_bytes, 0, bytes, 0, Math.min(len, s_bytes.length));
        out.write(bytes);
    }

    public void writeStringNullTerminated(String s, Charset charset) throws IOException {
        out.write(s.getBytes(charset));
        out.write(0);
    }

    public void writeFloat(float f) throws IOException {
        int asInt = Float.floatToIntBits(f);
        writeInt(asInt);
    }


    @Override
    public void close() throws IOException {
        out.close();
    }


}
