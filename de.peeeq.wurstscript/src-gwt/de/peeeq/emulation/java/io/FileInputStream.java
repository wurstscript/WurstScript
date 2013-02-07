package java.io;

public class FileInputStream extends InputStream {

	public FileInputStream() {
	}
	
	public FileInputStream(String filename) {
	}
	
	public FileInputStream(File f) {
	}
	
	@Override
	public int read() throws IOException {
		return 0;
	}
	
	@Override
	public void close() throws IOException {
	}

}
