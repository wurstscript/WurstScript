package de.peeeq.wurstscript.ast;

import katja.common.KatjaSort;

public interface OpBinaryPos extends de.peeeq.wurstscript.ast.OpPos, de.peeeq.wurstscript.ast.AST.SortPos {

    //----- methods of OpBinaryPos -----

    public de.peeeq.wurstscript.ast.OpBinary termOp();
    public KatjaSort term();
    public de.peeeq.wurstscript.ast.OpBinary termOpBinary();
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.OpBinaryPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of OpBinaryPos -----

    public static interface Switch<CT, E extends Throwable> {

        //----- methods of Switch<CT, E extends Throwable> -----

        public CT CaseOpOrPos(de.peeeq.wurstscript.ast.OpOrPos term) throws E;
        public CT CaseOpAndPos(de.peeeq.wurstscript.ast.OpAndPos term) throws E;
        public CT CaseOpEqualsPos(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E;
        public CT CaseOpUnequalsPos(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E;
        public CT CaseOpLessEqPos(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E;
        public CT CaseOpLessPos(de.peeeq.wurstscript.ast.OpLessPos term) throws E;
        public CT CaseOpGreaterEqPos(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E;
        public CT CaseOpGreaterPos(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E;
        public CT CaseOpPlusPos(de.peeeq.wurstscript.ast.OpPlusPos term) throws E;
        public CT CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
        public CT CaseOpMultPos(de.peeeq.wurstscript.ast.OpMultPos term) throws E;
        public CT CaseOpDivRealPos(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E;
        public CT CaseOpModRealPos(de.peeeq.wurstscript.ast.OpModRealPos term) throws E;
        public CT CaseOpModIntPos(de.peeeq.wurstscript.ast.OpModIntPos term) throws E;
        public CT CaseOpDivIntPos(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E;
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E;
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
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.OpBinaryPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E> variantVisit$OpBinaryPos = new de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E>() { public final Object CaseOpOrPos(de.peeeq.wurstscript.ast.OpOrPos term) throws E { visit(term); return null; } public final Object CaseOpAndPos(de.peeeq.wurstscript.ast.OpAndPos term) throws E { visit(term); return null; } public final Object CaseOpEqualsPos(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E { visit(term); return null; } public final Object CaseOpUnequalsPos(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E { visit(term); return null; } public final Object CaseOpLessEqPos(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E { visit(term); return null; } public final Object CaseOpLessPos(de.peeeq.wurstscript.ast.OpLessPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterEqPos(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterPos(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E { visit(term); return null; } public final Object CaseOpPlusPos(de.peeeq.wurstscript.ast.OpPlusPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } public final Object CaseOpMultPos(de.peeeq.wurstscript.ast.OpMultPos term) throws E { visit(term); return null; } public final Object CaseOpDivRealPos(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E { visit(term); return null; } public final Object CaseOpModRealPos(de.peeeq.wurstscript.ast.OpModRealPos term) throws E { visit(term); return null; } public final Object CaseOpModIntPos(de.peeeq.wurstscript.ast.OpModIntPos term) throws E { visit(term); return null; } public final Object CaseOpDivIntPos(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E {
            term.Switch(variantVisit$OpBinaryPos);
        }
    }
}

