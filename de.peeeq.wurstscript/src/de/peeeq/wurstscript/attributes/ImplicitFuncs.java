package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.HasTypeArgs;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;

public class ImplicitFuncs {

	public static FuncDef findToIndexFunc(WurstType typ, AstElement e) {
		typ = typ.normalize();
		for (NameLink nl : findToIndexFuncs(typ, e)) {
			if (nl.getNameDef() instanceof FuncDef) {
				return (FuncDef) nl.getNameDef();
			}
		}
		throw new CompileError(e.attrSource(), "Could not find func " + toIndexFuncName(typ));
	}
	
	public static FuncDef findFromIndexFunc(WurstType typ, AstElement e) {
		typ = typ.normalize();
		for (NameLink nl : findFromIndexFuncs(typ, e)) {
			if (nl.getNameDef() instanceof FuncDef) {
				return (FuncDef) nl.getNameDef();
			}
		}
		throw new CompileError(e.attrSource(), "Could not find func " + fromIndexFuncName(typ));
	}

		
	public static String toIndexFuncName(WurstType typ) {
		return typ + "ToIndex";
	}

	public static String fromIndexFuncName(WurstType typ) {
		return typ + "FromIndex";
	}

	public static Collection<NameLink> findToIndexFuncs(WurstType typ,
			AstElement e) {
		return e.lookupFuncs(toIndexFuncName(typ), false);
	}


	public static Collection<NameLink> findFromIndexFuncs(WurstType typ,
			AstElement e) {
		return e.lookupFuncs(fromIndexFuncName(typ), false);
	}
}
