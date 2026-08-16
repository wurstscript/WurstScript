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

    public ImTypeVar getTypeVariable() {
        return typeVariable;
    }

    public WurstType getType() {
        return WurstTypeInfer.instance();
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        // Comparing this against a real value is refused by WurstOperator, which can see both
        // operands; doing it here would depend on which side the stand-in happened to land on.
        return other instanceof ILconstUnsafeDefault;
    }
}
