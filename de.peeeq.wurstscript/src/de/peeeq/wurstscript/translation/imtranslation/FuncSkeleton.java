package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class FuncSkeleton {

	public static void create(ConstructorDef constr, ImTranslator translator, ImFunction f) {
		f.setReturnType(TypesHelper.imInt());
		ImHelper.translateParameters(constr.getParameters(), f.getParameters(), translator);
	}

	public static void create(ExtensionFuncDef funcDef, ImTranslator translator, ImFunction f) {
		// return type:
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		// parameters
		ImVar thisVar = translator.getThisVar(funcDef);
		thisVar.setType(funcDef.getExtendedType().attrTyp().imTranslateType());
		f.getParameters().add(thisVar);
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
	}

	public static void create(FuncDef funcDef, ImTranslator translator, ImFunction f) {
		// return type:
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		// parameters
		if (funcDef.attrIsDynamicClassMember()) {
			ImVar thisVar = translator.getThisVar(funcDef);
			f.getParameters().add(thisVar);
		}
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
	}

	public static void create(InitBlock initBlock, ImTranslator translator, ImFunction f) {
	}

	public static void create(NativeFunc funcDef, ImTranslator translator, ImFunction f) {
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters(), translator);
	}

	public static void create(TupleDef tupleDef, ImTranslator translator, ImFunction f) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public static void create(OnDestroyDef onDestroyDef, ImTranslator translator, ImFunction f) {
	}

}
