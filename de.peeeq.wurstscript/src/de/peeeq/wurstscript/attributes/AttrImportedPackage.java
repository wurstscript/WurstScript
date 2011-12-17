package de.peeeq.wurstscript.attributes;

import java.util.Map;
import org.apache.log4j.helpers.CyclicBuffer;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;

public class AttrImportedPackage {

	public static WPackage getImportedPackage(WImport i) {
		CompilationUnit root = (CompilationUnit) Utils.getRoot(i);
		WPackage p = root.attrPackages().get(i.getPackagename());
		if (p == null) {
			attr.addError(i.getSource(), "Could not find imported package " + i.getPackagename());
		}
		return p;
	}

	public static Map<String, WPackage> getPackages(CompilationUnit c) {
		Map<String, WPackage> result = Maps.newHashMap();
		for (TopLevelDeclaration e : c ) {
			if (e instanceof WPackage) {
				WPackage p = (WPackage) e;
				result.put(p.getName(), p);
			}
		}
		return result;
	}

}
