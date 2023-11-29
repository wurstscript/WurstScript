package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.names.NameLink;

public class UsedPackages {

    public static ImmutableCollection<WPackage> usedPackages(Element e) {
        if (e.size() == 0) {
            return ImmutableList.of();
        }

        ImmutableSet.Builder<WPackage> result = ImmutableSet.builder();
        processChildren(e, result);
        return result.build();
    }

    private static void processChildren(Element e,
                                        ImmutableSet.Builder<WPackage> result) {
        for (int i = 0; i < e.size(); i++) {
            ImmutableCollection<WPackage> used = e.get(i).attrUsedPackages();
            if (!used.isEmpty()) {
                result.addAll(used);
            }
        }
    }

    public static ImmutableCollection<WPackage> usedPackages(NameRef e) {
        ImmutableSet.Builder<WPackage> result = ImmutableSet.builder();
        NameLink def = e.attrNameLink();
        if (def.getDef() instanceof VarDef) {
            if (def.getDef().attrNearestPackage() instanceof WPackage) {
                result.add((WPackage) e.attrNearestPackage());
            }
        }
        processChildren(e, result);
        return result.build();
    }


}
