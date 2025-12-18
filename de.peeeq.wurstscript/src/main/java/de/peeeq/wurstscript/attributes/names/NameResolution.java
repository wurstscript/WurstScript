package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.GlobalCaches;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

public class NameResolution {
    private static String memberFuncCacheName(String name, WurstType receiverType) {
        return name
            + "@"
            + receiverType
            + "#"
            + System.identityHashCode(receiverType);
    }

    public static ImmutableCollection<FuncLink> lookupFuncsNoConfig(Element node, String name, boolean showErrors) {
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.FUNC);
            @SuppressWarnings("unchecked")
            ImmutableCollection<FuncLink> cached = (ImmutableCollection<FuncLink>) GlobalCaches.lookupCache.get(key);
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

        List<FuncLink> result = new ArrayList<>(4);
        WScope scope = node.attrNearestScope();

        List<WScope> scopes = new ArrayList<>(8);
        while (scope != null) {
            scopes.add(scope);
            scope = nextScope(scope);
        }

        Set<NameDef> seen = new HashSet<>();

        for (WScope s : scopes) {
            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            for (DefLink n : links) {
                if (n instanceof FuncLink && n.getReceiverType() == null) {
                    if (seen.add(n.getDef())) {
                        result.add((FuncLink) n);
                    }
                }
            }
        }

        ImmutableCollection<FuncLink> immutableResult = ImmutableList.copyOf(result);

        // Cache the result
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.FUNC);
            GlobalCaches.lookupCache.put(key, immutableResult);
        }

        return immutableResult;
    }

    public static ImmutableCollection<FuncLink> lookupFuncs(Element e, String name, boolean showErrors) {
        final ImmutableCollection<FuncLink> raw = e.lookupFuncsNoConfig(name, showErrors);

        if (raw.isEmpty()) {
            return ImmutableList.of();
        }

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
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, memberFuncCacheName(name, receiverType), GlobalCaches.LookupType.MEMBER_FUNC);
            @SuppressWarnings("unchecked")
            ImmutableCollection<FuncLink> cached = (ImmutableCollection<FuncLink>) GlobalCaches.lookupCache.get(key);
            if (cached != null) {
                WLogger.trace("[LOOKUPCACHE] HIT MEMBER_FUNC node=" + System.identityHashCode(node)
                    + " name=" + name
                    + " recv=" + receiverType
                    + " recvId=" + System.identityHashCode(receiverType)
                    + " size=" + cached.size());
                return cached;
            }
        }

        List<FuncLink> result = new ArrayList<>(4);
        WLogger.trace("[LMF] addMemberMethods recv=" + receiverType
            + " recvId=" + System.identityHashCode(receiverType)
            + " name=" + name
            + " node=" + System.identityHashCode(node));
        // Collect from the type first, but *validate/adapt* each candidate to the actual receiverType.
        List<FuncLink> fromType = new ArrayList<>(4);
        addMemberMethods(node, receiverType, name, fromType);
        for (FuncLink cand : fromType) {
            DefLink m = matchDefLinkReceiver(cand, receiverType, node, showErrors);
            if (m instanceof FuncLink) {
                result.add((FuncLink) m);
            }
        }

        for (FuncLink f : result) {
            WLogger.trace("[LMF]  addMemberMethods -> " + f
                + " recv=" + f.getReceiverType()
                + " recvId=" + System.identityHashCode(f.getReceiverType())
                + " linkVB=" + f.getVariableBinding()
                + " linkTypeParams=" + f.getTypeParams());
        }

        WScope scope = node.attrNearestScope();

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
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, memberFuncCacheName(name, receiverType), GlobalCaches.LookupType.MEMBER_FUNC);
            WLogger.trace("[LOOKUPCACHE] PUT MEMBER_FUNC node=" + System.identityHashCode(node)
                + " name=" + name
                + " recv=" + receiverType
                + " recvId=" + System.identityHashCode(receiverType)
                + " size=" + immutableResult.size());
            GlobalCaches.lookupCache.put(key, immutableResult);
        }

        return immutableResult;
    }

    public static void addMemberMethods(Element node, WurstType receiverType, String name, List<FuncLink> result) {
        receiverType.addMemberMethods(node, name, result);
    }

    public static NameLink lookupVarNoConfig(Element node, String name, boolean showErrors) {
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.VAR);
            NameLink cached = (NameLink) GlobalCaches.lookupCache.get(key);
            if (cached != null) {
                return cached;
            }
        }

        NameLink privateCandidate = null;
        List<NameLink> candidates = new ArrayList<>(1);

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
                            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.VAR);
                            GlobalCaches.lookupCache.put(key, link);
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
                    GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.VAR);
                    GlobalCaches.lookupCache.put(key, result);
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
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, memberFuncCacheName(name, receiverType), GlobalCaches.LookupType.MEMBER_VAR);
            NameLink cached = (NameLink) GlobalCaches.lookupCache.get(key);
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

        DefLinkMatch bestMatch = null;

        for (WScope s : scopes) {
            Collection<DefLink> links = s.attrNameLinks().get(name);
            if (links.isEmpty()) continue;

            DefLinkMatch candidate = findBestMemberVarMatch(links, receiverType, node, showErrors);
            if (candidate != null) {
                if (bestMatch == null || candidate.distance < bestMatch.distance) {
                    bestMatch = candidate;
                    if (bestMatch.distance == 0) {
                        break;
                    }
                }
            }
        }

        if (bestMatch != null) {
            if (!showErrors) {
                GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, memberFuncCacheName(name, receiverType), GlobalCaches.LookupType.MEMBER_VAR);
                WLogger.trace("[LOOKUPCACHE] PUT MEMBER_FUNC node=" + System.identityHashCode(node)
                    + " name=" + name
                    + " recv=" + receiverType
                    + " recvId=" + System.identityHashCode(receiverType));
                GlobalCaches.lookupCache.put(key, bestMatch.link);
            }
            return bestMatch.link;
        }

        if (receiverType instanceof WurstTypeClassOrInterface) {
            WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) receiverType;
            Collection<DefLink> typeNameLinks = ct.nameLinks().get(name);
            DefLinkMatch candidate = findBestMemberVarMatch(typeNameLinks, receiverType, node, showErrors);
            if (candidate != null && candidate.link.getVisibility().isPublic()) {
                return candidate.link;
            }
            for (DefLink n : typeNameLinks) {
                if (n instanceof TypeDefLink && n.getVisibility().isPublic()) {
                    return n;
                }
            }
        }

        return null;
    }

    private static @Nullable DefLinkMatch findBestMemberVarMatch(Collection<DefLink> links, WurstType receiverType, Element node, boolean showErrors) {
        DefLink bestLink = null;
        int bestDistance = Integer.MAX_VALUE;

        for (DefLink n : links) {
            if (!(n instanceof VarLink)) {
                continue;
            }
            DefLink matched = matchDefLinkReceiver(n, receiverType, node, showErrors);
            if (matched == null) {
                continue;
            }
            int distance = receiverDistance(receiverType, matched.getReceiverType(), node);
            if (distance < bestDistance) {
                bestLink = matched;
                bestDistance = distance;
                if (distance == 0) {
                    break;
                }
            }
        }

        if (bestLink == null) {
            return null;
        }
        return new DefLinkMatch(bestLink, bestDistance);
    }

    private static int receiverDistance(WurstType receiverType, @Nullable WurstType candidateType, Element node) {
        if (candidateType == null) {
            return Integer.MAX_VALUE;
        }

        if (receiverType.equalsType(candidateType, node)) {
            return 0;
        }

        ClassDef receiverClass = owningClass(receiverType);
        ClassDef candidateClass = owningClass(candidateType);
        if (receiverClass != null && candidateClass != null) {
            int distance = inheritanceDistance(receiverClass, candidateClass);
            if (distance >= 0) {
                return distance;
            }
        }

        return Integer.MAX_VALUE / 2;
    }

    private static int inheritanceDistance(ClassDef start, ClassDef target) {
        int distance = 0;
        ClassDef current = start;
        while (current != null) {
            if (current == target) {
                return distance;
            }
            OptTypeExpr extended = current.getExtendedClass();
            if (!(extended instanceof TypeExpr)) {
                break;
            }
            WurstType extendedType = ((TypeExpr) extended).attrTyp();
            if (!(extendedType instanceof WurstTypeClass)) {
                break;
            }
            current = ((WurstTypeClass) extendedType).getClassDef();
            distance++;
        }
        return -1;
    }

    private static @Nullable ClassDef owningClass(WurstType type) {
        if (type instanceof WurstTypeClass) {
            return ((WurstTypeClass) type).getClassDef();
        }
        if (type instanceof WurstTypeClassOrInterface) {
            ClassOrInterface def = ((WurstTypeClassOrInterface) type).getDef();
            if (def instanceof ClassDef) {
                return (ClassDef) def;
            }
            return null;
        }
        if (type instanceof WurstTypeModuleInstanciation) {
            NamedScope inst = ((WurstTypeModuleInstanciation) type).getDef();
            return inst.attrNearestClassDef();
        }
        if (type instanceof WurstTypeModule) {
            ModuleDef moduleDef = ((WurstTypeModule) type).getDef();
            return moduleDef.attrNearestClassDef();
        }
        return null;
    }

    private static final class DefLinkMatch {
        private final DefLink link;
        private final int distance;

        private DefLinkMatch(DefLink link, int distance) {
            this.link = link;
            this.distance = distance;
        }
    }

    public static DefLink matchDefLinkReceiver(DefLink n, WurstType receiverType, Element node, boolean showErrors) {
        WurstType candRecv = n.getReceiverType();
        if (candRecv == null) return null;

        VariableBinding seed = VariableBinding.emptyMapping().withTypeVariables(n.getTypeParams());
        VariableBinding mapping = receiverType.matchAgainstSupertype(candRecv, node, seed, VariablePosition.RIGHT);
        if (mapping == null) return null;

        WLogger.trace("[MATCHRECV] def=" + ((n instanceof FuncLink) ? ((FuncLink) n).getDef().getName() : n.getDef().getName())
            + " left=" + receiverType
            + " candRecv=" + candRecv
            + " linkTypeParams=" + n.getTypeParams()
            + (n instanceof FuncLink ? (" linkVB=" + ((FuncLink) n).getVariableBinding()) : ""));

        if (showErrors) {
            if (n.getVisibility() == Visibility.PRIVATE_OTHER) {
                node.addError(Utils.printElement(n.getDef()) + " is private and cannot be used here.");
            } else if (n.getVisibility() == Visibility.PROTECTED_OTHER && !receiverType.isSubtypeOf(candRecv, node)) {
                node.addError(Utils.printElement(n.getDef()) + " is protected and cannot be used here.");
            }
        }

        return n.withTypeArgBinding(node, mapping);
    }

    private static Iterable<TypeParamDef> typeParamsOfReceiverType(WurstType t) {
        if (t instanceof WurstTypeClassOrInterface) {
            return ((WurstTypeClassOrInterface) t).getDef().getTypeParameters();
        }
        if (t instanceof WurstTypeClass) {
            return ((WurstTypeClass) t).getClassDef().getTypeParameters();
        }
        if (t instanceof WurstTypeInterface) {
            return ((WurstTypeInterface) t).getInterfaceDef().getTypeParameters();
        }
        return java.util.Collections.emptyList();
    }

    public static @Nullable TypeDef lookupType(Element node, String name, boolean showErrors) {
        if (!showErrors) {
            GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.TYPE);
            TypeDef cached = (TypeDef) GlobalCaches.lookupCache.get(key);
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
                    GlobalCaches.CacheKey key = new GlobalCaches.CacheKey(node, name, GlobalCaches.LookupType.TYPE);
                    GlobalCaches.lookupCache.put(key, result);
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

}
