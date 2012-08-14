package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.types.WurstType;

public class ILconstString extends ILconstAbstract implements ILconstAddable {

	private String val; // including the quotes

	public ILconstString(String strVal) {
		this.val = strVal;
	}

	public String getVal() {
		return val;
	}
	
	@Override
	public String print() {
		return "\"" + val + "\"";
	}

	public WurstType getType() {
		return WurstTypeString.instance();
	}

	@Override
	public ILconstAddable add(ILconstAddable other) {
		return new ILconstString(val + ((ILconstString)other).val);
	}

	@Override
	public boolean isEqualTo(ILconst other) {
		if (other instanceof ILconstString) {
			return ((ILconstString) other).val.equals(val);
		}
		return false;
	}
	
}
