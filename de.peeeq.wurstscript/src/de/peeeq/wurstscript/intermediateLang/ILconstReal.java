package de.peeeq.wurstscript.intermediateLang;


import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PscriptType;


public class ILconstReal extends ILconstAbstract implements ILconstNum {

	private float val;

	public ILconstReal(String numVal) {
		this.val = Float.parseFloat(numVal);
	}

	public ILconstReal(double numVal) {
		this.val = (float) numVal;
	}
	

	@Override
	public String print() {
		return val + "";
	}

	public PscriptType getType() {
		return PScriptTypeReal.instance();
	}

	@Override
	public ILconstNum negate() {
		return create(-val);
	}

	static ILconstReal create(float f) {
		return new ILconstReal(f);
	}

	@Override
	public ILconstNum add(ILconstAddable other) {
		if (other instanceof ILconstInt) {
			return create(val + ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return create(val + ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum sub(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val - ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return create(val - ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum mul(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val * ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return create(val * ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	@Override
	public ILconstNum div(ILconstNum other) {
		if (other instanceof ILconstInt) {
			return create(val / ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return create(val / ((ILconstReal) other).getVal());
		} else {
			throw new Error();
		}
	}

	public float getVal() {
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
			return (val >= ((ILconstInt) other).getVal());
		} else if (other instanceof ILconstReal) {
			return (val >= ((ILconstReal) other).getVal());
		} else {
			return false;
		}
	}

	

	
}
