package de.peeeq.wurstio;

import de.peeeq.wurstio.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class Checksums {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("usage: 1. folder, 2. output file");
        }
        File dir = new File(args[0]);
        File outFile = new File(args[1]);
        List<Data> data = getData(dir);
        String out = printData(data);
        try {
            FileUtils.write(out, outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String printData(List<Data> data) {
        StringBuilder sb = new StringBuilder();
        for (Data d : data) {
            sb.append(d.filePath);
            sb.append("\n");
            sb.append(d.md5);
            sb.append("\n");
        }
        return sb.toString();
    }

    private static List<Data> getData(File f) {
        List<Data> result = new ArrayList<>();
        for (File p : f.listFiles()) {
            getDataRec(result, "", p);
        }
        return result;
    }

    private static void getDataRec(List<Data> result, String path, File f) {
        if (f.isDirectory()) {
            for (File p : f.listFiles()) {
                getDataRec(result, path + "/" + f.getName(), p);
            }
        } else {
            result.add(new Data(path + "/" + f.getName(), md5(f)));
        }
    }

    // stolen from http://stackoverflow.com/a/304350/303637
    private static String md5(File f) {
        try {
            byte[] buf = new byte[1024];
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = java.nio.file.Files.newInputStream(f.toPath());
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                while (dis.read(buf) >= 0) ;
            }
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // stolen from http://stackoverflow.com/a/9855338/303637
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}

class Data {
    final String filePath;
    final String md5;

    public Data(String filePath, String md5) {
        this.filePath = filePath;
        this.md5 = md5;
    }
}