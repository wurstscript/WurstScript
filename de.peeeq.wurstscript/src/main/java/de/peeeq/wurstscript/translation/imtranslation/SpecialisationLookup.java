package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImVar;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Answers what a specialised node was copied from.
 * <p>
 * The interpreter is handed a program rather than the translation which produced it, which is why it
 * had no way to tell two type variables apart except by name - and two parameters which merely share
 * a name are not the same parameter. This is the one question it needs answered, narrow enough to hand
 * over without handing over the translator.
 */
public interface SpecialisationLookup {

    /** The node {@code node} was ultimately copied from, or {@code node} itself. */
    <T extends Element> T canonical(T node);

    /**
     * The generic class a static field belongs to, or null when it is not one.
     * <p>
     * The alternative was reading the owner out of the global's name, which a class name containing
     * an underscore answers wrongly and without saying so.
     */
    @Nullable ImClass genericStaticOwnerOf(ImVar global);

    /**
     * For a static inner class which captured the type parameters of its generic owner, the owner's type as the inner
     * class sees it, {@code Outer<T>} with the inner class's own copy of {@code T}; null for any other class. An inner
     * class's instantiation is its owner's this way, and a static of the owner or of a superclass of it belongs to
     * that.
     */
    default @Nullable ImClassType capturedOwnerType(ImClass staticInnerClass) {
        return null;
    }

    /**
     * For a program which did not come from a translation that recorded anything - a hand-built
     * program in a test, say. Every node is its own original, which is what a program with no
     * specialisation in it means.
     */
    SpecialisationLookup NONE = new SpecialisationLookup() {
        @Override
        public <T extends Element> T canonical(T node) {
            return node;
        }

        @Override
        public @Nullable ImClass genericStaticOwnerOf(ImVar global) {
            return null;
        }
    };
}
