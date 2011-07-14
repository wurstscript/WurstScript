package de.peeeq.pscript.intermediateLang;

public interface ILStatementSwitchVoid {

	void match(Ilbinary ilbinary);

	void match(IlbuildinFunctionCall ilbuildinFunctionCall);

	void match(ILcopy iLcopy);

	void match(ILexitwhen iLexitwhen);

	void match(ILfunctionCall iLfunctionCall);

	void match(ILif iLif);

	void match(ILloop iLloop);

	void match(ILreturn iLreturn);

	void match(IlsetConst ilsetConst);

	void match(Ilunary ilunary);


}
