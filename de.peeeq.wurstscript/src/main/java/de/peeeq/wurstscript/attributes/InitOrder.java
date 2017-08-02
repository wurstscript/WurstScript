package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InitOrder {

    public static ImmutableList<WPackage> initDependencies(WPackage p) {
        Set<WPackage> packages = Sets.newLinkedHashSet();

        // add all imported packages, which do not import this package again
        packages.addAll(p.attrImportedPackagesTransitive());

        // add config package if it exists:
        WPackage configPackage = p.getModel().attrConfigOverridePackages().get(p);
        if (configPackage != null) {
            packages.add(configPackage);
        }

        return ImmutableList.copyOf(packages);
    }

    private static List<String> toStringArray(List<WPackage> importChain) {
        return importChain.stream()
                .map(WPackage::getName)
                .collect(Collectors.toList());
    }

    public static ImmutableList<WPackage> initDependenciesTransitive(WPackage p) {
        List<WPackage> result = Lists.newArrayList();
        for (WPackage dep : p.attrInitDependencies()) {
            addInitDependenciesTransitive(dep, result);
        }
        return ImmutableList.copyOf(result);
    }

    private static void addInitDependenciesTransitive(WPackage p, List<WPackage> result) {
        if (result.contains(p)) {
            return;
        }
        result.add(p);
        for (WPackage dep : p.attrInitDependencies()) {
            addInitDependenciesTransitive(dep, result);
        }
    }

    public static ImmutableCollection<WPackage> importedPackagesTrans(WPackage p) {
        Collection<WPackage> result = Sets.newLinkedHashSet();
        List<WPackage> callStack = Lists.newArrayList();
        collectImportedPackages(callStack, p, result);
        return ImmutableList.copyOf(result);
    }

    private static void collectImportedPackages(List<WPackage> callStack, WPackage p, Collection<WPackage> result) {
        callStack.add(p);
        for (WImport i : p.getImports()) {
            WPackage imported = i.attrImportedPackage();

            if (imported == null || i.getIsInitLater()) {
                continue;
            }
            if (imported == p) {
                // import self is handled elsewhere
                continue;
            }

            if (imported == callStack.get(0)) {
                String packagesMsg = Utils.join(toStringArray(callStack), " -> ");

                String msg = "Cyclic init dependency between packages: " + packagesMsg + " -> " + imported.getName() +
                        "\nChange some imports to 'initlater' imports to avoid this problem.";
                for (WImport imp : imported.getImports()) {
                    if (callStack.size() > 1 && imp.attrImportedPackage() == callStack.get(1)) {
                        imp.addError(msg);
                        return;
                    }
                }
                imported.addError(msg);
                return;
            }

            if (result.contains(imported)) {
                continue;
            }
            result.add(imported);
            collectImportedPackages(callStack, imported, result);
        }
        callStack.remove(callStack.size() - 1);
    }

}
