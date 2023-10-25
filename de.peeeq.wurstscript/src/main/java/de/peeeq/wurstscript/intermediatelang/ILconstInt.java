package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInt;

import java.util.Objects;

public class ILconstInt extends ILconstAbstract implements ILconstNum {

    private int val;

    public ILconstInt(int intVal) {
        this.val = intVal;
    }


    @Override
    public String print() {
        return val + "";
    }

    public WurstType getType() {
        return WurstTypeInt.instance();
    }

    @Override
    public ILconstNum negate() {
        return create(-val);
    }

    public static ILconstInt create(int i) {
        return new ILconstInt(i);
    }


    @Override
    public ILconstNum add(ILconstAddable other) {
        if (other instanceof ILconstInt) {
            return create(val + ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstReal.create(val + ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }

    @Override
    public ILconstNum sub(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return create(val - ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstReal.create(val - ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }

    @Override
    public ILconstNum mul(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return create(val * ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstReal.create(val * ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }

    @Override
    public ILconstNum div(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return create(val / ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstReal.create(val / ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }


    public int getVal() {
        return val;
    }


    @Override
    public ILconstBool less(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return ILconstBool.instance(val < ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstBool.instance(val < ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }


    @Override
    public ILconstBool lessEq(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return ILconstBool.instance(val <= ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstBool.instance(val <= ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }


    @Override
    public ILconstBool greater(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return ILconstBool.instance(val > ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstBool.instance(val > ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }


    @Override
    public ILconstBool greaterEq(ILconstNum other) {
        if (other instanceof ILconstInt) {
            return ILconstBool.instance(val >= ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return ILconstBool.instance(val >= ((ILconstReal) other).getVal());
        } else {
            throw new Error();
        }
    }


    @Override
    public boolean isEqualTo(ILconst other) {
        if (other instanceof ILconstInt) {
            return (val == ((ILconstInt) other).getVal());
        } else if (other instanceof ILconstReal) {
            return (val == ((ILconstReal) other).getVal());
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ILconstInt that = (ILconstInt) o;
        return val == that.val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
