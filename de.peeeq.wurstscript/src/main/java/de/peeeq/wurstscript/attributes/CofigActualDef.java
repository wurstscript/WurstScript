package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import org.eclipse.jdt.annotation.Nullable;

public class CofigActualDef {

    public static NameDef calculate(NameDef d) {
        // default is to be not configurable
        return d;
    }


    public static NameDef calculate(GlobalVarDef g) {
        WPackage p = getConfigPackage(g);
        if (p != null) {
            NameLink v = p.getElements().lookupVarNoConfig(g.getName(), false);
            WurstModel m = g.getModel();
            if (v != null && hasConfigAnnotation(v.getDef(m))) {
                return v.getDef(m);
            }
        }
        // not configured
        return g;
    }

    public static NameDef calculate(FuncDef f) {
        WPackage p = getConfigPackage(f);
        if (p != null) {
            ImmutableCollection<FuncLink> links = p.getElements().lookupFuncsNoConfig(f.getName(), false);
            for (NameLink link : links) {
                WurstModel m = f.getModel();
                if (hasConfigAnnotation(link.getDef(m))) {
                    return link.getDef(m);
                }
            }
        }
        // not configured
        return f;
    }


    private static boolean hasConfigAnnotation(NameDef v) {
        return v.hasAnnotation("@config");
    }


    private static @Nullable WPackage getConfigPackage(Element e) {
        PackageOrGlobal p = e.attrNearestPackage();
        return p.getModel().attrConfigOverridePackages().get(p);
    }


}
