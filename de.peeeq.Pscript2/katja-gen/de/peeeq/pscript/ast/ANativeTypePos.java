package de.peeeq.pscript.ast;

import java.util.List;
import katja.common.*;
import java.io.IOException;

public interface ANativeTypePos extends de.peeeq.pscript.ast.ATypeDefPos, de.peeeq.pscript.ast.pscriptAST.TuplePos<de.peeeq.pscript.ast.ANativeType> {

    //----- methods of ANativeTypePos -----

    public de.peeeq.pscript.ast.ANativeType termATypeDef();
    public de.peeeq.pscript.ast.ANativeType termAElement();
    public de.peeeq.pscript.ast.ANativeType term();
    public de.peeeq.pscript.ast.EObjectPos source();
    public de.peeeq.pscript.ast.ANativeTypePos replaceSource(org.eclipse.emf.ecore.EObject source);
    public de.peeeq.pscript.ast.StringPos name();
    public de.peeeq.pscript.ast.ANativeTypePos replaceName(java.lang.String name);
    public de.peeeq.pscript.ast.StringPos origName();
    public de.peeeq.pscript.ast.ANativeTypePos replaceOrigName(java.lang.String origName);
    public de.peeeq.pscript.ast.StringPos superName();
    public de.peeeq.pscript.ast.ANativeTypePos replaceSuperName(java.lang.String superName);
    public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i);
    public int size();
    public de.peeeq.pscript.ast.ANativeTypePos replace(de.peeeq.pscript.ast.ANativeType term);
    public de.peeeq.pscript.ast.AElementsPos parent();
    public de.peeeq.pscript.ast.AElementPos lsib();
    public de.peeeq.pscript.ast.AElementPos rsib();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder();
    public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart();
    public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeDefPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElementPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ANativeTypePos -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.pscript.ast.EObjectPos term) throws E;
        public void visit(de.peeeq.pscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.pscript.ast.ANativeTypePos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ANativeTypePos.VisitorType<E> {
    }

    static class Impl extends KatjaTuplePosImpl<de.peeeq.pscript.ast.ACompilationUnitPos, de.peeeq.pscript.ast.ANativeType> implements de.peeeq.pscript.ast.ANativeTypePos {

        //----- attributes of Impl -----

        private de.peeeq.pscript.ast.EObjectPos _source = null;
        private de.peeeq.pscript.ast.StringPos _name = null;
        private de.peeeq.pscript.ast.StringPos _origName = null;
        private de.peeeq.pscript.ast.StringPos _superName = null;

        //----- methods of Impl -----

        public de.peeeq.pscript.ast.ANativeType termATypeDef() {
            return term();
        }

        public de.peeeq.pscript.ast.ANativeType termAElement() {
            return term();
        }

        public de.peeeq.pscript.ast.EObjectPos source() {
            if(_source == null)
                _source = de.peeeq.pscript.ast.pscriptAST.EObjectPos(this, _term.source(), 0);

            return _source;
        }

        public de.peeeq.pscript.ast.ANativeTypePos replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(_term.replaceSource(source));
        }

        public de.peeeq.pscript.ast.StringPos name() {
            if(_name == null)
                _name = de.peeeq.pscript.ast.pscriptAST.StringPos(this, _term.name(), 1);

            return _name;
        }

        public de.peeeq.pscript.ast.ANativeTypePos replaceName(java.lang.String name) {
            return replace(_term.replaceName(name));
        }

        public de.peeeq.pscript.ast.StringPos origName() {
            if(_origName == null)
                _origName = de.peeeq.pscript.ast.pscriptAST.StringPos(this, _term.origName(), 2);

            return _origName;
        }

        public de.peeeq.pscript.ast.ANativeTypePos replaceOrigName(java.lang.String origName) {
            return replace(_term.replaceOrigName(origName));
        }

        public de.peeeq.pscript.ast.StringPos superName() {
            if(_superName == null)
                _superName = de.peeeq.pscript.ast.pscriptAST.StringPos(this, _term.superName(), 3);

            return _superName;
        }

        public de.peeeq.pscript.ast.ANativeTypePos replaceSuperName(java.lang.String superName) {
            return replace(_term.replaceSuperName(superName));
        }

        Impl(KatjaNodePos<de.peeeq.pscript.ast.ACompilationUnitPos, ?> parent, de.peeeq.pscript.ast.ANativeType term, int pos) {
            super(parent, term, pos);
        }

        public de.peeeq.pscript.ast.pscriptAST.LeafPos<?> get(int i) {
            int ith = i;

            if(ith < 0) ith += 4;

            switch(ith) {
                case 0:
                    if(_source == null)
                        _source = pscriptAST.EObjectPos(this, _term.source(), 0);
                    
                    return _source;
                case 1:
                    if(_name == null)
                        _name = pscriptAST.StringPos(this, _term.name(), 1);
                    
                    return _name;
                case 2:
                    if(_origName == null)
                        _origName = pscriptAST.StringPos(this, _term.origName(), 2);
                    
                    return _origName;
                case 3:
                    if(_superName == null)
                        _superName = pscriptAST.StringPos(this, _term.superName(), 3);
                    
                    return _superName;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ANativeTypePos invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ANativeTypePos invoked with too large parameter "+i+", sort has only 4 components");
                    }
            }
        }

        public int size() {
            return 4;
        }

        public de.peeeq.pscript.ast.ANativeTypePos replace(de.peeeq.pscript.ast.ANativeType term) {
            return (de.peeeq.pscript.ast.ANativeTypePos) super.replace(term);
        }

        protected de.peeeq.pscript.ast.ACompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.pscript.ast.ACompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort ACompilationUnit");

            return pscriptAST.ACompilationUnitPos((ACompilationUnit) term);
        }

        public de.peeeq.pscript.ast.AElementsPos parent() {
            return (de.peeeq.pscript.ast.AElementsPos) super.parent();
        }

        public de.peeeq.pscript.ast.AElementPos lsib() {
            return (de.peeeq.pscript.ast.AElementPos) super.lsib();
        }

        public de.peeeq.pscript.ast.AElementPos rsib() {
            return (de.peeeq.pscript.ast.AElementPos) super.rsib();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos preOrderSkip() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrder() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrder();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos postOrderStart() {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.postOrderStart();
        }

        public de.peeeq.pscript.ast.pscriptAST.SortPos follow(List<Integer> path) {
            return (de.peeeq.pscript.ast.pscriptAST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeDefPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANativeTypePos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElementPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANativeTypePos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ACompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@ACompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "ANativeTypePos";
        }
    }
}

