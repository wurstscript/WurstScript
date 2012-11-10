package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.validation.WurstValidator;

public class OverriddenFunctions {


	public static FunctionDefinition getRealFuncDef(ExtensionFuncDef f) {
		// extension functions cannot be overridden
		return f;
	}

	public static FunctionDefinition getRealFuncDef(NativeFunc f) {
		// native functions cannot be overridden
		return f;
	}
	
	public static FunctionDefinition getRealFuncDef(FuncDef f) {
		if (f.attrIsPrivate()) {
			// private functions cannot be overridden
			return f;
		}
		if (f.attrNearestNamedScope() == null) {
			return f;
		}
		if (f.attrNearestNamedScope().getParent() == null) {
			return f;
		}
		WScope scope = f.attrNearestNamedScope().getParent().attrNearestScope();
		return getRealFuncDef(f, scope);
	}

	private static FunctionDefinition getRealFuncDef(FuncDef f, WScope scope) {
		if (scope instanceof StructureDef) {
			StructureDef c = (StructureDef) scope;
			
			NameLink fNameLink = NameLink.create(f, f.attrNearestScope());
			
			if (c.attrNameLinks().containsKey(f.getName())) {
				for (NameLink nl : c.attrNameLinks().get(f.getName())) {
					NameDef n = nl.getNameDef();
					if (nl.getLevel() == c.attrLevel()
							&& n instanceof FunctionDefinition
							&& WurstValidator.overrides(nl, fNameLink)
							) {
						return ((FunctionDefinition) n).attrRealFuncDef();
					}
				}
			}
			if (scope.getParent() != null) { 
				return getRealFuncDef(f, scope.getParent().attrNearestNamedScope());
			}
		}
		return f;
	}

	public static FunctionDefinition getRealFuncDef(TupleDef t) {
		return t;
	}
	
}
