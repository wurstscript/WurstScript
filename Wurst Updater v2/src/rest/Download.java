package rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Download {
	
	private static final String basicUrl = "http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/";
	
	public static boolean isServerOnline(){
		URL website;
		try {
			website = new URL(basicUrl);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			return false;
		}
		try {
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			rbc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	
	public static File downloadFile(String url, File f) throws IOException{
		if (url.startsWith("/")){
			url = url.replaceFirst("/", "");
		}
		URL website = new URL(basicUrl + url.replace(" ", "%20"));
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(f);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		rbc.close();
		return f;
	}
	
	public static void downloadPack() throws IOException{
		File f = downloadFile("wurstpackcomplete.zip", File.createTempFile("blubber", ".zip"));
	}

}
