package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
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
        if (other instanceof ILconstUnsafeDefault) {
            return true;
        }
        // Answering "not equal" is how this stays quiet. The value is the default of a type
        // parameter whose binding was not known where the value was produced, so it is not
        // comparable to anything concrete. ProgramState.resolveDefault replaces it wherever the
        // frames that know the binding are in reach; getting here means a path it does not cover.
        throw new InterpreterException("The default value of type parameter " + typeVariable.getName()
            + " is not known here, so it cannot be compared to " + other.print() + ".");
    }

}
