package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImFunction;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 */
public interface AbstractInterpreter {

    void runFuncRef(ILconstFuncRef obj, @Nullable Element trace);
}
