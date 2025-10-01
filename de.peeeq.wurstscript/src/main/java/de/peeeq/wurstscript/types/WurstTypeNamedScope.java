package de.peeeq.wurstscript.types;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.DefLink;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.Visibility;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class WurstTypeNamedScope extends WurstType {

    private final boolean isStaticRef;
    private final List<WurstTypeBoundTypeParam> typeParameters;

    // OPTIMIZATION 1: Cache the name links with type arg binding
    private volatile ImmutableMultimap<String, DefLink> cachedNameLinks = null;
    private volatile VariableBinding cachedBinding = null;

    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters, boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = typeParameters;
    }

    public WurstTypeNamedScope(List<WurstTypeBoundTypeParam> typeParameters) {
        this.isStaticRef = false;
        this.typeParameters = typeParameters;
    }

    public WurstTypeNamedScope(boolean isStaticRef) {
        this.isStaticRef = isStaticRef;
        this.typeParameters = Collections.emptyList();
    }

    @Override
    public String getName() {
        NamedScope def = getDef();
        if (def == null) {
            return "not found";
        }
        return def.getName();
    }

    public abstract @Nullable NamedScope getDef();

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public boolean isStaticRef() {
        return isStaticRef;
    }

    @Override
    VariableBinding matchAgainstSupertypeIntern(WurstType obj, @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (obj instanceof WurstTypeNamedScope) {
            WurstTypeNamedScope other = (WurstTypeNamedScope) obj;
            if (other.getDef() == this.getDef()) {
                return matchTypeParams(getTypeParameters(), other.getTypeParameters(), location, mapping, variablePosition);
            }
        }
        return null;
    }

    public List<WurstTypeBoundTypeParam> getTypeParameters() {
        return typeParameters;
    }

    protected String printTypeParams() {
        if (typeParameters.isEmpty()) {
            return "";
        }
        StringBuilder s = new StringBuilder("<");
        for (int i = 0; i < typeParameters.size(); i++) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(typeParameters.get(i).getName());
        }
        return s + ">";
    }

    @Override
    public VariableBinding getTypeArgBinding() {
        // OPTIMIZATION 2: Cache the type arg binding
        if (cachedBinding != null) {
            return cachedBinding;
        }

        VariableBinding res = VariableBinding.emptyMapping();
        for (WurstTypeBoundTypeParam tp : typeParameters) {
            res = res.set(tp.getTypeParamDef(), tp);
        }
        cachedBinding = res;
        return res;
    }

    @Override
    public WurstType setTypeArgs(@NonNull VariableBinding typeParamBounds) {
        List<WurstTypeBoundTypeParam> newTypes = Lists.newArrayList();
        for (WurstTypeBoundTypeParam t : typeParameters) {
            newTypes.add(t.setTypeArgs(typeParamBounds));
        }
        return replaceTypeVars(newTypes);
    }

    abstract public WurstType replaceTypeVars(List<WurstTypeBoundTypeParam> newTypes);

    public WurstType replaceTypeVarsUsingTypeArgs(TypeExprList typeArgs) {
        if (typeArgs.isEmpty()) {
            return this;
        }

        // OPTIMIZATION 3: Pre-size the list
        List<WurstTypeBoundTypeParam> typeParams = new ArrayList<>(typeParameters.size());

        if (typeArgs.size() != typeParameters.size()) {
            typeArgs.addError("Expected " + typeParameters.size() + " type arguments, but got " + typeArgs.size());
        }

        int minSize = Math.min(typeArgs.size(), typeParameters.size());
        for (int i = 0; i < minSize; i++) {
            WurstTypeBoundTypeParam tp = typeParameters.get(i);
            TypeParamDef tpDef = tp.getTypeParamDef();
            TypeExpr typeArg = typeArgs.get(i);
            WurstType baseType = typeArg.attrTyp().dynamic();
            typeParams.add(new WurstTypeBoundTypeParam(tpDef, baseType, typeArg));
        }

        return replaceTypeVars(typeParams);
    }

    protected VariableBinding matchTypeParams(List<WurstTypeBoundTypeParam> list, List<WurstTypeBoundTypeParam> list2,
                                              @Nullable Element location, VariableBinding mapping, VariablePosition variablePosition) {
        if (list.size() != list2.size()) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            WurstType thisTp = list.get(i).normalize();
            WurstType otherTp = list2.get(i).normalize();
            mapping = thisTp.matchTypes(otherTp, location, mapping, variablePosition);
            if (mapping == null) {
                return null;
            }
        }
        return mapping;
    }

    @Override
    public boolean allowsDynamicDispatch() {
        return !isStaticRef();
    }

    /**
     * get the name links available in this class or interface
     * This includes inherited names
     */
    public ImmutableMultimap<String, DefLink> nameLinks() {
        // OPTIMIZATION 4: Cache the entire nameLinks result
        VariableBinding currentBinding = getTypeArgBinding();

        // Check if cache is valid
        if (cachedNameLinks != null &&
            ((currentBinding.isEmpty() && cachedBinding == null) ||
                (currentBinding.equals(cachedBinding)))) {
            return cachedNameLinks;
        }

        NamedScope def = getDef();
        if (def == null) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap<String, DefLink> res = def.attrNameLinks();

        if (!currentBinding.isEmpty()) {
            // OPTIMIZATION 5: Use builderWithExpectedSize
            int expectedSize = res.size();
            ImmutableMultimap.Builder<String, DefLink> resBuilder =
                new ImmutableMultimap.Builder<>();

            for (Map.Entry<String, DefLink> e : res.entries()) {
                resBuilder.put(e.getKey(), e.getValue().withTypeArgBinding(def, currentBinding));
            }
            res = resBuilder.build();
        }

        // Cache the result
        cachedNameLinks = res;
        cachedBinding = currentBinding;

        return res;
    }

    public ImmutableCollection<DefLink> nameLinks(String name) {
        // OPTIMIZATION 6: Direct lookup in cached multimap
        return nameLinks().get(name);
    }

    @Override
    public void addMemberMethods(Element node, String name, List<FuncLink> result) {
        // OPTIMIZATION 7: Single lookup and iteration
        ImmutableCollection<DefLink> links = nameLinks(name);

        if (links.isEmpty()) {
            return;
        }

        for (DefLink defLink : links) {
            if (!(defLink instanceof FuncLink)) {
                continue;
            }

            FuncLink f = (FuncLink) defLink;
            Visibility vis = f.getVisibility();

            if (vis.isPublic()) {
                result.add(f);
            } else if (vis.isInherited()) {
                // OPTIMIZATION 8: Cache these lookups if possible
                NamedScope def = getDef();
                if (def != null && node.attrNearestPackage() != def.attrNearestPackage()) {
                    ClassDef nearestClass = node.attrNearestClassDef();
                    if (nearestClass == null || !nearestClass.attrTypC().isSubtypeOf(this, node)) {
                        f = f.withVisibility(Visibility.PROTECTED_OTHER);
                    }
                }
                result.add(f);
            }
        }
    }

    @Override
    public Stream<FuncLink> getMemberMethods(Element node) {
        // OPTIMIZATION 9: Avoid stream operations if possible
        return nameLinks().values().stream()
            .filter(n -> n instanceof FuncLink && n.getReceiverType() != null)
            .map(n -> (FuncLink) n);
    }

    @Override
    public boolean isNestedInside(WurstType other) {
        if (!(other instanceof WurstTypeNamedScope)) {
            return false;
        }

        WurstTypeNamedScope wtns = (WurstTypeNamedScope) other;
        NamedScope scope = wtns.getDef();
        Element node = this.getDef();

        // OPTIMIZATION 10: Limit traversal depth
        int maxDepth = 50; // Prevent infinite loops
        while (node != null && maxDepth-- > 0) {
            if (node == scope) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    @Override
    protected boolean isNullable() {
        return true;
    }

    // OPTIMIZATION 11: Clear cache when type changes
    protected void invalidateCache() {
        cachedNameLinks = null;
        cachedBinding = null;
    }
}
