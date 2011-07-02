package de.peeeq.pscript.ast;

import katja.common.*;
import java.io.IOException;

public interface ANativeType extends de.peeeq.pscript.ast.ATypeDef, KatjaTuple {

    //----- methods of ANativeType -----

    public org.eclipse.emf.ecore.EObject source();
    public de.peeeq.pscript.ast.ANativeType replaceSource(org.eclipse.emf.ecore.EObject source);
    public java.lang.String name();
    public de.peeeq.pscript.ast.ANativeType replaceName(java.lang.String name);
    public java.lang.String origName();
    public de.peeeq.pscript.ast.ANativeType replaceOrigName(java.lang.String origName);
    public java.lang.String superName();
    public de.peeeq.pscript.ast.ANativeType replaceSuperName(java.lang.String superName);
    public Object get(int i);
    public int size();
    public de.peeeq.pscript.ast.ANativeType replace(int pos, Object term);
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeDef.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E;

    //----- nested classes of ANativeType -----

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(org.eclipse.emf.ecore.EObject term) throws E;
        public void visit(java.lang.String term) throws E;
        public void visit(de.peeeq.pscript.ast.ANativeType term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.pscript.ast.ANativeType.VisitorType<E> {
    }

    static class Impl extends KatjaTupleImpl implements de.peeeq.pscript.ast.ANativeType {

        //----- attributes of Impl -----

        private org.eclipse.emf.ecore.EObject _source = null;
        private java.lang.String _name = null;
        private java.lang.String _origName = null;
        private java.lang.String _superName = null;

        //----- methods of Impl -----

        public org.eclipse.emf.ecore.EObject source() {
            return _source;
        }

        public de.peeeq.pscript.ast.ANativeType replaceSource(org.eclipse.emf.ecore.EObject source) {
            return replace(0, source);
        }

        public java.lang.String name() {
            return _name;
        }

        public de.peeeq.pscript.ast.ANativeType replaceName(java.lang.String name) {
            return replace(1, name);
        }

        public java.lang.String origName() {
            return _origName;
        }

        public de.peeeq.pscript.ast.ANativeType replaceOrigName(java.lang.String origName) {
            return replace(2, origName);
        }

        public java.lang.String superName() {
            return _superName;
        }

        public de.peeeq.pscript.ast.ANativeType replaceSuperName(java.lang.String superName) {
            return replace(3, superName);
        }

        Impl(org.eclipse.emf.ecore.EObject source, java.lang.String name, java.lang.String origName, java.lang.String superName) {
            if(source == null)
                throw new IllegalArgumentException("constructor of sort ANativeType invoked with null parameter source");
            if(name == null)
                throw new IllegalArgumentException("constructor of sort ANativeType invoked with null parameter name");
            if(origName == null)
                throw new IllegalArgumentException("constructor of sort ANativeType invoked with null parameter origName");
            if(superName == null)
                throw new IllegalArgumentException("constructor of sort ANativeType invoked with null parameter superName");

            this._source = source;
            this._name = name;
            this._origName = origName;
            this._superName = superName;
        }

        public Object get(int i) {
            int ith = i;

            if(ith < 0) ith += 4;

            switch(ith) {
                case 0: return _source;
                case 1: return _name;
                case 2: return _origName;
                case 3: return _superName;
                default:
                    if(ith < 0) {
                        throw new IllegalArgumentException("get on sort ANativeType invoked with negative parameter "+i);
                    } else {
                        throw new IllegalArgumentException("get on sort ANativeType invoked with too large parameter "+i+", sort has only 4 components");
                    }
            }
        }

        public int size() {
            return 4;
        }

        public de.peeeq.pscript.ast.ANativeType replace(int pos, Object term) {
            if(pos < 0)
                throw new IllegalArgumentException("replace on sort ANativeType invoked with negative parameter "+pos);
            if(pos >= 4)
                throw new IllegalArgumentException("replace on sort ANativeType invoked with too large parameter "+pos+", sort has only 4 components");

            if(pos == 0 && !(term instanceof org.eclipse.emf.ecore.EObject))
                throw new IllegalArgumentException("replace on sort ANativeType invoked with term of incorrect sort, EObject expected");
            if(pos == 1 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ANativeType invoked with term of incorrect sort, String expected");
            if(pos == 2 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ANativeType invoked with term of incorrect sort, String expected");
            if(pos == 3 && !(term instanceof java.lang.String))
                throw new IllegalArgumentException("replace on sort ANativeType invoked with term of incorrect sort, String expected");

            return (de.peeeq.pscript.ast.ANativeType) pscriptAST.unique(new de.peeeq.pscript.ast.ANativeType.Impl(
                pos == 0 ? (org.eclipse.emf.ecore.EObject) term : _source,
                pos == 1 ? (java.lang.String) term : _name,
                pos == 2 ? (java.lang.String) term : _origName,
                pos == 3 ? (java.lang.String) term : _superName
            ));
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.ATypeDef.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANativeType(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.pscript.ast.AElement.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseANativeType(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("pscriptAST.ANativeType");
            builder.append("( ");
            if(_source instanceof KatjaElement) ((KatjaElement) _source).toJavaCode(builder); else throw new UnsupportedOperationException("extern sort EObject can't be unparsed to Java code (don't know how to do it)");
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            builder.append("\"").append(_origName.toString()).append("\"");
            builder.append(", ");
            builder.append("\"").append(_superName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            builder.append("ANativeType");
            builder.append("( ");
            builder.append(_source.toString());
            builder.append(", ");
            builder.append("\"").append(_name.toString()).append("\"");
            builder.append(", ");
            builder.append("\"").append(_origName.toString()).append("\"");
            builder.append(", ");
            builder.append("\"").append(_superName.toString()).append("\"");
            builder.append(" )");

            return builder;
        }

        public final String sortName() {
            return "ANativeType";
        }
    }
}

