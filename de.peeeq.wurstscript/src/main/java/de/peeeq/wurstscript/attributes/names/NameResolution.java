package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

public class NameResolution {

    // OPTIMIZATION 1: Thread-local caches to avoid synchronization overhead
    private static final ThreadLocal<Map<CacheKey, Object>> lookupCache =
        ThreadLocal.withInitial(() -> new WeakHashMap<>(256));

    // OPTIMIZATION 2: Cache key for lookups
    private static class CacheKey {
        final Element element;
        final String name;
        final LookupType type;

        CacheKey(Element element, String name, LookupType type) {
            this.element = element;
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey that = (CacheKey) o;
            return element == that.element && name.equals(that.name) && type == that.type;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * System.identityHashCode(element) + name.hashCode()) + type.hashCode();
        }
    }

    private enum LookupType {
        FUNC, VAR, TYPE, PACKAGE, MEMBER_FUNC, MEMBER_VAR
    }

    public static ImmutableCollection<FuncLink> lookupFuncsNoConfig(Element node, String name, boolean showErrors) {
        // OPTIMIZATION 3: Check cache first
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name, LookupType.FUNC);
            @SuppressWarnings("unchecked")
            ImmutableCollection<FuncLink> cached = (ImmutableCollection<FuncLink>) lookupCache.get().get(key);
            if (cached != null) {
                return cached;
            }
        }

        StructureDef nearestStructureDef = node.attrNearestStructureDef();
        if (nearestStructureDef != null) {
            WurstType receiverType = nearestStructureDef.attrTyp();
            ImmutableCollection<FuncLink> funcs = node.lookupMemberFuncs(receiverType, name, showErrors);
            if (!funcs.isEmpty()) {
                return funcs;
            }
        }

        // OPTIMIZATION 4: Pre-allocate with reasonable size
        List<FuncLink> result = new ArrayList<>(4);
        WScope scope = node.attrNearestScope();

        // OPTIMIZATION 5: Collect all scopes first to avoid repeated nextScope calls
        List<WScope> scopes = new ArrayList<>(8);
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        // OPTIMIZATION 6: Use Set to track seen definitions for deduplication
        Set<NameDef> seen = new HashSet<>();

        for (WScope s : scopes) {
            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (DefLink n : links) {
                if (n instanceof FuncLink && n.getReceiverType() == null) {
                    // OPTIMIZATION 7: Deduplicate during collection
                    if (seen.add(n.getDef())) {
                        result.add((FuncLink) n);
                    }
                }
            }
        }

        ImmutableCollection<FuncLink> immutableResult = ImmutableList.copyOf(result);

        // Cache the result
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name, LookupType.FUNC);
            lookupCache.get().put(key, immutableResult);
        }

        return immutableResult;
    }

    public static ImmutableCollection<FuncLink> lookupFuncs(Element e, String name, boolean showErrors) {
        final ImmutableCollection<FuncLink> raw = e.lookupFuncsNoConfig(name, showErrors);

        if (raw.isEmpty()) {
            return ImmutableList.of();
        }

        // OPTIMIZATION 8: Avoid builder allocation for single element
        if (raw.size() == 1) {
            FuncLink only = raw.iterator().next();
            return ImmutableList.of(only.withConfigDef());
        }

        final ImmutableList.Builder<FuncLink> b = ImmutableList.builderWithExpectedSize(raw.size());
        for (FuncLink f : raw) {
            b.add(f.withConfigDef());
        }
        return b.build();
    }

    private static <T extends NameLink> ImmutableCollection<T> removeDuplicates(List<T> nameLinks) {
        if (nameLinks.size() <= 1) {
            return ImmutableList.copyOf(nameLinks);
        }

        // OPTIMIZATION 9: Use IdentityHashSet for deduplication
        Set<NameDef> seen = Collections.newSetFromMap(new IdentityHashMap<>(nameLinks.size()));
        List<T> result = new ArrayList<>(nameLinks.size());

        for (T nl : nameLinks) {
            if (seen.add(nl.getDef())) {
                result.add(nl);
            }
        }

        return ImmutableList.copyOf(result);
    }

    private static @Nullable WScope nextScope(WScope scope) {
        Element parent = scope.getParent();
        if (parent == null) {
            return null;
        }
        WScope currentScope = scope;
        if (currentScope instanceof ModuleInstanciation) {
            ModuleInstanciation moduleInstanciation = (ModuleInstanciation) currentScope;
            return nextScope(moduleInstanciation.attrModuleOrigin());
        }
        return parent.attrNearestScope();
    }

    public static ImmutableCollection<FuncLink> lookupMemberFuncs(Element node, WurstType receiverType, String name, boolean showErrors) {
        // OPTIMIZATION 10: Cache member function lookups
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name + "@" + receiverType, LookupType.MEMBER_FUNC);
            @SuppressWarnings("unchecked")
            ImmutableCollection<FuncLink> cached = (ImmutableCollection<FuncLink>) lookupCache.get().get(key);
            if (cached != null) {
                return cached;
            }
        }

        List<FuncLink> result = new ArrayList<>(4);
        addMemberMethods(node, receiverType, name, result);

        WScope scope = node.attrNearestScope();

        // OPTIMIZATION 11: Collect scopes once
        List<WScope> scopes = new ArrayList<>(8);
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        for (WScope s : scopes) {
            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (DefLink n : links) {
                if (!(n instanceof FuncLink)) {
                    continue;
                }
                DefLink n2 = matchDefLinkReceiver(n, receiverType, node, false);
                if (n2 != null) {
                    FuncLink f = (FuncLink) n2;
                    result.add(f);
                }
            }
        }

        ImmutableCollection<FuncLink> immutableResult = removeDuplicates(result);

        if (!showErrors) {
            CacheKey key = new CacheKey(node, name + "@" + receiverType, LookupType.MEMBER_FUNC);
            lookupCache.get().put(key, immutableResult);
        }

        return immutableResult;
    }

    public static void addMemberMethods(Element node, WurstType receiverType, String name, List<FuncLink> result) {
        receiverType.addMemberMethods(node, name, result);
    }

    public static NameLink lookupVarNoConfig(Element node, String name, boolean showErrors) {
        // OPTIMIZATION 12: Cache variable lookups
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name, LookupType.VAR);
            NameLink cached = (NameLink) lookupCache.get().get(key);
            if (cached != null) {
                return cached;
            }
        }

        NameLink privateCandidate = null;
        List<NameLink> candidates = new ArrayList<>(1);

        // OPTIMIZATION 13: Collect scopes once
        List<WScope> scopes = new ArrayList<>(8);
        WScope scope = node.attrNearestScope();
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        for (WScope s : scopes) {
            if (s instanceof LoopStatementWithVarDef) {
                LoopStatementWithVarDef loop = (LoopStatementWithVarDef) s;
                if (!Utils.elementContained(Optional.of(node), loop.getBody())) {
                    continue;
                }
            }

            if (s instanceof StructureDef) {
                StructureDef nearestStructureDef = (StructureDef) s;
                WurstTypeNamedScope receiverType = (WurstTypeNamedScope) nearestStructureDef.attrTyp();
                for (DefLink link : receiverType.nameLinks(name)) {
                    if (!(link instanceof FuncLink)) {
                        if (!showErrors) {
                            CacheKey key = new CacheKey(node, name, LookupType.VAR);
                            lookupCache.get().put(key, link);
                        }
                        return link;
                    }
                }
            }

            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (DefLink n : links) {
                WurstType n_receiverType = n.getReceiverType();
                if (n instanceof VarLink && n_receiverType == null) {
                    if (n.getVisibility() != Visibility.PRIVATE_OTHER
                        && n.getVisibility() != Visibility.PROTECTED_OTHER) {
                        candidates.add(n);
                    } else if (privateCandidate == null) {
                        privateCandidate = n;
                    }
                } else if (n instanceof TypeDefLink) {
                    candidates.add(n);
                }
            }

            if (!candidates.isEmpty()) {
                if (showErrors && candidates.size() > 1) {
                    node.addError("Reference to variable " + name + " is ambiguous. Alternatives are:\n"
                        + Utils.printAlternatives(candidates));
                }
                NameLink result = candidates.get(0);
                if (!showErrors) {
                    CacheKey key = new CacheKey(node, name, LookupType.VAR);
                    lookupCache.get().put(key, result);
                }
                return result;
            }
        }

        if (showErrors) {
            if (privateCandidate == null) {
                node.addError("Could not find variable " + name + ".");
            } else {
                node.addError(Utils.printElementWithSource(Optional.of(privateCandidate.getDef()))
                    + " is not visible inside this package. If you want to access it, declare it public.");
                return privateCandidate;
            }
        }
        return null;
    }

    public static NameLink lookupMemberVar(Element node, WurstType receiverType, String name, boolean showErrors) {
        // OPTIMIZATION 14: Cache member var lookups
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name + "@" + receiverType, LookupType.MEMBER_VAR);
            NameLink cached = (NameLink) lookupCache.get().get(key);
            if (cached != null) {
                return cached;
            }
        }

        // Collect scopes once
        List<WScope> scopes = new ArrayList<>(8);
        WScope scope = node.attrNearestScope();
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        for (WScope s : scopes) {
            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (DefLink n : links) {
                if (!(n instanceof VarLink)) {
                    continue;
                }
                DefLink n2 = matchDefLinkReceiver(n, receiverType, node, showErrors);
                if (n2 != null) {
                    if (!showErrors) {
                        CacheKey key = new CacheKey(node, name + "@" + receiverType, LookupType.MEMBER_VAR);
                        lookupCache.get().put(key, n2);
                    }
                    return n2;
                }
            }
        }

        if (receiverType instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) receiverType;
            for (DefLink n : ct.nameLinks().get(name)) {
                if (n instanceof VarLink || n instanceof TypeDefLink) {
                    if (n.getVisibility().isPublic()) {
                        return n;
                    }
                }
            }
        }

        return null;
    }

    public static DefLink matchDefLinkReceiver(DefLink n, WurstType receiverType, Element node, boolean showErrors) {
        WurstType n_receiverType = n.getReceiverType();
        if (n_receiverType == null) {
            return null;
        }
        VariableBinding mapping = receiverType.matchAgainstSupertype(n_receiverType, node, VariableBinding.emptyMapping().withTypeVariables(n.getTypeParams()), VariablePosition.RIGHT);
        if (mapping == null) {
            return null;
        }
        if (showErrors) {
            if (n.getVisibility() == Visibility.PRIVATE_OTHER) {
                node.addError(Utils.printElement(n.getDef()) + " is private and cannot be used here.");
            } else if (n.getVisibility() == Visibility.PROTECTED_OTHER) {
                node.addError(Utils.printElement(n.getDef()) + " is protected and cannot be used here.");
            }
        }
        return n.withTypeArgBinding(node, mapping);
    }

    public static @Nullable TypeDef lookupType(Element node, String name, boolean showErrors) {
        // OPTIMIZATION 15: Cache type lookups
        if (!showErrors) {
            CacheKey key = new CacheKey(node, name, LookupType.TYPE);
            TypeDef cached = (TypeDef) lookupCache.get().get(key);
            if (cached != null) {
                return cached;
            }
        }

        NameLink privateCandidate = null;
        List<NameLink> candidates = new ArrayList<>(1);

        // Collect scopes once
        List<WScope> scopes = new ArrayList<>(8);
        WScope scope = node.attrNearestScope();
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        for (WScope s : scopes) {
            ImmutableCollection<TypeLink> links = s.attrTypeNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (NameLink n : links) {
                if (n.getDef() instanceof TypeDef) {
                    if (n.getVisibility() != Visibility.PRIVATE_OTHER
                        && n.getVisibility() != Visibility.PROTECTED_OTHER) {
                        candidates.add(n);
                    } else if (privateCandidate == null) {
                        privateCandidate = n;
                    }
                }
            }

            if (!candidates.isEmpty()) {
                if (showErrors && candidates.size() > 1) {
                    node.addError("Reference to type " + name + " is ambiguous. Alternatives are:\n"
                        + Utils.printAlternatives(candidates));
                }
                TypeDef result = (TypeDef) candidates.get(0).getDef();
                if (!showErrors) {
                    CacheKey key = new CacheKey(node, name, LookupType.TYPE);
                    lookupCache.get().put(key, result);
                }
                return result;
            }
        }

        if (showErrors) {
            if (privateCandidate == null) {
                node.addError("Could not find type " + name + ".");
            } else {
                node.addError(Utils.printElementWithSource(Optional.of(privateCandidate.getDef()))
                    + " is not visible inside this package. If you want to access it, declare it public.");
                return (TypeDef) privateCandidate.getDef();
            }
        }
        return null;
    }

    public static PackageLink lookupPackage(Element node, String name, boolean showErrors) {
        WScope scope = node.attrNearestScope();
        while (scope != null) {
            for (NameLink n : scope.attrNameLinks().get(name)) {
                if (n instanceof PackageLink) {
                    return (PackageLink) n;
                }
            }
            scope = nextScope(scope);
        }
        return null;
    }

    public static ImmutableCollection<FuncLink> lookupFuncsShort(Element elem, String name) {
        return lookupFuncs(elem, name, true);
    }

    public static ImmutableCollection<FuncLink> lookupMemberFuncsShort(Element elem, WurstType receiverType, String name) {
        return lookupMemberFuncs(elem, receiverType, name, true);
    }

    public static NameLink lookupVarShort(Element node, String name) {
        return lookupVar(node, name, true);
    }

    public static NameLink lookupMemberVarShort(Element node, WurstType receiverType, String name) {
        return lookupMemberVar(node, receiverType, name, true);
    }

    public static @Nullable TypeDef lookupTypeShort(Element node, String name) {
        return lookupType(node, name, true);
    }

    public static PackageLink lookupPackageShort(Element node, String name) {
        return lookupPackage(node, name, true);
    }

    public static NameLink lookupVar(Element e, String name, boolean showErrors) {
        NameLink v = e.lookupVarNoConfig(name, showErrors);
        if (v != null) {
            NameDef actual = v.getDef().attrConfigActualNameDef();
            return v.withDef(actual);
        }
        return null;
    }

    // OPTIMIZATION 16: Add cache clearing method for when AST changes
    public static void clearCache() {
        lookupCache.get().clear();
    }
}
