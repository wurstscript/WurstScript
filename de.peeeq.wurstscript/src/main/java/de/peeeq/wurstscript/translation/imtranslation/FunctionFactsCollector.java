package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * What the optimiser asks of the body of a function, answered in one walk: the functions it uses
 * ({@link UsedFunctions#calculate}), the variables it reads ({@link UsedVariables#calculateReadVars(ImFunction)}) and,
 * when asked, its assignments ({@link ImTranslator#setStatementsOf}).
 * <p>
 * The three used to be three walks over every reachable function, once for each garbage removal of a build, and the
 * garbage removal runs between four and nine times. The answers are the ones the separate walks give, in the same order:
 * the order of the used functions decides the order of the call relation, which the inliner and the emission of Jass
 * follow. In a unit test {@link ImTranslator} compares every answer with the separate walks.
 */
final class FunctionFactsCollector extends ImFunction.DefaultVisitor {
    final Set<ImFunction> usedFunctions = new LinkedHashSet<>();
    final Set<ImVar> readVariables = new LinkedHashSet<>();
    /** The assignments, the ones inside another assignment before it; null when they were not asked for. */
    final List<ImSet> sets;

    FunctionFactsCollector(boolean collectSets) {
        this.sets = collectSets ? new ArrayList<>() : null;
    }

    static FunctionFactsCollector collect(ImFunction function, boolean collectSets) {
        FunctionFactsCollector collector = new FunctionFactsCollector(collectSets);
        function.accept(collector);
        return collector;
    }

    @Override
    public void visit(ImFunctionCall e) {
        super.visit(e);
        usedFunctions.add(e.getFunc());
    }

    @Override
    public void visit(ImFuncRef e) {
        super.visit(e);
        usedFunctions.add(e.getFunc());
    }

    @Override
    public void visit(ImMethodCall e) {
        super.visit(e);
        for (ImMethod sub : e.getMethod().getSubMethods()) {
            usedFunctions.add(sub.getImplementation());
        }
        usedFunctions.add(e.getMethod().getImplementation());
    }

    @Override
    public void visit(ImVarAccess e) {
        readVariables.add(e.getVar());
    }

    @Override
    public void visit(ImVarArrayAccess e) {
        readVariables.add(e.getVar());
        super.visit(e); // the indexes
    }

    @Override
    public void visit(ImMemberAccess e) {
        readVariables.add(e.getVar());
        super.visit(e); // the receiver and the indexes
    }

    @Override
    public void visit(ImVarargLoop e) {
        e.getLoopVars().forEach(v -> readVariables.add(v.getVar()));
        super.visit(e);
    }

    @Override
    public void visit(ImSet e) {
        // What is assigned to is not read, but what it is made of is (the index of an array element, the receiver of a
        // field), and may call a function: those are visited in the order of the walk over the whole assignment.
        if (!(e.getLeft() instanceof ImVarAccess)) {
            visitTargetOperands(e.getLeft());
        }
        e.getRight().accept(this);
        if (sets != null) {
            sets.add(e);
        }
    }

    private void visitTargetOperands(ImLExpr target) {
        if (target instanceof ImVarAccess) {
            // written only
        } else if (target instanceof ImMemberAccess memberAccess) {
            memberAccess.getReceiver().accept(this);
            memberAccess.getIndexes().accept(this);
        } else if (target instanceof ImVarArrayAccess arrayAccess) {
            arrayAccess.getIndexes().accept(this);
        } else if (target instanceof ImTupleSelection tupleSelection) {
            visitTargetOperands((ImLExpr) tupleSelection.getTupleExpr());
        } else if (target instanceof ImStatementExpr statementExpr) {
            statementExpr.getStatements().accept(this);
            visitTargetOperands((ImLExpr) statementExpr.getExpr());
        } else if (target instanceof ImTupleExpr tupleExpr) {
            for (ImExpr element : tupleExpr.getExprs()) {
                visitTargetOperands((ImLExpr) element);
            }
        }
    }
}
