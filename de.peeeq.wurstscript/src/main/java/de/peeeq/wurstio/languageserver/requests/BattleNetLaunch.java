package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import net.moonlightflower.wc3libs.port.Orient;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** The Battle.net launcher reads Warcraft III launch arguments once, when it starts. */
@SuppressWarnings("StringConcatToTextBlock") // Error Prone 2.50 crashes on this source under javac 27.
public final class BattleNetLaunch {
    private static final String ARGUMENTS = "AdditionalLaunchArguments";
    private static final String LAUNCHER = "Battle.net.exe";
    private static final List<String> TARGETS = List.of("launch.w3x", "launch.w3m", "launch.wgc");
    private static final Pattern TOKEN = Pattern.compile("(?:[^\\s\"]+|\"[^\"]*\")+");
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private final Path config;
    private final Path stageDir;
    private final Path backup;
    private final Path launcher;

    private BattleNetLaunch(Path config, Path stageDir, Path launcher) {
        this.config = config;
        this.stageDir = stageDir;
        this.backup = stageDir.resolve("original-arguments.json");
        this.launcher = launcher;
    }

    static Optional<BattleNetLaunch> find() {
        String appData = System.getenv("APPDATA");
        String localAppData = System.getenv("LOCALAPPDATA");
        if (!Orient.isWindowsSystem() || appData == null || localAppData == null) return Optional.empty();
        Path config = Path.of(appData, "Battle.net", "Battle.net.config");
        Path stageDir = Path.of(localAppData, "wc3-e2e", "battlenet");
        if (!Files.isRegularFile(config)) return Optional.empty();
        return launcherExecutable().map(path -> new BattleNetLaunch(config, stageDir, path));
    }

    private static Optional<Path> launcherExecutable() {
        Optional<Path> running = ProcessHandle.allProcesses().map(BattleNetLaunch::command)
            .flatMap(Optional::stream).filter(path -> named(path, LAUNCHER)).findFirst();
        if (running.isPresent()) return running;
        return Arrays.asList(System.getenv("ProgramFiles(x86)"), System.getenv("ProgramFiles")).stream()
            .filter(java.util.Objects::nonNull).map(dir -> Path.of(dir, "Battle.net", LAUNCHER))
            .filter(Files::isRegularFile).findFirst();
    }

    static boolean isRegisteredGame(Path executable) throws IOException {
        String key = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Warcraft III";
        for (String view : List.of("/reg:32", "/reg:64")) {
            List<String> output = readCommand(new ProcessBuilder("reg.exe", "query", key, "/v", "InstallLocation", view), false);
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

    private static Optional<Path> command(ProcessHandle process) {
        return process.info().command().map(Path::of);
    }

    private static boolean named(Path executable, String image) {
        Path name = executable.getFileName();
        return name != null && name.toString().equalsIgnoreCase(image);
    }

    boolean isRunning() {
        return ProcessHandle.allProcesses().map(BattleNetLaunch::command)
            .flatMap(Optional::stream).anyMatch(path -> named(path, LAUNCHER));
    }

    void launch(Path map, List<String> gameArgs) throws IOException {
        Path target = stageDir.resolve("launch.w3x");
        try (ServerSocket ignored = acquireGate()) {
            Set<Long> existing = mapProcesses(target);
            if (!existing.isEmpty()) {
                throw new IOException("Close the Warcraft III test map that is already open and run the map again.");
            }
            if (!isRunning()) throw new IOException("Start Battle.net, sign in, and run the map again.");
            boolean restarted = configure(gameArgs, target);
            Files.copy(map, target, StandardCopyOption.REPLACE_EXISTING);
            boolean started = false;
            try {
                int attempts = restarted ? 5 : 1;
                for (int attempt = 0; attempt < attempts; attempt++) {
                    start("--exec=launch W3");
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(restarted ? 7 : 20);
                    do {
                        pause(500);
                        for (long pid : mapProcesses(target)) {
                            Optional<ProcessHandle> game = ProcessHandle.of(pid);
                            if (game.isEmpty()) continue;
                            started = true;
                            game.get().onExit().thenRun(() -> removeOnExit(target));
                            return;
                        }
                    } while (System.nanoTime() < deadline);
                }
                throw new IOException("Battle.net did not start the map. Check that Battle.net is signed in and Warcraft III is ready to play.");
            } finally {
                if (!started && mapProcesses(target).isEmpty()) Files.deleteIfExists(target);
            }
        }
    }

    private void removeOnExit(Path target) {
        try (ServerSocket ignored = acquireGate()) {
            if (mapProcesses(target).isEmpty()) Files.deleteIfExists(target);
        } catch (IOException ex) {
            WLogger.warning("Could not remove the staged Battle.net map: " + ex.getMessage());
        }
    }

    private boolean configure(List<String> gameArgs, Path target) throws IOException {
        JsonElement current = currentArguments(readJson(config));
        JsonElement original = originalArguments(current);
        String wanted = launchArguments(asString(original), gameArgs, target);
        if (wanted.equals(asString(current))) return false;

        stop();
        JsonObject data = readJson(config);
        original = originalArguments(currentArguments(data));
        JsonObject saved = new JsonObject();
        saved.add("original", original);
        writeJson(backup, saved);
        warcraftSettings(data).addProperty(ARGUMENTS, launchArguments(asString(original), gameArgs, target));
        writeJson(config, data);
        start();
        return true;
    }

    public static boolean restore() throws IOException {
        Optional<BattleNetLaunch> found = find();
        if (found.isEmpty()) return false;
        BattleNetLaunch launch = found.get();
        try (ServerSocket ignored = launch.acquireGate()) {
            return launch.restoreSettings();
        }
    }

    private boolean restoreSettings() throws IOException {
        if (!Files.isRegularFile(backup)) return false;
        if (isManaged(currentArguments(readJson(config)))) {
            boolean running = isRunning();
            stop();
            JsonObject data = readJson(config);
            if (isManaged(currentArguments(data))) {
                JsonElement original = readJson(backup).get("original");
                if (original == null || original.isJsonNull()) warcraftSettings(data).remove(ARGUMENTS);
                else warcraftSettings(data).add(ARGUMENTS, original);
                writeJson(config, data);
            }
            if (running) start();
        }
        Files.delete(backup);
        for (String file : TARGETS) {
            Path target = stageDir.resolve(file);
            if (Files.exists(target) && mapProcesses(target).isEmpty()) Files.delete(target);
        }
        return true;
    }

    private JsonElement originalArguments(JsonElement current) throws IOException {
        if (!isManaged(current)) return current;
        JsonElement saved = readJson(backup).get("original");
        return saved == null ? JsonNull.INSTANCE : saved;
    }

    private boolean isManaged(JsonElement current) {
        String value = asString(current);
        List<String> tokens = TOKEN.matcher(value).results().map(match -> match.group()).toList();
        for (int i = 0; i + 1 < tokens.size(); i++) {
            if (!tokens.get(i).equalsIgnoreCase("-loadfile")) continue;
            String file = tokens.get(i + 1).replace("\"", "");
            return TARGETS.stream().anyMatch(name -> stageDir.resolve(name).toString().equalsIgnoreCase(file));
        }
        return false;
    }

    static String launchArguments(String original, List<String> gameArgs, Path target) {
        List<String> stored = TOKEN.matcher(original).results().map(match -> match.group()).toList();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < stored.size(); i++) {
            String arg = stored.get(i);
            switch (arg.toLowerCase(Locale.ROOT)) {
                case "-loadfile", "-windowmode", "-uid" -> {
                    if (i + 1 < stored.size() && !stored.get(i + 1).startsWith("-")) i++;
                }
                case "-editor", "-launch", "-nowfpause" -> { }
                default -> result.append(arg).append(' ');
            }
        }
        for (String arg : gameArgs) {
            switch (arg.toLowerCase(Locale.ROOT)) {
                case "-editor", "-launch" -> { }
                case "-loadfile", "-uid" -> throw new IllegalArgumentException("Battle.net supplies -uid and Wurst supplies -loadfile.");
                default -> result.append(arg).append(' ');
            }
        }
        return result.append("-loadfile \"").append(target).append('"').toString();
    }

    private static String asString(JsonElement value) {
        return value.isJsonNull() ? "" : value.getAsString();
    }

    private static JsonElement currentArguments(JsonObject data) {
        JsonObject games = data.getAsJsonObject("Games");
        JsonObject wc3 = games == null ? null : games.getAsJsonObject("w3");
        return wc3 == null || !wc3.has(ARGUMENTS) ? JsonNull.INSTANCE : wc3.get(ARGUMENTS);
    }

    private static JsonObject warcraftSettings(JsonObject data) {
        if (!data.has("Games")) data.add("Games", new JsonObject());
        JsonObject games = data.getAsJsonObject("Games");
        if (!games.has("w3")) games.add("w3", new JsonObject());
        return games.getAsJsonObject("w3");
    }

    private static JsonObject readJson(Path path) throws IOException {
        try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (RuntimeException ex) {
            throw new IOException("Could not parse " + path.getFileName(), ex);
        }
    }

    private static void writeJson(Path path, JsonObject data) throws IOException {
        Path temp = Files.createTempFile(path.getParent(), "wurst-battlenet-", ".tmp");
        try {
            Files.writeString(temp, JSON.toJson(data));
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    // The Node harness uses the same localhost socket while it owns the launcher settings or staged map.
    static int gatePort(Path config) {
        try {
            String key = config.toAbsolutePath().normalize().toString().toLowerCase(Locale.ROOT);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return 33000 + Integer.remainderUnsigned(ByteBuffer.wrap(hash).getInt(), 10000);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private ServerSocket acquireGate() throws IOException {
        Files.createDirectories(stageDir);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (true) {
            ServerSocket socket = new ServerSocket();
            try {
                socket.bind(new InetSocketAddress("127.0.0.1", gatePort(config)));
                return socket;
            } catch (BindException ex) {
                socket.close();
                if (System.nanoTime() >= deadline) throw new IOException("Another Warcraft III test is using Battle.net.", ex);
                pause(250);
            } catch (IOException ex) {
                socket.close();
                throw ex;
            }
        }
    }

    private void start(String... args) throws IOException {
        List<String> command = new java.util.ArrayList<>();
        command.add(launcher.toString());
        command.addAll(List.of(args));
        new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD).start();
    }

    private void stop() throws IOException {
        if (!isRunning()) return;
        start("--exec=shutdown");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (isRunning()) {
            if (System.nanoTime() >= deadline) throw new IOException("Battle.net did not shut down. Its settings were not edited.");
            pause(500);
        }
    }

    private static Set<Long> mapProcesses(Path target) throws IOException {
        String script = """
            $ErrorActionPreference = 'Stop'
            $map = [regex]::Escape($env:WURST_BATTLENET_MAP)
            Get-CimInstance Win32_Process -Filter "Name='Warcraft III.exe'" | Where-Object {
                $_.CommandLine -match ('(?:^|\\s)-loadfile\\s+(?:"' + $map + '"|' + $map + ')(?=\\s|$)')
            } | ForEach-Object { $_.ProcessId }
            """;
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        ProcessBuilder powershell = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encoded);
        powershell.environment().put("WURST_BATTLENET_MAP", target.toString());
        Set<Long> pids = new HashSet<>();
        for (String line : readCommand(powershell, true)) {
            if (line.matches("\\d+")) pids.add(Long.parseLong(line));
        }
        return pids;
    }

    private static List<String> readCommand(ProcessBuilder builder, boolean check) throws IOException {
        Process process = builder.redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("Windows process query timed out.");
            }
            if (check && process.exitValue() != 0) throw new IOException("Could not inspect running Warcraft III clients.");
            try (var output = process.inputReader()) {
                return output.lines().toList();
            }
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new java.util.concurrent.CancellationException();
        }
    }

    private static void pause(long millis) {
        CompilationCancellation.check();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new java.util.concurrent.CancellationException();
        }
    }
}
