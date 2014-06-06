package de.peeeq.wurstscript.attributes;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;

public class CofigOverridePackages {

	public static final String CONFIG_POSTFIX = "_config";

	public static Map<WPackage, WPackage> calculate(WurstModel model) {
		Map<WPackage, WPackage> result = Maps.newHashMap();
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
		return result;
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
