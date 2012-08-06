package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public interface NativesProvider {

	ILconst invoke(String funcname, ILconst[] args);

}
