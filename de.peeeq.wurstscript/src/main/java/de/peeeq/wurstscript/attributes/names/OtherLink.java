package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableCollection;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.attributes.prettyPrint.Spacer;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.VariableBinding;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;

/**
 *
 */
public abstract class OtherLink extends NameLink {
    private static final NameDef dummyDef = new NameDef() {
        @Override
        public void setSource(WPos source) {

        }

        @Override
        public WPos getSource() {
            return null;
        }

        @Override
        public void setModifiers(Modifiers modifiers) {

        }

        @Override
        public Modifiers getModifiers() {
            return null;
        }

        @Override
        public void setNameId(Identifier nameId) {

        }

        @Override
        public Identifier getNameId() {
            return null;
        }

        @Override
        public Element getParent() {
            return null;
        }

        @Override
        public <T> T match(Matcher<T> s) {
            return null;
        }

        @Override
        public void match(MatcherVoid s) {

        }

        @Override
        public NameDef copy() {
            return null;
        }

        @Override
        public NameDef copyWithRefs() {
            return null;
        }

        @Override
        public WurstType attrTyp() {
            return null;
        }

        @Override
        public boolean attrIsDynamicContext() {
            return false;
        }

        @Override
        public @Nullable PackageOrGlobal attrNearestPackage() {
            return null;
        }

        @Override
        public @Nullable NamedScope attrNearestNamedScope() {
            return null;
        }

        @Override
        public @Nullable WScope attrNearestScope() {
            return null;
        }

        @Override
        public String attrPathDescription() {
            return null;
        }

        @Override
        public CompilationUnit attrCompilationUnit() {
            return null;
        }

        @Override
        public @Nullable ClassDef attrNearestClassDef() {
            return null;
        }

        @Override
        public @Nullable ClassOrInterface attrNearestClassOrInterface() {
            return null;
        }

        @Override
        public @Nullable ClassOrModule attrNearestClassOrModule() {
            return null;
        }

        @Override
        public @Nullable StructureDef attrNearestStructureDef() {
            return null;
        }

        @Override
        public @Nullable FunctionImplementation attrNearestFuncDef() {
            return null;
        }

        @Override
        public @Nullable ExprClosure attrNearestExprClosure() {
            return null;
        }

        @Override
        public @Nullable ExprStatementsBlock attrNearestExprStatementsBlock() {
            return null;
        }

        @Override
        public @Nullable NameDef tryGetNameDef() {
            return null;
        }

        @Override
        public boolean attrIsCompiletime() {
            return false;
        }

        @Override
        public boolean attrHasAnnotation(String name) {
            return false;
        }

        @Override
        public Annotation attrGetAnnotation(String name) {
            return null;
        }

        @Override
        public boolean attrIsPublic() {
            return false;
        }

        @Override
        public boolean attrIsPublicRead() {
            return false;
        }

        @Override
        public boolean attrIsPrivate() {
            return false;
        }

        @Override
        public boolean attrIsProtected() {
            return false;
        }

        @Override
        public boolean attrIsStatic() {
            return false;
        }

        @Override
        public boolean attrIsOverride() {
            return false;
        }

        @Override
        public boolean attrIsAbstract() {
            return false;
        }

        @Override
        public boolean attrIsConstant() {
            return false;
        }

        @Override
        public boolean attrIsVararg() {
            return false;
        }

        @Override
        public WPos attrSource() {
            return null;
        }

        @Override
        public WPos attrErrorPos() {
            return null;
        }

        @Override
        public WurstModel getModel() {
            return null;
        }

        @Override
        public NameDef attrConfigActualNameDef() {
            return null;
        }

        @Override
        public boolean hasAnnotation(String annotation) {
            return false;
        }

        @Override
        public Annotation getAnnotation(String annotation) {
            return null;
        }

        @Override
        public void addError(String msg) {

        }

        @Override
        public void addWarning(String msg) {

        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public @Nullable TypeDef lookupType(String name, boolean showErrors) {
            return null;
        }

        @Override
        public PackageLink lookupPackage(String name, boolean showErrors) {
            return null;
        }

        @Override
        public NameLink lookupVar(String name, boolean showErrors) {
            return null;
        }

        @Override
        public NameLink lookupVarNoConfig(String name, boolean showErrors) {
            return null;
        }

        @Override
        public NameLink lookupMemberVar(WurstType receiverType, String name, boolean showErrors) {
            return null;
        }

        @Override
        public ImmutableCollection<FuncLink> lookupFuncs(String name, boolean showErrors) {
            return null;
        }

        @Override
        public ImmutableCollection<FuncLink> lookupFuncsNoConfig(String name, boolean showErrors) {
            return null;
        }

        @Override
        public ImmutableCollection<FuncLink> lookupMemberFuncs(WurstType receiverType, String name, boolean showErrors) {
            return null;
        }

        @Override
        public @Nullable TypeDef lookupType(String name) {
            return null;
        }

        @Override
        public PackageLink lookupPackage(String name) {
            return null;
        }

        @Override
        public NameLink lookupVar(String name) {
            return null;
        }

        @Override
        public NameLink lookupMemberVar(WurstType receiverType, String name) {
            return null;
        }

        @Override
        public ImmutableCollection<FuncLink> lookupFuncs(String name) {
            return null;
        }

        @Override
        public ImmutableCollection<FuncLink> lookupMemberFuncs(WurstType receiverType, String name) {
            return null;
        }

        @Override
        public String attrComment() {
            return null;
        }

        @Override
        public ImmutableCollection<WPackage> attrUsedPackages() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }

        @Override
        public String descriptionHtml() {
            return null;
        }

        @Override
        public boolean isSubtreeOf(Element other) {
            return false;
        }

        @Override
        public void prettyPrint(Spacer spacer, StringBuilder sb, int indent) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public <T> T match(AstElementWithSource.Matcher<T> s) {
            return null;
        }

        @Override
        public void match(AstElementWithSource.MatcherVoid s) {

        }

        @Override
        public <T> T match(Documentable.Matcher<T> s) {
            return null;
        }

        @Override
        public void match(Documentable.MatcherVoid s) {

        }

        @Override
        public <T> T match(HasModifier.Matcher<T> s) {
            return null;
        }

        @Override
        public void match(HasModifier.MatcherVoid s) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void clearAttributes() {

        }

        @Override
        public void clearAttributesLocal() {

        }

        @Override
        public Element get(int i) {
            return null;
        }

        @Override
        public Element set(int i, Element newElement) {
            return null;
        }

        @Override
        public void setParent(Element parent) {

        }

        @Override
        public void replaceBy(Element other) {

        }

        @Override
        public boolean structuralEquals(Element elem) {
            return false;
        }

        @Override
        public <T> T match(Element.Matcher<T> s) {
            return null;
        }

        @Override
        public void match(Element.MatcherVoid s) {

        }

        @Override
        public void accept(Visitor v) {

        }
    };

    private final String name;
    private final WurstType type;

    public OtherLink(Visibility visibility, String name, WurstType type) {
        super(visibility, null, Collections.emptyList());
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NameDef getDef() {
        return dummyDef;
    }

    @Override
    public NameLink withVisibility(Visibility newVis) {
        return this;
    }

    @Override
    public boolean receiverCompatibleWith(WurstType receiverType, Element location) {
        return false;
    }

    @Override
    public NameLink withTypeArgBinding(Element context, VariableBinding binding) {
        return this;
    }

    @Override
    public WurstType getTyp() {
        return type;
    }

    @Override
    public NameLink withDef(NameDef actual) {
        return this;
    }

    public abstract ImExpr translate(NameRef e, ImTranslator t, ImFunction f);
}
