package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
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
	
	public static  Multimap<String, FunctionDefinition> calculate(WScope node) {
		final Multimap<String, FunctionDefinition> result = ArrayListMultimap.create();
		return node.match(new WScope.Matcher<Multimap<String, FunctionDefinition>>() {

			@Override
			public Multimap<String, FunctionDefinition> case_WPackage(WPackage term)
					 {
				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					Multimap<String, FunctionDefinition> importedFunctions = importedPackage.attrExportedFunctions();
					result.putAll(importedFunctions);
				}
				
				for (WEntity e : term.getElements()) {
					if (e instanceof FunctionDefinition) {
						FunctionDefinition f = (FunctionDefinition) e;
						result.put(f.getSignature().getName(), f);
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_ClassDef(ClassDef term)
					 {
				for (ClassSlot e : term.getSlots()) {
					if (e instanceof FunctionDefinition) {
						FunctionDefinition f = (FunctionDefinition) e;
						result.put(f.getSignature().getName(), f);
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_FuncDef(FuncDef term)
					 {
				// functions cannot include other functions (not yet?)
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_CompilationUnit(CompilationUnit term)  {
				for (TopLevelDeclaration e : term) {
					if (e instanceof FunctionDefinition) {
						FunctionDefinition f = (FunctionDefinition) e;
						result.put(f.getSignature().getName(), f);
					}
				}
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_ConstructorDef(ConstructorDef term)  {
				// constructors cannot include other functions
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_OnDestroyDef(OnDestroyDef term)  {
				// onDestroy cannot include other functions
				return result;
			}

			@Override
			public Multimap<String, FunctionDefinition> case_InitBlock(InitBlock term)  {
				return result;
			}
		});
	}

	public static Multimap<String, FunctionDefinition> calculatePackage(WScope scope) {
		Multimap<String, FunctionDefinition> result = HashMultimap.create();
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

	public static Multimap<String, FunctionDefinition> calculatePublic(WScope scope) {
		Multimap<String, FunctionDefinition> result = HashMultimap.create();
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
}
