import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstio.utils.W3InstallationData;
import de.peeeq.wurstscript.gui.*;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import jdk.jfr.*;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class RunMapProfile {
    static class WithoutLaunch extends RunMap {
        final WurstGui gui;
        boolean stoppedBeforeLaunch;
        WithoutLaunch(File root, File map, List<String> args, WurstGui gui) {
            super(null, WFile.create(root), Optional.empty(), Optional.of(map), args, Optional.empty());
            this.gui = gui;
        }
        @Override protected WurstGui createGui(ModelManager manager, String title) { return gui; }
        @Override protected W3InstallationData resolveLaunchData(W3InstallationData data) {
            stoppedBeforeLaunch = true;
            timeTaker.endPhase();
            timeTaker.printReport();
            return null;
        }
    }
    public static void main(String[] args) throws Exception {
        File root = new File(args[0]);
        var manager = new ModelManagerImpl(root, new BufferManager());
        long initialization = System.nanoTime();
        manager.loadProject();
        var initialGui = new WurstGuiCliImpl();
        manager.checkProject(initialGui);
        if (manager.hasErrors() || initialGui.getErrorCount() != 0) throw new IllegalStateException("Editor initialization failed");
        System.out.println("EDITOR_READY ms=" + (System.nanoTime()-initialization)/1_000_000.0);
        List<String> compileArgs = new ArrayList<>(WurstCommands.getCompileArgs(WFile.create(root), false));
        compileArgs.add("-measure");
        var runArgs = new RunArgs(compileArgs);
        if (runArgs.isInline() || runArgs.isOptimize() || runArgs.isLocalOptimizations()) throw new IllegalStateException("Optimizations must be disabled");
        System.out.println("RUNMAP_PROFILE_ARGS " + compileArgs);
        System.out.println("DIAGNOSTIC_NO_COPY " + Boolean.getBoolean("profile.unscoped"));
        for (int run = 0; run < 3; run++) {
            try (Recording recording = new Recording(Configuration.getConfiguration("profile"))) {
                recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(2));
                recording.enable("jdk.ObjectAllocationSample").with("throttle", "300/s");
                recording.start();
                long start = System.nanoTime();
                var gui = new WurstGuiCliImpl();
                var request = new WithoutLaunch(root, new File(root, args[1]), compileArgs, gui);
                try {
                    if (Boolean.getBoolean("profile.unscoped")) {
                        request.execute(manager);
                    } else {
                        CompilationCancellation.run(() -> false, () -> request.execute(manager));
                    }
                } catch (RequestFailedException stop) {
                    if (!request.stoppedBeforeLaunch || !stop.getMessage().equals("Run canceled.")) throw stop;
                } finally {
                    recording.stop();
                    recording.dump(Path.of(args[2] + "-" + run + ".jfr"));
                }
                if (!request.stoppedBeforeLaunch || gui.getErrorCount() != 0) throw new IllegalStateException("RunMap failed");
                System.out.println("RUNMAP_COMPLETE run=" + run + " ms=" + (System.nanoTime()-start)/1_000_000.0
                    + " errors=" + gui.getErrorCount() + " units=" + manager.getModel().size());
            }
        }
    }
}
