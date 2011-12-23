package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.MutableValue;
import de.peeeq.wurstscript.utils.Utils;

public class OverriddenFunctions {


	public static Collection<FunctionDefinition> getOverriddenFunctions(FunctionDefinition f) {
		return Lists.newLinkedList();
	}

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
		if (scope instanceof ClassOrModuleOrModuleInstanciation) {
			ClassOrModuleOrModuleInstanciation c = (ClassOrModuleOrModuleInstanciation) scope;
			if (c.attrDefinedNames().containsKey(f.getName())) {
				for (NameDef n : c.attrDefinedNames().get(f.getName())) {
					if (n instanceof FunctionDefinition) {
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
	
}
