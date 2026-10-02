package de.peeeq.wurstscript.intermediatelang;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

public class ILconstObject extends ILconstAbstract {
    private final ImClassType classType;
    private final int objectId;
    private @Nullable Object2ObjectLinkedOpenHashMap<ImVar, ILconst> scalarAttributes;
    private @Nullable Table<ImVar, List<Integer>, ILconst> indexedAttributes;
    private boolean destroyed = false;
    private final Element trace;
    private Map<ImTypeVar, ImType> capturedTypeSubstitutions = Collections.emptyMap();

    public Map<ImTypeVar, ImType> getCapturedTypeSubstitutions() {
        return capturedTypeSubstitutions;
    }

    public void captureTypeSubstitutions(Map<ImTypeVar, ImType> subst) {
        if (subst == null || subst.isEmpty()) {
            capturedTypeSubstitutions = Collections.emptyMap();
        } else {
            capturedTypeSubstitutions = Collections.unmodifiableMap(new HashMap<>(subst));
        }
    }

    public ILconstObject(ImClassType classType, int objectId, Element trace) {
        this.classType = classType;
        this.objectId = objectId;
        this.trace = trace;
    }

    public int getObjectId() {
        return objectId;
    }

    @Override
    public String print() {
        return classType + "_" + hashCode();
    }



    @Override
    public boolean isEqualTo(ILconst other) {
        return other == this;
    }

    public void set(ImVar attr, List<Integer> indexes, ILconst value) {
        Objects.requireNonNull(attr);
        Objects.requireNonNull(value);
        if (indexes.isEmpty()) {
            if (scalarAttributes == null) {
                scalarAttributes = new Object2ObjectLinkedOpenHashMap<>(4);
            }
            scalarAttributes.put(attr, value);
        } else {
            if (indexedAttributes == null) {
                indexedAttributes = HashBasedTable.create();
            }
            indexedAttributes.put(attr, indexes, value);
        }
    }

    public Optional<ILconst> get(ImVar attr, List<Integer> indexes) {
        if (indexes.isEmpty()) {
            return Optional.ofNullable(scalarAttributes == null ? null : scalarAttributes.get(attr));
        }
        return Optional.ofNullable(indexedAttributes == null ? null : indexedAttributes.get(attr, indexes));
    }


    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        destroyed = true;
    }

    public ImClass getImClass() {
        return classType.getClassDef();
    }

    public Element getTrace() {
        return trace;
    }

    public ImClassType getType() {
        return classType;
    }

    @Override
    public int hashCode() {
        return objectId;
    }

    /** Snapshot of initialized fields for compiletime state migration. */
    public Table<ImVar, List<Integer>, ILconst> getAttributes() {
        Table<ImVar, List<Integer>, ILconst> result = HashBasedTable.create();
        if (scalarAttributes != null) {
            scalarAttributes.forEach((field, value) -> result.put(field, Collections.emptyList(), value));
        }
        if (indexedAttributes != null) {
            result.putAll(indexedAttributes);
        }
        return result;
    }
}
