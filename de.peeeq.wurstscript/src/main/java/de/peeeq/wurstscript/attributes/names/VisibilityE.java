package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.Element;

public enum VisibilityE implements Visibility {
    PUBLIC, LOCAL;

    @Override
    public boolean isInherited() {
        return this == PUBLIC ;
    }

    @Override
    public boolean isPublic() {
        return this == PUBLIC;
    }

    @Override
    public boolean isVisibleAt(Element n) {
        return true;
    }

    @Override
    public String toString() {
        switch (this) {
            case PUBLIC:
                return "public";
            case LOCAL:
                return "local";
            default:
                return "unknown";
        }
    }
}
