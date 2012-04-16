package de.peeeq.wurstscript.attributes;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.utils.Utils;

public class AttrImportedPackage {

	public static WPackage getImportedPackage(WImport i) {
		WurstModel root = i.getModel();
		WPackage p = root.attrPackages().get(i.getPackagename());
		if (p == null) {
			attr.addError(i.getSource(), "Could not find imported package " + i.getPackagename());
		}
		return p;
	}

	public static WurstModel getModel(AstElement elem) {
		AstElement e = elem.attrCompilationUnit();
		while (e != null) {
			if (e instanceof WurstModel) {
				return (WurstModel) e;
			}
			e = e.getParent();
		}
		throw new Error("trying to get model for element " + Utils.printElement(elem) + ", which is not attached to a model");
	}

	public static Map<String, WPackage> getPackages(WurstModel wurstModel) {
		Map<String, WPackage> result = Maps.newHashMap();
		for (CompilationUnit cu : wurstModel) {
			for (WPackage p : cu.getPackages()) {
				result.put(p.getName(), p);
			}
		}
		return result;
	}


}
