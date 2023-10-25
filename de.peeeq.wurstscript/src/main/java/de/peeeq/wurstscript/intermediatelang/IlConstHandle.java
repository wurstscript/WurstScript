package de.peeeq.wurstscript.intermediatelang;

public class IlConstHandle implements ILconst {

    private final String name;
    private final Object obj;


    public IlConstHandle(String name, Object obj) {
        this.name = name;
        this.obj = obj;
    }

    @Override
    public String print() {
        return name;
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        if (other == this) return true;
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    public Object getObj() {
        return obj;
    }

}
