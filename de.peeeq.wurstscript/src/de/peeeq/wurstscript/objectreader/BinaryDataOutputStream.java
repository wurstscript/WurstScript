package de.peeeq.wurstscript.objectreader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BinaryDataOutputStream {

	private BufferedOutputStream out;
	private boolean littleEndian;

	public BinaryDataOutputStream(File file, boolean littleEndian) throws FileNotFoundException {
		this(new FileOutputStream(file), littleEndian);
	}
	
	
	public BinaryDataOutputStream(OutputStream out, boolean littleEndian) {
		this.out = new BufferedOutputStream(out);
		this.littleEndian = littleEndian;
	}

	
	public void flush() throws IOException {
		out.flush();
	}

	public void writeInt(int i) throws IOException {
//		byte[] data = new byte[4];
//		data[3] = (byte) (i >> 24);
//		data[2] = (byte) (i >> 16);
//		data[1] = (byte) (i >> 8);
//		data[0] = (byte) (i >> 0);
		
		byte[] data = ByteBuffer.allocate(4).putInt(i).array();
		System.out.println(i + " ::: " + data);
		for (int j=0; j<4; j++) {
			byte b =  data[littleEndian ? (3-j) : j];
			System.out.println("writing " + b);
			out.write(b);
		}
	}
	

	public void writeString(String s, int len) throws IOException {
		byte [] bytes = new byte[len];
		System.arraycopy(s.getBytes(), 0, bytes, 0, len);
		out.write(bytes);
	}
	
	public void writeStringNullTerminated(String s) throws IOException {
		out.write(s.getBytes());
		out.write(0);
	}

	public void writeFloat(float f) throws IOException {
		int asInt = Float.floatToIntBits(f);
		writeInt(asInt);
	}

}
