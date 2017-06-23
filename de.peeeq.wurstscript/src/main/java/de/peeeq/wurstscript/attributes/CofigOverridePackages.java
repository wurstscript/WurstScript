package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public class CofigOverridePackages {

    public static final String CONFIG_POSTFIX = "_config";

    public static ImmutableMap<WPackage, WPackage> calculate(WurstModel model) {
        ImmutableMap.Builder<WPackage, WPackage> result = ImmutableMap.builder();
        Map<String, WPackage> packages = model.attrPackages();
        for (WPackage p : packages.values()) {
            if (p.getName().endsWith(CONFIG_POSTFIX)) {
                WPackage origP = getOriginalPackage(p);
                if (origP == null) {
                    p.addError("No package named " + p.getName() + " exists, so it is not possible to have a config package for it.");
                } else {
                    result.put(origP, p);
                }
            }
        }
        return result.build();
    }

    public static @Nullable WPackage getOriginalPackage(WPackage configPackage) {
        Preconditions.checkArgument(configPackage.getName().endsWith(CONFIG_POSTFIX));
        Map<String, WPackage> packages = configPackage.getModel().attrPackages();
        String name = configPackage.getName();
        name = name.substring(0, name.length() - CONFIG_POSTFIX.length());
        WPackage origP = packages.get(name);
        return origP;
    }

}
