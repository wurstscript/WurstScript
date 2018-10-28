package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;

/**
 *
 */
public abstract class TerminalInstruction {
    protected final ImBlock parent;

    public TerminalInstruction(ImBlock parent) {
        this.parent = parent;
    }

    abstract void intoStatements(ImStmts stmts, LlvmToIm llvmToIm);

    public abstract void accept(de.peeeq.wurstscript.jassIm.Element.Visitor visitor);

    static class Return extends TerminalInstruction {
        private final ImExprOpt expr;


        public Return(ImBlock parent, ImExprOpt expr) {
            super(parent);
            this.expr = expr;
        }

        @Override
        public void intoStatements(ImStmts stmts, LlvmToIm llvmToIm) {
            stmts.add(JassIm.ImReturn(llvmToIm.getTrace(), expr));
        }

        @Override
        public void accept(de.peeeq.wurstscript.jassIm.Element.Visitor visitor) {
            expr.accept(visitor);
        }
    }

    static class Goto extends TerminalInstruction {
        private final ImBlock block;

        public Goto(ImBlock parent, ImBlock block) {
            super(parent);
            this.block = block;
        }

        @Override
        public void intoStatements(ImStmts stmts, LlvmToIm llvmToIm) {
            stmts.add(JassIm.ImSet(llvmToIm.getTrace(), JassIm.ImVarAccess(parent.getBlockVar()), JassIm.ImIntVal(block.getNumber())));
        }

        @Override
        public void accept(de.peeeq.wurstscript.jassIm.Element.Visitor visitor) {

        }
    }

    static class ConditionalGoto extends TerminalInstruction {
        private final ImExpr condition;
        private final ImBlock ifTrueBlock;
        private final ImBlock ifFalseBlock;


        public ConditionalGoto(ImBlock parent, ImExpr condition, ImBlock ifTrueBlock, ImBlock ifFalseBlock) {
            super(parent);
            this.condition = condition;
            this.ifTrueBlock = ifTrueBlock;
            this.ifFalseBlock = ifFalseBlock;
        }

        @Override
        void intoStatements(ImStmts stmts, LlvmToIm llvmToIm) {
            Element trace = llvmToIm.getTrace();
            ImVar blockVar = parent.getBlockVar();
            stmts.add(
                    JassIm.ImIf(trace, condition,
                            JassIm.ImStmts(
                                    JassIm.ImSet(trace, JassIm.ImVarAccess(blockVar), JassIm.ImIntVal(ifTrueBlock.getNumber()))
                            ),
                            JassIm.ImStmts(
                                    JassIm.ImSet(trace, JassIm.ImVarAccess(blockVar), JassIm.ImIntVal(ifFalseBlock.getNumber()))
                            ))
            );
        }

        @Override
        public void accept(de.peeeq.wurstscript.jassIm.Element.Visitor visitor) {
            condition.accept(visitor);
        }
    }

    static class Unreachable extends TerminalInstruction {

        public Unreachable(ImBlock parent) {
            super(parent);
        }

        @Override
        void intoStatements(ImStmts stmts, LlvmToIm llvmToIm) {

        }

        @Override
        public void accept(de.peeeq.wurstscript.jassIm.Element.Visitor visitor) {

        }
    }

}
