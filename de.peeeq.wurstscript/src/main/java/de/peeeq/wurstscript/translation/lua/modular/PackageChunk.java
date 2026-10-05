package de.peeeq.wurstscript.translation.lua.modular;

import java.util.Collections;
import java.util.List;

public class PackageChunk {
    private final String packageName;
    private final String contentHash;
    private final String luaCode;
    private final List<String> dependencies;
    private final boolean fromCache;

    public PackageChunk(String packageName, String contentHash, String luaCode, List<String> dependencies, boolean fromCache) {
        this.packageName = packageName;
        this.contentHash = contentHash;
        this.luaCode = luaCode;
        this.dependencies = dependencies != null ? dependencies : Collections.emptyList();
        this.fromCache = fromCache;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getLuaCode() {
        return luaCode;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public boolean isFromCache() {
        return fromCache;
    }
}
