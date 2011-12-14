package de.peeeq.wurstscript.attributes;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute calculates the available functions for a given Scope
 * 
 *  note that there can be more than one function of the same name inside one scope
 */
public class AttrScopeFunctions {
	
	public static  Map<String, NotExtensionFunction> calculate(WScope node) {
		final Map<String, NotExtensionFunction> result = Maps.newHashMap();
		return node.match(new WScope.Matcher<Map<String, NotExtensionFunction>>() {

			@Override
			public Map<String, NotExtensionFunction> case_WPackage(WPackage term) {
				// add imported Names
				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					Map<String, NotExtensionFunction> importedFunctions = importedPackage.attrExportedFunctions();
					result.putAll(importedFunctions);
				}
				
				// add names in package
				for (WEntity e : term.getElements()) {
					if (e instanceof NotExtensionFunction) {
						NotExtensionFunction f = (NotExtensionFunction) e;
						result.put(f.getSignature().getName(), f);
					}
				}
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_ClassDef(ClassDef term) {
				return case_ClassOrModule(term);
			}

			private Map<String, NotExtensionFunction> case_ClassOrModule(ClassOrModule term) {
				result.putAll(term.attrAllFunctions());
				return result;
			}

			

			@Override
			public Map<String, NotExtensionFunction> case_FuncDef(FuncDef term)
					 {
				// functions cannot include other functions (not yet?)
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_CompilationUnit(CompilationUnit term)  {
				for (TopLevelDeclaration e : term) {
					if (e instanceof NotExtensionFunction) {
						NotExtensionFunction f = (NotExtensionFunction) e;
						result.put(f.getSignature().getName(), f);
					}
				}
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_ConstructorDef(ConstructorDef term)  {
				// constructors cannot include other functions
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_OnDestroyDef(OnDestroyDef term)  {
				// onDestroy cannot include other functions
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_InitBlock(InitBlock term)  {
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_ModuleDef(ModuleDef term) {
				return case_ClassOrModule(term);
			}

			@Override
			public Map<String, NotExtensionFunction> case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
				// functions cannot include other functions (not yet?)
				return result;
			}

			@Override
			public Map<String, NotExtensionFunction> case_ModuleInstanciation(ModuleInstanciation m) {
				// add all functions in this scope:
				result.putAll(m.attrAllFunctions());
				
				// check for functions in lower scope if they override a function here
				WScope parent = (WScope) m.getParent().getParent();
				Map<String, NotExtensionFunction> parentFunctions = parent.attrScopeFunctions();
				for (String functionName : parentFunctions.keySet()) {
					if (result.containsKey(functionName)) {
						result.put(functionName, parentFunctions.get(functionName));
					}
				}
				return result;
			}
		});
	}

	/**
	 * functions visible on package level 
	 */
	public static Map<String, NotExtensionFunction> calculatePackage(WScope scope) {
		Map<String, NotExtensionFunction> result = Maps.newHashMap();
		for (FunctionDefinition f : scope.attrScopeFunctions().values()) {
			if (f instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) f;
				if (!funcDef.attrIsPrivate()) {
					result.put(funcDef.getSignature().getName(), funcDef);
				}
			}
		}
		return result;
	}

	/**
	 * functions visible everywhere
	 */
	public static Map<String, NotExtensionFunction> calculatePublic(WScope scope) {
		Map<String, NotExtensionFunction> result = Maps.newHashMap();
		for (FunctionDefinition f : scope.attrScopeFunctions().values()) {
			if (f instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) f;
				if (funcDef.attrIsPublic()) {
					result.put(funcDef.getSignature().getName(), funcDef);
				}
			}
		}
		return result;
	}


	public static Multimap<String, ExtensionFuncDef> calculateExtensionFunctions(WPackage pack) {
		Multimap<String, ExtensionFuncDef> result = HashMultimap.create();
		// add imports
		for (WImport i : pack.getImports()) {
			WPackage imported = attr.getImportedPackage(i);
			result.putAll(imported.attrExportedExtensionFunctions());
		}
		for (WEntity e : pack.getElements()) {
			if (e instanceof ExtensionFuncDef) {
				ExtensionFuncDef extensionFuncDef = (ExtensionFuncDef) e;
				result.put(extensionFuncDef.getSignature().getName(), extensionFuncDef);
			}
		}
		return result;
	}
}
