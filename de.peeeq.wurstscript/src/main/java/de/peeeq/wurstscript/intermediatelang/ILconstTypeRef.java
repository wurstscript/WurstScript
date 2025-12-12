package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.jassIm.ImClassType;

public final class ILconstTypeRef implements ILconst {
    private final ImClassType type; // usually

    public ILconstTypeRef(ImClassType type) {
        this.type = type;
    }

    public ImClassType getType() {
        return type;
    }

    @Override
    public String print() {
        return "";
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        return false;
    }

    @Override public String toString() {
        return "type(" + type + ")";
    }
}
