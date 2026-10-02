package de.peeeq.wurstscript.validation;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ValidationCache {
    public static class Entry {
        final long lastModified;
        final long length;
        final Map<String, String> packageAbis;

        public Entry(long lastModified, long length, Map<String, String> packageAbis) {
            this.lastModified = lastModified;
            this.length = length;
            this.packageAbis = packageAbis;
        }
    }

    private final File cacheFile;
    private final Map<String, Entry> entries = new HashMap<>();
    private boolean isDirty = false;

    public ValidationCache(File cacheDir) {
        this.cacheFile = new File(cacheDir, "validation_cache.properties");
        load();
    }

    private void load() {
        if (!cacheFile.exists() || !cacheFile.isFile()) {
            return;
        }
        try {
            List<String> lines = Files.readLines(cacheFile, Charsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String path = line.substring(0, eq);
                String rest = line.substring(eq + 1);
                String[] parts = rest.split(";", 3);
                if (parts.length >= 2) {
                    long mtime = Long.parseLong(parts[0]);
                    long len = Long.parseLong(parts[1]);
                    Map<String, String> abis = new HashMap<>();
                    if (parts.length == 3 && !parts[2].isEmpty()) {
                        for (String pkgEntry : parts[2].split(",")) {
                            int colon = pkgEntry.indexOf(':');
                            if (colon > 0) {
                                abis.put(pkgEntry.substring(0, colon), pkgEntry.substring(colon + 1));
                            }
                        }
                    }
                    entries.put(path, new Entry(mtime, len, abis));
                }
            }
        } catch (Exception e) {
            WLogger.warning("Could not read validation cache: " + e.getMessage());
            entries.clear();
        }
    }

    public @Nullable Entry get(String filePath) {
        return entries.get(filePath);
    }

    public void update(CompilationUnit cu) {
        String path = cu.getCuInfo().getFile();
        if (path == null) return;
        File f = new File(path);
        if (!f.exists()) return;
        Map<String, String> abis = new HashMap<>();
        for (WPackage p : cu.getPackages()) {
            abis.put(p.getName(), PackageAbi.computeAbiHash(p));
        }
        Entry existing = entries.get(path);
        if (existing == null || existing.lastModified != f.lastModified() || existing.length != f.length()
            || !existing.packageAbis.equals(abis)) {
            entries.put(path, new Entry(f.lastModified(), f.length(), abis));
            isDirty = true;
        }
    }

    public void save() {
        if (!isDirty) {
            return;
        }
        try {
            if (cacheFile.getParentFile() != null && !cacheFile.getParentFile().exists()) {
                cacheFile.getParentFile().mkdirs();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("# Wurst validation cache\n");
            for (Map.Entry<String, Entry> e : entries.entrySet()) {
                sb.append(e.getKey()).append("=")
                  .append(e.getValue().lastModified).append(";")
                  .append(e.getValue().length).append(";");
                boolean first = true;
                for (Map.Entry<String, String> pe : e.getValue().packageAbis.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append(pe.getKey()).append(":").append(pe.getValue());
                    first = false;
                }
                sb.append("\n");
            }
            Files.asCharSink(cacheFile, Charsets.UTF_8).write(sb.toString());
        } catch (IOException e) {
            WLogger.warning("Could not write validation cache: " + e.getMessage());
        }
    }

    public Set<CompilationUnit> filterUnitsToWalk(Collection<CompilationUnit> toCheck) {
        Set<CompilationUnit> toWalk = new HashSet<>();
        Set<String> changedAbis = new HashSet<>();

        for (CompilationUnit cu : toCheck) {
            String path = cu.getCuInfo().getFile();
            if (path == null) {
                toWalk.add(cu);
                continue;
            }
            if (cu.getCuInfo().isLibrary()) {
                continue;
            }
            if (path.endsWith("war3map.j") || path.endsWith("war3map.lua")) {
                toWalk.add(cu);
                continue;
            }
            File f = new File(path);
            if (!f.exists()) {
                toWalk.add(cu);
                continue;
            }
            Entry entry = entries.get(path);
            if (entry == null || entry.lastModified != f.lastModified() || entry.length != f.length()) {
                toWalk.add(cu);
                for (WPackage p : cu.getPackages()) {
                    String newAbi = PackageAbi.computeAbiHash(p);
                    String oldAbi = entry != null ? entry.packageAbis.get(p.getName()) : null;
                    if (oldAbi == null || !oldAbi.equals(newAbi)) {
                        changedAbis.add(p.getName());
                    }
                }
            }
        }

        if (!changedAbis.isEmpty()) {
            for (CompilationUnit cu : toCheck) {
                if (toWalk.contains(cu) || cu.getCuInfo().isLibrary()) {
                    continue;
                }
                for (WPackage p : cu.getPackages()) {
                    for (WImport imp : p.getImports()) {
                        if (changedAbis.contains(imp.getPackagename())) {
                            toWalk.add(cu);
                            break;
                        }
                    }
                    if (toWalk.contains(cu)) break;
                }
            }
        }

        return toWalk;
    }
}
