package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.datastructures.UnionFind;
import de.peeeq.wurstscript.ast.AstElementWithFuncName;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class LuaDispatchPreparation {
    private static final Set<String> LUA_RESERVED_NAMES = Set.of(
        "print", "tostring", "error",
        "main", "config",
        "and", "break", "do", "else", "elseif", "end", "false", "for", "function",
        "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while"
    );

    private LuaDispatchPreparation() {
    }

    public static void prepare(ImProg prog) {
        List<ImMethod> allMethods = collectAllMethods(prog);
        assignDispatchGroupKeys(allMethods);
        normalizeMethodNames(prog, allMethods);
        assignDispatchAliases(prog, allMethods);
    }

    private static List<ImMethod> collectAllMethods(ImProg prog) {
        List<ImMethod> methods = new ArrayList<>();
        for (ImClass c : prog.getClasses()) {
            methods.addAll(c.getMethods());
        }
        methods.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
        return methods;
    }

    private static void assignDispatchGroupKeys(List<ImMethod> allMethods) {
        Set<ImMethod> knownMethods = new HashSet<>(allMethods);
        UnionFind<ImMethod> unions = new UnionFind<>();
        for (ImMethod method : allMethods) {
            unions.find(method);
            for (ImMethod subMethod : method.getSubMethods()) {
                if (knownMethods.contains(subMethod)) {
                    unions.union(method, subMethod);
                }
            }
        }

        Map<ImMethod, List<ImMethod>> grouped = new LinkedHashMap<>();
        for (ImMethod method : allMethods) {
            ImMethod root = unions.find(method);
            grouped.computeIfAbsent(root, ignored -> new ArrayList<>()).add(method);
        }

        for (List<ImMethod> group : grouped.values()) {
            Map<String, List<ImMethod>> partitions = new LinkedHashMap<>();
            group.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
            for (ImMethod method : group) {
                partitions.computeIfAbsent(dispatchSignatureKey(method), ignored -> new ArrayList<>()).add(method);
            }
            for (List<ImMethod> partition : partitions.values()) {
                partition.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
                if (partition.isEmpty()) {
                    continue;
                }
                String key = methodSortKey(partition.get(0)) + "|" + dispatchSignatureKey(partition.get(0));
                for (ImMethod method : partition) {
                    method.setLuaDispatchGroupKey(key);
                }
            }
        }
    }

    private static void normalizeMethodNames(ImProg prog, List<ImMethod> allMethods) {
        Set<String> usedNames = new HashSet<>(LUA_RESERVED_NAMES);
        collectPredefinedNames(prog, usedNames);

        Map<String, List<ImMethod>> groupedMethods = new TreeMap<>();
        for (ImMethod method : allMethods) {
            groupedMethods.computeIfAbsent(method.getLuaDispatchGroupKey(), ignored -> new ArrayList<>()).add(method);
        }
        List<List<ImMethod>> groups = new ArrayList<>(groupedMethods.values());
        groups.sort(Comparator.comparing(g -> g.isEmpty() ? "" : methodSortKey(g.get(0))));
        for (List<ImMethod> group : groups) {
            if (group.isEmpty()) {
                continue;
            }
            group.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
            String name = uniqueName(group.get(0).getName(), usedNames);
            for (ImMethod method : group) {
                method.setName(name);
            }
        }
    }

    private static void assignDispatchAliases(ImProg prog, List<ImMethod> allMethods) {
        Map<ImClass, List<ImMethod>> sortedMethodsByClass = new HashMap<>();
        Map<ImClass, Set<ImClass>> closureFamilyAnchorsCache = new HashMap<>();
        Map<ImClass, List<ImClass>> closureFamilyClassesByAnchor = new HashMap<>();

        for (ImMethod method : allMethods) {
            TreeSet<String> aliases = new TreeSet<>();
            addDirectAliases(method, aliases);
            addHierarchyAliases(method, aliases, sortedMethodsByClass);
            addClosureFamilyAliases(prog, method, aliases, sortedMethodsByClass, closureFamilyAnchorsCache, closureFamilyClassesByAnchor);
            method.setLuaMethodDispatchAliases(new ArrayList<>(aliases));
        }
    }

    private static void collectPredefinedNames(ImProg prog, Set<String> usedNames) {
        prog.getFunctions().forEach(function -> {
            if (function.isBj() || function.isExtern() || function.isNative()) {
                usedNames.add(function.getName());
            }
        });
        prog.getGlobals().forEach(global -> {
            if (global.getIsBJ()) {
                usedNames.add(global.getName());
            }
        });
    }

    private static String uniqueName(String name, Set<String> usedNames) {
        int i = 0;
        String result = name;
        while (usedNames.contains(result)) {
            result = name + ++i;
        }
        usedNames.add(result);
        return result;
    }

    private static void addDirectAliases(ImMethod method, Set<String> aliases) {
        if (method == null) {
            return;
        }
        String methodName = method.getName();
        if (!methodName.isEmpty()) {
            aliases.add(methodName);
        }
        ImClass owner = method.attrClass();
        String semanticName = semanticNameFromMethodName(methodName);
        if (owner != null && !semanticName.isEmpty()) {
            aliases.add(owner.getName() + "_" + semanticName);
        }
        String sourceSemanticName = sourceSemanticName(method);
        if (owner != null && isClosureGeneratedClass(owner) && !sourceSemanticName.isEmpty()) {
            aliases.add(sourceSemanticName);
            aliases.add(owner.getName() + "_" + sourceSemanticName);
        }
    }

    private static void addHierarchyAliases(ImMethod method, Set<String> aliases, Map<ImClass, List<ImMethod>> sortedMethodsByClass) {
        ImClass owner = method.attrClass();
        if (owner == null) {
            return;
        }
        Set<String> semanticNames = semanticNames(method);
        if (semanticNames.isEmpty()) {
            return;
        }
        String dispatchKey = dispatchSignatureKey(method);
        collectHierarchyAliases(owner, dispatchKey, semanticNames, aliases, sortedMethodsByClass, new HashSet<>());
    }

    private static void collectHierarchyAliases(ImClass c, String dispatchKey, Set<String> semanticNames, Set<String> aliases,
                                                Map<ImClass, List<ImMethod>> sortedMethodsByClass, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        for (ImMethod candidate : sortedMethodsForClass(c, sortedMethodsByClass)) {
            if (!dispatchKey.equals(dispatchSignatureKey(candidate))) {
                continue;
            }
            if (!sharesSemanticName(candidate, semanticNames)) {
                continue;
            }
            String candidateName = candidate.getName();
            if (!candidateName.isEmpty()) {
                aliases.add(candidateName);
                aliases.add(c.getName() + "_" + candidateName);
            }
        }
        for (ImClassType sc : c.getSuperClasses()) {
            collectHierarchyAliases(sc.getClassDef(), dispatchKey, semanticNames, aliases, sortedMethodsByClass, visited);
        }
    }

    private static void addClosureFamilyAliases(ImProg prog, ImMethod method, Set<String> aliases,
                                                Map<ImClass, List<ImMethod>> sortedMethodsByClass,
                                                Map<ImClass, Set<ImClass>> closureFamilyAnchorsCache,
                                                Map<ImClass, List<ImClass>> closureFamilyClassesByAnchor) {
        ImClass owner = method.attrClass();
        if (owner == null || !isClosureGeneratedClass(owner)) {
            return;
        }
        Set<String> semanticNames = semanticNames(method);
        if (semanticNames.isEmpty()) {
            return;
        }
        String runtimeKey = closureRuntimeDispatchKey(method);
        for (ImClass anchor : closureFamilyAnchors(owner, closureFamilyAnchorsCache)) {
            for (ImClass candidateClass : closureFamilyClassesForAnchor(prog, anchor, closureFamilyClassesByAnchor)) {
                for (ImMethod candidate : sortedMethodsForClass(candidateClass, sortedMethodsByClass)) {
                    if (!runtimeKey.equals(closureRuntimeDispatchKey(candidate))) {
                        continue;
                    }
                    if (!sharesSemanticName(candidate, semanticNames)) {
                        continue;
                    }
                    String candidateName = candidate.getName();
                    if (!candidateName.isEmpty()) {
                        aliases.add(candidateName);
                        aliases.add(candidateClass.getName() + "_" + candidateName);
                    }
                }
            }
        }
    }

    private static Set<String> semanticNames(ImMethod method) {
        Set<String> names = new HashSet<>();
        String semanticName = semanticNameFromMethodName(method.getName());
        if (!semanticName.isEmpty()) {
            names.add(semanticName);
        }
        String sourceSemanticName = sourceSemanticName(method);
        if (!sourceSemanticName.isEmpty()) {
            names.add(sourceSemanticName);
        }
        return names;
    }

    private static boolean sharesSemanticName(ImMethod method, Set<String> semanticNames) {
        if (semanticNames.isEmpty()) {
            return false;
        }
        return semanticNames.contains(semanticNameFromMethodName(method.getName()))
            || semanticNames.contains(sourceSemanticName(method));
    }

    private static List<ImMethod> sortedMethodsForClass(ImClass c, Map<ImClass, List<ImMethod>> sortedMethodsByClass) {
        return sortedMethodsByClass.computeIfAbsent(c, key -> {
            List<ImMethod> methods = new ArrayList<>(key.getMethods());
            methods.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
            return methods;
        });
    }

    private static Set<ImClass> closureFamilyAnchors(ImClass c, Map<ImClass, Set<ImClass>> cache) {
        return cache.computeIfAbsent(c, key -> {
            Set<ImClass> anchors = new TreeSet<>(Comparator.comparing(LuaDispatchPreparation::classSortKey));
            collectClosureFamilyAnchors(key, anchors, new HashSet<>());
            return anchors;
        });
    }

    private static void collectClosureFamilyAnchors(ImClass c, Set<ImClass> anchors, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        if (!isClosureGeneratedClass(c)) {
            anchors.add(c);
        }
        for (ImClassType sc : c.getSuperClasses()) {
            collectClosureFamilyAnchors(sc.getClassDef(), anchors, visited);
        }
    }

    private static List<ImClass> closureFamilyClassesForAnchor(ImProg prog, ImClass anchor, Map<ImClass, List<ImClass>> cache) {
        return cache.computeIfAbsent(anchor, a -> {
            List<ImClass> result = new ArrayList<>();
            for (ImClass candidate : prog.getClasses()) {
                if (sharesClosureFamilyAnchor(candidate, a, new HashSet<>())) {
                    result.add(candidate);
                }
            }
            result.sort(Comparator.comparing(LuaDispatchPreparation::classSortKey));
            return result;
        });
    }

    private static boolean sharesClosureFamilyAnchor(ImClass c, ImClass anchor, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return false;
        }
        if (c == anchor) {
            return true;
        }
        for (ImClassType sc : c.getSuperClasses()) {
            if (sharesClosureFamilyAnchor(sc.getClassDef(), anchor, visited)) {
                return true;
            }
        }
        return false;
    }

    private static String dispatchSignatureKey(ImMethod method) {
        ImFunction implementation = resolveDispatchSignatureImplementation(method, new HashSet<>());
        if (implementation == null) {
            return "<abstract>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(typeKey(implementation.getReturnType())).append("|");
        ImVars params = implementation.getParameters();
        for (int i = 1; i < params.size(); i++) {
            if (i > 1) {
                sb.append(",");
            }
            sb.append(typeKey(params.get(i).getType()));
        }
        return sb.toString();
    }

    private static ImFunction resolveDispatchSignatureImplementation(ImMethod method, Set<ImMethod> visited) {
        if (method == null || !visited.add(method)) {
            return null;
        }
        if (method.getImplementation() != null) {
            return method.getImplementation();
        }
        List<ImMethod> subMethods = new ArrayList<>(method.getSubMethods());
        subMethods.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
        for (ImMethod subMethod : subMethods) {
            ImFunction resolved = resolveDispatchSignatureImplementation(subMethod, visited);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private static String closureRuntimeDispatchKey(ImMethod method) {
        ImFunction implementation = resolveDispatchSignatureImplementation(method, new HashSet<>());
        if (implementation == null) {
            return "<abstract>";
        }
        return "" + Math.max(0, implementation.getParameters().size() - 1);
    }

    private static String typeKey(ImType type) {
        return type == null ? "<null>" : type.toString();
    }

    private static String methodSortKey(ImMethod method) {
        if (method == null) {
            return "";
        }
        String owner = classSortKey(method.attrClass());
        String impl = method.getImplementation() != null ? method.getImplementation().getName() : "";
        return owner + "|" + method.getName() + "|" + impl;
    }

    private static String classSortKey(ImClass c) {
        return c == null ? "" : c.getName();
    }

    private static String semanticNameFromMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return "";
        }
        int lastUnderscore = methodName.lastIndexOf('_');
        if (lastUnderscore >= 0 && lastUnderscore + 1 < methodName.length()) {
            return methodName.substring(lastUnderscore + 1);
        }
        return methodName;
    }

    private static boolean isClosureGeneratedClass(ImClass c) {
        return c != null && c.attrTrace() instanceof ExprClosure;
    }

    private static String sourceSemanticName(ImMethod method) {
        if (method == null) {
            return "";
        }
        de.peeeq.wurstscript.ast.Element trace = method.attrTrace();
        if (trace instanceof FuncDef funcDef) {
            return funcDef.getName();
        }
        if (trace instanceof AstElementWithFuncName withFuncName) {
            return withFuncName.getFuncNameId().getName();
        }
        if (method.getImplementation() != null) {
            String implementationName = method.getImplementation().getName();
            int firstUnderscore = implementationName.indexOf('_');
            if (firstUnderscore > 0) {
                return implementationName.substring(0, firstUnderscore);
            }
            return implementationName;
        }
        return "";
    }
}
