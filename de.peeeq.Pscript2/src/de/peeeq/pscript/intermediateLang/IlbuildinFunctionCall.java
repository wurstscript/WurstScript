package de.peeeq.pscript.intermediateLang;

import java.util.List;

import de.peeeq.pscript.utils.Utils;

public class IlbuildinFunctionCall extends ILStatementSet {

	private String funcName;
	private ILvar[] args;

	public IlbuildinFunctionCall(ILvar resultVar, String funcName, ILvar[] args) {
		super(resultVar);
		this.funcName = funcName;
		this.args = args;
	}

	public String getFuncName() {
		return funcName;
	}

	public List<ILvar> getArgs() {
		return Utils.list(args);
	}

}
