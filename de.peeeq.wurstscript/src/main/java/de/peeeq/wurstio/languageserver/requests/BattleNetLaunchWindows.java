package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.WLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Hands the desktop to the map client after a successful Battle.net renewal on Windows. */
final class BattleNetLaunchWindows {
    static final int GAME_EXITED = 2;
    private static final int NO_WINDOW = 3;
    private static final int FOCUS_DENIED = 4;
    private static final String SCRIPT = """
        $ErrorActionPreference = 'Stop'
        Add-Type -Name Windows -Namespace Wurst -MemberDefinition @'
        [DllImport("user32.dll")] public static extern bool IsIconic(IntPtr window);
        [DllImport("user32.dll")] public static extern bool ShowWindowAsync(IntPtr window, int command);
        [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr window);
        [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
        '@

        $game = Get-Process -Id %d -ErrorAction SilentlyContinue
        if (!$game -or $game.ProcessName -ne 'Warcraft III' -or $game.HasExited) { exit %d }

        function MinimizeLauncher {
            foreach ($app in (Get-Process -Name 'Battle.net' -ErrorAction SilentlyContinue)) {
                if ($app.SessionId -ne $game.SessionId) { continue }
                $window = $app.MainWindowHandle
                if ($window -ne [IntPtr]::Zero -and ![Wurst.Windows]::IsIconic($window)) {
                    [void][Wurst.Windows]::ShowWindowAsync($window, 7) # SW_SHOWMINNOACTIVE
                }
            }
        }

        MinimizeLauncher
        $wait = [Diagnostics.Stopwatch]::StartNew()
        do {
            $game.Refresh()
            if ($game.HasExited) { exit %d }
            $window = $game.MainWindowHandle
            if ($window -ne [IntPtr]::Zero) {
                # Battle.net may have shown itself again while the game was starting.
                MinimizeLauncher
                if ([Wurst.Windows]::IsIconic($window)) {
                    [void][Wurst.Windows]::ShowWindowAsync($window, 9) # SW_RESTORE
                }
                for ($attempt = 0; $attempt -lt 5; $attempt++) {
                    if ([Wurst.Windows]::GetForegroundWindow() -eq $window -or
                        [Wurst.Windows]::SetForegroundWindow($window)) { exit 0 }
                    Start-Sleep -Milliseconds 100
                }
                exit %d
            }
            Start-Sleep -Milliseconds 100
        } while ($wait.Elapsed.TotalSeconds -lt 30)
        exit %d
        """;

    private BattleNetLaunchWindows() {
    }

    static List<String> command(long gamePid) {
        String script = SCRIPT.formatted(gamePid, GAME_EXITED, GAME_EXITED, FOCUS_DENIED, NO_WINDOW);
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        return List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
            "-EncodedCommand", encoded);
    }

    static void handOff(Process game) {
        Thread.ofVirtual().name("Battle.net window handoff").start(() -> {
            if (!game.isAlive()) return;
            try {
                Process helper = new ProcessBuilder(command(game.pid()))
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
                try {
                    if (!helper.waitFor(45, TimeUnit.SECONDS)) {
                        WLogger.warning("Timed out waiting for the Warcraft III window handoff.");
                        return;
                    }
                    switch (helper.exitValue()) {
                        case 0, GAME_EXITED -> { }
                        case NO_WINDOW -> WLogger.warning("Warcraft III did not show a window within 30 seconds.");
                        case FOCUS_DENIED -> WLogger.warning("Windows did not allow Wurst to focus Warcraft III.");
                        default -> WLogger.warning("Warcraft III window handoff failed (exit " + helper.exitValue() + ").");
                    }
                } finally {
                    if (helper.isAlive()) helper.destroyForcibly();
                }
            } catch (IOException ex) {
                WLogger.warning("Could not start the Warcraft III window handoff: " + ex.getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
