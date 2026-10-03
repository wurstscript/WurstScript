package de.peeeq.wurstscript.translation.lua.modular;

import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;

import java.util.List;

public class PackageChunkResult {
    private final String preamble;
    private final List<PackageChunk> chunks;
    private final String postamble;
    private final String assembledScript;
    private final LuaCompilationUnit assembledCu;
    private final long assembleTimeMs;

    public PackageChunkResult(String preamble, List<PackageChunk> chunks, String postamble,
                              String assembledScript, LuaCompilationUnit assembledCu, long assembleTimeMs) {
        this.preamble = preamble;
        this.chunks = chunks;
        this.postamble = postamble;
        this.assembledScript = assembledScript;
        this.assembledCu = assembledCu;
        this.assembleTimeMs = assembleTimeMs;
    }

    public String getPreamble() {
        return preamble;
    }

    public List<PackageChunk> getChunks() {
        return chunks;
    }

    public String getPostamble() {
        return postamble;
    }

    public String getAssembledScript() {
        return assembledScript;
    }

    public LuaCompilationUnit getAssembledCu() {
        return assembledCu;
    }

    public long getAssembleTimeMs() {
        return assembleTimeMs;
    }
}
