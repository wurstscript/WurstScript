package de.peeeq.wurstscript.translation.lua.modular;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.validation.PackageAbi;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private static class FileEntry {
        final long lastModified;
        final long length;
        final String content;

        FileEntry(long lastModified, long length, String content) {
            this.lastModified = lastModified;
            this.length = length;
            this.content = content;
        }
    }

    private static final ConcurrentHashMap<String, FileEntry> fileCache = new ConcurrentHashMap<>();

    public static String computePackageHash(
        WPackage p,
        @Nullable RunArgs runArgs,
        @Nullable List<ImFunction> packageFuncs
    ) {
        String name = p.getName();
        String sourceContent = null;
        try {
            String filePath = p.attrSource().getFile();
            File f = new File(filePath);
            if (f.exists() && f.isFile()) {
                long lm = f.lastModified();
                long len = f.length();
                FileEntry entry = fileCache.get(filePath);
                if (entry != null && entry.lastModified == lm && entry.length == len) {
                    sourceContent = entry.content;
                } else {
                    sourceContent = Files.asCharSource(f, Charsets.UTF_8).read();
                    fileCache.put(filePath, new FileEntry(lm, len, sourceContent));
                }
            }
        } catch (Exception ignored) {
        }
        if (sourceContent == null) {
            sourceContent = p.toString();
        }

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

        // 3. Package name and source content
        sb.append("package:").append(name).append("\n");
        sb.append("source:").append(sourceContent).append("\n");

        // 4. Dependencies public ABI (transitive imports and @config overrides)
        try {
            List<WPackage> deps = new ArrayList<>(p.attrInitDependencies());
            deps.sort(Comparator.comparing(WPackage::getName));
            for (WPackage dep : deps) {
                sb.append("dep:").append(dep.getName()).append("=").append(PackageAbi.computeAbiHash(dep)).append("\n");
            }
        } catch (Exception e) {
            for (WImport imp : p.getImports()) {
                sb.append("import:").append(imp.getPackagename()).append("\n");
            }
        }

        // 5. Package functions / generic specializations
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
        return computePackageHash(p, null, null);
    }
}
