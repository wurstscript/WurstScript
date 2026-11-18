package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import net.moonlightflower.wc3libs.port.Orient;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renews the sign-in that Warcraft III 3.x uses for {@code -editor} launches, from the Battle.net desktop app.
 *
 * <p>A client started with {@code -editor} signs in with {@code EDITOR_TOKEN}
 * ({@code HKCU\Software\Blizzard Entertainment\Warcraft III}) and replaces it with a fresh one each time it signs in.
 * Once that token lapses (overnight is enough) every such launch stops at the in-game login panel. The app's own session
 * does not reach it: a client the app starts with {@code -loadfile} shows the login panel too.
 *
 * <p>Just before the app starts the game it writes a one-time {@code WEB_TOKEN}
 * ({@code ...\Battle.net\Launch Options\W3}). Stored as {@code EDITOR_TOKEN}, that token signs the next {@code -editor}
 * client in. {@link #renew()} runs {@value #TOKEN_SCRIPT}, which asks the app to start the game, closes that client
 * before it spends the token, and stores the token. The map is then launched directly, as without the app.
 *
 * <p>The app, the script and the map's client hand the sign-in to each other through {@code HKCU}, so all three must
 * see the same registry. A process started from a packaged (MSIX) app, such as the Claude desktop app or an editor
 * started from inside one, reads and writes {@code HKCU} through the package's private copy, and so does everything it
 * starts. The script and the client therefore start through WMI, in the user's own registry view
 * ({@link OutsideStart}), whatever view Wurst has. A Battle.net that was itself started from inside such an app writes
 * its token where nothing else reads it; the script then sees the app start the game but no new token, and the user is
 * told to start Battle.net from the Start menu.
 *
 * <p>Every tool that renews this sign-in holds the same localhost port as a lock from the renewal until the launched
 * client has signed in.
 */
@SuppressWarnings("StringConcatToTextBlock") // Error Prone 2.50 crashes on this source under javac 27.
public final class BattleNetSignIn {
    static final String GAME_KEY = "HKCU\\Software\\Blizzard Entertainment\\Warcraft III";
    private static final String LAUNCHER = "Battle.net.exe";
    private static final String GAME = "Warcraft III.exe";
    private static final long TIMEOUT_SECONDS = 90;
    /** How long past its own timeout the renewal script may take to start and to write its result. */
    private static final long SCRIPT_EXIT_GRACE_SECONDS = 30;
    /** How long the renewal script may take to exit, and close its output file, after writing its result. */
    private static final long OUTPUT_RELEASE_SECONDS = 5;
    /** How long the lock waits for the launched client to sign in; a client that takes longer has its own problems. */
    static final long SIGN_IN_TIMEOUT_SECONDS = 120;
    /** How long a canceled renewal keeps closing the client it asked the app for, which may still be on its way. */
    static final long LATE_GAME_SECONDS = 30;
    /** Another holder renews (up to {@value #TIMEOUT_SECONDS} s) and waits for its client to sign in; outwait both. */
    static final long GATE_WAIT_SECONDS = TIMEOUT_SECONDS + SIGN_IN_TIMEOUT_SECONDS + 30;

    /**
     * Another tool has held the renewal lock for longer than a renewal and a sign-in take. Not a reason to launch
     * without it: that launch could spend the token the other tool's client has not read yet.
     */
    static final class LockBusyException extends IOException {
        LockBusyException(String message) {
            super(message);
        }
    }

    /** What Run Map calls to renew; see {@link RunMap#renewOrRefuse}. */
    @FunctionalInterface
    interface Renewer {
        Renewal renew(Runnable onWait) throws IOException;
    }
    private static final Pattern REG_BINARY = Pattern.compile("^\\s*(\\S+)\\s+REG_BINARY\\s+([0-9A-Fa-f]*)\\s*$", Pattern.MULTILINE);

    private final Path config;
    private final Path gateDir;
    private final Path launcher;

    /** A renewal for {@code config} whose launcher is never started; for tests that only touch the lock. */
    static BattleNetSignIn forTest(Path config, Path gateDir) {
        return new BattleNetSignIn(config, gateDir, Path.of("Battle.net.exe"));
    }

    private BattleNetSignIn(Path config, Path gateDir, Path launcher) {
        this.config = config;
        this.gateDir = gateDir;
        this.launcher = launcher;
    }

    static Optional<BattleNetSignIn> find() {
        String appData = System.getenv("APPDATA");
        String localAppData = System.getenv("LOCALAPPDATA");
        if (!Orient.isWindowsSystem() || appData == null || localAppData == null) return Optional.empty();
        Path config = Path.of(appData, "Battle.net", "Battle.net.config");
        Path gateDir = Path.of(localAppData, "wc3-e2e", "battlenet");
        return launcherExecutable().map(path -> new BattleNetSignIn(config, gateDir, path));
    }

    private static Optional<Path> launcherExecutable() {
        Optional<Path> running = ProcessHandle.allProcesses().map(BattleNetSignIn::command)
            .flatMap(Optional::stream).filter(path -> named(path, LAUNCHER)).findFirst();
        if (running.isPresent()) return running;
        return Arrays.asList(System.getenv("ProgramFiles(x86)"), System.getenv("ProgramFiles")).stream()
            .filter(Objects::nonNull).map(dir -> Path.of(dir, "Battle.net", LAUNCHER))
            .filter(Files::isRegularFile).findFirst();
    }

    /** Whether {@code executable} belongs to the install Battle.net manages; the token is only valid there. */
    static boolean isRegisteredGame(Path executable) throws IOException {
        String key = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Warcraft III";
        for (String view : List.of("/reg:32", "/reg:64")) {
            List<String> output = readCommand(new ProcessBuilder("reg.exe", "query", key, "/v", "InstallLocation", view));
            for (String line : output) {
                String[] fields = line.strip().split("\\s{2,}", 3);
                if (fields.length == 3 && fields[0].equalsIgnoreCase("InstallLocation")
                    && executable.toAbsolutePath().normalize().startsWith(Path.of(fields[2]).toAbsolutePath().normalize())) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isRunning() {
        return ProcessHandle.allProcesses().map(BattleNetSignIn::command)
            .flatMap(Optional::stream).anyMatch(path -> named(path, LAUNCHER));
    }

    /**
     * Stores a token from the app as {@code EDITOR_TOKEN}. Throws, with a message for the user, when the app cannot
     * provide one: it is closed, signed out, updating, running a client of its own, or keeping its registry in a
     * packaged app's private copy.
     *
     * @return the renewal, which holds the Battle.net lock; launch the map, then call
     *     {@link Renewal#releaseWhenSignedIn()}, or {@link Renewal#close()} if the launch fails
     */
    Renewal renew() throws IOException {
        return renew(GATE_WAIT_SECONDS, () -> { });
    }

    /** @param onWait called once if another launch holds the lock and this one has to wait for it */
    Renewal renew(Runnable onWait) throws IOException {
        return renew(GATE_WAIT_SECONDS, onWait);
    }

    Renewal renew(long gateWaitSeconds, Runnable onWait) throws IOException {
        ServerSocket gate = acquireGate(gateWaitSeconds, onWait);
        Set<Long> seen = new HashSet<>();
        boolean[] asked = {false};
        try {
            String token = renewLocked(seen, asked);
            return signInCheck(gate, token);
        } catch (IOException | RuntimeException | Error ex) {
            if (asked[0]) {
                closeLateGames(gate, seen, LATE_GAME_SECONDS);
            } else {
                gate.close();
            }
            throw ex;
        }
    }

    /**
     * After a renewal that asked the app for a client and then failed or was canceled, that client may still open.
     * Keeps the lock and closes such a client for {@code seconds} ({@value #LATE_GAME_SECONDS} in use), sparing every
     * game in {@code seen}. Does not block.
     */
    static void closeLateGames(ServerSocket gate, Set<Long> seen, long seconds) {
        Thread closer = new Thread(() -> {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
            try {
                while (System.nanoTime() < deadline) {
                    for (ProcessHandle game : games()) {
                        if (seen.add(game.pid()) && startedByLauncher(game)) game.destroyForcibly();
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException | RuntimeException ex) {
                WLogger.warning("Stopped watching for a Warcraft III client after a canceled renewal: " + ex.getMessage());
            } finally {
                try {
                    gate.close();
                } catch (IOException ex) {
                    WLogger.warning("Could not release the Battle.net lock: " + ex.getMessage());
                }
            }
        }, "Battle.net canceled renewal");
        closer.setDaemon(true);
        closer.start();
    }

    /**
     * The Battle.net lock, held from the renewal until the launched client has signed in. Released earlier, another
     * tool's renewal could replace {@code EDITOR_TOKEN} before this client read it, leaving two clients one token.
     */
    static final class Renewal implements AutoCloseable {
        /** Whether the launched client has replaced the renewed {@code EDITOR_TOKEN} yet. */
        @FunctionalInterface
        interface SignedIn {
            /** @throws IOException when that can no longer be told */
            boolean check() throws IOException;
        }

        private final ServerSocket _gate;
        private final SignedIn _signedIn;
        private final Runnable _onClose;
        private final long _pollMillis;

        Renewal(ServerSocket gate, SignedIn signedIn, Runnable onClose, long pollMillis) {
            _gate = gate;
            _signedIn = signedIn;
            _onClose = onClose;
            _pollMillis = pollMillis;
        }

        /**
         * Releases the lock once the client has replaced {@code EDITOR_TOKEN}, which it does as it signs in, or after
         * {@value #SIGN_IN_TIMEOUT_SECONDS} seconds. Does not block.
         *
         * @return the thread that waits, for tests
         */
        Thread releaseWhenSignedIn() {
            Thread waiter = new Thread(() -> {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SIGN_IN_TIMEOUT_SECONDS);
                try {
                    while (System.nanoTime() < deadline && !_signedIn.check()) {
                        Thread.sleep(_pollMillis);
                    }
                } catch (IOException | InterruptedException | RuntimeException ex) {
                    WLogger.warning("Stopped waiting for the Warcraft III sign-in: " + ex.getMessage());
                } finally {
                    close();
                }
            }, "Battle.net sign-in lock");
            waiter.setDaemon(true);
            waiter.start();
            return waiter;
        }

        @Override
        public void close() {
            try {
                _gate.close();
            } catch (IOException ex) {
                WLogger.warning("Could not release the Battle.net lock: " + ex.getMessage());
            }
            _onClose.run();
        }
    }

    /**
     * {@link #SIGNIN_SCRIPT} running in the user's own registry view, where the client replaces {@code EDITOR_TOKEN}
     * as it signs in. Read from Wurst's own view, the token may never change: a packaged app's private copy keeps the
     * old one.
     */
    static final class SignInWatcher {
        private final @Nullable ProcessHandle _process;
        private final Path _output;

        SignInWatcher(@Nullable ProcessHandle process, Path output) {
            _process = process;
            _output = output;
        }

        private List<String> lines() {
            return parseTokenScriptOutput(completeLines(readOutput(_output))).lines();
        }

        boolean ready() {
            return lines().contains("ready");
        }

        /** True once the client has signed in; throws once the watcher has ended without seeing it. */
        boolean signedIn() throws IOException {
            boolean alive = _process != null && _process.isAlive();
            // Read after the check, so a watcher that saw the sign-in and ended in between is not missed.
            List<String> lines = lines();
            if (lines.contains("signed-in")) return true;
            if (lines.contains("timeout") || !alive) {
                throw new IOException("the sign-in watcher ended (" + (lines.isEmpty() ? "no output" : lines.getLast()) + ")");
            }
            return false;
        }

        void stop() {
            if (_process != null) {
                _process.destroyForcibly();
                try {
                    _process.onExit().get(OUTPUT_RELEASE_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | TimeoutException ex) {
                    // still running: the delete below falls back to deleteOnExit
                }
            }
            try {
                Files.deleteIfExists(_output);
            } catch (IOException ex) {
                _output.toFile().deleteOnExit();
            }
        }
    }

    /**
     * The held lock, with how it tells that the launched client has signed in: from a {@link SignInWatcher} started
     * outside Wurst's registry view, or, if that cannot start, by reading {@code EDITOR_TOKEN} from here. Called after
     * the renewal and before the launch, so the token the watcher starts from is the renewed one.
     *
     * @param token the renewed {@code EDITOR_TOKEN} as Wurst reads it, for the fallback; null when unreadable
     */
    private static Renewal signInCheck(ServerSocket gate, @Nullable String token) {
        SignInWatcher watcher = null;
        try {
            Path output = tempDir().resolve("wurst-battlenet-signin-" + UUID.randomUUID() + ".log");
            List<String> command = List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy",
                "Bypass", "-Command", "& " + powerShellQuote(scriptCopy(SIGNIN_SCRIPT, tempDir()).toString())
                    + " -TimeoutSeconds " + (SIGN_IN_TIMEOUT_SECONDS + 10) + " *> " + powerShellQuote(output.toString()));
            long pid = OutsideStart.startOrHere(command, null, OutsideStart.Show.HIDDEN);
            watcher = new SignInWatcher(ProcessHandle.of(pid).orElse(null), output);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SIGNIN_READY_SECONDS);
            while (!watcher.ready() && System.nanoTime() < deadline) pause(50);
            if (watcher.ready()) {
                SignInWatcher started = watcher;
                watcher = null;
                return new Renewal(gate, started::signedIn, started::stop, 250);
            }
            WLogger.warning("The Warcraft III sign-in watcher did not start; watching from Wurst instead.");
        } catch (IOException ex) {
            WLogger.warning("Watching the Warcraft III sign-in from Wurst instead: " + ex.getMessage());
        } finally {
            if (watcher != null) watcher.stop();
        }
        return new Renewal(gate, tokenReplaced(token, () -> readBinary(GAME_KEY, "EDITOR_TOKEN")), () -> { }, 500);
    }

    /** Reads {@code EDITOR_TOKEN}; empty when it is not set. */
    @FunctionalInterface
    interface TokenRead {
        Optional<String> read() throws IOException;
    }

    /**
     * The fallback sign-in check: whether the client has replaced the renewed token. If the renewed token could not
     * be read, the sign-in cannot be told, and the lock is held until the timeout rather than released at once.
     */
    static Renewal.SignedIn tokenReplaced(@Nullable String renewed, TokenRead current) {
        if (renewed == null) return () -> false;
        return () -> current.read().filter(renewed::equals).isEmpty();
    }

    /**
     * @param seen collects every client that is not this renewal's to close: those running before it, and those
     *     already handled
     * @param asked set once the app may have been asked for a client
     * @return the renewed {@code EDITOR_TOKEN} as Wurst reads it, to see the client replace it; null if unreadable
     */
    private @Nullable String renewLocked(Set<Long> seen, boolean[] asked) throws IOException {
        if (!isRunning()) throw new IOException("Battle.net is not running.");
        for (ProcessHandle game : games()) {
            if (startedByLauncher(game)) throw new IOException(APP_GAME_RUNNING);
            seen.add(game.pid());
        }
        Path script = scriptCopy(TOKEN_SCRIPT, tempDir());
        // Asked to start the game, the app creates a window in front, and shows it again as the map's client signs
        // in; the watcher hides it as it is created.
        watchLauncherWindow();
        long start = System.nanoTime();
        asked[0] = true;
        TokenScriptResult result = runTokenScript(script);
        String problem = problem(result);
        if (problem != null) {
            // The script found a client the app started after the check above, and asked for nothing. That client is
            // the user's, so no late client is closed.
            if ("app-game-running".equals(result.outcome())) asked[0] = false;
            WLogger.info("The Battle.net sign-in renewal ended with: " + String.join(" | ", result.lines()));
            throw new IOException(problem);
        }
        WLogger.info("Renewed the Warcraft III sign-in from Battle.net in "
            + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        try {
            return readBinary(GAME_KEY, "EDITOR_TOKEN").orElse(null);
        } catch (IOException ex) {
            WLogger.warning("Could not read the renewed Warcraft III sign-in: " + ex.getMessage());
            return null;
        }
    }

    static final String APP_GAME_RUNNING = "Close the Warcraft III client started from Battle.net, then run the map again.";

    /** What {@link #TOKEN_SCRIPT} printed: its outcome, null until its {@code result} line, and every line. */
    record TokenScriptResult(@Nullable String outcome, List<String> lines) {
    }

    private static final Pattern RESULT = Pattern.compile("result (\\S+)");

    static TokenScriptResult parseTokenScriptOutput(String output) {
        List<String> lines = output.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
        String outcome = lines.stream().map(RESULT::matcher).filter(Matcher::matches)
            .map(matcher -> matcher.group(1)).findFirst().orElse(null);
        return new TokenScriptResult(outcome, lines);
    }

    /**
     * The complete lines of a script's redirected output: Windows PowerShell writes UTF-16 with a byte order mark, and
     * a line still being written could read as a different result.
     */
    static String completeLines(byte[] output) {
        boolean utf16 = output.length >= 2 && (output[0] & 0xff) == 0xff && (output[1] & 0xff) == 0xfe;
        String text = utf16 ? new String(output, 2, output.length - 2, StandardCharsets.UTF_16LE)
            : new String(output, StandardCharsets.UTF_8);
        return text.substring(0, text.lastIndexOf('\n') + 1);
    }

    /** The message for the user when the renewal did not succeed; null when it did. */
    static @Nullable String problem(TokenScriptResult result) {
        return switch (Objects.requireNonNullElse(result.outcome(), "")) {
            case "refreshed" -> null;
            case "no-token" -> "Battle.net did not start Warcraft III. Check that it is signed in and the game is ready to play.";
            case "app-game-running" -> APP_GAME_RUNNING;
            case "token-not-visible" -> "Battle.net started Warcraft III, but wrote its sign-in where other programs cannot"
                + " read it, probably because Battle.net was started from a packaged app (such as the Claude desktop app)."
                + " Quit Battle.net from its tray icon and start it from the Start menu.";
            default -> {
                List<String> lines = result.lines();
                yield "The Battle.net sign-in renewal failed: " + (lines.isEmpty() ? "it printed nothing"
                    : String.join(" | ", lines.subList(Math.max(0, lines.size() - 3), lines.size()))) + ".";
            }
        };
    }

    /** A PowerShell single-quoted string literal. PowerShell also reads the typographic single quotes as quotes. */
    static String powerShellQuote(String text) {
        return "'" + text.replaceAll("['\\u2018\\u2019\\u201A\\u201B]", "$0$0") + "'";
    }

    /**
     * The command that runs {@code script} and redirects everything it prints to {@code output}. Started outside, the
     * script's output cannot come back through a pipe, so it goes to a file.
     */
    static List<String> tokenScriptCommand(Path script, Path launcher, long timeoutSeconds, Path output) {
        return List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command",
            "& " + powerShellQuote(script.toString()) + " -BattleNetExe " + powerShellQuote(launcher.toString())
                + " -TimeoutSeconds " + timeoutSeconds + " *> " + powerShellQuote(output.toString()));
    }

    /**
     * Runs {@link #TOKEN_SCRIPT} hidden in the user's own registry view, where the app writes {@code WEB_TOKEN} and the
     * client reads {@code EDITOR_TOKEN}, whatever view Wurst has. Only if WMI cannot start it does it run from here.
     */
    private TokenScriptResult runTokenScript(Path script) throws IOException {
        Path output = tempDir().resolve("wurst-battlenet-renewal-" + UUID.randomUUID() + ".log");
        try {
            long pid = OutsideStart.startOrHere(tokenScriptCommand(script, launcher, TIMEOUT_SECONDS, output), null,
                OutsideStart.Show.HIDDEN);
            @Nullable ProcessHandle handle = ProcessHandle.of(pid).orElse(null);
            TokenScriptResult result = awaitTokenScript(handle, output, TIMEOUT_SECONDS + SCRIPT_EXIT_GRACE_SECONDS);
            // The script ends right after its result line; let it close the output file before that is removed.
            if (handle != null) {
                try {
                    handle.onExit().get(OUTPUT_RELEASE_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | TimeoutException ex) {
                    // still running: the delete below falls back to deleteOnExit
                }
            }
            return result;
        } finally {
            try {
                Files.deleteIfExists(output);
            } catch (IOException ex) {
                output.toFile().deleteOnExit();
            }
        }
    }

    /**
     * Waits for the script's result line, or for the script to end. Ends the script if it outlives
     * {@code timeoutSeconds} or the run is canceled; a canceled renewal then closes the client the script may still
     * have coming ({@link #closeLateGames}).
     *
     * @param script null when it has ended already
     */
    static TokenScriptResult awaitTokenScript(@Nullable ProcessHandle script, Path output, long timeoutSeconds) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        boolean done = false;
        try {
            while (true) {
                // Read after the check, so a script that wrote its result and ended in between is not missed.
                boolean running = script != null && script.isAlive();
                TokenScriptResult result = parseTokenScriptOutput(completeLines(readOutput(output)));
                if (result.outcome() != null || !running) {
                    done = true;
                    return result;
                }
                if (System.nanoTime() >= deadline) {
                    List<String> lines = new ArrayList<>(result.lines());
                    lines.add("the renewal script did not finish");
                    return new TokenScriptResult(null, lines);
                }
                checkCancellation();
                pause(250);
            }
        } finally {
            if (!done && script != null) script.destroyForcibly();
        }
    }

    private static byte[] readOutput(Path output) {
        try {
            return Files.readAllBytes(output);
        } catch (IOException ex) {
            return new byte[0]; // not created yet
        }
    }

    private static Path tempDir() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    /** The renewal script, run in the user's own registry view. */
    static final String TOKEN_SCRIPT = "battlenet-token.ps1";
    /** The sign-in watcher, run in the user's own registry view ({@link SignInWatcher}). */
    static final String SIGNIN_SCRIPT = "battlenet-signin.ps1";
    /** How long the sign-in watcher may take to start and read the renewed token before the game is launched. */
    private static final long SIGNIN_READY_SECONDS = 15;
    /** The watcher that keeps the app's window out of sight during the renewal. */
    static final String WINDOW_SCRIPT = "battlenet-window.ps1";
    /** The watcher's limit without a client: every request the renewal may send, then a launch and a slow sign-in. */
    static final long WINDOW_WATCH_SECONDS = TIMEOUT_SECONDS + 150;
    private static final long WINDOW_READY_SECONDS = 3;

    /**
     * A copy of the resource {@code script} in {@code dir}, named by its content (a file, not -EncodedCommand: encoded,
     * the window watcher is close to Windows' 32,767-character command line limit), so a Wurst update never runs an
     * older copy.
     */
    static Path scriptCopy(String script, Path dir) throws IOException {
        try (var in = BattleNetSignIn.class.getClassLoader().getResourceAsStream(script)) {
            if (in == null) throw new IOException(script + " is missing from Wurst");
            byte[] content = in.readAllBytes();
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content), 0, 6);
            String name = script.substring(0, script.length() - ".ps1".length());
            Path file = dir.resolve("wurst-" + name + "-" + hash + ".ps1");
            if (!Files.isRegularFile(file) || !Arrays.equals(Files.readAllBytes(file), content)) Files.write(file, content);
            return file;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * The command that runs {@link #WINDOW_SCRIPT} with {@code arguments}, from a copy in {@code dir}
     * ({@link #scriptCopy}). Null when the script cannot be provided.
     */
    static List<String> windowWatcherCommand(List<String> arguments, Path dir) {
        try {
            Path file = scriptCopy(WINDOW_SCRIPT, dir);
            List<String> command = new ArrayList<>(List.of("powershell.exe", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass", "-File", file.toString()));
            command.addAll(arguments);
            return command;
        } catch (IOException ex) {
            WLogger.warning("Could not prepare the Battle.net window watcher: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Starts the app window watcher and waits until it has noted the windows already open (at most
     * {@value #WINDOW_READY_SECONDS} s). It then runs on its own: it ends a minute after the map's client appears, past
     * its sign-in, or after {@link #WINDOW_WATCH_SECONDS}. Best effort: a watcher that does not start only means the
     * app's window may show. Run Map focuses its client itself ({@link BattleNetLaunchWindows}), so the watcher does not.
     */
    private static void watchLauncherWindow() {
        List<String> command = windowWatcherCommand(List.of("-Seconds", String.valueOf(WINDOW_WATCH_SECONDS)),
            Path.of(System.getProperty("java.io.tmpdir")));
        if (command == null) return;
        Process process;
        try {
            process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        } catch (IOException ex) {
            WLogger.warning("Could not start the Battle.net window watcher: " + ex.getMessage());
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            // The watcher prints nothing after "ready", so its output can be let go then.
            try (var output = process.inputReader()) {
                String line;
                while ((line = output.readLine()) != null) {
                    if (line.strip().equals("ready")) break;
                }
            } catch (IOException ignored) {
                // A watcher that stopped early only means the app's window may show.
            } finally {
                ready.countDown();
            }
        }, "Battle.net window watcher");
        reader.setDaemon(true);
        reader.start();
        try {
            ready.await(WINDOW_READY_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static List<ProcessHandle> games() {
        return ProcessHandle.allProcesses()
            .filter(process -> command(process).filter(path -> named(path, GAME)).isPresent()).toList();
    }

    /** A client the app started; anything else is someone else's game and is left alone. */
    private static boolean startedByLauncher(ProcessHandle game) {
        return game.parent().flatMap(BattleNetSignIn::command).filter(path -> named(path, LAUNCHER)).isPresent();
    }

    private static Optional<Path> command(ProcessHandle process) {
        return process.info().command().map(Path::of);
    }

    private static boolean named(Path executable, String image) {
        Path name = executable.getFileName();
        return name != null && name.toString().equalsIgnoreCase(image);
    }

    static Optional<String> parseBinary(List<String> regQueryOutput, String value) {
        Matcher matcher = REG_BINARY.matcher(String.join("\n", regQueryOutput));
        while (matcher.find()) {
            if (matcher.group(1).equalsIgnoreCase(value)) return Optional.of(matcher.group(2).toUpperCase(Locale.ROOT));
        }
        return Optional.empty();
    }

    private static Optional<String> readBinary(String key, String value) throws IOException {
        return parseBinary(readCommand(new ProcessBuilder("reg.exe", "query", key, "/v", value)), value);
    }

    // Every tool that renews this sign-in takes the same localhost port while it does.
    static int gatePort(Path config) {
        try {
            String key = config.toAbsolutePath().normalize().toString().toLowerCase(Locale.ROOT);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return 33000 + Integer.remainderUnsigned(ByteBuffer.wrap(hash).getInt(), 10000);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private ServerSocket acquireGate(long waitSeconds, Runnable onWait) throws IOException {
        Files.createDirectories(gateDir);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(waitSeconds);
        boolean waiting = false;
        while (true) {
            ServerSocket socket = new ServerSocket();
            try {
                // 127.0.0.1 exactly, as every holder binds it; the loopback name could resolve to ::1
                socket.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), gatePort(config)));
                return socket;
            } catch (BindException ex) {
                socket.close();
                if (!waiting) {
                    waiting = true;
                    onWait.run();
                }
                if (System.nanoTime() >= deadline) {
                    LockBusyException busy = new LockBusyException(
                        "Another Warcraft III launch has held the Battle.net sign-in for over " + waitSeconds + " s.");
                    busy.initCause(ex);
                    throw busy;
                }
                pause(250);
            } catch (IOException ex) {
                socket.close();
                throw ex;
            }
        }
    }

    private static void checkCancellation() {
        CompilationCancellation.check();
        if (Thread.currentThread().isInterrupted()) throw new CancellationException("Map command canceled");
    }

    private static List<String> readCommand(ProcessBuilder builder) throws IOException {
        Process process = builder.redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("Windows registry query timed out.");
            }
            try (var output = process.inputReader()) {
                return output.lines().toList();
            }
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new CancellationException();
        }
    }

    private static void pause(long millis) {
        CompilationCancellation.check();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CancellationException();
        }
    }
}
