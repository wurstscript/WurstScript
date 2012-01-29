package de.peeeq.wurstscript;

import static de.peeeq.wurstscript.ast.Ast.Arguments;
import static de.peeeq.wurstscript.ast.Ast.ExprMemberMethod;
import static de.peeeq.wurstscript.ast.Ast.ExprVarAccess;
import static de.peeeq.wurstscript.ast.Ast.NoTypeExpr;
import static de.peeeq.wurstscript.ast.Ast.TypeExprList;
import static de.peeeq.wurstscript.ast.Ast.TypeExprSimple;
import static de.peeeq.wurstscript.ast.Ast.TypeParamDef;
import static de.peeeq.wurstscript.ast.Ast.TypeParamDefs;
import static de.peeeq.wurstscript.ast.Ast.WStatements;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.StmtForFrom;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprList;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.WEntities;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.attr;

public class SyntacticSugar {

	public void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
		if (hasCommonJ) {
			addDefaultImports(root);
		}
		addInstanceDecls(root);
		addDefaultConstructors(root);
		expandForInLoops(root);
		
	}

	
	private void addDefaultImports(CompilationUnit root) {
		for (WPackage i : root.attrGetByType().packageDefs) {
			i.getImports().add(Ast.WImport(i.getSource().copy(), false, "Wurst"));
		}
	}


	private void addInstanceDecls(CompilationUnit root) {
		for (ClassDef c : root.attrGetByType().classes) {
			WEntities ents = (WEntities) c.getParent();
			for (TypeExpr implemented : c.getImplementsList()) {
				if (!(implemented instanceof TypeExprSimple)) {
					attr.addError(implemented.getSource(), "Can only implement interfaces.");
					continue;
				}
				TypeExprSimple impl = (TypeExprSimple) implemented;
				WPos s = implemented.getSource();
				
				TypeExprList typeExprs = TypeExprList();
				TypeParamDefs typeParams = TypeParamDefs();
				for (TypeParamDef tp : c.getTypeParameters()) {
					String name = tp.getName();
					typeExprs.add(TypeExprSimple(s.copy(), name, TypeExprList()));
					typeParams.add(TypeParamDef(tp.getSource().copy(), Ast.Modifiers(), name));
				}
				TypeExpr classTyp = Ast.TypeExprSimple(s.copy(), c.getName(), typeExprs); // TODO type params
				TypeExpr implementedTyp = Ast.TypeExprSimple(s.copy(), impl.getTypeName(), impl.getTypeArgs().copy());
				
				
				
				InstanceDef instanceDef = Ast.InstanceDef(s.copy(), c.getModifiers().copy(), typeParams, classTyp, implementedTyp, Ast.FuncDefs());
				ents.add(instanceDef);
			}
		}
	}


	private void expandForInLoops(CompilationUnit root) {
		// collect loops
		final List<StmtForIn> loops = Lists.newArrayList();
		final List<StmtForFrom> loops2 = Lists.newArrayList();
		root.accept(new CompilationUnit.DefaultVisitor() {
			@Override
			public void visit(StmtForIn stmtForIn) {
				loops.add(stmtForIn);
			}
			
			@Override
			public void visit(StmtForFrom stmtForIn) {
				loops2.add(stmtForIn);
			}
		});
		
		// exand for ... in ... loops
		for (StmtForIn loop : loops) {
			if (loop.getParent() instanceof WStatements) {
				WStatements parent = (WStatements) loop.getParent();
				
				int position = parent.indexOf(loop);
				parent.remove(position);
				
				String iteratorName = "iterator" +  UUID.randomUUID().toString().replace("-", "x");
				WPos loopVarPos = loop.getLoopVar().getSource();
				WPos loopInPos = loop.getIn().getSource();
				parent.add(position, 
						Ast.LocalVarDef(
								loopInPos.copy(), 
								Ast.Modifiers(), 
								NoTypeExpr(), iteratorName, 
									Ast.ExprMemberMethod(loopInPos.copy(), (Expr) loop.getIn().copy(), "iterator", Ast.TypeExprList(), Arguments())));
				WStatements body = WStatements(
							Ast.LocalVarDef(loopVarPos.copy(), 
									Ast.Modifiers(),
									(OptTypeExpr) loop.getLoopVar().getOptTyp().copy(), 
									loop.getLoopVar().getName(), 
									ExprMemberMethod(loopInPos.copy(), 
											ExprVarAccess(loopVarPos.copy(), iteratorName), "next", Ast.TypeExprList(), Arguments()))
						);
				body.addAll(addIteratorCloseStatemenst(loop.getBody().removeAll(), iteratorName, loopVarPos, loopInPos));
				parent.add(position + 1, Ast.StmtWhile(
						loop.getSource().copy(), 
						ExprMemberMethod(loopInPos.copy(), 
								ExprVarAccess(loopVarPos.copy(), iteratorName), "hasNext", Ast.TypeExprList(), Arguments()),
						body));
				parent.add(position+2, 
						closeIteratorStatement(iteratorName, loopVarPos, loopInPos));
			} else {
				throw new CompileError(loop.getSource(), "Loop not in statements - " + loop.getParent().getClass().getName());
			}
		}
		
		// exand for .. from ... loops
		for (StmtForFrom loop : loops2) {
			if (loop.getParent() instanceof WStatements) {
				WStatements parent = (WStatements) loop.getParent();
				
				int position = parent.indexOf(loop);
				parent.remove(position);
				
				String iteratorName = "iterator" +  UUID.randomUUID().toString().replace("-", "x");
				WPos loopVarPos = loop.getLoopVar().getSource();
				WPos loopInPos = loop.getIn().getSource();
				if (loop.getIn() instanceof ExprVarAccess) {
					ExprVarAccess exprVarAccess = (ExprVarAccess) loop.getIn();
					iteratorName = exprVarAccess.getVarName();
				} else {
					parent.add(position, 
							Ast.LocalVarDef(
									loopInPos.copy(), 
									Ast.Modifiers(), 
									NoTypeExpr(), iteratorName, 
										(Expr) loop.getIn().copy()));
				}
				WStatements body = WStatements(
							Ast.LocalVarDef(loopVarPos.copy(), 
									Ast.Modifiers(),
									(OptTypeExpr) loop.getLoopVar().getOptTyp().copy(), 
									loop.getLoopVar().getName(), 
									ExprMemberMethod(loopInPos.copy(), 
											ExprVarAccess(loopVarPos.copy(), iteratorName), "next", Ast.TypeExprList(), Arguments()))
						);
				body.addAll(addIteratorCloseStatemenst(loop.getBody().removeAll(), iteratorName, loopVarPos, loopInPos));
				parent.add(position + 1, Ast.StmtWhile(
						loop.getSource().copy(), 
						ExprMemberMethod(loopInPos.copy(), 
								ExprVarAccess(loopVarPos.copy(), iteratorName), "hasNext", Ast.TypeExprList(), Arguments()),
						body));
				parent.add(position+2, 
						closeIteratorStatement(iteratorName, loopVarPos, loopInPos));
			} else {
				throw new CompileError(loop.getSource(), "Loop not in statements - " + loop.getParent().getClass().getName());
			}
		}
	}


	private ExprMemberMethod closeIteratorStatement(String iteratorName, WPos loopVarPos, WPos loopInPos) {
		return ExprMemberMethod(loopInPos.copy(), 
				ExprVarAccess(loopVarPos.copy(), iteratorName), "close", Ast.TypeExprList(), Arguments());
	}
	
	private List<WStatement> addIteratorCloseStatemenst(List<WStatement> statements, String iteratorName, WPos loopVarPos, WPos loopInPos) {
		ListIterator<WStatement> it = statements.listIterator();
		while (it.hasNext()) {
			WStatement s = it.next();
			if (s instanceof AstElementWithBody) {
				addIteratorCloseStatemenst(((AstElementWithBody) s).getBody(), iteratorName, loopVarPos, loopInPos);
			} else if (s instanceof StmtIf) {
				StmtIf stmtIf = (StmtIf) s;
				addIteratorCloseStatemenst(stmtIf.getThenBlock(), iteratorName, loopVarPos, loopInPos);
				addIteratorCloseStatemenst(stmtIf.getElseBlock(), iteratorName, loopVarPos, loopInPos);
			} else if (s instanceof StmtReturn) {
				// add the close statement
				it.previous();
				it.add(closeIteratorStatement(iteratorName, loopVarPos, loopInPos));
				it.next();
			}
			
		}
		return statements;
	}


	/**
	 * add a empty default constructor to every class without any constructor 
	 */
	private void addDefaultConstructors(CompilationUnit root) {
		outerLoop: for (ClassDef c : root.attrGetByType().classes) {
			for (ClassSlot s : c.getSlots()) {
				if (s instanceof ConstructorDef) {
					continue outerLoop;
				}
			}
			c.getSlots().add(Ast.ConstructorDef(
					c.getSource().copy(), 
					Ast.Modifiers(), 
					Ast.WParameters(), 
					Ast.WStatements()));
		}
	}
	
//	/**
//	 * return all classes occuring in a compilation unit 
//	 */
//	private List<ClassDef> getAllClasses(CompilationUnit root) {
//		List<ClassDef> result = Lists.newArrayList();
//		for (TopLevelDeclaration t : root) {
//			if (t instanceof WPackage) {
//				WPackage p = (WPackage) t;
//				for (WEntity e : p.getElements()) {
//					if (e instanceof ClassDef) {
//						result.add((ClassDef) e);
//					}
//				}
//			}
//		}
//		return result;
//	}
}
