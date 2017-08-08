package file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Download {

    private static final String baseUrl = "http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/";
    private static final String compileName = "wurstpack_compiler.zip";

    private static File downloadFile(String url, File targetFile) throws IOException {
        if (url.startsWith("/")) {
            url = url.replaceFirst("/", "");
        }
        URL website = new URL(url.replace(" ", "%20"));
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(targetFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        rbc.close();
        return targetFile;
    }

    public static File downloadCompiler() throws IOException {
        return downloadFile(baseUrl + compileName, File.createTempFile("tempWurstCompiler", ".zip"));
    }

    public static File downloadBareboneProject() throws IOException {
        return downloadFile("https://github.com/wurstscript/WurstBareboneTemplate/archive/master.zip", File.createTempFile("tempProject", ".zip"));
    }
}