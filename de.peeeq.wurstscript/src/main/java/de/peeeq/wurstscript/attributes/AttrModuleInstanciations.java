package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

public final class AttrModuleInstanciations {

    private AttrModuleInstanciations() {}

    public static @Nullable ModuleDef getModuleOrigin(ModuleInstanciation mi) {
        // NOTE: For ModuleInstanciation the "name" used for resolution has historically been getName().
        // Keep this to preserve prior behavior.
        final String name = mi.getName();

        // 1) Normal path: resolve relative to the lexical parent (old behavior)
        final Element parent = mi.getParent();
        if (parent != null) {
            TypeDef def = parent.lookupType(name, /*showErrors*/ false);
            if (def instanceof ModuleDef) {
                return (ModuleDef) def;
            }
            // Attached but not found -> keep the old error
            mi.addError("Could not find module origin for " + Utils.printElement(mi));
            return null;
        }

        // 2) Detached during incremental build: try the nearest attached scope
        final WScope scope = mi.attrNearestScope();
        if (scope != null) {
            TypeDef def = scope.lookupType(name, /*showErrors*/ false);
            if (def instanceof ModuleDef) {
                return (ModuleDef) def;
            }
        }

        // 3) Still not found and we're detached: this can be a transient state,
        // so don't emit an error here. Return null and let callers handle gracefully.
        return null;
    }
}
