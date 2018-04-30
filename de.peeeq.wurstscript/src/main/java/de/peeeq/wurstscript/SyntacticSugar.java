package de.peeeq.wurstscript;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import static de.peeeq.wurstscript.ast.Ast.Arguments;
import static de.peeeq.wurstscript.ast.Ast.ExprVarAccess;
/**
 * general rules for syntactic sugar:
 * <p>
 * 1. operations must be idempotent: syntacticSugar(syntacticSugar(program)) = syntacticSugar(program)
 * 2. operations must not depend on other compilation units.
 */
public class SyntacticSugar {

    private int wurstIteratorCounter = 0;


    public void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
        if (hasCommonJ) {
            addDefaultImports(root);
        }
        rewriteNegatedInts(root);
        addDefaultConstructors(root);
        addEndFunctionStatements(root);
//        expandForInLoops(root);
        replaceTypeIdUse(root);
    }


    private void replaceTypeIdUse(CompilationUnit root) {
        final Map<Expr, Expr> replacements = Maps.newLinkedHashMap();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprMemberVarDot e) {
                super.visit(e);
                if (e.getVarName().equals("typeId")) {
                    replacements.put(e, Ast.ExprTypeId(e.getSource(), (Expr) e.getLeft().copy()));
                }
            }
        });
        doReplacements(replacements, "Cannot use typeId here");
    }


    private void rewriteNegatedInts(CompilationUnit root) {
        final Map<Expr, Expr> replacements = Maps.newLinkedHashMap();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprUnary e) {
                super.visit(e);
                if (e.getOpU() == WurstOperator.UNARY_MINUS
                        && e.getRight() instanceof ExprIntVal) {
                    ExprIntVal iv = (ExprIntVal) e.getRight();
                    ExprIntVal newExpr = Ast.ExprIntVal(e.getSource(), "-" + iv.getValIraw());
                    replacements.put(e, newExpr);
                }
            }
        });
        doReplacements(replacements, "Cannot use typeId here");
    }


    private void doReplacements(Map<Expr, Expr> replacements, String msg) {
        for (Entry<Expr, Expr> e : replacements.entrySet()) {
            Expr oldE = e.getKey();
            Expr newE = e.getValue();
            try {
                doSingleReplacement(oldE, newE);
            } catch (ClassCastException ex) {
                oldE.addError(msg);
            }
        }

    }

    public void doSingleReplacement(Expr oldE, Expr newE) throws Error {
        Element parent = oldE.getParent();
        for (int i = 0; i < parent.size(); i++) {
            if (parent.get(i) == oldE) {
                parent.set(i, newE);
                return;
            }
        }
        throw new Error("could not replace " + oldE + " with " + newE);
    }

    private void addEndFunctionStatements(CompilationUnit root) {

        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExtensionFuncDef f) {
                super.visit(f);
                addEnd(f);
            }


            @Override
            public void visit(FuncDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(ConstructorDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(InitBlock f) {
                super.visit(f);
                addEnd(f);
            }


            @Override
            public void visit(OnDestroyDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(ExprStatementsBlock f) {
                super.visit(f);
                addEnd(f);
            }

            private void addEnd(AstElementWithBody f) {
                WPos pos = f.attrSource();
                pos = pos.withRightPos(pos.getLeftPos() - 1);
                f.getBody().add(Ast.EndFunctionStatement(pos));
                f.getBody().add(0, Ast.StartFunctionStatement(pos));
            }

        });

    }


    private void addDefaultImports(CompilationUnit root) {
        nextPackage:
        for (WPackage p : root.attrGetByType().packageDefs) {
            // add 'import Wurst' if it does not exist
            for (WImport imp : p.getImports()) {
                if (imp.getPackagename().equals("Wurst")) {
                    // wurst package already imported
                    continue nextPackage;
                }
                if (imp.getPackagename().equals("NoWurst")) {
                    // NoWurst package imported --> no standard lib wanted
                    continue nextPackage;
                }
            }
            WPos source = p.getSource();
            source = source.withRightPos(source.getLeftPos() - 1);
            p.getImports().add(Ast.WImport(source, false, false, Ast.Identifier(source, "Wurst")));
        }
    }

//    private void expandForInLoops(CompilationUnit root) {
//        // collect loops
//        final List<StmtForIn> loops = Lists.newArrayList();
//        final List<StmtForFrom> loops2 = Lists.newArrayList();
//        root.accept(new WurstModel.DefaultVisitor() {
//            @Override
//            public void visit(StmtForIn stmtForIn) {
//                super.visit(stmtForIn);
//                loops.add(stmtForIn);
//            }
//
//            @Override
//            public void visit(StmtForFrom stmtForIn) {
//                super.visit(stmtForIn);
//                loops2.add(stmtForIn);
//            }
//        });
//
//        // exand for ... in ... loops
//        for (StmtForIn loop : loops) {
//            if (loop.getParent() instanceof WStatements) {
//                WStatements parent = (WStatements) loop.getParent();
//
//                int position = parent.indexOf(loop);
//                parent.remove(position);
//
//                String iteratorName = "wurst__iterator" + wurstIteratorCounter++;
//                WPos loopVarPos = loop.getLoopVar().getSource().artificial();
//                WPos loopInPos = loop.getIn().getSource().artificial();
//
//                parent.add(position,
//                        Ast.LocalVarDef(
//                                loopInPos,
//                                Ast.Modifiers(),
//                                NoTypeExpr(), Ast.Identifier(loopInPos, iteratorName),
//                                Ast.ExprMemberMethodDot(loopInPos, (Expr) loop.getIn().copy(), Ast.Identifier(loopInPos, "iterator"), Ast.TypeExprList(), Arguments())));
//                WStatements body = WStatements(
//                        Ast.LocalVarDef(loopVarPos,
//                                Ast.Modifiers(),
//                                (OptTypeExpr) loop.getLoopVar().getOptTyp().copy(),
//                                Ast.Identifier(loop.getLoopVar().getSource(), loop.getLoopVar().getName()),
//                                Ast.ExprMemberMethodDot(loopInPos,
//                                        ExprVarAccess(loopVarPos, Ast.Identifier(loopVarPos, iteratorName)),
//                                        Ast.Identifier(loopInPos, "next"), Ast.TypeExprList(), Arguments()))
//                );
//                body.addAll(addIteratorCloseStatemenst(loop.getBody().removeAll(), iteratorName, loopVarPos, loopInPos));
//                parent.add(position + 1, Ast.StmtWhile(
//                        loop.getSource(),
//                        Ast.ExprMemberMethodDot(loopInPos,
//                                ExprVarAccess(loopVarPos, Ast.Identifier(loopVarPos, iteratorName)), Ast.Identifier(loopInPos, "hasNext"), Ast.TypeExprList(), Arguments()),
//                        body));
//                parent.add(position + 2,
//                        closeIteratorStatement(iteratorName, loopVarPos, loopInPos));
//            } else {
//                throw new CompileError(loop.getSource(), "Loop not in statements - " + loop.getParent().getClass().getName());
//            }
//        }
//
//        // exand for .. from ... loops
//        for (StmtForFrom loop : loops2) {
//            if (loop.getParent() instanceof WStatements) {
//                WStatements parent = (WStatements) loop.getParent();
//
//                int position = parent.indexOf(loop);
//                parent.remove(position);
//
//                String iteratorName = "wurst__iterator" + wurstIteratorCounter++;
//                WPos loopVarPos = loop.getLoopVar().getSource().artificial();
//                WPos loopInPos = loop.getIn().getSource().artificial();
//                if (loop.getIn() instanceof ExprVarAccess) {
//                    ExprVarAccess exprVarAccess = (ExprVarAccess) loop.getIn();
//                    iteratorName = exprVarAccess.getVarName();
//                } else {
//                    parent.add(position,
//                            Ast.LocalVarDef(
//                                    loopInPos,
//                                    Ast.Modifiers(),
//                                    NoTypeExpr(), Ast.Identifier(loopInPos, iteratorName),
//                                    (Expr) loop.getIn().copy()));
//                    position++;
//                }
//                WStatements body = WStatements(
//                        Ast.LocalVarDef(loopVarPos,
//                                Ast.Modifiers(),
//                                (OptTypeExpr) loop.getLoopVar().getOptTyp().copy(),
//                                Ast.Identifier(loop.getLoopVar().getSource(), loop.getLoopVar().getName()),
//                                Ast.ExprMemberMethodDot(loopInPos,
//                                        ExprVarAccess(loopVarPos, Ast.Identifier(loopVarPos, iteratorName)), Ast.Identifier(loopInPos, "next"), Ast.TypeExprList(), Arguments()))
//                );
//                body.addAll(addIteratorCloseStatemenst(loop.getBody().removeAll(), iteratorName, loopVarPos, loopInPos));
//                parent.add(position, Ast.StmtWhile(
//                        loop.getSource(),
//                        Ast.ExprMemberMethodDot(loopInPos,
//                                ExprVarAccess(loopVarPos, Ast.Identifier(loopVarPos, iteratorName)), Ast.Identifier(loopInPos, "hasNext"), Ast.TypeExprList(), Arguments()),
//                        body));
//            } else {
//                throw new CompileError(loop.getSource(), "Loop not in statements - " + loop.getParent().getClass().getName());
//            }
//        }
//    }


    private ExprMemberMethod closeIteratorStatement(String iteratorName, WPos loopVarPos, WPos loopInPos) {
        return Ast.ExprMemberMethodDot(loopInPos,
                ExprVarAccess(loopVarPos, Ast.Identifier(loopVarPos, iteratorName)), Ast.Identifier(loopInPos, "close"), Ast.TypeExprList(), Arguments());
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
        for (ClassDef c : root.attrGetByType().classes) {
            if (c.getConstructors().size() == 0) {
                // add default constructor if none exists:
                WPos source = c.getSource().withRightPos(c.getSource().getLeftPos() - 1);
                c.getConstructors().add(Ast.ConstructorDef(
                        source,
                        Ast.Modifiers(),
                        Ast.WParameters(),
                        false, Ast.Arguments(),
                        Ast.WStatements()));
            }
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
