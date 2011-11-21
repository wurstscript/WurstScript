package de.peeeq.wurstscript.attributes;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.DefinitionMap;

/**
 * this attribute calculates the available variables for a given VariableScope
 */
public class AttrScopeNames {

	public static Map<String, NameDef> calculate(WScope node) {
		final Map<String, NameDef> result = new DefinitionMap<String, NameDef>() {

			@Override
			public void onElementRedefined(NameDef firstDefinition, NameDef secondDefinition, String name) {
				attr.addError(secondDefinition.getSource(),
						"An element with name " + name + " redefined. Already defined in " + firstDefinition.getSource());
			}
		};
		return node.match(new WScope.Matcher<Map<String, NameDef>>() {

			@Override
			public Map<String, NameDef> case_WPackage(WPackage term) {

				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					result.putAll(importedPackage.attrExportedVariables());
				}
				for (WEntity e : term.getElements()) {
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					} else if (e instanceof ClassDef) {
						ClassDef classDef = (ClassDef) e;
						result.put(classDef.getName(), classDef);						
					} else if (e instanceof ModuleDef) {
						ModuleDef moduleDef = (ModuleDef) e;
						result.put(moduleDef.getName(), moduleDef);
					}
				}
				return result;
			}

			@Override
			public Map<String, NameDef> case_ClassDef(ClassDef c) {
				
				
				for (ClassSlot e : c.getSlots()) {
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, NameDef> case_FuncDef(FuncDef term) {

				term.accept(new CompilationUnit.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}

					@Override
					public void visit(WParameter v) {
						result.put(v.getName(), v);
					}
				});
				return result;
			}

			@Override
			public Map<String, NameDef> case_CompilationUnit(CompilationUnit term) {
				for (TopLevelDeclaration e : term) {
					if (e instanceof JassGlobalBlock) {
						JassGlobalBlock jassGlobalBlockPos = (JassGlobalBlock) e;
						for (GlobalVarDef g : jassGlobalBlockPos) {
							result.put(g.getName(), g);
						}
					}
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, NameDef> case_ConstructorDef(ConstructorDef term) {
				term.accept(new ConstructorDef.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}

					@Override
					public void visit(WParameter v) {
						result.put(v.getName(), v);
					}
				});
				return result;
			}

			@Override
			public Map<String, NameDef> case_OnDestroyDef(OnDestroyDef term) {
				term.accept(new OnDestroyDef.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}

				});
				return result;
			}

			@Override
			public Map<String, NameDef> case_InitBlock(InitBlock term) {
				term.accept(new InitBlock.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}

				});
				return result;
			}

			@Override
			public Map<String, NameDef> case_ModuleDef(ModuleDef moduleDef) {
				for (ClassSlot e : moduleDef.getSlots()) {
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					}
				}
				return result;
			}
		});
	}

	public static Map<String, NameDef> calculatePackage(WScope scope) {
		Map<String, NameDef> result = Maps.newHashMap();
		for (NameDef f : scope.attrScopeNames().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (!g.attrIsPrivate()) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}

	public static Map<String, NameDef> calculatePublic(WScope scope) {
		Map<String, NameDef> result = Maps.newHashMap();
		for (NameDef f : scope.attrScopeNames().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (g.attrIsPublic()) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}

	public static Map<String, NameDef> calculatePublicRead(WScope scope) {
		Map<String, NameDef> result = Maps.newHashMap();
		for (NameDef f : scope.attrScopeNames().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (g.attrIsPublic() || g.attrIsPublicRead()) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}

}
