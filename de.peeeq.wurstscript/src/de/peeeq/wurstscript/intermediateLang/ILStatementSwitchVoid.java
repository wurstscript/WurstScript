package de.peeeq.wurstscript.intermediateLang;

public interface ILStatementSwitchVoid {

	void match(ILsetBinary ilbinary);

	void match(IlbuildinFunctionCall ilbuildinFunctionCall);

	void match(ILsetVar iLcopy);

	void match(ILexitwhen iLexitwhen);

	void match(ILfunctionCall iLfunctionCall);

	void match(ILif iLif);

	void match(ILloop iLloop);

	void match(ILreturn iLreturn);

	void match(IlsetConst ilsetConst);

	void match(IlsetUnary ilunary);


}
