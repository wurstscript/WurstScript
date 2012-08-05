package de.peeeq.wurstscript.translation.imtojass;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;

public class ImAttributes {

	public static ImFunction getNearestFunc(JassImElement e ) {
		while (e != null && !(e instanceof ImFunction)) {
			e = e.getParent();
		}
		return (ImFunction) e;
	}

	
	public static String translateTypeFirst(ImTupleType t) {
		return t.getTypes().get(0);
	}

	public static String translateTypeFirst(ImVoid t) {
		return "nothing";
	}

	public static String translateTypeFirst(ImArrayType t) {
		return t.getTypename();
	}

	public static String translateTypeFirst(ImSimpleType t) {
		return t.getTypename();
	}


	public static List<String> translateType(ImArrayType t) {
		return Collections.singletonList(t.translateTypeFirst());
	}


	public static List<String> translateType(ImSimpleType t) {
		return Collections.singletonList(t.translateTypeFirst());
	}


	public static List<String> translateType(ImTupleType t) {
		List<String> result = Lists.newArrayList();
		for (String p : t.getTypes()) {
			result.add(p);
		}
		return result;
	}


	public static List<String> translateType(ImVoid t) {
		return Collections.singletonList(t.translateTypeFirst());
	}


	public static String translateTypeFirst(ImTupleArrayType t) {
		return t.getTypes().get(0);
	}


	public static List<String> translateType(ImTupleArrayType t) {
		List<String> result = Lists.newArrayList();
		for (String p : t.getTypes()) {
			result.add(p);
		}
		return result;
	}


	public static boolean isGlobal(ImVar imVar) {
		return imVar.getParent().getParent() instanceof ImProg;
	}


	public static boolean isBj(ImFunction f) {
		return f.getFlags().contains(FunctionFlag.IS_BJ);
	}
	

	public static boolean isNative(ImFunction f) {
		return f.getFlags().contains(FunctionFlag.IS_NATIVE);
	}
	
	public static boolean isCompiletime(ImFunction f) {
		return f.getFlags().contains(FunctionFlag.IS_COMPILETIME);
	}
	
}
