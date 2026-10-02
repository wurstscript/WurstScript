package de.peeeq.wurstscript.translation.lua.modular;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;

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
        try {
            Files.asCharSink(file, Charsets.UTF_8).write(chunk.getLuaCode());
        } catch (IOException e) {
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

    private static final java.util.concurrent.ConcurrentHashMap<String, FileEntry> fileCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, String> packageHashCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static String computePackageHash(WPackage p) {
        String name = p.getName();
        try {
            String filePath = p.attrSource().getFile();
            File f = new File(filePath);
            if (f.exists() && f.isFile()) {
                long lm = f.lastModified();
                long len = f.length();
                String cacheKey = name + "@" + filePath + "@" + lm + "@" + len;
                String cachedHash = packageHashCache.get(cacheKey);
                if (cachedHash != null) {
                    return cachedHash;
                }

                FileEntry entry = fileCache.get(filePath);
                String sourceContent;
                if (entry != null && entry.lastModified == lm && entry.length == len) {
                    sourceContent = entry.content;
                } else {
                    sourceContent = Files.asCharSource(f, Charsets.UTF_8).read();
                    fileCache.put(filePath, new FileEntry(lm, len, sourceContent));
                }
                String toHash = "v1:" + name + ":" + sourceContent;
                String hash = Hashing.sha256().hashString(toHash, Charsets.UTF_8).toString().substring(0, 16);
                packageHashCache.put(cacheKey, hash);
                return hash;
            }
        } catch (Exception ignored) {
        }
        String toHash = "v1:" + name + ":" + p.toString();
        return Hashing.sha256().hashString(toHash, Charsets.UTF_8).toString().substring(0, 16);
    }
}
