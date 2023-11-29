package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeString;

import java.util.Objects;

public class ILconstString extends ILconstAbstract implements ILconstAddable {

    private final String val; // including the quotes

    public ILconstString(String strVal) {
        this.val = strVal;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String print() {
        return "\"" + val + "\"";
    }

    public WurstType getType() {
        return WurstTypeString.instance();
    }

    @Override
    public ILconstAddable add(ILconstAddable other) {
        if (other instanceof ILconstNull) {
            return this;
        }
        return new ILconstString(val + ((ILconstString) other).val);
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        if (other instanceof ILconstString) {
            return ((ILconstString) other).val.equals(val);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
