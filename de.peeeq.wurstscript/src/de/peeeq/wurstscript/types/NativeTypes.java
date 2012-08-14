package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;

public class NativeTypes {
	/**
	 * returns the PscriptType for a given nativetype definition
	 * @param b 
	 */
	public static WurstType nativeType(String typeName, boolean isJassCode) {
		if (typeName.equals("int") || typeName.equals("integer")) {
			if (isJassCode) {
				return WurstTypeJassInt.instance();
			} else {
				return WurstTypeInt.instance();
			}
		}
		if (typeName.equals("bool") || typeName.equals("boolean")) {
			return WurstTypeBool.instance();
		}
		if (typeName.equals("real")) {
			return WurstTypeReal.instance();
		}
		if (typeName.equals("string")) {
			return WurstTypeString.instance();
		}
		if (typeName.equals("code")) {
			return WurstTypeCode.instance();
		}
		if (typeName.equals("handle")) {
			return WurstTypeHandle.instance();
		}
		return null;
	}

	public static ILconst getDefaultValue(WurstType type) {
		if (type.isSubtypeOf(WurstTypeInt.instance(), null)) {
			return new ILconstInt(0);
		}
		if (type.isSubtypeOf(WurstTypeBool.instance(), null)) {
			return ILconstBool.FALSE;
		}
		if (type.isSubtypeOf(WurstTypeReal.instance(), null)) {
			return new ILconstReal(0.0);
		}
		if (type.isSubtypeOf(WurstTypeString.instance(), null)) {
			return new ILconstString(null);
		}
		if (type.isSubtypeOf(WurstTypeCode.instance(), null)) {
			return ILconstNull.instance();
		}
		if (type.isSubtypeOf(WurstTypeHandle.instance(), null)) {
			return ILconstNull.instance();
		}
		if (type instanceof WurstTypeClass) {
			return new ILconstInt(0);
		}
		throw new Error("default value for type " + type + " not implemented");
	}
}
