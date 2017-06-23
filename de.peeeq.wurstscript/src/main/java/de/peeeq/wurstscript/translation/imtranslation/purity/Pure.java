package de.peeeq.wurstscript.translation.imtranslation.purity;

public class Pure extends PurityLevel {
    static Pure instance = new Pure();

    private Pure() {
    }

    @Override
    public PurityLevel merge(PurityLevel other) {
        return other;
    }
}
