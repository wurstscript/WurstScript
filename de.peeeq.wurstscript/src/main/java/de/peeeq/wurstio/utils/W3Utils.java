package de.peeeq.wurstio.utils;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.WinRegistry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class W3Utils {

    private static final Pattern patchPattern = Pattern.compile("(?i)Patch (\\d.\\d\\d)");

    public static double parsePatchVersion(File wc3Path) {
        WLogger.info("Parsing Patch Version");
        File patchTxt = new File(wc3Path, "Patch.txt");
        File releaseNotes = new File(wc3Path, "Release Notes.txt");
        File gamedll = new File(wc3Path, "game.dll");

        if (!patchTxt.exists() && !releaseNotes.exists() && !gamedll.exists()) {
            // If neither of the patch logs exist and the gamedll is gone as well,
            // we likely are dealing with a 1.29 install or higher
            WLogger.info("Assumed Version: 1.29");
            return 1.29;
        }

        try {
            final List<String> matches = new ArrayList<>();
            if (patchTxt.exists()) {
                Matcher matcher = patchPattern.matcher(new String(java.nio.file.Files.readAllBytes(patchTxt.toPath())));
                while (matcher.find()) {
                    matches.add(matcher.group(1));
                }
            }
            if (releaseNotes.exists()) {
                Matcher matcher = patchPattern.matcher(new String(java.nio.file.Files.readAllBytes(releaseNotes.toPath())));
                while (matcher.find()) {
                    matches.add(matcher.group(1));
                }
            }

            if (matches.size() > 0) {
                matches.sort(Comparator.comparing(Double::parseDouble, Collections.reverseOrder()));
                double patchVersion = Double.parseDouble(matches.get(0));
                WLogger.info("Patch Version: " + patchVersion);
                return patchVersion;

            } else {
                WLogger.severe("Could not determine wc3 version");
            }
        } catch (IOException e) {
            WLogger.severe(e);
        }
        WLogger.severe("Could not determine wc3 version");
        return -1;
    }

    /**
     * Reads the installation path from windows registry
     *
     * @return The wc3 installation path, or null if no registry entry exists
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static String getGamePath() throws InvocationTargetException, IllegalAccessException {
        String path = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Blizzard Entertainment\\Warcraft III", "InstallPath");
        if (path != null && !path.endsWith("\\")) path = path + "\\";
        return path;
    }
}
