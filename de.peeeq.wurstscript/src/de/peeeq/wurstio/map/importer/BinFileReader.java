package de.peeeq.wurstio.map.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinFileReader {
	private RandomAccessFile reader = null;
	
	public BinFileReader(File dooFile){
		try {
			reader = new RandomAccessFile(dooFile, "r");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] readBytes(int amount){
		byte[] bytes = new byte[amount];
		try {
			reader.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	public String readString(){
		byte b = readByte();
		byte[] bytes = new byte[2000];
		int i = 0;
		while(b != 0){
			bytes[i] = b;
			i++;
			b = readByte();
		}
		return new String(bytes);
	}
	
	public byte readByte(){
		try {
			return reader.readByte();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int readInt(){
		byte[] bytes = readBytes(4);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	public short readShort(){
		byte[] bytes = readBytes(2);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}
	
	public float readFloat(){
		byte[] bytes = readBytes(4);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();	
	}
	
	public String readFourchar(){
		return new String(readBytes(4));
	}
	
	public void close(){
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
