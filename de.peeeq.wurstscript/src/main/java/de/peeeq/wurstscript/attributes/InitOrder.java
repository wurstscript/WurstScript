package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WImports;
import de.peeeq.wurstscript.ast.WPackage;

import java.util.*;

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
        Set<String> reportedErrors = Sets.newHashSet();
        List<WPackage> callStack = Lists.newArrayList();
        // the old cyclic check reports cyclic dependencies as errors
        // it ignores cyclic dependencies caused by config packages
        // it stores reported errors in reportedErrors
        collectImportedPackages(callStack, p, result, reportedErrors, false, false);
        // the new cyclic check reports cyclic dependencies caused by config packages as warnings
        // it does not report warnings for cyclic dependencies stored in reportedErrors
        collectImportedPackages(callStack, p, Sets.newLinkedHashSet(), reportedErrors, true, true);
        // only the result of the old cyclic check is used
        return ImmutableList.copyOf(result);
    }

    private static String getCyclicDependencyString(List<WPackage> callStack, WPackage imported) {
        return getCyclicDependencyString(callStack, imported, false);
    }

    private static String getCyclicDependencyString(List<WPackage> callStack, WPackage imported, boolean considerConfig) {
        // string representation of a cyclic dependency
        // used for the error message and for storing already reported cyclic dependencies
        StringBuilder msg = new StringBuilder();
        Map<WPackage, WPackage> configuredPackage = new HashMap<>();
        if (considerConfig) {
            for (WPackage configured : imported.getModel().attrConfigOverridePackages().keySet()) {
                configuredPackage.put(imported.getModel().attrConfigOverridePackages().get(configured), configured);
            }
        }
        for (WPackage p : callStack) {
            String packageString = p.getName();
            if (considerConfig && configuredPackage.containsKey(p)) {
                packageString = configuredPackage.get(p).getName() + "/" + packageString;
            }
            msg.append(packageString).append(" -> ");
        }
        String importedString = imported.getName();
        if (considerConfig && configuredPackage.containsKey(imported)) {
            importedString = configuredPackage.get(imported).getName() + "/" + importedString;
        }
        return msg + importedString;
    }

    private static String getErrorMsg(List<WPackage> callStack, WPackage imported) {
        String cyclicDependency = getCyclicDependencyString(callStack, imported, false);
        return "Cyclic init dependency between packages: " + cyclicDependency +
            "\nChange some imports to 'initlater' imports to avoid this problem.";
    }

    private static String getWarningMsg(List<WPackage> callStack, WPackage imported) {
        String cyclicDependency = getCyclicDependencyString(callStack, imported, true);
        return "Cyclic init dependency between packages: " + cyclicDependency +
            "\nChange some imports to 'initlater' imports to avoid this problem." +
            "\nThis will be an error in future Wurst versions.";
    }

    private static void reportCyclicDependency(List<WPackage> callStack, WPackage wPackage, Set<String> reportedErrors, boolean useWarnings) {
        String cyclicString = getCyclicDependencyString(callStack, wPackage);
        String errorMsg = getErrorMsg(callStack, wPackage);
        String warningMsg = getWarningMsg(callStack, wPackage);
        for (WImport imp : wPackage.getImports()) {
            if (callStack.size() > 1 && imp.attrImportedPackage() == callStack.get(1)) {
                if (!reportedErrors.contains(cyclicString)) {
                    reportedErrors.add(cyclicString);
                    if (useWarnings) {
                        wPackage.addWarning(warningMsg);
                    } else {
                        wPackage.addError(errorMsg);
                    }
                }
                return;
            }
        }
        if (!reportedErrors.contains(cyclicString)) {
            reportedErrors.add(cyclicString);
            if (useWarnings) {
                wPackage.addWarning(warningMsg);
            } else {
                wPackage.addError(errorMsg);
            }
        }
    }

    private static void addCollectImportedPackage(List<WPackage> callStack, WPackage p, Collection<WPackage> result, WImports imports, Set<String> reportedErrors, boolean considerConfig, boolean useWarnings) {
        for (WImport i : imports) {
            WPackage imported = i.attrImportedPackage();

            if (imported == null || i.getIsInitLater()) {
                continue;
            }
            if (imported == p) {
                // import self is handled elsewhere
                continue;
            }

            if (imported == callStack.get(0)) {
                reportCyclicDependency(callStack, imported, reportedErrors, useWarnings);
                return;
            }

            if (!result.contains(imported)) {
                result.add(imported);
                collectImportedPackages(callStack, imported, result, reportedErrors, considerConfig, useWarnings);
            }
            // add imports of configured package to config package
            // that way cyclic dependencies are checked for the config package and errors will be reported for the config package
            if (considerConfig) {
                WPackage configPackage = p.getModel().attrConfigOverridePackages().get(imported);
                if (configPackage != null && configPackage != p) {
                    if (configPackage == callStack.get(0)) {
                        reportCyclicDependency(callStack, configPackage, reportedErrors, useWarnings);
                        return;
                    }
                    if (!result.contains(configPackage)) {
                        result.add(configPackage);
                        collectImportedPackages(callStack, configPackage, result, reportedErrors, considerConfig, useWarnings);
                    }
                }
            }
        }
    }

    private static void collectImportedPackages(List<WPackage> callStack, WPackage p, Collection<WPackage> result, Set<String> reportedErrors, boolean considerConfig, boolean useWarnings) {
        callStack.add(p);
        addCollectImportedPackage(callStack, p, result, p.getImports(), reportedErrors, considerConfig, useWarnings);
        /*
        Imports of config packages are added to the imports of the configured package.
        Since config packages are initialized directly before the configured package,
        all imports are merged on the configured package to ensure it is initialized at the correct time.
        This enables importing the configured package in the config package,
        even though the configured package will be initialized after the config package.
         */
        if (considerConfig) {
            for (Map.Entry<WPackage, WPackage> e : p.getModel().attrConfigOverridePackages().entrySet()) {
                if (e.getValue().equals(p)) {
                    addCollectImportedPackage(callStack, e.getKey(), result, e.getKey().getImports(), reportedErrors, considerConfig, useWarnings);
                }
            }
        }
        callStack.remove(callStack.size() - 1);
    }

}
