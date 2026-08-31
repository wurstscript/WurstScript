package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.validation.NamePreservation;

import java.util.HashSet;
import java.util.Set;

public class ImCompressor {

    private final ImTranslator trans;
    private final ImProg prog;
    private final NameGenerator ng;
    private final Set<String> preservedNames = new HashSet<>();

    public ImCompressor(ImTranslator translator) {
        this.trans = translator;
        this.prog = translator.getImProg();
        ng = new NameGenerator();
        for (ImVar global : prog.getGlobals()) {
            if (NamePreservation.isPreserved(global)) {
                preservedNames.add(global.getName());
            }
        }
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            if (NamePreservation.isPreserved(function)) {
                preservedNames.add(function.getName());
            }
        }
    }

    public void compressNames() {
        compressGlobals();
        compressFunctions();
    }


    public void compressGlobals() {
        for (final ImVar global : prog.getGlobals()) {
            if (global.getIsBJ() || NamePreservation.isPreserved(global)) {
                // do not rename bj constants or names exposed to Warcraft III
                continue;
            }

            String replacement = nextCompressedName();

            global.setName(replacement);
        }
    }

    public void compressFunctions() {
        for (ImFunction func : ImHelper.calculateFunctionsOfProg(prog)) {
            if (func.isNative() || func.isBj() || func.isCompiletime() || func.isExtern()
                || NamePreservation.isPreserved(func)) {
                // do not rename builtin an bj functions
                continue;
            }
            compressLocals(func);
            if (func.getName().equals("main") || func.getName().equals("config")) {
                // do not rename main and config functions
                continue;
            }
            String rname = nextCompressedName();
            func.setName(rname);
        }

    }

    private String nextCompressedName() {
        String replacement;
        do {
            replacement = ng.getUniqueToken();
        } while (preservedNames.contains(replacement));
        return replacement;
    }

    private void compressLocals(ImFunction func) {
        // TODO compressing locals should not use the global name pool but use a own pool
        for (ImVar local : func.getParameters()) {
            local.setName(ng.getUniqueToken());
        }
        for (ImVar local : func.getLocals()) {
            local.setName(ng.getUniqueToken());
        }
    }
}
