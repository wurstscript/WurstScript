package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PscriptType;

public class ILconstInt extends ILconstAbstract implements ILconstNum {

	private int val;

	public ILconstInt(int intVal) {
		this.val = intVal;
	}

	
	@Override
	public String print() {
		return val+"";
	}
	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append(print());
	}
	
	@Override
	public PscriptType getType() {
		return PScriptTypeInt.instance();
	}

	@Override
	public ILconstNum negate() {
		return create(-val);
	}

	public static ILconstNum create(int i) {
		return new ILconstInt(i);
	}


	@Override
	public ILconstNum add(ILconstAddable other) {
		if (other instanceof ILconstInt) {
			return create(val + ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstReal.create(val + ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum sub(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val - ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstReal.create(val - ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum mul(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val * ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstReal.create(val * ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum div(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val / ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstReal.create(val / ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}


	public int getVal() {
		return val;
	}


	@Override
	public ILconstBool less(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return ILconstBool.instance(val < ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstBool.instance(val < ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}


	@Override
	public ILconstBool lessEq(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return ILconstBool.instance(val <= ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstBool.instance(val <= ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}


	@Override
	public ILconstBool greater(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return ILconstBool.instance(val > ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstBool.instance(val > ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}


	@Override
	public ILconstBool greaterEq(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return ILconstBool.instance(val >= ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return ILconstBool.instance(val >= ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}


	@Override
	public boolean isEqualTo(ILconst other) {
		if (other instanceof ILconstInt) {
			return (val == ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return (val == ((ILconstReal) other).getVal());
		} else {
			return false;
		}
	}



}
