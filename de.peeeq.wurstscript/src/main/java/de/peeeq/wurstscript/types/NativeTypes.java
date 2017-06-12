package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.ILconstString;

public class NativeTypes {
	/**
	 * returns the WurstScriptType for a given nativetype definition
	 * @param b 
	 */
	public static @Nullable WurstType nativeType(String typeName, boolean isJassCode) {
		if (typeName.equals("int") || typeName.equals("integer")) {
			return WurstTypeInt.instance();
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

	
}
