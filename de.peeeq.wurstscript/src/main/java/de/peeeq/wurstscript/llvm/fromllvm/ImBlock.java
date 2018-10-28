package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ImBlock {
    private String name;
    private List<ImStmt> stmts;
    private TerminalInstruction terminalInstruction;
    private int number = -42;
    private ImVar blockVar;

    public ImBlock(String name) {
        this.name = name;
        this.stmts = new ArrayList<>();
        this.terminalInstruction = new TerminalInstruction.Return(this, JassIm.ImNoExpr());
    }

    @Override
    public String toString() {
        return name + " { " + stmts + "\n" + terminalInstruction +  " }";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ImStmt> getStmts() {
        return stmts;
    }

    public TerminalInstruction getTerminalInstruction() {
        return terminalInstruction;
    }

    public void setTerminalInstruction(TerminalInstruction terminalInstruction) {
        this.terminalInstruction = terminalInstruction;
    }

    public void toStatements(LlvmToIm llvmToIm, ImFunction func, ImStmts into) {
        into.addAll(stmts);
        if (terminalInstruction != null) {
            terminalInstruction.intoStatements(into, llvmToIm);
        }
    }

    public ImVar getBlockVar() {
        return blockVar;
    }


    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setBlockVar(ImVar blockVar) {
        this.blockVar = blockVar;
    }

    public void addStmt(ImStmt stmt) {
        stmts.add(stmt);
    }

    public void accept(Element.Visitor visitor) {
        for (ImStmt stmt : stmts) {
            stmt.accept(visitor);
        }
        if (terminalInstruction != null) {
            terminalInstruction.accept(visitor);
        }
    }
}
