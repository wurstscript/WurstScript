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
import java.util.Arrays;
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

    // This is a saved-game compatibility warning, not invalid generated Jass.
    private static final String longStringDiagnostic =
        "String literals over 1023 chars long crash the game upon loading a saved game.";
    private static final Pattern failureTotal = Pattern.compile(
        "Parse failed: (\\d+) errors? total|.* failed with (\\d+) errors?");

    private static boolean isLongStringWarning(String line) {
        Matcher diagnostic = Result.pat.matcher(line);
        return diagnostic.matches() && diagnostic.group(3).strip().equals(longStringDiagnostic);
    }

    private static Result checkedResult(File file, int exitValue, String output) {
        int warningCount = 0;
        boolean otherDiagnostic = false;
        StringBuilder warnings = new StringBuilder();
        for (String line : output.split("[\\r\\n]+")) {
            if (isLongStringWarning(line)) {
                warningCount++;
                warnings.append(line).append('\n');
                WLogger.warning(line);
            } else if (Result.pat.matcher(line).matches()) {
                otherDiagnostic = true;
            }
        }
        String trimmed = output.strip();
        Matcher total = failureTotal.matcher(trimmed.substring(trimmed.lastIndexOf('\n') + 1));
        // Only accept a normal pjass validation failure whose entire error count is accounted
        // for by this warning. Missing files, other diagnostics and process failures still fail.
        if (exitValue == 1 && warningCount > 0 && !otherDiagnostic && total.matches()
            && Integer.toString(warningCount).equals(total.group(1) != null ? total.group(1) : total.group(2))) {
            return new Result(file, true, "pjass warnings:\n" + warnings
                + "Pjass validation successful with " + warningCount + " warnings.\n");
        }
        return new Result(file, exitValue == 0, exitValue == 0 ? output : "pjass errors: \n" + output);
    }

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

        /** The full output on failure or warnings; otherwise only its last line, the total. */
        public String getLogMessage() {
            if (!ok || message.startsWith("pjass warnings:")) {
                return message;
            }
            String trimmed = message.strip();
            return trimmed.substring(trimmed.lastIndexOf('\n') + 1);
        }

        private static final Pattern pat = Pattern.compile("(.*):([0-9]+):(.*)");

        public List<CompileError> getDiagnostics(Map<Integer, WPos> sourceMap) {
            Map<File, LineOffsets> offsets = new HashMap<>();
            List<CompileError> result = Lists.newArrayList();
            for (String error : getMessage().split("([\n\r])+")) {
                Matcher match = pat.matcher(error);
                if (!match.matches()) {
                    continue;
                }
                File reportedFile = new File(match.group(1)).getAbsoluteFile();
                int line = Integer.parseInt(match.group(2));
                WPos source = reportedFile.equals(jassFile.getAbsoluteFile()) ? sourceMap.get(line) : null;
                if (source == null) {
                    LineOffsets lineOffsets = offsets.computeIfAbsent(reportedFile, Result::readLineOffsets);
                    source = new WPos(reportedFile.getPath(), lineOffsets,
                        lineOffsets.get(line - 1) + 1, lineOffsets.get(line));
                }
                boolean warning = isLongStringWarning(error);
                String msg = match.group(3).strip();
                result.add(new CompileError(source,
                    warning ? "Jass save/load compatibility: " + msg
                        : "Generated Jass failed validation: " + msg + "\nPlease report this compiler error.",
                    warning ? CompileError.ErrorType.WARNING : CompileError.ErrorType.ERROR));
            }
            return result;
        }

        private static LineOffsets readLineOffsets(File file) {
            try {
                String content = Files.asCharSource(file, Charsets.UTF_8).read();
                LineOffsets offsets = new LineOffsets();
                int line = 1;
                for (int i = 0; i < content.length(); i++) {
                    if (content.charAt(i) == '\n') offsets.set(line++, i);
                }
                offsets.set(line, content.length());
                return offsets;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public List<CompileError> getErrors() {
            if (isOk()) return Collections.emptyList();
            return getDiagnostics(Collections.emptyMap()).stream()
                .filter(d -> d.getErrorType() == CompileError.ErrorType.ERROR).toList();
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
        return runPjassEach(Utils.getResourceFile("common.j"), Utils.getResourceFile("blizzard.j"), files);
    }

    /** If common.j or blizzard.j is not reported as parsed, every file fails with that report. */
    public static List<Result> runPjassEach(String commonJPath, String blizzardJPath, List<File> files) {
        File commonJ = new File(commonJPath);
        File blizzardJ = new File(blizzardJPath);
        List<File> reported = new ArrayList<>(Arrays.asList(commonJ, blizzardJ));
        reported.addAll(files);
        List<String> paths = new ArrayList<>();
        paths.add("--each");
        for (File file : files) {
            paths.add(file.getPath());
        }
        List<String> args = command(commonJ.getPath(), blizzardJ.getPath(), paths);
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
        List<Result> results = resultsPerFile(reported, output.toString());
        for (Result base : results.subList(0, 2)) {
            if (!base.isOk()) {
                return failed(files, base.getMessage());
            }
        }
        return results.subList(2, results.size());
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
                    byPath.put(path, checkedResult(file, ok ? 0 : 1, messages.toString() + line));
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
            return checkedResult(outputFile, exitValue, output.toString());
        } catch (IOException e) {
            WLogger.severe("Could not run pjass:");
            WLogger.severe(e);
            return new Result(outputFile, false, "IO Exception");
        } catch (InterruptedException e) {
            return new Result(outputFile, false, "Interrupted");
        }

    }
}
