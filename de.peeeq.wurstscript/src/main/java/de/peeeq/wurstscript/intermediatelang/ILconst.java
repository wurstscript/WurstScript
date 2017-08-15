package de.peeeq.wurstscript.intermediatelang;

public interface ILconst {

    String print();

    boolean isEqualTo(ILconst other);

    @Override
    String toString();

}
