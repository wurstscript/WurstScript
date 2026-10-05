package de.peeeq.wurstscript.translation.lua.modular;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.prettyPrint.MaxOneSpacer;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.validation.PackageAbi;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PackageChunkCache {
    private final File cacheDir;

    public PackageChunkCache(File cacheDir) {
        this.cacheDir = cacheDir;
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public File getChunkFile(String packageName, String contentHash) {
        return new File(cacheDir, packageName + "_" + contentHash + ".lua");
    }

    public @Nullable PackageChunk get(String packageName, String contentHash) {
        File file = getChunkFile(packageName, contentHash);
        if (file.exists() && file.isFile()) {
            try {
                String code = Files.asCharSource(file, Charsets.UTF_8).read();
                return new PackageChunk(packageName, contentHash, code, null, true);
            } catch (IOException e) {
                WLogger.warning("Failed to read cached chunk: " + file + " (" + e.getMessage() + ")");
            }
        }
        return null;
    }

    public void put(PackageChunk chunk) {
        File file = getChunkFile(chunk.getPackageName(), chunk.getContentHash());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        File tempFile = new File(parent, file.getName() + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.asCharSink(tempFile, Charsets.UTF_8).write(chunk.getLuaCode());
            java.nio.file.Files.move(
                tempFile.toPath(),
                file.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            tempFile.delete();
            WLogger.warning("Failed to write chunk to cache: " + file + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Source actually being compiled for this package: the parser snapshot (including unsaved
     * editor buffers), falling back to a pretty-print of the live AST. Never re-read the disk
     * file when a newer in-memory snapshot exists.
     */
    public static String packageSourceSnapshot(WPackage p) {
        try {
            CompilationUnit cu = p.attrCompilationUnit();
            if (cu != null) {
                String snapshot = cu.getCuInfo().getSourceContent();
                if (snapshot != null) {
                    return snapshot;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            StringBuilder sb = new StringBuilder();
            p.prettyPrint(new MaxOneSpacer(), sb, 0);
            return sb.toString();
        } catch (Exception e) {
            return p.toString();
        }
    }

    /**
     * Semantic compilation dependencies: every imported package (including {@code initlater})
     * and config overrides, transitively. Distinct from {@link WPackage#attrInitDependencies()},
     * which deliberately excludes initlater for initialization ordering.
     */
    public static List<WPackage> compilationDependencies(WPackage p) {
        Set<WPackage> deps = new LinkedHashSet<>();
        collectCompilationDependencies(p, deps);
        // Own package is the seed; strip it so the list is only dependents.
        deps.remove(p);
        List<WPackage> result = new ArrayList<>(deps);
        result.sort(Comparator.comparing(WPackage::getName));
        return result;
    }

    private static void collectCompilationDependencies(WPackage p, Set<WPackage> out) {
        for (WImport imp : p.getImports()) {
            WPackage imported;
            try {
                imported = imp.attrImportedPackage();
            } catch (Exception e) {
                continue;
            }
            if (imported == null || imported == p) {
                continue;
            }
            if (out.add(imported)) {
                collectCompilationDependencies(imported, out);
            }
        }
        try {
            WurstModel model = p.getModel();
            if (model != null) {
                WPackage configPackage = model.attrConfigOverridePackages().get(p);
                if (configPackage != null && out.add(configPackage)) {
                    collectCompilationDependencies(configPackage, out);
                }
            }
        } catch (Error | Exception ignored) {
            // Detached packages can occur transiently; ignore for dependency collection.
        }
    }

    /**
     * Fingerprint of the whole-program shape that cached per-package chunks can silently depend
     * on: the class inventory (which determines {@code prog.attrTypeId()} numbering), superclasses,
     * and the method inventory (which determines dispatch-slot assignment).
     *
     * <p>Adding an unrelated package changes every existing class's neighbours in this numbering
     * (e.g. a new {@code A} shifts {@code Z} from 1 to 2) and may shift dispatch slots. Cached
     * chunks also bake {@code X.typeId} in as integer literals
     * ({@code ImTypeIdOfClass} lowering), which the postamble overwrite of the class-table field
     * cannot repair. Mixing this fingerprint into every chunk hash therefore invalidates all
     * chunks on any shape change; the cache still hits when only bodies change in ways the
     * dependency fingerprints below already cover.
     */
    public static String computeProgramShapeFingerprint(ImProg prog) {
        List<ImClass> classes = new ArrayList<>(prog.getClasses());
        classes.sort(Comparator.comparing(ImClass::getName));
        StringBuilder sb = new StringBuilder();
        for (ImClass c : classes) {
            sb.append("class:").append(shapePackageName(c)).append('.').append(c.getName());
            List<String> supers = new ArrayList<>();
            try {
                for (ImClassType s : c.getSuperClasses()) {
                    supers.add(s.getClassDef().getName());
                }
            } catch (Exception ignored) {
            }
            Collections.sort(supers);
            for (String s : supers) {
                sb.append("<:").append(s);
            }
            List<String> methods = new ArrayList<>();
            try {
                for (ImMethod m : c.getMethods()) {
                    methods.add(m.getName());
                }
            } catch (Exception ignored) {
            }
            Collections.sort(methods);
            for (String m : methods) {
                sb.append('#').append(m);
            }
            sb.append("\n");
        }
        return Hashing.sha256().hashString(sb.toString(), Charsets.UTF_8).toString().substring(0, 16);
    }

    private static String shapePackageName(ImClass c) {
        try {
            de.peeeq.wurstscript.ast.Element trace = c.attrTrace();
            if (trace == null) {
                return "global";
            }
            PackageOrGlobal nearest = trace.attrNearestPackage();
            if (nearest instanceof WPackage) {
                return ((WPackage) nearest).getName();
            }
        } catch (Exception ignored) {
        }
        return "global";
    }

    public static String computePackageHash(
        WPackage p,
        @Nullable RunArgs runArgs,
        @Nullable List<ImFunction> packageFuncs
    ) {
        return computePackageHash(p, runArgs, packageFuncs, null, null);
    }

    /**
     * Full chunk key. Returns {@code null} when the semantic dependency graph cannot be resolved;
     * callers must treat {@code null} as a cache miss (translate fresh, store nothing) rather
     * than falling back to a weak key that could silently hit.
     *
     * @param programShape whole-program class inventory, see
     *     {@link #computeProgramShapeFingerprint}. Null skips the check (only for contexts
     *     without an IM program at hand).
     * @param imFingerprint structural hash of this package's lowered IM partition, see
     *     {@link ImStructuralFingerprint}. Null skips the check.
     */
    public static @Nullable String computePackageHash(
        WPackage p,
        @Nullable RunArgs runArgs,
        @Nullable List<ImFunction> packageFuncs,
        @Nullable String programShape,
        @Nullable String imFingerprint
    ) {
        String name = p.getName();
        String sourceContent = packageSourceSnapshot(p);

        StringBuilder sb = new StringBuilder();
        // 1. Compiler version
        sb.append("version:").append(CompileTimeInfo.version).append("\n");

        // 2. Compiler flags
        if (runArgs != null) {
            sb.append("flags:opt=").append(runArgs.isOptimize())
              .append(",inline=").append(runArgs.isInline())
              .append(",localOpt=").append(runArgs.isLocalOptimizations())
              .append(",stack=").append(runArgs.isIncludeStacktraces())
              .append(",uncheckedDispatch=").append(runArgs.isUncheckedDispatch())
              .append(",dev=").append(runArgs.isDevBuild())
              .append(",inc=").append(runArgs.isIncremental())
              .append("\n");
        }

        // 3. Package name and current source snapshot
        sb.append("package:").append(name).append("\n");
        sb.append("source:").append(sourceContent).append("\n");

        // 4. Whole-program shape (class inventory for typeId numbering, method
        // inventory for dispatch slots). A new class anywhere renumbers every class after
        // it, and cached chunks bake X.typeId in as integer literals that the postamble
        // overwrite cannot repair. See computeProgramShapeFingerprint.
        if (programShape != null) {
            sb.append("shape:").append(programShape).append("\n");
        }

        // 5. Lowered IM of this package's own partition. This is what the chunk is printed
        // from, so equal fingerprints mean re-emission would print equal code: body-only
        // edits upstream that leave this lowering untouched (ordinary calls) keep their
        // cache hit, while edits that change it (folded compiletime constants, tuple
        // indices, specialised bodies, …) invalidate precisely.
        if (imFingerprint != null) {
            sb.append("im:").append(imFingerprint).append("\n");
        }

        // 6. Transitive public ABI of semantic compilation dependencies (every import form,
        // including initlater, plus config overrides). Selectivity comes from the IM
        // fingerprint above; this stays as defence in depth for interface aspects that
        // never reach lowering.
        try {
            for (WPackage dep : compilationDependencies(p)) {
                sb.append("dep:").append(dep.getName())
                  .append("=abi:").append(PackageAbi.computeAbiHash(dep))
                  .append("\n");
            }
        } catch (Exception e) {
            // The dependency graph is unreadable (e.g. detached packages mid-edit). Any
            // name-only fallback could collide across builds, so force a miss instead.
            WLogger.warning("Could not resolve compilation dependencies of " + name + "; skipping chunk cache for it");
            return null;
        }

        // 7. Package functions / generic specializations
        if (packageFuncs != null) {
            List<String> funcNames = new ArrayList<>(packageFuncs.size());
            for (ImFunction f : packageFuncs) {
                funcNames.add(f.getName());
            }
            Collections.sort(funcNames);
            for (String fn : funcNames) {
                sb.append("func:").append(fn).append("\n");
            }
        }

        return Hashing.sha256().hashString(sb.toString(), Charsets.UTF_8).toString().substring(0, 16);
    }

    public static String computePackageHash(WPackage p) {
        String hash = computePackageHash(p, null, null, null, null);
        // No RunArgs and no shape: dependency resolution over a test model effectively always
        // succeeds; fall back to a name-only key only if it truly cannot be resolved.
        if (hash == null) {
            hash = Hashing.sha256().hashString("package:" + p.getName() + "\n", Charsets.UTF_8).toString().substring(0, 16);
        }
        return hash;
    }
}
