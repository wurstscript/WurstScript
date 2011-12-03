package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;


/**
 * this attribute calculates all named elements available inside in a package (including imports)
 */
public class AttrPackageElements {
	
	public static  Multimap<String, WEntity> calculate(PackageOrGlobal node) {
		final Multimap<String, WEntity> result = ArrayListMultimap.create();

		CompilationUnit cu = (CompilationUnit) Utils.getRoot(node);

		addPackage(result, node);
		// add imported stuff
		if (node instanceof WPackage) {
			for (WImport imp : ((WPackage) node).getImports()) {
				String packageName = imp.getPackagename();
				WPackage importedPackage = null;
				for (TopLevelDeclaration tl : cu) {
					if (tl instanceof WPackage) {
						WPackage p = (WPackage) tl;
						if (p.getName().equals(packageName)) {
							importedPackage = p;
							break;
						}
					}
				}
				if (importedPackage == null) {
					attr.addError(imp.getSource(), "Could not resolve import " + packageName);
				} else {
					addPackage(result, importedPackage);
				}
			}
		}
		return result;
	}

	private static void addPackage(final Multimap<String, WEntity> result, 	PackageOrGlobal node) {
		node.match(new PackageOrGlobal.MatcherVoid() {

			@Override
			public void case_WPackage(WPackage pack)  {
				for (WEntity e : pack.getElements()) {
					e.match(new WEntity.MatcherVoid() {

						@Override
						public void case_NativeType(NativeType term)  {
							result.put(term.getName(), term);
						}

						@Override
						public void case_ClassDef(ClassDef term)  {
							result.put(term.getName(), term);
						}

						@Override
						public void case_FuncDef(FuncDef term)  {
							result.put(term.getSignature().getName(), term);
						}

						@Override
						public void case_GlobalVarDef(GlobalVarDef term)  {
							result.put(term.getName(), term);
						}

						@Override
						public void case_InitBlock(InitBlock term)  {
						}

						@Override
						public void case_NativeFunc(NativeFunc term)  {
							result.put(term.getSignature().getName(), term);
						}

						@Override
						public void case_ModuleDef(ModuleDef term) {
							result.put(term.getName(), term);
						}

						@Override
						public void case_ExtensionFuncDef(ExtensionFuncDef term) {
							result.put(term.getSignature().getName(), term);
						}
					});
				}
			}

			@Override
			public void case_CompilationUnit(CompilationUnit term)  {
				for (TopLevelDeclaration elem : term) {
					elem.match(new TopLevelDeclaration.MatcherVoid() {

						@Override
						public void case_JassGlobalBlock(JassGlobalBlock term)  {
							for (GlobalVarDef g : term) {
								result.put(g.getName(), g);
							}
						}

						@Override
						public void case_NativeType(NativeType term)  {
							result.put(term.getName(), term);
						}

						@Override
						public void case_FuncDef(FuncDef term)  {
							result.put(term.getSignature().getName(), term);
						}

						@Override
						public void case_NativeFunc(NativeFunc term)  {
							result.put(term.getSignature().getName(), term);
						}

						@Override
						public void case_WPackage(WPackage term)  {
						}

						@Override
						public void case_ExtensionFuncDef(ExtensionFuncDef term) {
							result.put(term.getSignature().getName(), term);
						}

					});
				}
			}
		});


	}


}
