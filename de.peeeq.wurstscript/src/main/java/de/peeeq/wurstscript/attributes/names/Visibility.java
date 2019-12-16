package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.*;

public interface Visibility {

    static VisibilityPrivate visibilityPrivate(NamedScope s) {
        return new VisibilityPrivate(s);
    }

    static VisibilityProtected visibilityProtected(StructureDef s) {
        return new VisibilityProtected(s);
    }

    boolean isInherited();

    boolean isPublic();

    boolean isVisibleAt(Element n);
}
