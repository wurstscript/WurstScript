package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.datastructures.UnionFind;
import de.peeeq.wurstscript.ast.AstElementWithFuncName;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.translation.lua.translation.LuaIdentifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.jdt.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class LuaDispatchPreparation {
    private static final Set<String> LUA_RESERVED_NAMES =
        de.peeeq.wurstscript.translation.lua.translation.LuaReservedNames.all();

    private LuaDispatchPreparation() {
    }

    public static void prepare(ImProg prog, ImTranslator tr) {
        List<ImMethod> allMethods = collectAllMethods(prog);
        assignDispatchGroupKeys(allMethods);
        normalizeMethodNames(prog, allMethods, tr);
        assignDispatchAliases(prog, allMethods, tr);
    }

    private static List<ImMethod> collectAllMethods(ImProg prog) {
        List<ImMethod> methods = new ArrayList<>();
        List<ImClass> classes = prog.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            ImClass c = classes.get(i);
            methods.addAll(c.getMethods());
        }
        methods.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
        return methods;
    }

    private static void assignDispatchGroupKeys(List<ImMethod> allMethods) {
        Set<ImMethod> knownMethods = new HashSet<>(allMethods);
        UnionFind<ImMethod> unions = new UnionFind<>();
        for (int i = 0; i < allMethods.size(); i++) {
            ImMethod method = allMethods.get(i);
            unions.find(method);
            List<ImMethod> subMethods = method.getSubMethods();
            for (int j = 0; j < subMethods.size(); j++) {
                ImMethod subMethod = subMethods.get(j);
                if (knownMethods.contains(subMethod)) {
                    unions.union(method, subMethod);
                }
            }
        }

        Map<ImMethod, List<ImMethod>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < allMethods.size(); i++) {
            ImMethod method = allMethods.get(i);
            ImMethod root = unions.find(method);
            grouped.computeIfAbsent(root, ignored -> new ArrayList<>()).add(method);
        }

        List<List<ImMethod>> groups = new ArrayList<>(grouped.values());
        for (int i = 0; i < groups.size(); i++) {
            List<ImMethod> group = groups.get(i);
            Map<String, List<ImMethod>> partitions = new LinkedHashMap<>();
            group.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
            for (int j = 0; j < group.size(); j++) {
                ImMethod method = group.get(j);
                partitions.computeIfAbsent(dispatchSignatureKey(method), ignored -> new ArrayList<>()).add(method);
            }
            List<List<ImMethod>> partitionGroups = new ArrayList<>(partitions.values());
            for (int j = 0; j < partitionGroups.size(); j++) {
                List<ImMethod> partition = partitionGroups.get(j);
                partition.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
                if (partition.isEmpty()) {
                    continue;
                }
                String key = methodSortKey(partition.get(0)) + "|" + dispatchSignatureKey(partition.get(0));
                for (int k = 0; k < partition.size(); k++) {
                    ImMethod method = partition.get(k);
                    method.setLuaDispatchGroupKey(key);
                }
            }
        }
    }

    private static void normalizeMethodNames(ImProg prog, List<ImMethod> allMethods, ImTranslator tr) {
        Set<String> usedNames = new HashSet<>(LUA_RESERVED_NAMES);
        collectPredefinedNames(prog, usedNames);

        Map<String, List<ImMethod>> groupedMethods = new TreeMap<>();
        for (int i = 0; i < allMethods.size(); i++) {
            ImMethod method = allMethods.get(i);
            groupedMethods.computeIfAbsent(method.getLuaDispatchGroupKey(), ignored -> new ArrayList<>()).add(method);
        }
        List<List<ImMethod>> groups = new ArrayList<>(groupedMethods.values());
        groups.sort(Comparator.comparing(g -> g.isEmpty() ? "" : methodSortKey(g.get(0))));
        for (int i = 0; i < groups.size(); i++) {
            List<ImMethod> group = groups.get(i);
            if (group.isEmpty()) {
                continue;
            }
            group.sort(Comparator.comparing(LuaDispatchPreparation::methodSortKey));
            // The name is about to become a Lua table key. Sanitising before uniquing means two
            // names that only differed in characters Lua has no place for still get one slot each.
            String name = uniqueName(LuaIdentifiers.toIdentifier(group.get(0).getName()), usedNames);
            // The group is named after one member, whose name is that member's own class and then
            // the method, so stripping the class here is the one place the boundary is known rather
            // than guessed at. Every member shares the segment, including members of other classes
            // whose own name appears nowhere in it - which is why no method can work this out for
            // itself afterwards.
            String segment = segmentOf(name, group.get(0));
            for (int j = 0; j < group.size(); j++) {
                ImMethod method = group.get(j);
                method.setName(name);
                tr.recordDispatchSegment(method, segment);
            }
        }
    }

    private static void assignDispatchAliases(ImProg prog, List<ImMethod> allMethods, ImTranslator tr) {
        Map<ImClass, List<ImMethod>> sortedMethodsByClass = new HashMap<>();
        Map<ImClass, Set<ImClass>> closureFamilyAnchorsCache = new HashMap<>();
        Map<ImClass, List<ImClass>> closureFamilyClassesByAnchor = new HashMap<>();

        Set<String> ambiguousDirectAliases = ambiguousDirectAliases(allMethods, tr);

        for (int i = 0; i < allMethods.size(); i++) {
            ImMethod method = allMethods.get(i);
            TreeSet<String> aliases = new TreeSet<>();
            addDirectAliases(method, aliases, ambiguousDirectAliases, tr);
            addHierarchyAliases(method, aliases, sortedMethodsByClass, tr);
            addClosureFamilyAliases(prog, method, aliases, sortedMethodsByClass, closureFamilyAnchorsCache, closureFamilyClassesByAnchor, tr);
            method.setLuaMethodDispatchAliases(new ArrayList<>(aliases));
        }
    }

    private static void collectPredefinedNames(ImProg prog, Set<String> usedNames) {
        List<ImFunction> functions = prog.getFunctions();
        for (int i = 0; i < functions.size(); i++) {
            ImFunction function = functions.get(i);
            if (function.isBj() || function.isExtern() || function.isNative()) {
                usedNames.add(function.getName());
            }
        }
        ImVar[] globals = prog.getGlobals().toArray(new ImVar[0]);
        for (int i = 0; i < globals.length; i++) {
            ImVar global = globals[i];
            if (global.getIsBJ()) {
                usedNames.add(global.getName());
            }
        }
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

    /**
     * The composed names which more than one method of the same class produces.
     * <p>
     * The name is the owner's plus the segment after the last underscore of the method's. For a
     * specialised class that segment is the type argument, so every method of
     * {@code FastHashMap<int, int>} composes the same one, which then names no method in particular.
     * Whichever is bound first would claim it, so it is left unbound: a name meaning "one of these,
     * arbitrarily" is worse than a name meaning nothing. {@code LuaTranslator} skips composing the
     * matching slot for the same reason.
     */
    private static Set<String> ambiguousDirectAliases(List<ImMethod> allMethods, ImTranslator tr) {
        Map<String, String> claimedBy = new LinkedHashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (int i = 0; i < allMethods.size(); i++) {
            ImMethod method = allMethods.get(i);
            String composed = directAliasFor(method, tr);
            if (composed == null) {
                continue;
            }
            // A method and its overrides are one dispatchable thing and must share a slot - that is
            // what dispatch is - so they are not a collision, and they all declare the same name in
            // the source. The siblings of one specialisation declare different ones and merely end up
            // composing the same segment, because for them that segment is the type argument.
            //
            // The dispatch group key would separate overloads too, but it embeds the signature, and a
            // generic override chain's signatures differ by each class's type variable - so overrides
            // would read as unrelated and lose the slot they must share. Backlog item 15 records what
            // that leaves: overloads inside a specialised class keep one dead key.
            String identity = declaredName(method);
            String previous = claimedBy.put(composed, identity);
            if (previous != null && !previous.equals(identity)) {
                ambiguous.add(composed);
            }
        }
        return ambiguous;
    }

    private static @Nullable String directAliasFor(ImMethod method, ImTranslator tr) {
        if (method == null) {
            return null;
        }
        ImClass owner = method.attrClass();
        String semanticName = tr.dispatchSegmentOf(method);
        if (owner == null || semanticName.isEmpty()) {
            return null;
        }
        return owner.getName() + "_" + semanticName;
    }

    private static void addDirectAliases(ImMethod method, Set<String> aliases,
                                         Set<String> ambiguousDirectAliases, ImTranslator tr) {
        if (method == null) {
            return;
        }
        String methodName = method.getName();
        if (!methodName.isEmpty()) {
            aliases.add(methodName);
        }
        ImClass owner = method.attrClass();
        String composed = directAliasFor(method, tr);
        if (composed != null && !ambiguousDirectAliases.contains(composed)) {
            aliases.add(composed);
        }
        String sourceSemanticName = sourceSemanticName(method);
        if (owner != null && isClosureGeneratedClass(owner) && !sourceSemanticName.isEmpty()) {
            aliases.add(sourceSemanticName);
            aliases.add(owner.getName() + "_" + sourceSemanticName);
        }
    }

    private static void addHierarchyAliases(ImMethod method, Set<String> aliases, Map<ImClass, List<ImMethod>> sortedMethodsByClass, ImTranslator tr) {
        ImClass owner = method.attrClass();
        if (owner == null) {
            return;
        }
        Set<String> semanticNames = semanticNames(method, tr);
        if (semanticNames.isEmpty()) {
            return;
        }
        String dispatchKey = dispatchParameterSignatureKey(method);
        collectHierarchyAliases(owner, method, dispatchKey, semanticNames, aliases, sortedMethodsByClass, new HashSet<>(), tr);
    }

    private static void collectHierarchyAliases(ImClass c, ImMethod method, String dispatchKey, Set<String> semanticNames, Set<String> aliases,
                                                Map<ImClass, List<ImMethod>> sortedMethodsByClass, Set<ImClass> visited, ImTranslator tr) {
        if (c == null || !visited.add(c)) {
            return;
        }
        List<ImMethod> candidates = sortedMethodsForClass(c, sortedMethodsByClass);
        for (int i = 0; i < candidates.size(); i++) {
            ImMethod candidate = candidates.get(i);
            if (!dispatchKey.equals(dispatchParameterSignatureKey(candidate))) {
                continue;
            }
            if (!sharesSemanticName(method, candidate, semanticNames, tr)) {
                continue;
            }
            if (!compatibleReturnTypes(method, candidate, tr)) {
                continue;
            }
            String candidateName = candidate.getName();
            if (!candidateName.isEmpty()) {
                aliases.add(candidateName);
                aliases.add(c.getName() + "_" + candidateName);
            }
        }
        List<ImClassType> superClasses = c.getSuperClasses();
        for (int i = 0; i < superClasses.size(); i++) {
            ImClassType sc = superClasses.get(i);
            collectHierarchyAliases(sc.getClassDef(), method, dispatchKey, semanticNames, aliases, sortedMethodsByClass, visited, tr);
        }
    }

    private static void addClosureFamilyAliases(ImProg prog, ImMethod method, Set<String> aliases,
                                                Map<ImClass, List<ImMethod>> sortedMethodsByClass,
                                                Map<ImClass, Set<ImClass>> closureFamilyAnchorsCache,
                                                Map<ImClass, List<ImClass>> closureFamilyClassesByAnchor, ImTranslator tr) {
        ImClass owner = method.attrClass();
        if (owner == null || !isClosureGeneratedClass(owner)) {
            return;
        }
        Set<String> semanticNames = semanticNames(method, tr);
        if (semanticNames.isEmpty()) {
            return;
        }
        String runtimeKey = closureRuntimeDispatchKey(method);
        List<ImClass> anchors = new ArrayList<>(closureFamilyAnchors(owner, closureFamilyAnchorsCache));
        for (int i = 0; i < anchors.size(); i++) {
            ImClass anchor = anchors.get(i);
            List<ImClass> candidateClasses = closureFamilyClassesForAnchor(prog, anchor, closureFamilyClassesByAnchor);
            for (int j = 0; j < candidateClasses.size(); j++) {
                ImClass candidateClass = candidateClasses.get(j);
                List<ImMethod> candidates = sortedMethodsForClass(candidateClass, sortedMethodsByClass);
                for (int k = 0; k < candidates.size(); k++) {
                    ImMethod candidate = candidates.get(k);
                    if (!runtimeKey.equals(closureRuntimeDispatchKey(candidate))) {
                        continue;
                    }
                    if (!sharesSemanticName(method, candidate, semanticNames, tr)) {
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

    private static Set<String> semanticNames(ImMethod method, ImTranslator tr) {
        Set<String> names = new HashSet<>();
        String semanticName = tr.dispatchSegmentOf(method);
        if (!semanticName.isEmpty()) {
            names.add(semanticName);
        }
        String sourceSemanticName = sourceSemanticName(method);
        if (!sourceSemanticName.isEmpty()) {
            names.add(sourceSemanticName);
        }
        return names;
    }

    /**
     * Whether {@code candidate} is the same method as {@code method} under a different name, which
     * is what makes it worth claiming its slot.
     *
     * <p>When both were declared in source, their declared names settle it. The name-derived
     * fallback below reads the segment after the last underscore, which for a specialised method
     * is a fragment of the type argument: two unrelated methods of one specialisation both end in
     * {@code integer} and would otherwise be taken for one another.
     */
    private static boolean sharesSemanticName(ImMethod method, ImMethod candidate, Set<String> semanticNames, ImTranslator tr) {
        String declared = declaredName(method);
        String candidateDeclared = declaredName(candidate);
        if (!declared.isEmpty() && !candidateDeclared.isEmpty()) {
            return declared.equals(candidateDeclared);
        }
        return sharesSemanticName(candidate, semanticNames, tr);
    }

    private static boolean sharesSemanticName(ImMethod method, Set<String> semanticNames, ImTranslator tr) {
        if (semanticNames.isEmpty()) {
            return false;
        }
        return semanticNames.contains(tr.dispatchSegmentOf(method))
            || semanticNames.contains(sourceSemanticName(method));
    }

    /** The name the method was written with, or empty when there is no declaration to ask. */
    /** The name a method carries in the source, which a method and its overrides all share. */
    public static String declaredName(ImMethod method) {
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
        return "";
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
        List<ImClassType> superClasses = c.getSuperClasses();
        for (int i = 0; i < superClasses.size(); i++) {
            ImClassType sc = superClasses.get(i);
            collectClosureFamilyAnchors(sc.getClassDef(), anchors, visited);
        }
    }

    private static List<ImClass> closureFamilyClassesForAnchor(ImProg prog, ImClass anchor, Map<ImClass, List<ImClass>> cache) {
        return cache.computeIfAbsent(anchor, a -> {
            List<ImClass> result = new ArrayList<>();
            List<ImClass> classes = prog.getClasses();
            for (int i = 0; i < classes.size(); i++) {
                ImClass candidate = classes.get(i);
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
        List<ImClassType> superClasses = c.getSuperClasses();
        for (int i = 0; i < superClasses.size(); i++) {
            ImClassType sc = superClasses.get(i);
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

    /**
     * The runtime dispatch slot is selected by the receiver and parameters, not by the return
     * type. This matters for interface methods returning {@code thistype}: the interface method
     * resolves to the interface type while a module-provided implementation resolves to the
     * concrete class type, but both still need the interface alias on the concrete class table.
     */
    private static String dispatchParameterSignatureKey(ImMethod method) {
        ImFunction implementation = resolveDispatchSignatureImplementation(method, new HashSet<>());
        if (implementation == null) {
            return "<abstract>";
        }
        StringBuilder sb = new StringBuilder();
        ImVars params = implementation.getParameters();
        for (int i = 1; i < params.size(); i++) {
            if (i > 1) {
                sb.append(",");
            }
            sb.append(typeKey(params.get(i).getType()));
        }
        return sb.toString();
    }

    /**
     * Alias a covariant implementation return, such as a concrete class returned for an
     * interface method returning {@code thistype}, but keep unrelated same-name methods apart.
     */
    public static boolean compatibleReturnTypes(ImMethod left, ImMethod right) {
        return compatibleReturnTypes(left, right, null);
    }

    public static boolean compatibleReturnTypes(ImMethod left, ImMethod right, ImTranslator tr) {
        ImType leftReturnType = dispatchReturnType(left, tr);
        ImType rightReturnType = dispatchReturnType(right, tr);
        if (leftReturnType == null || rightReturnType == null) {
            return false;
        }
        if (leftReturnType.equalsType(rightReturnType)) {
            return true;
        }
        // A generic method's return type can still be represented by the owning type variable
        // when comparing it with an erased/specialized override. The generic dispatch machinery
        // already guarantees that relationship; the Lua alias check must not discard that slot.
        if ((leftReturnType instanceof ImTypeVarRef || rightReturnType instanceof ImTypeVarRef)
            && sameOverrideFamily(left, right)) {
            return true;
        }
        // A specialized generic override can have concrete return types on both sides after
        // elimination (Holder<T>.get_it() -> Doubler.get_it() is one example). That is safe only
        // when the IM method union links the two methods; unrelated same-name methods in a generic
        // owner must still remain separate.
        if ((hasGenericOwner(left) || hasGenericOwner(right)
            || hasGenericSourceOwner(left) || hasGenericSourceOwner(right))
            && sameOverrideFamily(left, right)) {
            return true;
        }
        if (!(leftReturnType instanceof ImClassType leftClassType)
            || !(rightReturnType instanceof ImClassType rightClassType)) {
            return false;
        }
        ImClass leftClass = leftClassType.getClassDef();
        ImClass rightClass = rightClassType.getClassDef();
        ImClass leftOwner = left == null ? null : left.attrClass();
        ImClass rightOwner = right == null ? null : right.attrClass();
        if (leftOwner != null && rightOwner != null && leftOwner != rightOwner) {
            if (leftOwner.isSubclassOf(rightOwner)) {
                return leftClass.isSubclassOf(rightClass);
            }
            if (rightOwner.isSubclassOf(leftOwner)) {
                return rightClass.isSubclassOf(leftClass);
            }
        }
        if (left != null && right != null && left.getIsAbstract() != right.getIsAbstract()) {
            return left.getIsAbstract()
                ? rightClass.isSubclassOf(leftClass)
                : leftClass.isSubclassOf(rightClass);
        }
        return false;
    }

    private static boolean sameOverrideFamily(ImMethod left, ImMethod right) {
        return reaches(left, right, new HashSet<>()) || reaches(right, left, new HashSet<>());
    }

    private static boolean reaches(ImMethod current, ImMethod target, Set<ImMethod> visited) {
        if (current == null || !visited.add(current)) {
            return false;
        }
        if (current == target) {
            return true;
        }
        List<ImMethod> subMethods = current.getSubMethods();
        for (int i = 0; i < subMethods.size(); i++) {
            ImMethod subMethod = subMethods.get(i);
            if (reaches(subMethod, target, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGenericOwner(ImMethod method) {
        return method != null
            && method.attrClass() != null
            && !method.attrClass().getTypeVariables().isEmpty();
    }

    private static boolean hasGenericSourceOwner(ImMethod method) {
        return method != null
            && method.attrTrace() instanceof FuncDef funcDef
            && funcDef.attrNearestClassOrInterface() instanceof AstElementWithTypeParameters owner
            && !owner.getTypeParameters().isEmpty();
    }

    private static ImType dispatchReturnType(ImMethod method, ImTranslator tr) {
        ImFunction implementation = resolveDispatchSignatureImplementation(method, new HashSet<>());
        if (implementation != null) {
            return implementation.getReturnType();
        }
        if (tr != null && method != null && method.attrTrace() instanceof FuncDef funcDef) {
            return funcDef.attrReturnTyp().imTranslateType(tr);
        }
        return null;
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
        for (int i = 0; i < subMethods.size(); i++) {
            ImMethod subMethod = subMethods.get(i);
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

    /** The assigned name without the prefix naming the class whose member the group was named after. */
    private static String segmentOf(String assignedName, ImMethod namedAfter) {
        ImClass owner = namedAfter == null ? null : namedAfter.attrClass();
        if (owner == null) {
            return assignedName;
        }
        String prefix = LuaIdentifiers.toIdentifier(owner.getName()) + "_";
        return assignedName.startsWith(prefix) ? assignedName.substring(prefix.length()) : assignedName;
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
