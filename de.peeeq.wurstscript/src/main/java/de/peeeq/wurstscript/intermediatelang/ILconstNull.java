package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInfer;
import org.eclipse.jdt.annotation.Nullable;

public class ILconstNull extends ILconstAbstract implements ILconstAddable {


    private static ILconstNull instance = new ILconstNull();

    private ILconstNull() {
    }

    @Override
    public String print() {
        return "null";
    }

    public WurstType getType() {
        return WurstTypeInfer.instance();
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        return other instanceof ILconstNull;
    }

    public static ILconstNull instance() {
        return instance;
    }

    @Override
    public ILconstAddable add(ILconstAddable other) {
        if (other instanceof ILconstNull) {
            return this;
        } else if (other instanceof ILconstString) {
            return other;
        } else {
            throw new Error("unsupported: " + other + " // " + other.getClass());
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

}
