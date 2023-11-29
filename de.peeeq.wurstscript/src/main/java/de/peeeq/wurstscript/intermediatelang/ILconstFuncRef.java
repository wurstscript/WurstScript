package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeCode;

public class ILconstFuncRef extends ILconstAbstract {

    private ImFunction func = null;

    private final String funcName;
    public ILconstFuncRef(String funcName) {
        this.funcName = funcName;
    }

    public ILconstFuncRef(ImFunction func) {
        this.func = func;
        this.funcName = func.getName();
    }

    public ImFunction getFunc() {
        return func;
    }

    @Override
    public String print() {
        return "function " + funcName;
    }


    public WurstType getType() {
        return WurstTypeCode.instance();
    }

    public String getFuncName() {
        return funcName;
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        if (other instanceof ILconstFuncRef) {
            ILconstFuncRef f = (ILconstFuncRef) other;
            return f.funcName.equals(funcName);
        }
        return false;
    }

}
