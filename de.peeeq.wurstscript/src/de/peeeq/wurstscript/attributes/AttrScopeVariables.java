package de.peeeq.wurstscript.attributes;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VisibilityDefault;
import de.peeeq.wurstscript.ast.VisibilityProtected;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.VisibilityPublicread;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.DefinitionMap;


/**
 * this attribute calculates the available variables for a given VariableScope 
 */
public class AttrScopeVariables {
	
	public static  Map<String, VarDef> calculate(WScope node) {
		final Map<String, VarDef> result = new DefinitionMap<String, VarDef>(){

			@Override
			public void onElementRedefined(VarDef firstDefinition, VarDef secondDefinition, String name) {
				attr.addError(secondDefinition.getSource(), "Variable " + 
						name + " redefined. Already defined in " + firstDefinition.getSource());
			}
		};
		return node.match(new WScope.Matcher<Map<String, VarDef>>() {

			@Override
			public Map<String, VarDef> case_WPackage(WPackage term)
					 {
				System.out.println("WPackage: " + term);
				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					System.out.println("Adding imports for: " + importedPackage.getName());
					result.putAll(importedPackage.attrExportedVariables());
					System.out.println("Added imports for: " + importedPackage.getName());
				}
				for (WEntity e : term.getElements()) {
					System.out.println("	WEntity: " + e);
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, VarDef> case_ClassDef(ClassDef term)
					 {
				for (ClassSlot e : term.getSlots()) {
					if (e instanceof VarDef) {
						VarDef v = (VarDef) e;
						result.put(v.getName(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, VarDef> case_FuncDef(FuncDef term)
					 {
//				Utils.visitPostOrder(term, new Function<AST.SortPos, Void>() {
//					
//					@Override
//					public Void apply(SortPos e) {
//						if (e instanceof VarDef) {
//							VarDef v = (VarDef) e;
//							result.put(v.name(), v);
//						}
//						return null;
//					}
//				});
				term.accept(new  CompilationUnit.DefaultVisitor() {
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
			public Map<String, VarDef> case_CompilationUnit(CompilationUnit term)  {
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
			public Map<String, VarDef> case_ConstructorDef(ConstructorDef term)  {
				term.accept(new  ConstructorDef.DefaultVisitor() {
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
			public Map<String, VarDef> case_OnDestroyDef(OnDestroyDef term)  {
				term.accept(new  OnDestroyDef.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}
					
				});
				return result;
			}

			@Override
			public Map<String, VarDef> case_InitBlock(InitBlock term)  {
				term.accept(new  InitBlock.DefaultVisitor() {
					@Override
					public void visit(LocalVarDef v) {
						result.put(v.getName(), v);
					}
					
				});
				return result;
			}
		});
	}

	
	public static Map<String, VarDef> calculatePackage(WScope scope) {
		Map<String, VarDef> result = Maps.newHashMap();
		for (VarDef f : scope.attrScopeVariables().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (g.getVisibility() instanceof VisibilityDefault
						|| g.getVisibility() instanceof VisibilityProtected
						|| g.getVisibility() instanceof VisibilityPublic) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}

	public static Map<String, VarDef> calculatePublic(WScope scope) {
		Map<String, VarDef> result = Maps.newHashMap();
		for (VarDef f : scope.attrScopeVariables().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (g.getVisibility() instanceof VisibilityPublic) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}
	
	public static Map<String, VarDef> calculatePublicRead(WScope scope) {
		Map<String, VarDef> result = Maps.newHashMap();
		for (VarDef f : scope.attrScopeVariables().values()) {
			if (f instanceof GlobalVarDef) {
				GlobalVarDef g = (GlobalVarDef) f;
				if (g.getVisibility() instanceof VisibilityPublic
						|| g.getVisibility() instanceof VisibilityPublicread) {
					result.put(g.getName(), g);
				}
			}
		}
		return result;
	}

}
