package de.peeeq.wurstio.languageserver.requests;

import net.moonlightflower.wc3libs.port.Orient;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class BattleNetSignInTest {
    @Test
    public void aRenewalLockHeldElsewhereIsReportedAsBusy() throws Exception {
        if (!Orient.isWindowsSystem()) throw new SkipException("Battle.net paths are Windows paths");
        Path dir = Files.createTempDirectory("wurst-battlenet-lock");
        Path config = dir.resolve("Battle.net.config");
        try (ServerSocket holder = new ServerSocket()) {
            holder.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), BattleNetSignIn.gatePort(config)));
            AtomicBoolean waited = new AtomicBoolean();
            expectThrows(BattleNetSignIn.LockBusyException.class,
                () -> BattleNetSignIn.forTest(config, dir).renew(1, () -> waited.set(true)));
            assertTrue(waited.get(), "the user is told why the run waits");
        }
    }

    @Test
    public void aBusyRenewalLockFailsTheRunInsteadOfLaunchingWithoutIt() {
        // In "auto" an IOException means "launch on the stored token"; that launch could spend another client's token.
        RequestFailedException failure = expectThrows(RequestFailedException.class, () -> RunMap.renewOrRefuse(
            onWait -> { throw new BattleNetSignIn.LockBusyException("Another launch holds it."); }, () -> { }));
        assertTrue(failure.getMessage().contains("Run the map again"), failure.getMessage());
        // Any other renewal failure still reaches the caller's fallback.
        expectThrows(java.io.IOException.class, () -> RunMap.renewOrRefuse(
            onWait -> { throw new java.io.IOException("Battle.net is not running."); }, () -> { }));
    }

    @Test
    public void theWindowWatcherRunsFromACopyNamedByItsContent() throws Exception {
        Path dir = Files.createTempDirectory("wurst-window-watcher");
        List<String> command = BattleNetSignIn.windowWatcherCommand(List.of("-Seconds", "30"), dir);
        assertEquals(command.subList(0, 6), List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File"));
        assertEquals(command.subList(7, command.size()), List.of("-Seconds", "30"));
        Path file = Path.of(command.get(6));
        assertEquals(file.getParent(), dir);
        String script = Files.readString(file);
        assertTrue(script.contains("param([int]$Seconds = 20, [int]$GameSeconds = 60, [switch]$FocusGame, [switch]$Log)"));
        assertTrue(script.contains("\"ready\""), "the renewal waits for this line");
        assertTrue(script.chars().allMatch(c -> c < 128), "Windows PowerShell reads a .ps1 without BOM as ANSI");
        assertEquals(BattleNetSignIn.windowWatcherCommand(List.of(), dir).get(6), command.get(6), "the copy is reused");
    }

    @Test
    public void theRenewalScriptRunsFromACopyNamedByItsContent() throws Exception {
        Path dir = Files.createTempDirectory("wurst-renewal-script");
        Path file = BattleNetSignIn.scriptCopy(BattleNetSignIn.TOKEN_SCRIPT, dir);
        assertEquals(file.getParent(), dir);
        assertTrue(file.getFileName().toString().matches("wurst-battlenet-token-[0-9a-f]{12}\\.ps1"), file.toString());
        String script = Files.readString(file);
        assertTrue(script.contains("[Parameter(Mandatory = $true)][string]$BattleNetExe"));
        for (String outcome : List.of("refreshed", "no-token", "app-game-running", "token-not-visible")) {
            assertTrue(script.contains("\"result " + outcome + "\""), outcome);
        }
        assertTrue(script.chars().allMatch(c -> c < 128), "Windows PowerShell reads a .ps1 without BOM as ANSI");
        assertEquals(BattleNetSignIn.scriptCopy(BattleNetSignIn.TOKEN_SCRIPT, dir), file, "the copy is reused");
    }

    @Test
    public void theRenewalScriptRedirectsEverythingItPrintsToTheOutputFile() {
        List<String> command = BattleNetSignIn.tokenScriptCommand(Path.of("C:\\Temp\\wurst-battlenet-token.ps1"),
            Path.of("C:\\Program Files (x86)\\Battle.net\\Battle.net.exe"), 90, Path.of("C:\\Users\\O'Neil\\renewal.log"));
        assertEquals(command, List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
            "-Command", "& 'C:\\Temp\\wurst-battlenet-token.ps1' -BattleNetExe 'C:\\Program Files (x86)\\Battle.net\\Battle.net.exe'"
                + " -TimeoutSeconds 90 *> 'C:\\Users\\O''Neil\\renewal.log'"));
        assertEquals(BattleNetSignIn.powerShellQuote("a\u2019b"), "'a\u2019\u2019b'");
    }

    @Test
    public void readsOnlyCompleteLinesOfTheRedirectedUtf16Output() {
        byte[] partial = utf16WithBom("asked 0.0\r\ntoken 0.5\r\nresult refre");
        assertEquals(BattleNetSignIn.completeLines(partial), "asked 0.0\r\ntoken 0.5\r\n");
        assertEquals(BattleNetSignIn.parseTokenScriptOutput(BattleNetSignIn.completeLines(partial)).outcome(), null,
            "a result line still being written is no result");
        // Half a UTF-16 character at the end is part of an unfinished line.
        byte[] odd = Arrays.copyOf(utf16WithBom("asked 0.0\r\nr"), utf16WithBom("asked 0.0\r\nr").length - 1);
        assertEquals(BattleNetSignIn.completeLines(odd), "asked 0.0\r\n");

        BattleNetSignIn.TokenScriptResult result = BattleNetSignIn.parseTokenScriptOutput(BattleNetSignIn.completeLines(
            utf16WithBom("asked 0.0\r\ntoken 0.5\r\nclosed 1234 0.6\r\nresult refreshed\r\n")));
        assertEquals(result.outcome(), "refreshed");
        assertEquals(result.lines(), List.of("asked 0.0", "token 0.5", "closed 1234 0.6", "result refreshed"));
        // Without a byte order mark the output is read as UTF-8.
        assertEquals(BattleNetSignIn.completeLines("result no-token\n".getBytes(StandardCharsets.UTF_8)), "result no-token\n");
        assertEquals(BattleNetSignIn.completeLines(new byte[0]), "");
    }

    @Test
    public void aRenewalScriptThatEndedIsReadOnceMore() throws Exception {
        Path output = Files.createTempFile("wurst-renewal", ".log");
        Files.write(output, utf16WithBom("asked 0.0\r\nresult no-token\r\n"));
        // null: the script has ended; its result line still counts.
        assertEquals(BattleNetSignIn.awaitTokenScript(null, output, 5).outcome(), "no-token");
        Files.write(output, utf16WithBom("asked 0.0\r\n"));
        BattleNetSignIn.TokenScriptResult ended = BattleNetSignIn.awaitTokenScript(null, output, 5);
        assertEquals(ended.outcome(), null);
        assertTrue(BattleNetSignIn.problem(ended).contains("asked 0.0"), BattleNetSignIn.problem(ended));
        // A running script's result line ends the wait at once.
        Files.write(output, utf16WithBom("result refreshed\r\n"));
        assertEquals(BattleNetSignIn.awaitTokenScript(ProcessHandle.current(), output, 5).outcome(), "refreshed");
    }

    @Test
    public void eachRenewalOutcomeHasItsMessage() {
        assertEquals(BattleNetSignIn.problem(outcome("refreshed")), null);
        assertTrue(BattleNetSignIn.problem(outcome("no-token")).startsWith("Battle.net did not start Warcraft III."));
        assertEquals(BattleNetSignIn.problem(outcome("app-game-running")),
            "Close the Warcraft III client started from Battle.net, then run the map again.");
        String notVisible = BattleNetSignIn.problem(outcome("token-not-visible"));
        assertTrue(notVisible.contains("wrote its sign-in where other programs cannot read it"), notVisible);
        assertTrue(notVisible.contains("packaged app (such as the Claude desktop app)"), notVisible);
        assertTrue(notVisible.contains("Quit Battle.net from its tray icon and start it from the Start menu."), notVisible);
        String failed = BattleNetSignIn.problem(new BattleNetSignIn.TokenScriptResult(null,
            List.of("one", "two", "three", "four")));
        assertEquals(failed, "The Battle.net sign-in renewal failed: two | three | four.");
        assertEquals(BattleNetSignIn.problem(new BattleNetSignIn.TokenScriptResult(null, List.of())),
            "The Battle.net sign-in renewal failed: it printed nothing.");
        assertTrue(BattleNetSignIn.problem(outcome("something-new")).startsWith("The Battle.net sign-in renewal failed"));
    }

    private static BattleNetSignIn.TokenScriptResult outcome(String outcome) {
        return BattleNetSignIn.parseTokenScriptOutput("asked 0.0\nresult " + outcome + "\n");
    }

    private static byte[] utf16WithBom(String text) {
        byte[] body = text.getBytes(StandardCharsets.UTF_16LE);
        byte[] bytes = new byte[body.length + 2];
        bytes[0] = (byte) 0xff;
        bytes[1] = (byte) 0xfe;
        System.arraycopy(body, 0, bytes, 2, body.length);
        return bytes;
    }

    @Test
    public void theWindowWatcherOutlivesEveryRequestOfASlowRenewal() {
        // The renewal asks the app again every 25 s for up to 90 s, and the app shows its window once more as the
        // launched client signs in (up to 120 s); each can bring its window back.
        assertTrue(BattleNetSignIn.WINDOW_WATCH_SECONDS >= 90 + BattleNetSignIn.SIGN_IN_TIMEOUT_SECONDS,
            BattleNetSignIn.WINDOW_WATCH_SECONDS + " s");
    }

    @Test
    public void theRenewalLockOutwaitsAnotherRenewalAndItsSignIn() {
        assertTrue(BattleNetSignIn.GATE_WAIT_SECONDS > 90 + BattleNetSignIn.SIGN_IN_TIMEOUT_SECONDS,
            BattleNetSignIn.GATE_WAIT_SECONDS + " s");
    }

    @Test
    public void aCanceledRenewalKeepsTheLockWhileItWatchesForTheLateClient() throws Exception {
        ServerSocket gate = new ServerSocket();
        gate.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 0));

        BattleNetSignIn.closeLateGames(gate, new HashSet<>(), 1);

        assertFalse(gate.isClosed(), "the lock is held while a late client may still appear");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!gate.isClosed() && System.nanoTime() < deadline) Thread.sleep(50);
        assertTrue(gate.isClosed(), "the lock is released once the watch ends");
    }

    @Test
    public void readsOnlyTheRequestedBinaryValueFromRegQuery() {
        List<String> output = List.of(
            "",
            "HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Battle.net\\Launch Options\\W3",
            "    WEB_TOKEN    REG_BINARY    01000000d08c9ddf",
            "    LOCALE    REG_SZ    enUS",
            "");
        assertEquals(BattleNetSignIn.parseBinary(output, "WEB_TOKEN"), Optional.of("01000000D08C9DDF"));
        assertEquals(BattleNetSignIn.parseBinary(output, "web_token"), Optional.of("01000000D08C9DDF"));
        assertEquals(BattleNetSignIn.parseBinary(output, "LOCALE"), Optional.empty());
        assertEquals(BattleNetSignIn.parseBinary(List.of("ERROR: The system was unable to find the specified registry key or value."),
            "WEB_TOKEN"), Optional.empty());
    }

    @Test
    public void takesTheSharedSignInLockPort() {
        if (!Orient.isWindowsSystem()) throw new SkipException("Battle.net paths are Windows paths");
        // 33000 + sha256(lower-cased path) as uint32 BE % 10000, the port every tool that renews this sign-in binds
        assertEquals(BattleNetSignIn.gatePort(Path.of("C:\\Users\\tester\\AppData\\Roaming\\Battle.net\\Battle.net.config")), 36941);
    }

    @Test
    public void offersRetryLegacyAndCancelThroughTheLanguageClient() {
        assertEquals(RunMap.askBattleNet(clientChoosing("Use Legacy Launch"), "Open Battle.net."), "Use Legacy Launch");
        assertEquals(RunMap.askBattleNet(clientChoosing(null), "Open Battle.net."), "Cancel");
    }

    private static LanguageClient clientChoosing(String title) {
        return (LanguageClient) Proxy.newProxyInstance(LanguageClient.class.getClassLoader(),
            new Class<?>[]{LanguageClient.class}, (_, method, args) -> {
                assertEquals(method.getName(), "showMessageRequest");
                ShowMessageRequestParams prompt = (ShowMessageRequestParams) args[0];
                assertEquals(prompt.getActions().stream().map(MessageActionItem::getTitle).toList(),
                    List.of("Retry", "Use Legacy Launch", "Cancel"));
                return CompletableFuture.completedFuture(title == null ? null : new MessageActionItem(title));
            });
    }
}
