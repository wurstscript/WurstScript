package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public class AttrImportedPackage {

    public static @Nullable WPackage getImportedPackage(WImport i) {
        try {
            WurstModel root = i.getModel();
            WPackage p = root.attrPackages().get(i.getPackagename());
            if (p == null && !i.getPackagename().equals("NoWurst")) {
                i.addError("Could not find imported package " + i.getPackagename());
            }
            return p;
        } catch (Error e) {
            i.addError("Could not find imported package " + i.getPackagename() + "\n" + e.getMessage());
            return null;
        }
    }

    public static WurstModel getModel(Element elem) {
        Element e = elem;
        while (e != null) {
            if (e instanceof WurstModel) {
                return (WurstModel) e;
            }
            e = e.getParent();
        }
        throw new Error("trying to get model for element " + Utils.printElement(elem) + ", which is not attached to a model");
    }

    public static ImmutableMap<String, WPackage> getPackages(WurstModel wurstModel) {
        Map<String, WPackage> result = Maps.newLinkedHashMap();
        for (CompilationUnit cu : wurstModel) {
            for (WPackage p : cu.getPackages()) {
                WPackage old = result.put(p.getName(), p);
                if (old != null) {
                    if (!p.getName().equals("Wurst")) {
                        // TODO should this really error?
                        p.addError("Package " + p.getName() + " is already defined in " + Utils.printPos(old.getSource()));
                        old.addError("Package " + p.getName() + " is already defined in " + Utils.printPos(p.getSource()));
                    }
                }
            }
        }
        return ImmutableMap.copyOf(result);
    }


}
