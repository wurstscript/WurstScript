package de.peeeq.wurstscript.intermediatelang;

import org.eclipse.jdt.annotation.Nullable;

public abstract class ILconstAbstract implements ILconst {

    @Override
    public abstract String print();


    @Override
    public String toString() {
        return print();
    }


    @Override
    public boolean equals(@Nullable Object other) {
        if (other instanceof ILconst) {
            return isEqualTo((ILconst) other);
        }
        return false;
    }


    @Override
    public int hashCode() {
        throw new Error("hashcode not implemented for " + getClass() + "/" + this);
    }

}
