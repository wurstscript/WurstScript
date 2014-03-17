package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;

public class InitOrder {

	public static List<WPackage> initDependencies(WPackage p) {
		Set<VarDef> readGlobals = Sets.newLinkedHashSet();
		collectReadGlobals(p, readGlobals);
		
		
		Set<WPackage> packages = Sets.newLinkedHashSet();
		for (VarDef v : readGlobals) {
			PackageOrGlobal pkg = v.attrNearestPackage();
			if (pkg != p && pkg instanceof WPackage) {
				packages.add((WPackage) pkg);
			}
		}
		
		return Lists.newArrayList(packages);
	}

	private static void collectReadGlobals(AstElement e, Set<VarDef> result) {
		
		if (e instanceof GlobalVarDef) {
			GlobalVarDef v = (GlobalVarDef) e;
			if (v.getInitialExpr() instanceof Expr && !v.attrIsDynamicContext()) {
				Expr expr = (Expr) v.getInitialExpr();
				result.addAll(expr.attrReadGlobalVariables());
			}
			return;
		} else if (e instanceof InitBlock) {
			InitBlock initBlock = (InitBlock) e;
			result.addAll(initBlock.attrReadGlobalVariables());
			return;
		} else if (e instanceof FunctionLike) {
			return;
		}
		for (int i=0; i<e.size();i++) {
			collectReadGlobals(e.get(i), result);
		}
		
	}

	public static List<WPackage> initDependenciesTransitive(WPackage p) {
		List<WPackage> result = Lists.newArrayList();
		for (WPackage dep : p.attrInitDependencies()) {
			addInitDependenciesTransitive(dep, result);
		}
		return result;
	}

	private static void addInitDependenciesTransitive(WPackage p, List<WPackage> result) {
		if (result.contains(p)) {
			return;
		}
		result.add(p);
		for (WPackage dep : p.attrInitDependencies()) {
			addInitDependenciesTransitive(dep, result);
		}
	}

}
