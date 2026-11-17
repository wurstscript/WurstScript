package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.WLogger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Starts processes on Windows in the user's own registry view.
 *
 * <p>A child process sees the registry its parent sees. Under a packaged (MSIX) app, such as the Claude desktop app or a
 * VS Code started from a terminal inside it, that is the package's private copy of {@code HKCU}: what such a process
 * writes there no other process reads, and once it has written a value it no longer reads the user's. Battle.net, the
 * sign-in renewal and the game pass the sign-in between them through {@code HKCU} ({@link BattleNetSignIn}), so each of
 * them started from inside such an app would keep it where the rest of the machine never sees it. A process created
 * through WMI ({@code Win32_Process.Create}) starts in the user's own context instead, in the same session and on the
 * same desktop.
 */
final class OutsideStart {
    /** The {@code SW_} value WMI passes to the new process's first window. */
    enum Show {
        HIDDEN(0), NORMAL(1), MINIMIZED(7);

        final int value;

        Show(int value) {
            this.value = value;
        }
    }

    private static final long WMI_TIMEOUT_SECONDS = 30;
    private static final Pattern CREATED = Pattern.compile("^created (\\d+) (\\d+)\\s*$", Pattern.MULTILINE);
    // The command line, window and directory travel in the environment, so nothing in them needs PowerShell quoting.
    private static final String CREATE_SCRIPT = String.join("; ",
        "$ErrorActionPreference = 'Stop'",
        "[Console]::OutputEncoding = [Text.Encoding]::UTF8",
        "$startup = New-CimInstance -ClassName Win32_ProcessStartup -ClientOnly -Property @{ ShowWindow = [uint16]$env:WURST_OUTSIDE_SHOW }",
        "$arguments = @{ CommandLine = $env:WURST_OUTSIDE_COMMAND; ProcessStartupInformation = $startup }",
        "if ($env:WURST_OUTSIDE_CWD) { $arguments.CurrentDirectory = $env:WURST_OUTSIDE_CWD }",
        "$created = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments $arguments",
        "'created {0} {1}' -f $created.ReturnValue, $created.ProcessId");

    private OutsideStart() {
    }

    /** One argument quoted for a Windows command line, as CommandLineToArgvW and the C runtime split it. */
    static String quoteWindowsArg(String arg) {
        if (!arg.isEmpty() && arg.chars().noneMatch(c -> c == '"' || Character.isWhitespace(c))) return arg;
        StringBuilder quoted = new StringBuilder("\"");
        int backslashes = 0;
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            if (c == '\\') {
                backslashes++;
                continue;
            }
            quoted.append("\\".repeat(c == '"' ? backslashes * 2 + 1 : backslashes)).append(c);
            backslashes = 0;
        }
        return quoted.append("\\".repeat(backslashes * 2)).append('"').toString();
    }

    static String commandLine(List<String> command) {
        return command.stream().map(OutsideStart::quoteWindowsArg).collect(Collectors.joining(" "));
    }

    /**
     * Starts {@code command} through WMI, in the user's own registry view, and returns its pid. Throws when WMI cannot
     * create it.
     *
     * @param directory the new process's working directory; null for WMI's default
     */
    static long start(List<String> command, @Nullable Path directory, Show show) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command",
            CREATE_SCRIPT).redirectErrorStream(true);
        builder.environment().put("WURST_OUTSIDE_COMMAND", commandLine(command));
        builder.environment().put("WURST_OUTSIDE_SHOW", String.valueOf(show.value));
        builder.environment().put("WURST_OUTSIDE_CWD", directory == null ? "" : directory.toString());
        Process process = builder.start();
        String output;
        try {
            process.getOutputStream().close();
            // Read while it runs: an error record could fill the pipe and stall PowerShell until the timeout.
            CompletableFuture<String> read = CompletableFuture.supplyAsync(() -> {
                try (var in = process.getInputStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    return "";
                }
            }, runnable -> Thread.ofVirtual().name("WMI process start").start(runnable));
            if (!process.waitFor(WMI_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IOException("WMI did not answer within " + WMI_TIMEOUT_SECONDS + " s");
            }
            output = read.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CancellationException();
        } catch (ExecutionException | TimeoutException ex) {
            throw new IOException("WMI's answer could not be read", ex);
        } finally {
            process.destroyForcibly();
        }
        String name = String.valueOf(Path.of(command.get(0)).getFileName());
        Matcher created = CREATED.matcher(output);
        if (!created.find()) {
            String first = output.strip().lines().findFirst().orElse("no answer");
            throw new IOException("WMI could not start " + name + ": " + first);
        }
        if (!created.group(1).equals("0")) {
            throw new IOException("WMI could not start " + name + " (Win32_Process.Create returned " + created.group(1) + ")");
        }
        return Long.parseLong(created.group(2));
    }

    /**
     * {@link #start} if WMI can, else a direct start from this process (whose registry view the process then shares).
     * Returns the pid.
     */
    static long startOrHere(List<String> command, @Nullable Path directory, Show show) throws IOException {
        try {
            return start(command, directory, show);
        } catch (IOException ex) {
            WLogger.warning("Starting " + Path.of(command.get(0)).getFileName() + " from Wurst instead: " + ex.getMessage());
            ProcessBuilder builder = new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD);
            if (directory != null) builder.directory(directory.toFile());
            return builder.start().pid();
        }
    }
}
