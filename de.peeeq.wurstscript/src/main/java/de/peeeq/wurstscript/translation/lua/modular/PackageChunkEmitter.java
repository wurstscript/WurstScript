package de.peeeq.wurstscript.translation.lua.modular;

import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.lua.translation.LuaTranslator;

import java.io.File;

public class PackageChunkEmitter {

    public static PackageChunkResult emitAndAssemble(ImProg imProg, ImTranslator imTranslator, File cacheDir) {
        PackageChunkCache cache = new PackageChunkCache(cacheDir);
        LuaTranslator translator = new LuaTranslator(imProg, imTranslator);
        return translator.translateModular(cache);
    }
}
