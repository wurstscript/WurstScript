package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public interface OptimizerPass {
  int optimize(ImTranslator trans);

  String getName();
}
