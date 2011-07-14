package de.peeeq.pscript.intermediateLang;

public abstract class ILStatement {

	public void doSwitchVoid(ILStatementSwitchVoid s) {
		
		if (this instanceof Ilbinary) s.match((Ilbinary) this);
		else if (this instanceof IlbuildinFunctionCall) s.match((IlbuildinFunctionCall) this);
		else if (this instanceof ILcopy) s.match((ILcopy) this);
		else if (this instanceof ILexitwhen) s.match((ILexitwhen) this);
		else if (this instanceof ILfunctionCall) s.match((ILfunctionCall) this);
		else if (this instanceof ILif) s.match((ILif) this);
		else if (this instanceof ILloop) s.match((ILloop) this);
		else if (this instanceof ILreturn) s.match((ILreturn) this);
		else if (this instanceof IlsetConst) s.match((IlsetConst) this);
		else if (this instanceof Ilunary) s.match((Ilunary) this);
		else throw new Error("no case matched.");
	}

}
