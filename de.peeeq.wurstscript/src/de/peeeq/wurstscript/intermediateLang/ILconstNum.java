package de.peeeq.wurstscript.intermediateLang;


public interface ILconstNum extends ILconstAddable {

	ILconstNum negate();
	ILconstNum sub(ILconstNum other);
	ILconstNum mul(ILconstNum other);
	ILconstNum div(ILconstNum other);
	ILconstBool less(ILconstNum other);
	ILconstBool lessEq(ILconstNum other);
	ILconstBool greater(ILconstNum other);
	ILconstBool greaterEq(ILconstNum other);
}