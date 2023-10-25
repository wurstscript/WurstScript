package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.jassIm.ImTypeVar;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInfer;

public class ILconstUnsafeDefault extends ILconstAbstract {


    private final ImTypeVar typeVariable;

    public ILconstUnsafeDefault(ImTypeVar typeVariable) {
        this.typeVariable = typeVariable;
    }

    @Override
    public String print() {
        return "unsafe-default<" + typeVariable.getName() + ">";
    }

    public WurstType getType() {
        return WurstTypeInfer.instance();
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        return other instanceof ILconstUnsafeDefault;
    }

}
