package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
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
public class AttrScopeVariables extends Attribute<WScope, Map<String, VarDef>> {


	public AttrScopeVariables(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, VarDef> calculate(WScope node) {
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
				for (WImport i : term.getImports()) {
					WPackage importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					result.putAll(attr.exportedVariables.get(importedPackage));
				}
				for (WEntity e : term.getElements()) {
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


}
