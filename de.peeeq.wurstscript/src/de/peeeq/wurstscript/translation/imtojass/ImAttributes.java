package de.peeeq.wurstscript.translation.imtojass;

import org.eclipse.jdt.annotation.NonNull;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImArrayTypeMulti;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.jassIm.ElementWithTrace;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;

public class ImAttributes {


	public static ImFunction getNearestFunc(Element e ) {
		while (e != null && !(e instanceof ImFunction)) {
			e = e.getParent();
		}
		return (ImFunction) e;
	}

	
	public static String translateType(ImArrayType t) {
		return t.getTypename();
	}
	
	public static String translateType(ImArrayTypeMulti imArrayTypeMulti) {
		throw new Error("multi-arrays should be eliminated in earlier phase");
	}


	public static String translateType(ImSimpleType t) {
		return t.getTypename();
	}


	public static String translateType(ImTupleType t) {
		throw new Error("tuples should be eliminated in earlier phase");
	}


	public static String translateType(ImVoid t) {
		return "nothing";
	}


	


	public static String translateType(ImTupleArrayType t) {
		throw new Error("tuples should be eliminated in earlier phase");
	}


	public static boolean isGlobal(ImVar imVar) {
		return imVar.getParent().getParent() instanceof ImProg;
	}


	public static boolean isBj(ImFunction f) {
		return f.getFlags().contains(FunctionFlagEnum.IS_BJ);
	}
	
	public static boolean isExtern(ImFunction f) {
		return f.getFlags().contains(FunctionFlagEnum.IS_EXTERN);
	}

	public static boolean isNative(ImFunction f) {
		return f.getFlags().contains(FunctionFlagEnum.IS_NATIVE);
	}
	
	public static boolean isCompiletime(ImFunction f) {
		return f.getFlags().contains(FunctionFlagEnum.IS_COMPILETIME);
	}

	
	public static de.peeeq.wurstscript.ast.Element getTrace(ElementWithTrace t) {
		return t.getTrace();
	}
	
	public static de.peeeq.wurstscript.ast.Element getTrace(Element t) {
		if (t.getParent() != null) {
			return t.getParent().attrTrace();
		}
		return Ast.NoExpr(); 
	}


	public static boolean hasFlag(ImFunction f, FunctionFlag flag) {
		return f.getFlags().contains(flag);
	}


	public static ImProg getProg(Element el) {
		Element e = el;
		while (e  != null) {
			if (e instanceof ImProg) {
				return (ImProg) e;
			}
			e = e.getParent();
		}
		throw new Error("Element "+ el + " not attached to root.");
	}


	public static ImClass attrClass(ImMethod m) {
		return (ImClass) m.getParent().getParent();
	}







	
	
}
