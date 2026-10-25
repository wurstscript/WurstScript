import de.peeeq.wurstio.languageserver.*;
import de.peeeq.wurstio.languageserver.requests.*;
import de.peeeq.wurstscript.gui.*;
import de.peeeq.wurstscript.translation.imtranslation.IncrementalImTranslation;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.wurstscript.projectconfig.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class IncrementalHotReloadProfile {
    static class Manager extends ModelManagerImpl {
        boolean cached;
        Manager(File root) { super(root, new BufferManager()); }
        @Override public IncrementalImTranslation incrementalImTranslation() {
            return cached ? super.incrementalImTranslation() : null;
        }
        IncrementalImTranslation cache() { return super.incrementalImTranslation(); }
    }
    static class Probe extends RunMap {
        Probe(File root) {
            super(null, WFile.create(root), Optional.empty(), Optional.empty(),
                List.of("-lua", "-luaHotReload", "-hotreload", "-runcompiletimefunctions", "-injectobjects", "-stacktraces"),
                Optional.of(new File(root, "NO_GAME.exe").getAbsolutePath()));
        }
        File compile(Manager manager, WurstGui gui, File root) throws Exception {
            return compileScript(gui, manager, compileArgs, Optional.of(new File(root, "base.w3x")),
                WurstProjectConfigReader.load(root.toPath().resolve("wurst.build")), false, new File(root, "wurst/war3map.j"));
        }
        LuaHotReload.Manifest manifest() { return luaHotReloadManifest; }
    }
    public static void main(String[] args) throws Exception {
        File root = new File(args[0]);
        Path editFile = root.toPath().resolve("wurst/IncrementalProfile.wurst");
        String source = "package IncrementalProfile\nimport Printing\nfunction profileValue() returns int\n    return 1\ninit\n    print(I2S(profileValue()))\n";
        Files.writeString(editFile, source);
        Manager manager = new Manager(root);
        manager.buildProject();
        if (manager.hasErrors()) throw new AssertionError(manager.getFirstErrorDescription());
        Map<Integer, String> expected = new HashMap<>();
        Map<Integer, LuaHotReload.Manifest> manifests = new HashMap<>();
        for (int run = 0; run < 12; run++) {
            int version = run % 4 < 2 ? 1 : 2;
            String edited = source.replace("return 1", "return " + version);
            Files.writeString(editFile, edited);
            manager.reconcile(manager.syncCompilationUnitContent(WFile.create(editFile), edited));
            if (manager.hasErrors()) throw new AssertionError(manager.getFirstErrorDescription());
            manager.cached = run % 4 == 1 || run % 4 == 2;
            Probe probe = new Probe(root);
            WurstGuiCliImpl gui = new WurstGuiCliImpl();
            long start = System.nanoTime();
            File[] result = new File[1];
            CompilationCancellation.run(() -> false, () -> result[0] = probe.compile(manager, gui, root));
            File output = result[0];
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            if (gui.getErrorCount() != 0) throw new AssertionError(gui.getErrors());
            String script = Files.readString(output.toPath());
            if (!expected.containsKey(version)) {
                expected.put(version, script); manifests.put(version, probe.manifest());
                Files.writeString(root.toPath().resolve("_build/incremental-profile-expected.lua"), script);
                Files.writeString(root.toPath().resolve("_build/incremental-profile-expected-manifest.json"), new com.google.gson.Gson().toJson(probe.manifest()));
            } else if (!expected.get(version).equals(script) || !manifests.get(version).equals(probe.manifest())) {
                Files.writeString(root.toPath().resolve("_build/incremental-profile-different.lua"), script);
                Files.writeString(root.toPath().resolve("_build/incremental-profile-different-manifest.json"), new com.google.gson.Gson().toJson(probe.manifest()));
                throw new AssertionError("fresh/cached output differs, run " + run);
            }
            System.out.println("RELOAD_PROFILE run=" + run + " cached=" + manager.cached + " ms=" + ms + " " + manager.cache().statistics());
        }
    }
}
