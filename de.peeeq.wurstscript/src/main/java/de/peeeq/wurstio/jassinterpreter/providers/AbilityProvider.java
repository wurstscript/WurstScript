package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class AbilityProvider extends Provider {
    public AbilityProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstString BlzGetAbilityIcon(ILconstInt abilCode) {
        return new ILconstString("");
    }

    public ILconstString BlzGetAbilityExtendedTooltip(ILconstInt abilCode, ILconstInt level) {
        return new ILconstString("");
    }
}
