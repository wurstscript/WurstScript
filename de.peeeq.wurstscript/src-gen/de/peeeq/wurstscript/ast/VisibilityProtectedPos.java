package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaNodePos;
import katja.common.KatjaSort;
import katja.common.KatjaTuplePosImpl;

public interface VisibilityProtectedPos extends de.peeeq.wurstscript.ast.VisibilityModifierPos, de.peeeq.wurstscript.ast.AST.TuplePos<de.peeeq.wurstscript.ast.VisibilityProtected> {

    //----- methods of VisibilityProtectedPos -----

    public de.peeeq.wurstscript.ast.VisibilityProtected termVisibilityModifier();
    public de.peeeq.wurstscript.ast.VisibilityProtected term();
    public de.peeeq.wurstscript.ast.WPosPos source();
    public de.peeeq.wurstscript.ast.VisibilityProtectedPos replaceSource(de.peeeq.wurstscript.ast.WPos source);
    public de.peeeq.wurstscript.ast.WPosPos get(int i);
    public int size();
    public de.peeeq.wurstscript.ast.VisibilityProtectedPos replace(de.peeeq.wurstscript.ast.VisibilityProtected term);
    public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent();
    public de.peeeq.wurstscript.ast.WPosPos lsib();
    public de.peeeq.wurstscript.ast.AST.TermPos<?> rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of VisibilityProtectedPos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.VisibilityProtectedPos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.VisibilityProtected> implements de.peeeq.wurstscript.ast.VisibilityProtectedPos {

        //----- attributes of Impl -----

        private de.peeeq.wurstscript.ast.WPosPos _source = null;

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.VisibilityProtected termVisibilityModifier() {
            return term();
        }

        public de.peeeq.wurstscript.ast.WPosPos source() {
            if(_source == null)
                _source = de.peeeq.wurstscript.ast.AST.WPosPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.wurstscript.ast.VisibilityProtectedPos replaceSource(de.peeeq.wurstscript.ast.WPos source) {
            return replace(_term.replaceSource(source));
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.VisibilityProtected term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.wurstscript.ast.WPosPos get(int i) {
            int ith = i;

            if(ith < 0) ith += 1;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = AST.WPosPos(this, _term.source(), 0);
                    
                    return _source;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort VisibilityProtectedPos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort VisibilityProtectedPos invoked with too large parameter "+i+", sort has only 1 components");
                    }
            }
        }

        public int size() {
            return 1;
        }

        public de.peeeq.wurstscript.ast.VisibilityProtectedPos replace(de.peeeq.wurstscript.ast.VisibilityProtected term) {
            return (de.peeeq.wurstscript.ast.VisibilityProtectedPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.AST.TuplePos<?> parent() {
            return (de.peeeq.wurstscript.ast.AST.TuplePos<?>) super.parent();
        }

        public de.peeeq.wurstscript.ast.WPosPos lsib() {
            return (de.peeeq.wurstscript.ast.WPosPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.AST.TermPos<?> rsib() {
            return (de.peeeq.wurstscript.ast.AST.TermPos<?>) super.rsib();
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

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseVisibilityProtectedPos(this);
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
            return "VisibilityProtectedPos";
        }
    }
}

