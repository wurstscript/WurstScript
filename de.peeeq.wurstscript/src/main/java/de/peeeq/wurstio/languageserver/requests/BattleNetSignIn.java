package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import net.moonlightflower.wc3libs.port.Orient;

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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
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
 * client in. {@link #renew()} asks the app to start the game, closes that client before it spends the token, and
 * stores the token. The map is then launched directly, as without the app.
 *
 * <p>The wc3-e2e-harness does the same (docs/BattleNetSignIn.md there). Both hold the same localhost port as a lock
 * from the renewal until the launched client has signed in.
 */
@SuppressWarnings("StringConcatToTextBlock") // Error Prone 2.50 crashes on this source under javac 27.
public final class BattleNetSignIn {
    static final String LAUNCH_KEY = "HKCU\\Software\\Blizzard Entertainment\\Battle.net\\Launch Options\\W3";
    static final String GAME_KEY = "HKCU\\Software\\Blizzard Entertainment\\Warcraft III";
    private static final String LAUNCHER = "Battle.net.exe";
    private static final String GAME = "Warcraft III.exe";
    private static final long TIMEOUT_SECONDS = 90;
    private static final long ASK_AGAIN_SECONDS = 25;
    /** The app writes the token and then starts the game; wait this long for that game so it is closed. */
    private static final long GAME_AFTER_TOKEN_SECONDS = 10;
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
     * provide one: it is closed, signed out, updating, or running a client of its own.
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
            return new Renewal(gate, renewLocked(seen, asked));
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
        private final ServerSocket _gate;
        private final String _token;

        private Renewal(ServerSocket gate, String token) {
            _gate = gate;
            _token = token;
        }

        /**
         * Releases the lock once the client has replaced {@code EDITOR_TOKEN}, which it does as it signs in, or after
         * {@value #SIGN_IN_TIMEOUT_SECONDS} seconds. Does not block.
         */
        void releaseWhenSignedIn() {
            Thread waiter = new Thread(() -> {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(SIGN_IN_TIMEOUT_SECONDS);
                try {
                    while (System.nanoTime() < deadline
                        && readBinary(GAME_KEY, "EDITOR_TOKEN").filter(_token::equals).isPresent()) {
                        Thread.sleep(500);
                    }
                } catch (IOException | InterruptedException | RuntimeException ex) {
                    WLogger.warning("Stopped waiting for the Warcraft III sign-in: " + ex.getMessage());
                } finally {
                    close();
                }
            }, "Battle.net sign-in lock");
            waiter.setDaemon(true);
            waiter.start();
        }

        @Override
        public void close() {
            try {
                _gate.close();
            } catch (IOException ex) {
                WLogger.warning("Could not release the Battle.net lock: " + ex.getMessage());
            }
        }
    }

    /**
     * @param seen collects every client that is not this renewal's to close: those running before it, and those
     *     already handled
     * @param asked set once the app has been asked for a client
     */
    private String renewLocked(Set<Long> seen, boolean[] asked) throws IOException {
        if (!isRunning()) throw new IOException("Battle.net is not running.");
        for (ProcessHandle game : games()) {
            if (startedByLauncher(game)) {
                throw new IOException("Close the Warcraft III client started from Battle.net, then run the map again.");
            }
            seen.add(game.pid());
        }
        Optional<String> before = readBinary(LAUNCH_KEY, "WEB_TOKEN");
        long start = System.nanoTime();
        long askedAt = Long.MIN_VALUE;
        String token = null;
        long tokenAt = 0;
        int closed = 0;
        while (System.nanoTime() - start < TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS)) {
            checkCancellation();
            if (token == null && (askedAt == Long.MIN_VALUE
                || System.nanoTime() - askedAt >= TimeUnit.SECONDS.toNanos(ASK_AGAIN_SECONDS))) {
                askedAt = System.nanoTime();
                asked[0] = true;
                // An app that is still starting ignores the command, so it is repeated while waiting.
                startLauncher("--exec=launch W3");
            }
            for (ProcessHandle game : games()) {
                if (!seen.add(game.pid()) || !startedByLauncher(game)) continue;
                game.destroyForcibly();
                closed++;
            }
            if (token == null) {
                Optional<String> now = readBinary(LAUNCH_KEY, "WEB_TOKEN");
                if (now.isPresent() && !now.equals(before)) {
                    token = now.get();
                    tokenAt = System.nanoTime();
                }
            }
            if (token != null && (closed > 0
                || System.nanoTime() - tokenAt >= TimeUnit.SECONDS.toNanos(GAME_AFTER_TOKEN_SECONDS))) break;
            pause(50);
        }
        if (token == null) {
            throw new IOException("Battle.net did not start Warcraft III. Check that it is signed in and the game is ready to play.");
        }
        writeBinary(GAME_KEY, "EDITOR_TOKEN", token);
        WLogger.info("Renewed the Warcraft III sign-in from Battle.net in "
            + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        return token;
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

    private static void writeBinary(String key, String value, String hex) throws IOException {
        Process process = new ProcessBuilder("reg.exe", "add", key, "/v", value, "/t", "REG_BINARY", "/d", hex, "/f")
            .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            if (!process.waitFor(8, TimeUnit.SECONDS) || process.exitValue() != 0) {
                process.destroyForcibly();
                throw new IOException("Could not store the Warcraft III sign-in.");
            }
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new CancellationException();
        }
    }

    private void startLauncher(String... args) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(launcher.toString());
        command.addAll(List.of(args));
        new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD).start();
    }

    // The Node harness takes the same localhost port while it renews the sign-in.
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
                // 127.0.0.1 exactly, as the harness binds it; the loopback name could resolve to ::1
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
