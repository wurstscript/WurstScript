package de.peeeq.wurstscript.translation.imtojass;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeCode;
import de.peeeq.wurstscript.types.PScriptTypeHandle;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeString;

public class ImAttrType {

	public static ImType getType(ImBoolVal e) {
		return PScriptTypeBool.instance().imTranslateType();
	}

	public static ImType getType(ImFuncRef e) {
		return PScriptTypeCode.instance().imTranslateType();
	}

	public static ImType getType(ImFunctionCall e) {
		return e.getFunc().getReturnType();
	}

	public static ImType getType(ImIntVal e) {
		return PScriptTypeInt.instance().imTranslateType();
	}

	public static ImType getType(ImNull e) {
		return PScriptTypeHandle.instance().imTranslateType();
	}

	public static ImType getType(ImOperatorCall e) {
		// TODO this is a lie
		return PScriptTypeReal.instance().imTranslateType();
	}

	public static ImType getType(ImRealVal e) {
		return PScriptTypeReal.instance().imTranslateType();
	}

	public static ImType getType(ImStatementExpr e) {
		return e.getExpr().attrTyp();
	}

	public static ImType getType(ImStringVal e) {
		return PScriptTypeString.instance().imTranslateType();
	}

	public static ImType getType(ImTupleSelection e) {
		ImTupleType tt = (ImTupleType) e.getTupleExpr().attrTyp();
		return JassIm.ImSimpleType(tt.getTypes().get(e.getTupleIndex()));
	}

	public static ImType getType(ImVarAccess e) {
		return e.getVar().getType();
	}

	public static ImType getType(ImVarArrayAccess e) {
		ImType ar = e.getVar().getType();
		if (ar instanceof ImArrayType) {
			ImArrayType t = (ImArrayType) ar;
			return JassIm.ImSimpleType(t.getTypename());
		} else if (ar instanceof ImTupleArrayType) {
			ImTupleArrayType t = (ImTupleArrayType) ar;
			return JassIm.ImTupleType(t.getTypes());
		}
		return ar;
	}

	public static ImType getType(ImTupleExpr imTupleExpr) {
		List<String> types = Lists.newArrayList();
		for (ImExpr e : imTupleExpr.getExprs()) {
			types.add(((ImSimpleType)e.attrTyp()).getTypename());
		}
		return JassIm.ImTupleType(types);
	}

}
