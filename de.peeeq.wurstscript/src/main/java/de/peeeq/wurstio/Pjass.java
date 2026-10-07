package de.peeeq.wurstio;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import net.moonlightflower.wc3libs.port.Orient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a helper class to run pjass
 */
public class Pjass {

    public static class Result {

        private final boolean ok;
        private final String message;
        private final File jassFile;

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

        /** The full output on failure; on success only its last line, the total. */
        public String getLogMessage() {
            if (!ok) {
                return message;
            }
            String trimmed = message.strip();
            return trimmed.substring(trimmed.lastIndexOf('\n') + 1);
        }

        private static final Pattern pat = Pattern.compile(".*:([0-9]+):(.*)");

        public List<CompileError> getErrors() {
            if (isOk()) {
                return Collections.emptyList();
            }
            LineOffsets lineOffsets = new LineOffsets();
            try {
                String cont = Files.asCharSource(jassFile, Charsets.UTF_8).read();
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
        return runPjass(outputFile, Utils.getResourceFile("common.j"), Utils.getResourceFile("blizzard.j"));
    }

    private static List<String> command(String commonJPath, String blizzardJPath, List<String> files) {
        List<String> args = new ArrayList<>();
        args.add(Utils.getResourceFile("pjass.exe"));
        args.add(commonJPath);
        args.add(blizzardJPath);
        args.addAll(files);
        if (Orient.isLinuxSystem()) {
            File fileName = Utils.getResourceFileF("pjass");
            boolean success = fileName.setExecutable(true);
            if (!success) {
                throw new RuntimeException("Could not make pjass executable.");
            }
            args.set(0, fileName.getAbsolutePath());
        } else if (Orient.isMacSystem()) {
            File fileName = Utils.getResourceFileF("pjass_osx");
            boolean success = fileName.setExecutable(true);
            if (!success) {
                throw new RuntimeException("Could not make pjass_osx executable.");
            }
            args.set(0, fileName.getAbsolutePath());
        } else if (!Orient.isWindowsSystem()) {
            WLogger.info("Unknown operating system detected.");
            WLogger.info("Trying to run with wine ...");
            // try to run with wine
            args.add(0, "wine");
        }
        return args;
    }

    /**
     * Checks each file on its own, against common.j and blizzard.j, with one pjass process.
     * The results are in the order of the files.
     */
    public static List<Result> runPjassEach(List<File> files) {
        List<String> paths = new ArrayList<>();
        paths.add("--each");
        for (File file : files) {
            paths.add(file.getPath());
        }
        List<String> args = command(Utils.getResourceFile("common.j"), Utils.getResourceFile("blizzard.j"), paths);
        WLogger.info("Starting pjass for " + files.size() + " files");
        Process p;
        try {
            p = Runtime.getRuntime().exec(args.toArray(new String[0]));
        } catch (IOException e) {
            return failed(files, "Pjass execution error: \n" + e);
        }
        StringBuilder output = new StringBuilder();
        try {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            p.waitFor();
        } catch (IOException e) {
            WLogger.severe("Could not run pjass:");
            WLogger.severe(e);
            return failed(files, "IO Exception");
        } catch (InterruptedException e) {
            return failed(files, "Interrupted");
        }
        return resultsPerFile(files, output.toString());
    }

    private static List<Result> failed(List<File> files, String message) {
        List<Result> results = new ArrayList<>();
        for (File file : files) {
            results.add(new Result(file, false, message));
        }
        return results;
    }

    private static final Pattern fileDone = Pattern.compile("Parse successful: +\\d+ lines: (.*)|(.*) failed with \\d+ errors?");

    private static List<Result> resultsPerFile(List<File> files, String output) {
        Map<String, Result> byPath = new HashMap<>();
        StringBuilder messages = new StringBuilder();
        for (String line : output.split("\n")) {
            Matcher done = fileDone.matcher(line);
            if (!done.matches()) {
                messages.append(line).append("\n");
                continue;
            }
            boolean ok = done.group(1) != null;
            String path = ok ? done.group(1) : done.group(2);
            for (File file : files) {
                if (file.getPath().equals(path)) {
                    byPath.put(path, new Result(file, ok, ok ? line : "pjass errors: \n" + messages + line));
                }
            }
            messages.setLength(0);
        }
        List<Result> results = new ArrayList<>();
        for (File file : files) {
            Result result = byPath.get(file.getPath());
            results.add(result != null ? result : new Result(file, false, "pjass did not report " + file.getPath() + ":\n" + output));
        }
        return results;
    }

    public static Result runPjass(File outputFile, String commonJPath, String blizzardJPath) {
        try {
            Process p;
            WLogger.info("Starting pjass");
            List<String> args = command(commonJPath, blizzardJPath, Collections.singletonList(outputFile.getPath()));

            try {
                p = Runtime.getRuntime().exec(args.toArray(new String[0]));
            } catch (IOException e) {
               return new Result(outputFile, false, "Pjass execution error: \n" + e);
            }

            StringBuilder output = new StringBuilder();

            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }


            int exitValue = p.waitFor();
            if (exitValue != 0) {
                return new Result(outputFile, false, "pjass errors: \n" + output);
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
