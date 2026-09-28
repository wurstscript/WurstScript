package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

/**
 * An optimizer pass which can share a local-player dependency analysis with
 * adjacent passes that preserve the analysis' conservative facts.
 */
public interface LocalPlayerAwareOptimizerPass extends OptimizerPass {

    int optimize(ImTranslator trans, LocalPlayerContextAnalyzer analyzer);

    @Override
    default int optimize(ImTranslator trans) {
        return optimize(trans, new LocalPlayerContextAnalyzer(trans.getImProg()));
    }
}
