package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class MathProvider extends Provider {
    private final JassPrng random = new JassPrng();

    public MathProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    // Measured on the 3.0.0 client: outside their domain SquareRoot, Asin and Acos give 0, not NaN.

    public ILconstReal SquareRoot(ILconstReal r) {
        return new ILconstReal(r.getVal() < 0 ? 0 : Math.sqrt(r.getVal()));
    }

    /** Measured on the 3.0.0 client: a negative base with a non-integer exponent takes the absolute value
     *  (Pow(-8, 1/3) is 2), an integer exponent keeps the sign (Pow(-2, 3) is -8), and Pow(0, -1) is 0. */
    public ILconstReal Pow(ILconstReal x, ILconstReal power) {
        double base = x.getVal();
        double exponent = power.getVal();
        if (base == 0 && exponent < 0) {
            return new ILconstReal(0);
        }
        if (base < 0 && exponent != Math.rint(exponent)) {
            base = -base;
        }
        return new ILconstReal(Math.pow(base, exponent));
    }

    public ILconstReal Sin(ILconstReal r) {
        return new ILconstReal(Math.sin(r.getVal()));
    }

    public ILconstReal Asin(ILconstReal r) {
        return new ILconstReal(Math.abs(r.getVal()) > 1 ? 0 : Math.asin(r.getVal()));
    }

    public ILconstReal Cos(ILconstReal r) {
        return new ILconstReal(Math.cos(r.getVal()));
    }

    public ILconstReal Acos(ILconstReal r) {
        return new ILconstReal(Math.abs(r.getVal()) > 1 ? 0 : Math.acos(r.getVal()));
    }

    public ILconstReal Tan(ILconstReal r) {
        return new ILconstReal(Math.tan(r.getVal()));
    }

    public ILconstReal Atan(ILconstReal r) {
        return new ILconstReal(Math.atan(r.getVal()));
    }

    public ILconstReal Atan2(ILconstReal y, ILconstReal x) {
        return new ILconstReal(Math.atan2(y.getVal(), x.getVal()));
    }

    public ILconstReal GetRandomReal(ILconstReal a, ILconstReal b) {
        return new ILconstReal(random.getRandomReal(a.getVal(), b.getVal()));
    }

    public ILconstInt GetRandomInt(ILconstInt a, ILconstInt b) {
        return new ILconstInt(random.getRandomInt(a.getVal(), b.getVal()));
    }

    public void SetRandomSeed(ILconstInt seed) {
        random.setRandomSeed(seed.getVal());
    }

    public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
        // must match Blizzard.j's ModuloInteger (truncated remainder, plus
        // divisor if negative), which is what the game executes at runtime
        return new ILconstInt(WurstOperator.moduloInteger(a.getVal(), b.getVal()));
    }

    public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
        return new ILconstReal(WurstOperator.moduloReal(a.getVal(), b.getVal()));
    }
}
