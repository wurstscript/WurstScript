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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BattleNetSignInTest {
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
    public void takesTheSameLockAsTheE2eHarness() {
        if (!Orient.isWindowsSystem()) throw new SkipException("Battle.net paths are Windows paths");
        // node/src/runner/battlenet.cjs in wc3-e2e-harness: 33000 + sha256(lower-cased path) as uint32 BE % 10000
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
