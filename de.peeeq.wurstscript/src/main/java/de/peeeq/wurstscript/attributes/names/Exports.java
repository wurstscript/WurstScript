package de.peeeq.wurstscript.attributes.names;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Set;

public class Exports {

    /**
     * calculates all the namelinks exported by package p
     */
    public static ImmutableMultimap<String, DefLink> exportedNameLinks(WPackage p) {
        Builder<String, DefLink> result = ImmutableMultimap.builder();
        addExportedNameLinks(result, p, Sets.newLinkedHashSet());
        return result.build();
    }


    /**
     * recursively adds all exported namelinks from package p to the result map
     */
    private static void addExportedNameLinks(Builder<String, DefLink> result, WPackage p, Set<WPackage> alreadyImported) {
        if (alreadyImported.contains(p)) {
            return;
        }
        alreadyImported.add(p);

        // add elements:
        NameLinks.addHidingPrivateAndProtected(result, p.getElements().attrNameLinks());

        // import public imports
        for (WImport imp2 : p.getImports()) {
            if (imp2.getIsPublic()) {
                @Nullable
                WPackage imported = imp2.attrImportedPackage();
                if (imported != null) {
                    addExportedNameLinks(result, imported, alreadyImported);
                }
            }
        }

    }

    public static ImmutableMultimap<String, TypeLink> exportedTypeNameLinks(WPackage p) {
        Builder<String, TypeLink> result = ImmutableMultimap.builder();
        addExportedTypeNameLinks(result, p, Sets.newLinkedHashSet());
        return result.build();
    }


    private static void addExportedTypeNameLinks(Builder<String, TypeLink> result, WPackage p, Set<WPackage> alreadyImported) {
        if (alreadyImported.contains(p)) {
            return;
        }
        alreadyImported.add(p);

        // add elements:
        NameLinks.addTypesHidingPrivateAndProtected(result, p.getElements().attrTypeNameLinks());
        // import public imports
        for (WImport imp2 : p.getImports()) {
            if (imp2.getIsPublic()) {
                WPackage imported = imp2.attrImportedPackage();
                if (imported != null) {
                    addExportedTypeNameLinks(result, imported, alreadyImported);
                }
            }
        }

    }
}
