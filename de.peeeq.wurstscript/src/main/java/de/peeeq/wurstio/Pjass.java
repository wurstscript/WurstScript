package de.peeeq.wurstio;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a helper class to run pjass
 */
public class Pjass {

    public static class Result {

        private boolean ok;
        private String message;
        private File jassFile;

        public Result(File jassFile, boolean ok, String message) {
            this.jassFile = jassFile;
            this.ok = ok;
            this.message = message;
        }

        public boolean isOk() {
            return ok;
        }

        public String getMessage() {
            return message;
        }

        public List<CompileError> getErrors() {
            if (isOk()) {
                return Collections.emptyList();
            }
            LineOffsets lineOffsets = new LineOffsets();
            try {
                String cont = Files.toString(jassFile, Charsets.UTF_8);
                int line = 0;
                lineOffsets.set(1, 0);
                for (int i = 0; i < cont.length(); i++) {
                    if (cont.charAt(i) == '\n') {
                        line++;
                        lineOffsets.set(line + 1, i);
                    }
                }
                lineOffsets.set(line + 1, cont.length() - 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<CompileError> result = Lists.newArrayList();
            for (String error : getMessage().split("([\n\r])+")) {
                Pattern pat = Pattern.compile(".*:([0-9]+):(.*)");
                Matcher match = pat.matcher(error);
                if (!match.matches()) {
                    WLogger.warning("no match: " + error);
                    continue;
                }
                int line = Integer.parseInt(match.group(1));
                String msg = match.group(2);
                result.add(new CompileError(
                        new WPos(jassFile.getAbsolutePath(), lineOffsets, lineOffsets.get(line), lineOffsets.get(line + 1)),
                        "This is a bug in the Wurst Compiler. Please Report it. Pjass has found the following problem: "
                                + msg));
            }

            return result;
        }


    }

    public static Result runPjass(File outputFile) {
        try {
            Process p;
            WLogger.info("Starting pjass");
            List<String> args = new ArrayList<>();
            args.add(Utils.getResourceFile("pjass.exe"));
            args.add(Utils.getResourceFile("common.j"));
            args.add(Utils.getResourceFile("blizzard.j"));
            args.add(outputFile.getPath());
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                WLogger.info("Operation system " + System.getProperty("os.name") + " detected.");
                WLogger.info("Trying to run with wine ...");
                // try to run with wine
                args.add(0, "wine");
            }
            p = Runtime.getRuntime().exec(args.toArray(new String[0]));

            StringBuilder output = new StringBuilder();

            try(BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    WLogger.info(line);
                    output.append(line).append("\n");
                }
            }



            int exitValue = p.waitFor();
            if (exitValue != 0) {
                return new Result(outputFile, false, "pjass errors: \n" + output.toString());
            } else {
                return new Result(outputFile, true, output.toString());
            }
        } catch (IOException e) {
            WLogger.severe("Could not run pjass:");
            WLogger.severe(e);
            return new Result(outputFile, false, "IO Exception");
        } catch (InterruptedException e) {
            return new Result(outputFile, false, "Interrupted");
        }

    }
}
