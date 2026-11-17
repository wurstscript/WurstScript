package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class BattleNetLaunchTest {
    @Test
    public void handsOffOnlyOneMapAndLeavesAuthenticationToTheLauncher() {
        Path target = Path.of("Test Maps", "launch.w3x");
        String arguments = BattleNetLaunch.launchArguments(
            "-legacykerning -loadfile \"old map.w3x\" -windowmode fullscreen",
            List.of("-editor", "-launch", "-windowmode", "windowed"), target);
        assertEquals(arguments, "-legacykerning -windowmode windowed -loadfile \"" + target + "\"");
        expectThrows(IllegalArgumentException.class,
            () -> BattleNetLaunch.launchArguments("", List.of("-uid", "w3"), target));
    }

    @Test
    public void offersRetryLegacyAndCancelThroughTheLanguageClient() {
        assertEquals(RunMap.askBattleNet(clientChoosing("Use Legacy Launch"), "Open Battle.net."), "Use Legacy Launch");
        assertEquals(RunMap.askBattleNet(clientChoosing(null), "Open Battle.net."), "Cancel");
    }

    @Test
    public void cancelingDuringShutdownReopensBattleNetWithoutEditingItsSettings() {
        var canceled = new AtomicBoolean();
        var calls = new ArrayList<String>();
        expectThrows(CancellationException.class, () -> CompilationCancellation.run(canceled::get, () ->
            BattleNetLaunch.editLauncherSettings(true,
                () -> {
                    calls.add("shutdown requested");
                    canceled.set(true);
                    calls.add("shutdown complete");
                },
                () -> calls.add("edit"),
                () -> calls.add("restart"))));
        assertEquals(calls, List.of("shutdown requested", "shutdown complete", "restart"));
    }

    @Test
    public void settingsWriteFailureStillReopensBattleNet() {
        var calls = new ArrayList<String>();
        IOException failure = new IOException("settings file write failed");
        assertEquals(expectThrows(IOException.class, () -> BattleNetLaunch.editLauncherSettings(true,
            () -> calls.add("stop"), () -> { throw failure; }, () -> calls.add("restart"))), failure);
        assertEquals(calls, List.of("stop", "restart"));
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
