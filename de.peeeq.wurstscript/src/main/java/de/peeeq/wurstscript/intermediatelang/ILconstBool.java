package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBool;

import java.util.Objects;

public class ILconstBool extends ILconstAbstract {

    private final boolean val;
    // Tracks the corresponding runtime value while compiletime code evaluates lazy initializers.
    private final boolean runtimeVal;
    private final boolean runtimeValKnown;

    public final static ILconstBool FALSE = new ILconstBool(false, false, true);
    public final static ILconstBool TRUE = new ILconstBool(true, true, true);

    public static ILconstBool instance(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static ILconstBool withRuntimeValue(boolean value, boolean runtimeValue) {
        if (value == runtimeValue) {
            return instance(value);
        }
        return new ILconstBool(value, runtimeValue, true);
    }

    public static ILconstBool withUnknownRuntimeValue(boolean value) {
        return new ILconstBool(value, false, false);
    }

    private ILconstBool(boolean val, boolean runtimeVal, boolean runtimeValKnown) {
        this.val = val;
        this.runtimeVal = runtimeVal;
        this.runtimeValKnown = runtimeValKnown;
    }

    public boolean getVal() {
        return val;
    }

    public boolean getRuntimeVal() {
        return runtimeVal;
    }

    public boolean isRuntimeValKnown() {
        return runtimeValKnown;
    }

    public boolean canDifferAtRuntime() {
        return !runtimeValKnown || val != runtimeVal;
    }

    @Override
    public String print() {
        return val ? "true" : "false";
    }


    public WurstType getType() {
        return WurstTypeBool.instance();
    }

    public ILconst negate() {
        if (!runtimeValKnown) {
            return withUnknownRuntimeValue(!val);
        }
        return withRuntimeValue(!val, !runtimeVal);
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        return other instanceof ILconstBool && val == ((ILconstBool) other).val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
