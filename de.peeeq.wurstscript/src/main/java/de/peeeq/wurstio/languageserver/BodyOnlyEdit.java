package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;

/**
 * A new version of a compilation unit which differs from the one in the model in nothing but the bodies of its
 * functions, constructors, destructors and init blocks.
 * <p>
 * The other units only see what such a unit declares, never its bodies, so the declarations they resolved to stay
 * valid. Applying the edit moves the new bodies, the new source positions and the new parse information into the
 * unit of the model, which keeps every node another unit's attributes point at. Only the unit itself has to be
 * checked again.
 * <p>
 * The bodies of a module and of an interface are not bodies in this sense: a module is copied into every class
 * using it, and whether an interface function has a body decides whether it is abstract. A unit declaring Jass
 * names is never edited in place either, because every unit sees them.
 */
final class BodyOnlyEdit {

    /** The one position both trees take while they are compared, so that positions never differ. */
    private static final WPos NO_POSITION = new WPos("", null, 0, 0);

    private BodyOnlyEdit() {
    }

    /** Whether {@code fresh} differs from {@code old} in nothing but bodies, see {@link BodyOnlyEdit}. */
    static boolean applies(CompilationUnit old, CompilationUnit fresh) {
        if (!old.getJassDecls().isEmpty() || !fresh.getJassDecls().isEmpty()) {
            return false;
        }
        if (!modulesExpandedCleanly(old.getPackages())) {
            return false;
        }
        WPackages a = old.getPackages().copy();
        WPackages b = fresh.getPackages().copy();
        normalise(a, false);
        normalise(b, false);
        return a.structuralEquals(b) && modulesStayPut(old.getPackages(), fresh.getPackages());
    }

    /**
     * Moves the bodies, positions and parse information of {@code fresh} into {@code old}, for which
     * {@link #applies} holds. {@code fresh} is left without its bodies.
     */
    static void apply(CompilationUnit old, CompilationUnit fresh) {
        transplant(old.getPackages(), fresh.getPackages(), false);
        old.setCuInfo(fresh.getCuInfo());
    }

    /**
     * Whether every class and module of the unit got an instantiation for each module it uses, without an error.
     * Expanding a module reports what is wrong with its use only while it expands, and an edit in place keeps the
     * instantiations rather than expanding them again.
     */
    private static boolean modulesExpandedCleanly(Element e) {
        if (e instanceof ClassOrModuleOrModuleInstanciation c && !(e instanceof ModuleInstanciation)) {
            if (c.getP_moduleInstanciations().size() != c.getModuleUses().size()) {
                return false;
            }
            for (ModuleUse use : c.getModuleUses()) {
                ModuleDef used = use.attrModuleDef();
                if (used == null || used.getTypeParameters().size() != use.getTypeArgs().size()) {
                    return false;
                }
            }
        }
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (e instanceof ClassOrModuleOrModuleInstanciation c && child == c.getP_moduleInstanciations()) {
                continue;
            }
            if (!modulesExpandedCleanly(child)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether nothing in a module the unit declares moves. Every class using the module, in this unit or another,
     * holds a copy of it with its positions, and the copies are kept.
     */
    private static boolean modulesStayPut(WPackages old, WPackages fresh) {
        for (int p = 0; p < old.size(); p++) {
            WEntities oldElements = old.get(p).getElements();
            WEntities freshElements = fresh.get(p).getElements();
            for (int i = 0; i < oldElements.size(); i++) {
                if (oldElements.get(i) instanceof ModuleDef && !samePositions(oldElements.get(i), freshElements.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean samePositions(Element old, Element fresh) {
        if (old instanceof AstElementWithSource o && fresh instanceof AstElementWithSource f
            && !samePosition(o.getSource(), f.getSource())) {
            return false;
        }
        for (int i = 0; i < old.size(); i++) {
            Element child = old.get(i);
            if (old instanceof ClassOrModuleOrModuleInstanciation c && child == c.getP_moduleInstanciations()) {
                continue;
            }
            if (!samePositions(child, fresh.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** The same characters at the same lines and columns: lines come from the line table of each parse. */
    private static boolean samePosition(WPos a, WPos b) {
        return a.getLeftPos() == b.getLeftPos() && a.getRightPos() == b.getRightPos()
            && a.getLine() == b.getLine() && a.getStartColumn() == b.getStartColumn()
            && a.getEndLine() == b.getEndLine() && a.getEndColumn() == b.getEndColumn();
    }

    /** Takes positions, module instantiations and bodies out of a copy, so that only the declarations remain. */
    private static void normalise(Element e, boolean inTemplate) {
        if (e instanceof AstElementWithSource s) {
            s.setSource(NO_POSITION);
        }
        if (e instanceof ClassOrModuleOrModuleInstanciation c) {
            c.getP_moduleInstanciations().clear();
        }
        boolean template = inTemplate || isTemplate(e);
        if (!template && e instanceof AstElementWithBody holder && holdsBody(e)) {
            holder.setBody(Ast.WStatements());
        }
        for (int i = 0; i < e.size(); i++) {
            normalise(e.get(i), template);
        }
    }

    private static void transplant(Element old, Element fresh, boolean inTemplate) {
        if (old instanceof AstElementWithSource o && fresh instanceof AstElementWithSource f) {
            o.setSource(f.getSource());
        }
        // the positions a node has besides its own, of a token in it which errors point at
        if (old instanceof SomeSuperConstructorCall o && fresh instanceof SomeSuperConstructorCall f) {
            o.setKeywordSource(f.getKeywordSource());
        }
        if (old instanceof ExprClosure o && fresh instanceof ExprClosure f) {
            o.setArrowSource(f.getArrowSource());
        }
        boolean template = inTemplate || isTemplate(old);
        WStatements movedBody = null;
        if (!template && old instanceof AstElementWithBody o && holdsBody(old)) {
            AstElementWithBody f = (AstElementWithBody) fresh;
            movedBody = f.getBody();
            f.setBody(Ast.WStatements());
            o.setBody(movedBody);
        }
        for (int i = 0; i < old.size(); i++) {
            Element oldChild = old.get(i);
            if (oldChild == movedBody) {
                continue;
            }
            if (old instanceof ClassOrModuleOrModuleInstanciation c && oldChild == c.getP_moduleInstanciations()) {
                continue;
            }
            transplant(oldChild, fresh.get(i), template);
        }
        if (old instanceof ClassOrModule c) {
            moveInstantiations(c);
        }
    }

    /**
     * An instantiation is a copy of the module, which keeps the module's positions, except for what it makes
     * itself: its own position and that of its name and of its default constructor, all made from the position of
     * the use, and its type arguments, made from theirs. Those follow the use, which just got its new position.
     */
    private static void moveInstantiations(ClassOrModule c) {
        for (int i = 0; i < c.getModuleUses().size(); i++) {
            ModuleUse use = c.getModuleUses().get(i);
            ModuleInstanciation mi = c.getP_moduleInstanciations().get(i);
            replacePosition(mi, mi.getSource(), use.getSource().artificial());
            mi.getNameId().setSource(use.getModuleNameId().getSource().artificial());
            for (int t = 0; t < mi.getTypeArgs().size() && t < use.getTypeArgs().size(); t++) {
                mi.getTypeArgs().get(t).setSource(use.getTypeArgs().get(t).getSource().artificial());
            }
        }
    }

    /** Gives every node of the instantiation which has the position {@code from} itself, not a copy of the module. */
    private static void replacePosition(Element e, WPos from, WPos to) {
        if (e instanceof AstElementWithSource s && s.getSource() == from) {
            s.setSource(to);
        }
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (e instanceof ClassOrModuleOrModuleInstanciation c && child == c.getP_moduleInstanciations()) {
                continue;
            }
            replacePosition(child, from, to);
        }
    }

    private static boolean isTemplate(Element e) {
        return e instanceof ModuleDef || e instanceof InterfaceDef || e instanceof ModuleInstanciation;
    }

    /** The bodies an edit can replace: of functions, constructors, destructors and init blocks, not of statements. */
    private static boolean holdsBody(Element e) {
        return e instanceof FunctionLike;
    }
}
