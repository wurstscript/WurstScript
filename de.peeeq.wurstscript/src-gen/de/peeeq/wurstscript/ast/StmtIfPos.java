package de.peeeq.wurstscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface StmtIfPos extends de.peeeq.wurstscript.ast.WStatementPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.StmtIf> {

    //----- methods of StmtIfPos -----

    public de.peeeq.wurstscript.ast.StmtIf termWStatement();
    public de.peeeq.wurstscript.ast.StmtIf term();
    public de.peeeq.wurstscript.ast.WPosPos source();
    public de.peeeq.wurstscript.ast.StmtIfPos replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.ExprPos cond();
    public de.peeeq.wurstscript.ast.StmtIfPos replaceCond(de.peeeq.wurstscript.ast.Expr cond);
    public de.peeeq.wurstscript.ast.WStatementsPos thenBlock();
    public de.peeeq.wurstscript.ast.StmtIfPos replaceThenBlock(de.peeeq.wurstscript.ast.WStatements thenBlock);
    public de.peeeq.wurstscript.ast.WStatementsPos elseBlock();
    public de.peeeq.wurstscript.ast.StmtIfPos replaceElseBlock(de.peeeq.wurstscript.ast.WStatements elseBlock);
    public de.peeeq.wurstscript.ast.AST.SortPos get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.StmtIfPos replace(de.peeeq.wurstscript.ast.StmtIf term);
    public de.peeeq.wurstscript.ast.WStatementsPos parent();
    public de.peeeq.wurstscript.ast.WStatementPos lsib();
    public de.peeeq.wurstscript.ast.WStatementPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatementPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of StmtIfPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatementsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAssignablePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAtomicPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprThisPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WStatementPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IndexesPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArgumentsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.DoublePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIfPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtWhilePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtLoopPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.LocalVarDefPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtSetPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtCallPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtReturnPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDestroyPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtIncRefCountPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtDecRefCountPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtErrPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StmtExitwhenPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpOrPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAndPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpPlusPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMultPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModRealPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModIntPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpNotPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssignmentPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAssignPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizesPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.StmtIfPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpAssignmentPos.Switch<Object, E> variantVisit$OpAssignmentPos = new de.peeeq.wurstscript.ast.OpAssignmentPos.Switch<Object, E>() { public final Object CaseOpAssignPos(de.peeeq.wurstscript.ast.OpAssignPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E> variantVisit$OpBinaryPos = new de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E>() { public final Object CaseOpOrPos(de.peeeq.wurstscript.ast.OpOrPos term) throws E { visit(term); return null; } public final Object CaseOpAndPos(de.peeeq.wurstscript.ast.OpAndPos term) throws E { visit(term); return null; } public final Object CaseOpEqualsPos(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E { visit(term); return null; } public final Object CaseOpUnequalsPos(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E { visit(term); return null; } public final Object CaseOpLessEqPos(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E { visit(term); return null; } public final Object CaseOpLessPos(de.peeeq.wurstscript.ast.OpLessPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterEqPos(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterPos(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E { visit(term); return null; } public final Object CaseOpPlusPos(de.peeeq.wurstscript.ast.OpPlusPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } public final Object CaseOpMultPos(de.peeeq.wurstscript.ast.OpMultPos term) throws E { visit(term); return null; } public final Object CaseOpDivRealPos(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E { visit(term); return null; } public final Object CaseOpModRealPos(de.peeeq.wurstscript.ast.OpModRealPos term) throws E { visit(term); return null; } public final Object CaseOpModIntPos(de.peeeq.wurstscript.ast.OpModIntPos term) throws E { visit(term); return null; } public final Object CaseOpDivIntPos(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E> variantVisit$OpUnaryPos = new de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E>() { public final Object CaseOpNotPos(de.peeeq.wurstscript.ast.OpNotPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExprPos.Switch<Object, E> variantVisit$OptExprPos = new de.peeeq.wurstscript.ast.OptExprPos.Switch<Object, E>() { public final Object CaseNoExprPos(de.peeeq.wurstscript.ast.NoExprPos term) throws E { visit(term); return null; } public final Object CaseExprBinaryPos(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E { visit(term); return null; } public final Object CaseExprUnaryPos(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E { visit(term); return null; } public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExprPos.Switch<Object, E> variantVisit$OptTypeExprPos = new de.peeeq.wurstscript.ast.OptTypeExprPos.Switch<Object, E>() { public final Object CaseNoTypeExprPos(de.peeeq.wurstscript.ast.NoTypeExprPos term) throws E { visit(term); return null; } public final Object CaseTypeExprPos(de.peeeq.wurstscript.ast.TypeExprPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.StmtCallPos.Switch<Object, E> variantVisit$StmtCallPos = new de.peeeq.wurstscript.ast.StmtCallPos.Switch<Object, E>() { public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.WStatementPos.Switch<Object, E> variantVisit$WStatementPos = new de.peeeq.wurstscript.ast.WStatementPos.Switch<Object, E>() { public final Object CaseStmtIfPos(de.peeeq.wurstscript.ast.StmtIfPos term) throws E { visit(term); return null; } public final Object CaseStmtWhilePos(de.peeeq.wurstscript.ast.StmtWhilePos term) throws E { visit(term); return null; } public final Object CaseStmtLoopPos(de.peeeq.wurstscript.ast.StmtLoopPos term) throws E { visit(term); return null; } public final Object CaseLocalVarDefPos(de.peeeq.wurstscript.ast.LocalVarDefPos term) throws E { visit(term); return null; } public final Object CaseStmtSetPos(de.peeeq.wurstscript.ast.StmtSetPos term) throws E { visit(term); return null; } public final Object CaseStmtReturnPos(de.peeeq.wurstscript.ast.StmtReturnPos term) throws E { visit(term); return null; } public final Object CaseStmtDestroyPos(de.peeeq.wurstscript.ast.StmtDestroyPos term) throws E { visit(term); return null; } public final Object CaseStmtIncRefCountPos(de.peeeq.wurstscript.ast.StmtIncRefCountPos term) throws E { visit(term); return null; } public final Object CaseStmtDecRefCountPos(de.peeeq.wurstscript.ast.StmtDecRefCountPos term) throws E { visit(term); return null; } public final Object CaseStmtErrPos(de.peeeq.wurstscript.ast.StmtErrPos term) throws E { visit(term); return null; } public final Object CaseStmtExitwhenPos(de.peeeq.wurstscript.ast.StmtExitwhenPos term) throws E { visit(term); return null; } public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignablePos term) throws E {
            term.Switch(variantVisit$OptExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomicPos term) throws E {
            term.Switch(variantVisit$OptExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E {
            term.Switch(variantVisit$OptExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpAssignmentPos term) throws E {
            term.Switch(variantVisit$OpAssignmentPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E {
            term.Switch(variantVisit$OpBinaryPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E {
            term.Switch(variantVisit$OpUnaryPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptExprPos term) throws E {
            term.Switch(variantVisit$OptExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExprPos term) throws E {
            term.Switch(variantVisit$OptTypeExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.StmtCallPos term) throws E {
            term.Switch(variantVisit$StmtCallPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.WStatementPos term) throws E {
            term.Switch(variantVisit$WStatementPos);
        }
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.StmtIf> implements de.peeeq.wurstscript.ast.StmtIfPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPosPos _source = null;
        private de.peeeq.wurstscript.ast.ExprPos _cond = null;
        private de.peeeq.wurstscript.ast.WStatementsPos _thenBlock = null;
        private de.peeeq.wurstscript.ast.WStatementsPos _elseBlock = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.StmtIf termWStatement() {
            return term();
        }

        public de.peeeq.wurstscript.ast.WPosPos source() {
            if(_source == null)
                _source = de.peeeq.wurstscript.ast.AST.WPosPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.wurstscript.ast.StmtIfPos replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.wurstscript.ast.ExprPos cond() {
            if(_cond == null)
                _cond = de.peeeq.wurstscript.ast.AST.ExprPos(this, _term.cond(), 1);

            return _cond;
        }

        public de.peeeq.wurstscript.ast.StmtIfPos replaceCond(de.peeeq.wurstscript.ast.Expr cond) {
            return replace(_term.replaceCond(cond));
        }

        public de.peeeq.wurstscript.ast.WStatementsPos thenBlock() {
            if(_thenBlock == null)
                _thenBlock = de.peeeq.wurstscript.ast.AST.WStatementsPos(this, _term.thenBlock(), 2);

            return _thenBlock;
        }

        public de.peeeq.wurstscript.ast.StmtIfPos replaceThenBlock(de.peeeq.wurstscript.ast.WStatements thenBlock) {
            return replace(_term.replaceThenBlock(thenBlock));
        }

        public de.peeeq.wurstscript.ast.WStatementsPos elseBlock() {
            if(_elseBlock == null)
                _elseBlock = de.peeeq.wurstscript.ast.AST.WStatementsPos(this, _term.elseBlock(), 3);

            return _elseBlock;
        }

        public de.peeeq.wurstscript.ast.StmtIfPos replaceElseBlock(de.peeeq.wurstscript.ast.WStatements elseBlock) {
            return replace(_term.replaceElseBlock(elseBlock));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.StmtIf term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.wurstscript.ast.AST.SortPos get(int i) {
            int ith = i;

            if(ith < 0) ith += 4;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = AST.WPosPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_cond == null)
                        _cond = AST.ExprPos(this, _term.cond(), 1);
                    
                    return _cond;
                case 2:
                    if(_thenBlock == null)
                        _thenBlock = AST.WStatementsPos(this, _term.thenBlock(), 2);
                    
                    return _thenBlock;
                case 3:
                    if(_elseBlock == null)
                        _elseBlock = AST.WStatementsPos(this, _term.elseBlock(), 3);
                    
                    return _elseBlock;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort StmtIfPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort StmtIfPos invoked with too large parameter "+i+", sort has only 4 components");
                    }
            }
        }

        public int size() {
            return 4;
        }

        public de.peeeq.wurstscript.ast.StmtIfPos replace(de.peeeq.wurstscript.ast.StmtIf term) {
            return (de.peeeq.wurstscript.ast.StmtIfPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.WStatementsPos parent() {
            return (de.peeeq.wurstscript.ast.WStatementsPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.WStatementPos lsib() {
            return (de.peeeq.wurstscript.ast.WStatementPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.WStatementPos rsib() {
            return (de.peeeq.wurstscript.ast.WStatementPos) super.rsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrderStart();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path) {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.WStatementPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseStmtIfPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.CompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "StmtIfPos";
        }
    }
}

