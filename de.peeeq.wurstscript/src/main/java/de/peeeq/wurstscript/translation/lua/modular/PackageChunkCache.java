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

    public static String computePackageHash(WPackage p) {
        String name = p.getName();
        String sourceContent = "";
        try {
            String filePath = p.attrSource().getFile();
            File f = new File(filePath);
            if (f.exists() && f.isFile()) {
                sourceContent = Files.asCharSource(f, Charsets.UTF_8).read();
            } else {
                sourceContent = p.toString();
            }
        } catch (Exception e) {
            sourceContent = p.toString();
        }
        String toHash = "v1:" + name + ":" + sourceContent;
        return Hashing.sha256().hashString(toHash, Charsets.UTF_8).toString().substring(0, 16);
    }
}
