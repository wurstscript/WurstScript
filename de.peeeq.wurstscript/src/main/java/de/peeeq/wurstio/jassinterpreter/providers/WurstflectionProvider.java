package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.jassIm.ImProg;

import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.calculateClassName;
import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.calculateMaxTypeId;

public class WurstflectionProvider extends Provider {

    public WurstflectionProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }


    public ILconstString typeIdToTypeName(ILconstInt typeId) {
        ImProg prog = interpreter.getImProg();
        int typeIdInt = typeId.getVal();
        return prog.attrTypeId().entrySet()
            .stream()
            .filter(e -> e.getValue() == typeIdInt)
            .map(e -> new ILconstString(calculateClassName(e.getKey())))
            .findFirst()
            .orElseGet(() -> {
                throw new InterpreterException("Could not determine type name for id " + typeId);
            });

    }

    public ILconstInt maxTypeId() {
        return new ILconstInt(calculateMaxTypeId(interpreter.getImProg()));
    }

    public ILconstInt instanceCount(ILconstInt typeId) {
        return new ILconstInt(interpreter.getInstanceCount(typeId.getVal()));
    }

    public ILconstInt maxInstanceCount(ILconstInt typeId) {
        return new ILconstInt(interpreter.getMaxInstanceCount(typeId.getVal()));
    }




}
