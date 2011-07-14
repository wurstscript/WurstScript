package de.peeeq.pscript.intermediateLang;

import java.util.List;

import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.utils.Utils;

public class ILfunctionCall extends ILStatement {

	private String name;
	private PscriptType[] argumentTypes;
	private ILvar[] argumentVars;
	private ILvar resultVar;

	public ILvar getResultVar() {
		return resultVar;
	}
	
	public String getName() {
		return name;
	}

	public PscriptType[] getArgumentTypes() {
		return argumentTypes;
	}

	public List<ILvar> getArgs() {
		return Utils.list(argumentVars);
	}

	public ILfunctionCall(ILvar resultVar, String name, PscriptType[] argumentTypes,
			ILvar[] argumentVars) {
		this.resultVar = resultVar;
		this.name = name;
		this.argumentTypes = argumentTypes;
		this.argumentVars = argumentVars;
	}

}
