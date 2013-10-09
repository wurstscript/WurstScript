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
		Map<String, WPackage> result = Maps.newLinkedHashMap();
		for (CompilationUnit cu : wurstModel) {
			for (WPackage p : cu.getPackages()) {
				WPackage old = result.put(p.getName(), p);
				if (old != null) {
					if (!p.getName().equals("Wurst")) {
						// TODO should this really error?
						p.addError( "Package " + p.getName() + " is already defined in " + Utils.printPos(old.getSource()));
						old.addError( "Package " + p.getName() + " is already defined in " + Utils.printPos(p.getSource()));
					}
				}
			}
		}
		return result;
	}

	public static Map<String, WPackage> getPackagesFresh(WurstModel wurstModel) {
		Map<String, WPackage> attrPackages = wurstModel.attrPackages();
		attrPackages.clear();
		attrPackages.putAll(getPackages(wurstModel));
		return attrPackages;
	}


}
