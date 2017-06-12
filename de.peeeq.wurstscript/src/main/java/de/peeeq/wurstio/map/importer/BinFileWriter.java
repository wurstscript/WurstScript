package de.peeeq.wurstio.map.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class BinFileWriter {
	private RandomAccessFile writer = null;
	
	public BinFileWriter(File newFile){
		try {
			writer = new RandomAccessFile(newFile, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeBytes(byte[] bytes){
		try {
			writer.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeByte(byte b){
		try {
			writer.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeString(String s){
		byte[] bytes = null;
		try {
			bytes = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writeBytes(bytes);
		writeByte((byte) 0);
	}
	
	public void writeInt(int i){
		writeBytes(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array());
	}
	
	
	
	
	
	
	
	public void close(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}