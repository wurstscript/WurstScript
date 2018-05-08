package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;

import java.util.Collection;

public class ImplicitFuncs {

    public static FuncDef findToIndexFunc(WurstType typ, Element e) {
        typ = typ.normalize();
        for (NameLink nl : findToIndexFuncs(typ, e)) {
            if (nl.getDef() instanceof FuncDef) {
                return (FuncDef) nl.getDef();
            }
        }
        e.addError("Could not find function " + toIndexFuncName(typ));
        return null;
    }

    public static FuncDef findFromIndexFunc(WurstType typ, Element e) {
        typ = typ.normalize();
        for (NameLink nl : findFromIndexFuncs(typ, e)) {
            if (nl.getDef() instanceof FuncDef) {
                return (FuncDef) nl.getDef();
            }
        }
        e.addError("Could not find function " + fromIndexFuncName(typ));
        return null;
    }


    public static String toIndexFuncName(WurstType typ) {
        return typ + "ToIndex";
    }

    public static String fromIndexFuncName(WurstType typ) {
        return typ + "FromIndex";
    }

    public static Collection<FuncLink> findToIndexFuncs(WurstType typ, Element e) {
        String funcName = toIndexFuncName(typ);
        return findFunc(e, funcName);
    }


    public static Collection<FuncLink> findFromIndexFuncs(WurstType typ, Element e) {
        String funcName = fromIndexFuncName(typ);
        return findFunc(e, funcName);
    }


    private static Collection<FuncLink> findFunc(Element e, String funcName) {
        return e.lookupFuncs(funcName, false);
    }

//	private static Collection<NameLink> findFunc(Element e, String funcName) {
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
