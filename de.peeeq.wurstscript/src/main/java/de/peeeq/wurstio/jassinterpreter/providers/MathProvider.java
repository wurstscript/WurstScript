package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.Random;

public class MathProvider extends Provider {
    private Random r = new Random();

    public MathProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstReal SquareRoot(ILconstReal r) {
        return new ILconstReal(Math.sqrt(r.getVal()));
    }

    public ILconstReal Pow(ILconstReal x, ILconstReal power) {
        return new ILconstReal(Math.pow(x.getVal(), power.getVal()));
    }

    public ILconstReal Sin(ILconstReal r) {
        return new ILconstReal(Math.sin(r.getVal()));
    }

    public ILconstReal Asin(ILconstReal r) {
        return new ILconstReal(Math.asin(r.getVal()));
    }

    public ILconstReal Cos(ILconstReal r) {
        return new ILconstReal(Math.cos(r.getVal()));
    }

    public ILconstReal Acos(ILconstReal r) {
        return new ILconstReal(Math.acos(r.getVal()));
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
        return new ILconstReal(a.getVal() + r.nextFloat() * (b.getVal() - a.getVal()));
    }

    public ILconstInt GetRandomInt(ILconstInt a, ILconstInt b) {
        return new ILconstInt(a.getVal() + r.nextInt(1 + b.getVal() - a.getVal()));
    }

    public ILconstInt ModuloInteger(ILconstInt a, ILconstInt b) {
        return new ILconstInt(a.getVal() % b.getVal());
    }

    public ILconstReal ModuloReal(ILconstReal a, ILconstReal b) {
        return new ILconstReal(a.getVal() % b.getVal());
    }
}
