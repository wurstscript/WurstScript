package de.peeeq.wurstscript.intermediateLang.interpreter;

import com.google.common.base.Supplier;

import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstAddable;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNum;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;

public class EvaluateBinaryOperator {

	public static ILconst eval(OpAnd opAnd, ILconst left, Supplier<ILconst> right) {
		return ILconstBool.instance(((ILconstBool)left).getVal() && ((ILconstBool)right.get()).getVal());
	}
	
	public static ILconst eval(OpOr opOr, ILconst left, Supplier<ILconst> right) {
		return ILconstBool.instance(((ILconstBool)left).getVal() || ((ILconstBool)right.get()).getVal());
	}

	public static ILconst eval(OpUnequals opUnequals, ILconst left, Supplier<ILconst> right) {
		return ILconstBool.instance(!left.equals(right.get()));
	}
	
	public static ILconst eval(OpEquals opEquals, ILconst left, Supplier<ILconst> right) {
		return ILconstBool.instance(left.equals(right.get()));
	}

	public static ILconst eval(OpPlus opPlus, ILconst left, Supplier<ILconst> right) {
		return ((ILconstAddable) left).add((ILconstAddable) right.get());
	}

	public static ILconst eval(OpMult opMult, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).mul((ILconstNum) right.get());
	}

	public static ILconst eval(OpMinus opMinus, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).sub((ILconstNum) right.get());
	}

	public static ILconst eval(OpLessEq opLessEq, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).lessEq((ILconstNum) right.get());
	}

	public static ILconst eval(OpLess opLess, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).less((ILconstNum) right.get());
	}

	public static ILconst eval(OpGreaterEq opGreaterEq, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).greaterEq((ILconstNum) right.get());
	}

	public static ILconst eval(OpGreater opGreater, ILconst left, Supplier<ILconst> right) {
		return ((ILconstNum) left).greater((ILconstNum) right.get());
	}

	

	
	public static ILconst eval(OpModReal opModReal, ILconst left, Supplier<ILconst> right) {
		return new ILconstReal(getReal(left) % getReal(right.get()));
	}

	public static ILconst eval(OpModInt opModInt, ILconst left, Supplier<ILconst> right) {
		return new ILconstInt(((ILconstInt)left).getVal() % ((ILconstInt)right.get()).getVal());
	}
	
	public static ILconst eval(OpDivReal opDivReal, ILconst left, Supplier<ILconst> right) {
		return new ILconstReal(getReal(left) / getReal(right.get()));
	}

	private static float getReal(ILconst c) {
		if (c instanceof ILconstReal) {
			return ((ILconstReal)c).getVal();
		} else if (c instanceof ILconstInt) {
			return ((ILconstInt) c).getVal();
		}
		throw new Error();
	}

	public static ILconst eval(OpDivInt opDivInt, ILconst left, Supplier<ILconst> right) {
		return new ILconstInt(((ILconstInt)left).getVal() / ((ILconstInt)right.get()).getVal());
	}

}
