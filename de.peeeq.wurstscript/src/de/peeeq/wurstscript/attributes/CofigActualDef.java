package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.names.NameLink;

public class CofigActualDef {

	public static NameDef calculate(NameDef d) {
		// default is to be not configurable
		return d;
	}

	
	public static NameDef calculate(GlobalVarDef g) {
		WPackage p = getConfigPackage(g);
		if (p != null) {
			NameDef v = p.getElements().lookupVar(g.getName(), false);
			if (v != null && hasConfigAnnotation(v)) {
				return v;
			}
		}
		// not configured
		return g;
	}
	
	public static NameDef calculate(FuncDef f) {
		WPackage p = getConfigPackage(f);
		if (p != null) {
			Collection<NameLink> links = p.getElements().lookupFuncs(f.getName(), false);
			for (NameLink link : links) {
				if (link.getNameDef().hasAnnotation("@config")) {
					return link.getNameDef();
				}
			}
		}
		// not configured
		return f;
	}


	private static boolean hasConfigAnnotation(NameDef v) {
		return v.hasAnnotation("@config");
	}


	private static @Nullable WPackage getConfigPackage(AstElement e) {
		PackageOrGlobal p = e.attrNearestPackage();
		return p.getModel().attrConfigOverridePackages().get(p);
	}


}
