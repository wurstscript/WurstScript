package de.peeeq.wurstscript.translation.imtojass;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstBool;
import de.peeeq.wurstscript.intermediateLang.ILconstInt;
import de.peeeq.wurstscript.intermediateLang.ILconstNull;
import de.peeeq.wurstscript.intermediateLang.ILconstReal;
import de.peeeq.wurstscript.intermediateLang.interpreter.ILconstError;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassIm;

public class DefaultValue {

	public static ILconst get(ImArrayType t) {
		return JassIm.ImSimpleType(t.getTypename()).defaultValue();
	}

	public static ILconst get(ImSimpleType t) {
		String typename = t.getTypename();
		if (typename.equals("string")) return ILconstNull.instance();
		if (typename.equals("integer")) return new ILconstInt(0);
		if (typename.equals("real" )) return new ILconstReal(0);
		if (typename.equals("boolean")) return ILconstBool.FALSE;
		return new ILconstError(typename);
	}

	public static ILconst get(ImTupleArrayType t) {
		throw new Error();
	}

	public static ILconst get(ImTupleType t) {
		throw new Error();
	}

	public static ILconst get(ImVoid t) {
		throw new Error();
	}

}
