package de.peeeq.wurstscript.validation;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Remembers per-file validation state across incremental builds so unchanged compilation units
 * (and units whose dependencies' public ABIs are unchanged) can be skipped.
 *
 * <p>Correctness rules, in order of importance:
 * <ul>
 *   <li>Identity is the <b>source snapshot being compiled</b>
 *       ({@code cu.getCuInfo().getSourceContent()}), never file mtime/length: the compiler
 *       routinely builds from unsaved editor buffers whose disk timestamp is stale.</li>
 *   <li>Dependents are followed <b>transitively to a fixed point</b> over every import form
 *       (including {@code initlater}); a direct-imports-only scan misses {@code A -> B -> C}.</li>
 *   <li>Libraries are tracked like any other file. Skipping them blindly keeps stale dependents
 *       when the stdlib changes.</li>
 *   <li>Deleted files evict their entries and invalidate their importers.</li>
 *   <li>Any doubt (unreadable content, unresolvable imports) walks the unit. The cache may only
 *       cause <i>more</i> checking, never less, relative to a clean build.</li>
 * </ul>
 */
public class ValidationCache {
    public static class Entry {
        final String sourceHash;
        final Map<String, String> packageAbis;

        public Entry(String sourceHash, Map<String, String> packageAbis) {
            this.sourceHash = sourceHash;
            this.packageAbis = packageAbis;
        }
    }

    private final File cacheFile;
    private final Map<String, Entry> entries = new HashMap<>();
    private boolean isDirty = false;

    public ValidationCache(File cacheDir) {
        this.cacheFile = new File(cacheDir, "validation_cache_v2.properties");
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
                String[] parts = rest.split(";", 2);
                if (parts.length == 2) {
                    String sourceHash = parts[0];
                    Map<String, String> abis = new HashMap<>();
                    if (!parts[1].isEmpty()) {
                        for (String pkgEntry : parts[1].split(",")) {
                            int colon = pkgEntry.indexOf(':');
                            if (colon > 0) {
                                abis.put(pkgEntry.substring(0, colon), pkgEntry.substring(colon + 1));
                            }
                        }
                    }
                    entries.put(path, new Entry(sourceHash, abis));
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

    static @Nullable String cacheKey(CompilationUnit cu) {
        String path;
        try {
            path = cu.getCuInfo().getFile();
        } catch (Exception e) {
            return null;
        }
        if (path != null && !path.isEmpty()) {
            return "file:" + path;
        }
        List<String> pkgs = new ArrayList<>();
        try {
            for (WPackage p : cu.getPackages()) {
                pkgs.add(p.getName());
            }
        } catch (Exception e) {
            return null;
        }
        if (pkgs.isEmpty()) {
            return null;
        }
        Collections.sort(pkgs);
        return "mem:" + String.join(",", pkgs);
    }

    /** Content the compiler actually parsed for this unit, or null when it cannot be had. */
    static @Nullable String contentOf(CompilationUnit cu) {
        try {
            String snapshot = cu.getCuInfo().getSourceContent();
            if (snapshot != null) {
                return snapshot;
            }
        } catch (Exception ignored) {
        }
        try {
            String path = cu.getCuInfo().getFile();
            if (path != null) {
                File f = new File(path);
                if (f.isFile()) {
                    return Files.asCharSource(f, Charsets.UTF_8).read();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    static String hashOf(String content) {
        return Hashing.sha256().hashString(content, Charsets.UTF_8).toString();
    }

    public void update(CompilationUnit cu) {
        String path = cacheKey(cu);
        if (path == null) return;
        String content = contentOf(cu);
        if (content == null) {
            // Nothing trustworthy to key on: leave any stale entry in place but mark the file
            // changed by removing it, so the next build walks it again.
            if (entries.remove(path) != null) {
                isDirty = true;
            }
            return;
        }
        Map<String, String> abis = new HashMap<>();
        for (WPackage p : cu.getPackages()) {
            try {
                abis.put(p.getName(), PackageAbi.computeAbiHash(p));
            } catch (Exception e) {
                // Unresolvable ABI: record nothing for this package so dependents stay dirty.
            }
        }
        String sourceHash = hashOf(content);
        Entry existing = entries.get(path);
        if (existing == null || !existing.sourceHash.equals(sourceHash)
            || !existing.packageAbis.equals(abis)) {
            entries.put(path, new Entry(sourceHash, abis));
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
            sb.append("# Wurst validation cache v2 (source-hash keyed)\n");
            List<String> paths = new ArrayList<>(entries.keySet());
            Collections.sort(paths);
            for (String path : paths) {
                Entry e = entries.get(path);
                sb.append(path).append("=").append(e.sourceHash).append(";");
                boolean first = true;
                List<String> pkgs = new ArrayList<>(e.packageAbis.keySet());
                Collections.sort(pkgs);
                for (String pkg : pkgs) {
                    if (!first) sb.append(",");
                    sb.append(pkg).append(":").append(e.packageAbis.get(pkg));
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
        // Packages whose ABI changed (or vanished): importers, transitively, must be re-walked.
        Set<String> changedPkgs = new HashSet<>();
        // package -> CUs containing an import of it, for the transitive closure.
        Map<String, Set<CompilationUnit>> importers = new HashMap<>();
        Set<String> livePaths = new HashSet<>();

        for (CompilationUnit cu : toCheck) {
            String path = cacheKey(cu);
            if (path == null) {
                toWalk.add(cu);
                continue;
            }
            livePaths.add(path);
            if (path.endsWith("war3map.j") || path.endsWith("war3map.lua")) {
                toWalk.add(cu);
                continue;
            }
            for (WPackage p : cu.getPackages()) {
                for (WImport imp : p.getImports()) {
                    try {
                        importers.computeIfAbsent(imp.getPackagename(), k -> new HashSet<>()).add(cu);
                    } catch (Exception ignored) {
                        // Unreadable import list: this CU must be walked.
                        toWalk.add(cu);
                    }
                }
            }
            String content = contentOf(cu);
            if (content == null) {
                toWalk.add(cu);
                continue;
            }
            Entry entry = entries.get(path);
            if (entry == null || !entry.sourceHash.equals(hashOf(content))) {
                toWalk.add(cu);
                recordAbiChanges(cu, entry, changedPkgs);
            }
        }

        // Deleted files: evict their entries; their packages count as changed.
        Set<String> deletedPaths = new HashSet<>(entries.keySet());
        deletedPaths.removeAll(livePaths);
        for (String deleted : deletedPaths) {
            Entry gone = entries.remove(deleted);
            if (gone != null) {
                changedPkgs.addAll(gone.packageAbis.keySet());
                isDirty = true;
            }
        }

        // Transitive closure over changed packages. Already-dirty units still propagate:
        // otherwise an unchanged importer behind one would never be walked.
        Deque<String> queue = new ArrayDeque<>(changedPkgs);
        while (!queue.isEmpty()) {
            String pkg = queue.removeFirst();
            Set<CompilationUnit> deps = importers.get(pkg);
            if (deps == null) continue;
            for (CompilationUnit cu : deps) {
                toWalk.add(cu);
                for (WPackage p : cu.getPackages()) {
                    if (changedPkgs.add(p.getName())) {
                        queue.addLast(p.getName());
                    }
                }
            }
        }

        return toWalk;
    }

    private static void recordAbiChanges(CompilationUnit cu, @Nullable Entry entry, Set<String> changedPkgs) {
        for (WPackage p : cu.getPackages()) {
            String newAbi;
            try {
                newAbi = PackageAbi.computeAbiHash(p);
            } catch (Exception e) {
                // Uncomputable ABI: assume changed.
                changedPkgs.add(p.getName());
                continue;
            }
            String oldAbi = entry != null ? entry.packageAbis.get(p.getName()) : null;
            if (oldAbi == null || !oldAbi.equals(newAbi)) {
                changedPkgs.add(p.getName());
            }
        }
    }
}
