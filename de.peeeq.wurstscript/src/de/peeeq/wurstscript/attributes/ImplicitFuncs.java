package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WEntities;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;

public class ImplicitFuncs {

	public static FuncDef findToIndexFunc(WurstType typ, AstElement e) {
		typ = typ.normalize();
		for (NameLink nl : findToIndexFuncs(typ, e)) {
			if (nl.getNameDef() instanceof FuncDef) {
				return (FuncDef) nl.getNameDef();
			}
		}
		throw new CompileError(e.attrSource(), "Could not find function " + toIndexFuncName(typ));
	}
	
	public static FuncDef findFromIndexFunc(WurstType typ, AstElement e) {
		typ = typ.normalize();
		for (NameLink nl : findFromIndexFuncs(typ, e)) {
			if (nl.getNameDef() instanceof FuncDef) {
				return (FuncDef) nl.getNameDef();
			}
		}
		throw new CompileError(e.attrSource(), "Could not find function " + fromIndexFuncName(typ));
	}

		
	public static String toIndexFuncName(WurstType typ) {
		return typ + "ToIndex";
	}

	public static String fromIndexFuncName(WurstType typ) {
		return typ + "FromIndex";
	}

	public static Collection<NameLink> findToIndexFuncs(WurstType typ, AstElement e) {
		String funcName = toIndexFuncName(typ);
		return findFunc(e, funcName);
	}


	public static Collection<NameLink> findFromIndexFuncs(WurstType typ, AstElement e) {
		String funcName = fromIndexFuncName(typ);
		return findFunc(e, funcName);
	}
	
	
	private static Collection<NameLink> findFunc(AstElement e, String funcName) {
		return e.lookupFuncs(funcName, false);
	}
	
//	private static Collection<NameLink> findFunc(AstElement e, String funcName) {
//		// we only look at functions defined on the global level
//		while (e != null && !(e instanceof WPackage)) {
//			e = e.getParent();
//		}
//		if (e == null) {
//			return Collections.emptyList();
//		}
//		WEntities entities = ((WPackage) e).getElements();
//		return entities.lookupFuncs(funcName, false);
//	}
	
	
}
