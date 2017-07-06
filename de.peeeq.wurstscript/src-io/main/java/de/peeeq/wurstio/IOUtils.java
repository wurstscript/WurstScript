package de.peeeq.wurstio;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class IOUtils {
    private static Map<String, File> resourceMap = new HashMap<>();


    public static String readWholeStream(BufferedReader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String readWholeStream(InputStream inputStream) throws IOException {
        return readWholeStream(new BufferedReader(new InputStreamReader(inputStream)));
    }

    public static MouseListener onClickDo(Consumer<MouseEvent> onclick) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(@Nullable MouseEvent e) {
                Preconditions.checkNotNull(e);
                onclick.accept(e);
            }
        };
    }

    /**
     * Executes a shell command in a given folder and returns the output of the executed command
     */
    public static String exec(File folder, String... cmds) {
        try {
            Process p = new ProcessBuilder(cmds)
                    .directory(folder)
                    .start();
            int res = p.waitFor();
            if (res != 0) {
                throw new RuntimeException("Could not execute " + Utils.join(Arrays.asList(cmds), " ")
                        + "\nErrors:\n"
                        + convertStreamToString(p.getErrorStream())
                        + "\nOutput:\n"
                        + convertStreamToString(p.getInputStream()));
            }
            return convertStreamToString(p.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts an input stream to a string
     * <p>
     * see http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
     */
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static byte[] convertStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    /**
     * Extracts a resource from the jar, stores it in a temp file and returns the abolute path to the tempfile
     */
    public static synchronized String getResourceFile(String name) {
        try {
            File f = resourceMap.get(name);
            if (f != null && f.exists()) {
                return f.getAbsolutePath();
            }
            String[] parts = name.split("\\.");
            f = File.createTempFile(parts[0], parts[1]);
            f.deleteOnExit();
            try (InputStream is = Pjass.class.getClassLoader().getResourceAsStream(name)) {
                if (is == null) {
                    throw new RuntimeException("Could not find resource file " + name);
                }
                byte[] bytes = convertStreamToBytes(is);
                Files.write(bytes, f);
                resourceMap.put(name, f);
                return f.getAbsolutePath();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isWurstFile(File f) {
        return Utils.isWurstFile(f.getName());
    }

    public static String getLibName(File f) {
        return f.getName().replaceAll("\\.[jw]urst$", "");
    }
}
