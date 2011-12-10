package de.peeeq.wurstscript.objectreader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;

public class BinaryDataInputStream {

	private BufferedInputStream in;
	private boolean littleEndian;

	public BinaryDataInputStream(File file, boolean littleEndian) throws FileNotFoundException {
		this(new FileInputStream(file), littleEndian);
		
	}
	
	
	public BinaryDataInputStream(InputStream in, boolean littleEndian) {
		this.in = new BufferedInputStream(in);
		this.littleEndian = littleEndian;
	}

	int readInt() throws IOException {
		byte[] data = readBytes(4);
		int result = 0;
		if (littleEndian) {
			for (int i=3; i>0; i--) {
				result += data[i];
				result <<= 8;
			}
			result += data[0];
		} else {
			for (int i=0; i<3; i++) {
				result += data[i];
				result <<= 8;
			}
			result += data[3];
		}
		return result;
		
	}

	private byte[] readBytes(int size) throws IOException {
		byte[] data = new byte[size];
		for (int i=0; i<size; i++) {
			data[i] = readByte();
		}
		return data;
	}

	private byte readByte() throws IOException {
		int d = in.read();
		if (d < 0 ) {
			throw new IOException("EOF reached");
		} else {
			return (byte) d;
		}
	}

	public String readString(int length) throws IOException {
		byte[] data = readBytes(length);
		return new String(data, Charset.defaultCharset());
	}

	public float readFloat() throws IOException {
		int asInt = readInt();
		return Float.intBitsToFloat(asInt);
	}

	public String readNullTerminatedString() throws IOException {
		byte[] buffer = new byte[128];
		int pos = 0;
		while (true) {
			byte b = readByte();
			if (b == 0) {
				return new String(buffer);
			}
			if (pos >= buffer.length) {
				byte[] newBuffer = new byte[buffer.length*2];
				System.arraycopy(buffer, 0, newBuffer, 0, pos-1);
				buffer = newBuffer;
			}
			buffer[pos] = b;
			pos++;
		}
	}

}
