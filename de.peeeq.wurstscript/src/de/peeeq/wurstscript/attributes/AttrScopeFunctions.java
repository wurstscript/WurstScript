package de.peeeq.wurstscript.attributes;

import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;


/**
 * this attribute calculates the available functions for a given Scope
 * 
 *  note that there can be more than one function of the same name inside one scope
 */
public class AttrScopeFunctions {
	
	public static  Multimap<String, FuncDefInstance> calculate(WScope node) {
		final Multimap<String, FuncDefInstance> result = ArrayListMultimap.create();
		return node.match(new WScope.Matcher<Multimap<String, FuncDefInstance>>() {

			@Override
			public Multimap<String, FuncDefInstance> case_WPackage(WPackage term)
					 {
				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					Multimap<String, FunctionDefinition> importedFunctions = importedPackage.attrExportedFunctions();
					for (Entry<String, FunctionDefinition> f : importedFunctions.entries()) {
						result.put(f.getKey(), FuncDefInstance.create(f.getValue()));
					}
				}
				
				for (WEntity e : term.getElements()) {
					if (e instanceof FunctionDefinition) {
						FunctionDefinition f = (FunctionDefinition) e;
						result.put(f.getSignature().getName(), FuncDefInstance.create(f));
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_ClassDef(ClassDef term) {
				return case_ClassOrModule(term);
			}

			private Multimap<String, FuncDefInstance> case_ClassOrModule(ClassOrModule term) {
				return term.attrAllFunctions();
			}

			

			@Override
			public Multimap<String, FuncDefInstance> case_FuncDef(FuncDef term)
					 {
				// functions cannot include other functions (not yet?)
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_CompilationUnit(CompilationUnit term)  {
				for (TopLevelDeclaration e : term) {
					if (e instanceof FunctionDefinition) {
						FunctionDefinition f = (FunctionDefinition) e;
						result.put(f.getSignature().getName(), FuncDefInstance.create(f));
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_ConstructorDef(ConstructorDef term)  {
				// constructors cannot include other functions
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_OnDestroyDef(OnDestroyDef term)  {
				// onDestroy cannot include other functions
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_InitBlock(InitBlock term)  {
				return result;
			}

			@Override
			public Multimap<String, FuncDefInstance> case_ModuleDef(ModuleDef term) {
				return case_ClassOrModule(term);
			}

			@Override
			public Multimap<String, FuncDefInstance> case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
				// functions cannot include other functions (not yet?)
				return result;
			}
		});
	}

	
	public static Multimap<String, FuncDefInstance> calculatePackage(WScope scope) {
		Multimap<String, FuncDefInstance> result = HashMultimap.create();
		for (FuncDefInstance f : scope.attrScopeFunctions().values()) {
			if (f.getDef() instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) f.getDef();
				if (!funcDef.attrIsPrivate()) {
					result.put(funcDef.getSignature().getName(), f);
				}
			}
		}
		return result;
	}

	public static Multimap<String, FuncDefInstance> calculatePublic(WScope scope) {
		Multimap<String, FuncDefInstance> result = HashMultimap.create();
		for (FuncDefInstance f : scope.attrScopeFunctions().values()) {
			if (f.getDef() instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) f.getDef();
				if (funcDef.attrIsPublic()) {
					result.put(funcDef.getSignature().getName(), f);
				}
			}
		}
		return result;
	}
}
