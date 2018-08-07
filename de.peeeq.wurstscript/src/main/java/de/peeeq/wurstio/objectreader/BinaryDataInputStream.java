package de.peeeq.wurstio.objectreader;

import com.google.common.base.Charsets;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class BinaryDataInputStream implements Closeable {

    private BufferedInputStream in;
    private boolean littleEndian;

    @SuppressWarnings("resource") // will be closed in close method
    public BinaryDataInputStream(File file, boolean littleEndian) throws IOException {
        this(Files.newInputStream(file.toPath()), littleEndian);
    }


    public BinaryDataInputStream(InputStream in, boolean littleEndian) {
        this.in = new BufferedInputStream(in);
        this.littleEndian = littleEndian;
    }

    public int readInt(boolean littleEndian) throws IOException {
        byte[] data = readBytes(4);
        int result = 0;
        if (littleEndian) {
            for (int i = 3; i > 0; i--) {
                result += interpret2compl(data[i]);
                result <<= 8;
            }
            result += interpret2compl(data[0]);
        } else {
            for (int i = 0; i < 3; i++) {
                result += data[i];
                result <<= 8;
            }
            result += data[3];
        }
        return result;

    }

    public int readInt() throws IOException {
        return readInt(littleEndian);
    }

    public int readIntReverse() throws IOException {
        return readInt(!littleEndian);
    }

    private int interpret2compl(byte b) {
        if (b >= 0) {
            return b;
        }
        return b + 256;
    }


    private byte[] readBytes(int size) throws IOException {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = readByte();
        }
        return data;
    }

    private byte readByte() throws IOException {
        int d = in.read();
        if (d < 0) {
            throw new IOException("EOF reached");
        } else {
            return (byte) d;
        }
    }

    public String readString(int length) throws IOException {
        byte[] data = readBytes(length);
        return new String(data, Charsets.UTF_8);
    }

    public float readFloat() throws IOException {
        int asInt = readInt();
        return Float.intBitsToFloat(asInt);
    }

    public String readNullTerminatedString(Charset charset) throws IOException {
        byte[] buffer = new byte[128];
        int pos = 0;
        while (true) {
            byte b = readByte();
            if (b == 0) {
                return new String(buffer, 0, pos, charset);
            }
            if (pos >= buffer.length) {
                byte[] newBuffer = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuffer, 0, pos);
                buffer = newBuffer;
            }
            buffer[pos] = b;
            pos++;
        }
    }


    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
