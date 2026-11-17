package de.peeeq.wurstio.languageserver.requests;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
