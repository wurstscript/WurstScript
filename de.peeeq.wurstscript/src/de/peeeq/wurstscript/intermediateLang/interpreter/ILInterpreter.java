package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassinterpreter.ReturnException;

public class ILInterpreter {
	private final ImProg prog;
	private ProgramState globalState;

	public ILInterpreter(ImProg prog) {
		this.prog = prog;
		this.globalState = new ProgramState();
	}
	
	public static ILconst runFunc(ProgramState globalState, ImFunction f, ILconst ... args) {
		LocalState localState = new LocalState();
		int i = 0;
		for (ImVar p : f.getParameters()) {
			localState.setVal(p, args[i]);
			i++;
		}
		try {
			f.getBody().runStatements(globalState, localState);
		} catch (ReturnException e) {
			return e.getVal();
		}
		return ILconstNull.instance();
	}


}
