package de.peeeq.wurstscript.attributes.names;

public enum Visibility {
    PRIVATE_OTHER, PUBLIC, LOCAL, PRIVATE_HERE, PROTECTED_HERE, PROTECTED_OTHER;

    public boolean isInherited() {
        return this == PUBLIC || this == PROTECTED_HERE || this == PROTECTED_OTHER;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

}
