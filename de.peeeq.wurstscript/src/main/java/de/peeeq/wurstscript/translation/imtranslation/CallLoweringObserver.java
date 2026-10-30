package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImFunctionCall;

/** A producer reports a call replacement while both its source and lowered target are available. */
@FunctionalInterface
public interface CallLoweringObserver {
    CallLoweringObserver NONE = (source, lowered) -> { };
    void replaced(ImFunctionCall source, ImFunctionCall lowered);
}
