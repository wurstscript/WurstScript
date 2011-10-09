package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.util.List;
import java.io.IOException;

public interface IndexesPos extends de.peeeq.wurstscript.ast.AST.ListPos<de.peeeq.wurstscript.ast.Indexes, de.peeeq.wurstscript.ast.ExprPos, de.peeeq.wurstscript.ast.Expr> {

    //----- methods of IndexesPos -----

    public de.peeeq.wurstscript.ast.Indexes term();
    public de.peeeq.wurstscript.ast.IndexesPos add(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.IndexesPos remove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.IndexesPos appBack(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos appFront(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.IndexesPos reverse();
    public de.peeeq.wurstscript.ast.IndexesPos toSet();
    public de.peeeq.wurstscript.ast.IndexesPos setAdd(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos setRemove(de.peeeq.wurstscript.ast.Expr element);
    public de.peeeq.wurstscript.ast.IndexesPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.IndexesPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public de.peeeq.wurstscript.ast.IndexesPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list);
    public KatjaList<de.peeeq.wurstscript.ast.ExprPos> toList();
    public de.peeeq.wurstscript.ast.IndexesPos replace(de.peeeq.wurstscript.ast.Indexes term);
    public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent();
    public de.peeeq.wurstscript.ast.StringPos lsib();
    public de.peeeq.wurstscript.ast.AST.SortPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);

    //----- nested classes of IndexesPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.ExprPos> implements KatjaList<de.peeeq.wurstscript.ast.ExprPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.wurstscript.ast.ExprPos... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.ExprPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort ExprPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.wurstscript.ast.ExprPos> createInstance(de.peeeq.wurstscript.ast.ExprPos[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.ExprPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort ExprPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.ExprPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.ExprPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.wurstscript.ast.ExprPos element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
        }

        public final String sortName() {
            return "<anonymous>";
        }
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E;
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
        public void visit(de.peeeq.wurstscript.ast.ExprNullPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IndexesPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArgumentsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.DoublePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.BooleanPos term) throws E;
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
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.IndexesPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<Object, E> variantVisit$ExprAssignablePos = new de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<Object, E>() { public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<Object, E> variantVisit$ExprAtomicPos = new de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<Object, E>() { public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } public final Object CaseExprNullPos(de.peeeq.wurstscript.ast.ExprNullPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.ExprPos.Switch<Object, E> variantVisit$ExprPos = new de.peeeq.wurstscript.ast.ExprPos.Switch<Object, E>() { public final Object CaseExprBinaryPos(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E { visit(term); return null; } public final Object CaseExprUnaryPos(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E { visit(term); return null; } public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } public final Object CaseExprNullPos(de.peeeq.wurstscript.ast.ExprNullPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E> variantVisit$OpBinaryPos = new de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E>() { public final Object CaseOpOrPos(de.peeeq.wurstscript.ast.OpOrPos term) throws E { visit(term); return null; } public final Object CaseOpAndPos(de.peeeq.wurstscript.ast.OpAndPos term) throws E { visit(term); return null; } public final Object CaseOpEqualsPos(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E { visit(term); return null; } public final Object CaseOpUnequalsPos(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E { visit(term); return null; } public final Object CaseOpLessEqPos(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E { visit(term); return null; } public final Object CaseOpLessPos(de.peeeq.wurstscript.ast.OpLessPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterEqPos(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterPos(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E { visit(term); return null; } public final Object CaseOpPlusPos(de.peeeq.wurstscript.ast.OpPlusPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } public final Object CaseOpMultPos(de.peeeq.wurstscript.ast.OpMultPos term) throws E { visit(term); return null; } public final Object CaseOpDivRealPos(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E { visit(term); return null; } public final Object CaseOpModRealPos(de.peeeq.wurstscript.ast.OpModRealPos term) throws E { visit(term); return null; } public final Object CaseOpModIntPos(de.peeeq.wurstscript.ast.OpModIntPos term) throws E { visit(term); return null; } public final Object CaseOpDivIntPos(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E> variantVisit$OpUnaryPos = new de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E>() { public final Object CaseOpNotPos(de.peeeq.wurstscript.ast.OpNotPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignablePos term) throws E {
            term.Switch(variantVisit$ExprAssignablePos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomicPos term) throws E {
            term.Switch(variantVisit$ExprAtomicPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E {
            term.Switch(variantVisit$ExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E {
            term.Switch(variantVisit$OpBinaryPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E {
            term.Switch(variantVisit$OpUnaryPos);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.Indexes, de.peeeq.wurstscript.ast.ExprPos, de.peeeq.wurstscript.ast.Expr> implements de.peeeq.wurstscript.ast.IndexesPos {

        //----- methods of Impl -----

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.Indexes term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.wurstscript.ast.ExprPos getElementInstance(int pos) {
            return de.peeeq.wurstscript.ast.AST.ExprPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.wurstscript.ast.ExprPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.ExprPos[size];
        }

        public de.peeeq.wurstscript.ast.IndexesPos add(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.add(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.wurstscript.ast.IndexesPos remove(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.wurstscript.ast.IndexesPos appBack(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos appFront(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.wurstscript.ast.IndexesPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.wurstscript.ast.IndexesPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.wurstscript.ast.IndexesPos setAdd(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos setRemove(de.peeeq.wurstscript.ast.Expr element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.wurstscript.ast.IndexesPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.wurstscript.ast.IndexesPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.wurstscript.ast.IndexesPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.Expr> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.wurstscript.ast.ExprPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new IndexesPos.ListImpl(values);
        }

        public de.peeeq.wurstscript.ast.IndexesPos replace(de.peeeq.wurstscript.ast.Indexes term) {
            return (de.peeeq.wurstscript.ast.IndexesPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent() {
            return (de.peeeq.wurstscript.ast.AST.TuplePos<?>) super.parent();
        }

        public de.peeeq.wurstscript.ast.StringPos lsib() {
            return (de.peeeq.wurstscript.ast.StringPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos rsib() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.rsib();
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
            return "IndexesPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.ExprPos> implements KatjaList<de.peeeq.wurstscript.ast.ExprPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.wurstscript.ast.ExprPos... elements) {
                super(elements);

                for(de.peeeq.wurstscript.ast.ExprPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort ExprPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.wurstscript.ast.ExprPos> createInstance(de.peeeq.wurstscript.ast.ExprPos[] elements, boolean isSet) {
                for(de.peeeq.wurstscript.ast.ExprPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort ExprPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) AST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.wurstscript.ast.ExprPos[] createArray(int size) {
                return new de.peeeq.wurstscript.ast.ExprPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.wurstscript.ast.ExprPos element : values) {
                    if(first) first = false;
                    else builder.append(", ");
                    element.toString(builder);
                }
                builder.append(" )");

                return builder;
            }

            public Appendable toJavaCode(Appendable builder) throws IOException {
                throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
            }

            public final String sortName() {
                return "<anonymous>";
            }
        }
    }
}

