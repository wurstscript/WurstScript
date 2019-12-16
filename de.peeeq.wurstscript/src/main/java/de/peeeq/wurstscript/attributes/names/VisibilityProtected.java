package de.peeeq.wurstscript.attributes.names;

import de.peeeq.wurstscript.ast.ClassOrInterface;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClassOrInterface;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

public class VisibilityProtected implements Visibility {

    private final StructureDef definingScope;

    public VisibilityProtected(StructureDef definingScope) {
        this.definingScope = definingScope;
    }

    @Override
    public boolean isInherited() {
        return true;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isVisibleAt(Element n) {

        if (n.attrNearestPackage() == definingScope.attrNearestPackage()) {
            // can use protected in same package
            return true;
        }
        ClassOrInterface namedScope = n.attrNearestClassOrInterface();
        if (namedScope != null) {
            // can use protected in same scope
            if (Utils.elementContained(namedScope, definingScope)) {
                return true;
            }
            // can use protected in subtypes
            WurstType t = namedScope.attrTyp();
            if (t instanceof WurstTypeClassOrInterface) {
                WurstTypeClassOrInterface ct = (WurstTypeClassOrInterface) t;
                return isSubtypeOf(ct, definingScope);
            }
        }
        return false;
    }

    private boolean isSubtypeOf(WurstTypeClassOrInterface ct, StructureDef definingScope) {
        if (ct.getDef() == definingScope) {
            return true;
        }
        for (WurstTypeClassOrInterface superType : ct.directSupertypes()) {
            if (isSubtypeOf(superType, definingScope)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "protected in " + definingScope.getName();
    }
}
