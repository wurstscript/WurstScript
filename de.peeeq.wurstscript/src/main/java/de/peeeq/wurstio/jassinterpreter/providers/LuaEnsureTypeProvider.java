package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

/**
 * Interpreter-side implementations of the raw Lua-primitive leaves that the
 * portable Lua ensureType/div/mod helpers delegate to (see
 * de.peeeq.wurstscript.translation.imtranslation.LuaEnsureFunctions and
 * LuaNativeLowering#lowerDivMod). Those helpers are plain, non-native IM
 * functions now - the interpreter runs their body directly - so only the
 * handful of leaves with no IM equivalent (Lua's {@code //} floor division,
 * {@code math.fmod}, {@code tonumber}, {@code math.tointeger}, string
 * concatenation) still need a native implementation here.
 *
 * <p>The interpreter's own type system already guarantees these are only
 * ever called with a genuinely-typed, non-null argument (the callers' nil
 * checks run as regular interpreted control flow first), so unlike the real
 * Lua runtime these can be simple, non-defensive passthroughs.
 */
public class LuaEnsureTypeProvider extends Provider {
    public LuaEnsureTypeProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstInt __wurst_rawToNumberInt(ILconstInt x) {
        return x;
    }

    public ILconstInt __wurst_rawToInteger(ILconstInt x) {
        return x;
    }

    public ILconstReal __wurst_rawToNumberReal(ILconstReal x) {
        return x;
    }

    public ILconstString __wurst_rawToString(ILconstString x) {
        return x;
    }

    public ILconstString __wurst_rawConcat(ILconstString x, ILconstString y) {
        return new ILconstString(x.getVal() + y.getVal());
    }

    public ILconstInt __wurst_rawFloorDivInt(ILconstInt a, ILconstInt b) {
        return ILconstInt.create(Math.floorDiv(a.getVal(), b.getVal()));
    }

    public ILconstInt __wurst_rawFmodInt(ILconstInt a, ILconstInt b) {
        return ILconstInt.create(a.getVal() % b.getVal());
    }

    public ILconstReal __wurst_rawFmodReal(ILconstReal a, ILconstReal b) {
        return ILconstReal.create(a.getVal() % b.getVal());
    }
}
