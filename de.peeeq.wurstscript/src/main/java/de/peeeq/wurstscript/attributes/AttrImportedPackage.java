package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AttrImportedPackage {

    public static @Nullable WPackage getImportedPackage(WImport i) {
        String packageName = i.getPackagename();
        try {
            WurstModel root = i.getModel();
            ImmutableCollection<WPackage> packages = root.attrPackages().get(packageName);
            if (packages.isEmpty()) {
                if (!packageName.equals("NoWurst")) {
                    i.addError("Could not find imported package " + packageName);
                }
                return null;
            }
            if (i.getDependencyId() instanceof Identifier) {
                String depName = ((Identifier) i.getDependencyId()).getName();
                Optional<WPackage> pa = packages.stream()
                        .filter(p -> libName(p).equals(depName))
                        .findFirst();
                if (pa.isPresent()) {
                    return pa.get();
                } else {
                    i.addError("The package " + packageName + " is not available in library " + depName + ". " +
                            "Did you mean: " + packages.stream().map(AttrImportedPackage::libName).collect(Collectors.joining(" or ")) + "?");
                    return null;
                }
            }

            Library currentLibrary = i.attrCompilationUnit().attrLibrary();
            // first search in current library:
            for (WPackage p : packages) {
                if (currentLibrary == p.attrCompilationUnit().attrLibrary()) {
                    return p;
                }
            }
            // then search in dependencies:
            Set<String> deps = currentLibrary.getDependencies();
            List<WPackage> depPackages = packages.stream()
                    .filter(p -> deps.contains(p.attrCompilationUnit().attrLibraryName()))
                    .collect(Collectors.toList());
            if (depPackages.isEmpty()) {
                for (WPackage p : packages) {
                    i.addWarning("The package " + packageName + " from library " + libName(p) + " is not a direct dependency.");
                }
                return Utils.getFirst(packages);
            } else if (depPackages.size() > 1) {
                i.addWarning("The import of package " + packageName + " is ambiguous. " +
                        "A package with this name is provided by the following libraries: " +
                        depPackages.stream().map(AttrImportedPackage::libName).collect(Collectors.joining(", ")));
            }
            return Utils.getFirst(depPackages);

        } catch (Error e) {
            i.addError("Could not find imported package " + packageName + "\n" + e.getMessage());
            return null;
        }
    }

    private static String libName(WPackage p) {
        return p.attrCompilationUnit().attrLibraryName();
    }

    public static WurstModel getModel(Element elem) {
        Element e = elem.attrCompilationUnit();
        while (e != null) {
            if (e instanceof WurstModel) {
                return (WurstModel) e;
            }
            e = e.getParent();
        }
        throw new Error("trying to get model for element " + Utils.printElement(elem) + ", which is not attached to a model");
    }

    public static ImmutableMultimap<String, WPackage> getPackages(WurstModel wurstModel) {
        Multimap<String, WPackage> result = LinkedHashMultimap.create();
        for (CompilationUnit cu : wurstModel.attrCompilationUnits()) {
            for (WPackage p : cu.getPackages()) {
                Collection<WPackage> sameNamePackages = result.get(p.getName());
                for (WPackage old : sameNamePackages) {
                    if (!p.getName().equals("Wurst")
                            && definedInSameLibrary(p, old)) {
                        // TODO should this really error?
                        p.addError("Package " + p.getName() + " is already defined in " + Utils.printPos(old.getSource()));
                        old.addError("Package " + p.getName() + " is already defined in " + Utils.printPos(p.getSource()));
                    }
                }
                result.put(p.getName(), p);
            }
        }
        return ImmutableMultimap.copyOf(result);
    }

    private static boolean definedInSameLibrary(WPackage p1, WPackage p2) {
        String lib1 = p1.attrCompilationUnit().attrLibraryName();
        String lib2 = p2.attrCompilationUnit().attrLibraryName();
        return lib1.equals(lib2);
    }


}
