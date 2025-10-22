package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

public final class AttrModuleInstanciations {

    private AttrModuleInstanciations() {}

    public static @Nullable ModuleDef getModuleOrigin(ModuleInstanciation mi) {
        // A ModuleInstanciation's parent is always ModuleInstanciations (the list),
        // whose parent is either a ClassDef or a ModuleDef.
        // We must resolve the module name through that owner's scope.

        final String name = mi.getName();

        Element parent = mi.getParent(); // This is ModuleInstanciations (plural)
        if (parent == null) {
            // Detached node during incremental compilation - this is transient.
            // Don't emit errors; return null and let the next full pass resolve it.
            return null;
        }

        // Get the actual owner (ClassDef or ModuleDef)
        Element owner = parent.getParent();
        if (owner == null) {
            // Still detached at the owner level
            return null;
        }

        // Resolve through the owner's scope
        TypeDef def = owner.lookupType(name, /*showErrors*/ false);
        if (def instanceof ModuleDef) {
            return (ModuleDef) def;
        }

        // Only emit error if we're fully attached (not in a transient state)
        if (mi.getModel() != null) {
            mi.addError("Could not find module origin for " + Utils.printElement(mi));
        }

        return null;
    }
}
