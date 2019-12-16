package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

public class VisibilityPrivate implements Visibility {

    private final NamedScope definingScope;

    public VisibilityPrivate(NamedScope definingScope) {
        this.definingScope = definingScope;
    }

    @Override
    public boolean isInherited() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isVisibleAt(Element n) {
        @Nullable NamedScope namedScope = n.attrNearestNamedScope();
        return Utils.elementContained(namedScope, definingScope);
    }

    @Override
    public String toString() {
        return "private in " + definingScope.getName();
    }
}
