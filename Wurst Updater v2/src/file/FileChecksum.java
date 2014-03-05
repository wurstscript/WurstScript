package file;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

public class FileChecksum {
	
	public static LinkedList<FileEx> readChecksums(File f) throws IOException{
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		LinkedList<FileEx> temp = new LinkedList<FileEx>();
		String file = raf.readLine();
		String sum = raf.readLine();
		while(sum != null && file != null){
			temp.add(new FileEx(file, sum));
			file = raf.readLine();
			sum = raf.readLine();
		}
		raf.close();
		return temp;
	}
	
	public static String get(File f) {
		try {
			byte[] buf = new byte[1024];
			MessageDigest md = MessageDigest.getInstance("MD5");
			try (InputStream is = new FileInputStream(f);
				DigestInputStream dis = new DigestInputStream(is, md);) {
			while (dis.read(buf) >= 0);
			}
			byte[] digest = md.digest();
			return bytesToHex(digest);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	// stolen from http://stackoverflow.com/a/9855338/303637
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

}
