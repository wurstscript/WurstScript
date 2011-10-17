package de.peeeq.wurstscript.attributes;

import java.util.Map;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ClassSlotPos;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.ConstructorDefPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.InitBlockPos;
import de.peeeq.wurstscript.ast.JassGlobalBlockPos;
import de.peeeq.wurstscript.ast.LocalVarDefPos;
import de.peeeq.wurstscript.ast.OnDestroyDefPos;
import de.peeeq.wurstscript.ast.TopLevelDeclarationPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WImportPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.ast.WParameterPos;
import de.peeeq.wurstscript.ast.WScopePos;
import de.peeeq.wurstscript.utils.DefinitionMap;


/**
 * this attribute calculates the available variables for a given VariableScope 
 */
public class AttrScopeVariables extends Attribute<WScopePos, Map<String, VarDefPos>> {


	public AttrScopeVariables(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, VarDefPos> calculate(WScopePos node) {
		final Map<String, VarDefPos> result = new DefinitionMap<String, VarDefPos>(){

			@Override
			public void onElementRedefined(VarDefPos firstDefinition, VarDefPos secondDefinition, String name) {
				attr.addError(secondDefinition.source(), "Variable " + name + " redefined. Already defined in " + firstDefinition.source().term());
			}
		};
		return node.Switch(new WScopePos.Switch<Map<String, VarDefPos>, NE>() {

			@Override
			public Map<String, VarDefPos> CaseWPackagePos(WPackagePos term)
					throws NE {
				for (WImportPos i : term.imports()) {
					WPackagePos importedPackage = attr.getImportedPackage(i);
					if (importedPackage == null) {
						continue;
					}
					result.putAll(attr.exportedVariables.get(importedPackage));
				}
				for (WEntityPos e : term.elements()) {
					if (e instanceof VarDefPos) {
						VarDefPos v = (VarDefPos) e;
						result.put(v.name().term(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseClassDefPos(ClassDefPos term)
					throws NE {
				for (ClassSlotPos e : term.slots()) {
					if (e instanceof VarDefPos) {
						VarDefPos v = (VarDefPos) e;
						result.put(v.name().term(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseFuncDefPos(FuncDefPos term)
					throws NE {
//				Utils.visitPostOrder(term, new Function<AST.SortPos, Void>() {
//					
//					@Override
//					public Void apply(SortPos e) {
//						if (e instanceof VarDefPos) {
//							VarDefPos v = (VarDefPos) e;
//							result.put(v.name().term(), v);
//						}
//						return null;
//					}
//				});
				new  CompilationUnitPos.DefaultVisitor<NE>() {
					@Override
					public void visit(LocalVarDefPos v) {
						result.put(v.name().term(), v);
					}
					
					@Override
					public void visit(WParameterPos v) {
						result.put(v.name().term(), v);
					}
				}.visit(term);
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseCompilationUnitPos(CompilationUnitPos term) throws NE {
				for (TopLevelDeclarationPos e : term) {
					if (e instanceof JassGlobalBlockPos) {
						JassGlobalBlockPos jassGlobalBlockPos = (JassGlobalBlockPos) e;
						for (GlobalVarDefPos g : jassGlobalBlockPos) {
							result.put(g.name().term(), g);
						}
					}
					if (e instanceof VarDefPos) {
						VarDefPos v = (VarDefPos) e;
						result.put(v.name().term(), v);
					}
				}
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseConstructorDefPos(ConstructorDefPos term) throws NE {
				new  CompilationUnitPos.DefaultVisitor<NE>() {
					@Override
					public void visit(LocalVarDefPos v) {
						result.put(v.name().term(), v);
					}
					
					@Override
					public void visit(WParameterPos v) {
						result.put(v.name().term(), v);
					}
				}.visit(term);
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseOnDestroyDefPos(OnDestroyDefPos term) throws NE {
				new  CompilationUnitPos.DefaultVisitor<NE>() {
					@Override
					public void visit(LocalVarDefPos v) {
						result.put(v.name().term(), v);
					}
					
				}.visit(term);
				return result;
			}

			@Override
			public Map<String, VarDefPos> CaseInitBlockPos(InitBlockPos term) throws NE {
				new  CompilationUnitPos.DefaultVisitor<NE>() {
					@Override
					public void visit(LocalVarDefPos v) {
						result.put(v.name().term(), v);
					}
					
				}.visit(term);
				return result;
			}
		});
	}


}
