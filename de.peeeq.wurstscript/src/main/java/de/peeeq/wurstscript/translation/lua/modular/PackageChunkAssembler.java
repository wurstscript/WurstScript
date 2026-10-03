package de.peeeq.wurstscript.translation.lua.modular;

import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;

import java.util.List;

public class PackageChunkAssembler {

    public static String assemble(String preamble, List<PackageChunk> chunks, String postamble) {
        int estimatedSize = preamble.length() + postamble.length() + 1024;
        for (PackageChunk chunk : chunks) {
            estimatedSize += chunk.getLuaCode().length() + 64;
        }
        StringBuilder sb = new StringBuilder(estimatedSize);
        sb.append("-- [[ Preamble: Runtime Polyfills ]]\n");
        sb.append(preamble).append("\n\n");

        for (PackageChunk chunk : chunks) {
            sb.append("-- [[ Package: ").append(chunk.getPackageName()).append(" ]]\n");
            sb.append(chunk.getLuaCode()).append("\n\n");
        }

        sb.append("-- [[ Postamble: Entry Points & Bootstrap ]]\n");
        sb.append(postamble).append("\n");
        return sb.toString();
    }

    public static LuaCompilationUnit assembleAst(String fullScript) {
        LuaCompilationUnit cu = LuaAst.LuaCompilationUnit();
        cu.add(LuaAst.LuaLiteral(fullScript));
        return cu;
    }
}
