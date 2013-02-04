package de.peeeq.wurstscript.attributes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;

public class PackageLevels {

	/**
	 * calculates a level for each package with the following properties:
	 * 
	 * forall packages p,q so that p imports q ::
	 * 		if q imports p then
	 * 			p.level == q.level
	 * 		else
	 * 			p.level > q.level
	 * 
	 * level is the smallest integer >= 0 which satisfies the property above
	 * 
	 * note that "imports" denotes the _transitive_ import-relation between packages 
	 */
	public static Map<WPackage, Integer> calculate(WurstModel wurstModel) {
		List<WPackage> packs = getPackages(wurstModel);
		
		Partitions<WPackage> packageClusters = calcPackageClusters(packs);
		Multimap<WPackage, WPackage> clusterImports = calcClusterImports(packs, packageClusters);
		
		Map<WPackage, Integer> result = Maps.newHashMap();
		for (CompilationUnit cu : wurstModel) {
			for (WPackage p : cu.getPackages()) {
				calcLevels(packageClusters, clusterImports, result, packageClusters.getRep(p));
			}
		}
		
		// extend result to all packages
		for (WPackage p : packs) {
			WPackage rep = packageClusters.getRep(p);
			result.put(p, result.get(rep));
		}
		return result;
	}

	/**
	 * calculates the levels of each package representing a cluster of packages
	 * 
	 * @param packageClusters
	 * @param clusterImports
	 * @param result
	 * @param rep
	 */
	private static void calcLevels(Partitions<WPackage> packageClusters,
			Multimap<WPackage, WPackage> clusterImports,
			Map<WPackage, Integer> result, WPackage rep) {
		if (result.containsKey(rep)) {
			return;
		}
		int level = 0;
		result.put(rep, -1);
		for (WPackage p : clusterImports.get(rep)) {
			if (p == rep) {
				// self import
				continue;
			}
			if (packageClusters.getRep(p) != p) {
				throw new Error("implementation error");
			}
			calcLevels(packageClusters, clusterImports, result, p);
			level = Math.max(level, result.get(p) + 1);
		}
		result.put(rep, level);
	}

	/**
	 * @param packs list of all packages
	 * @param packageClusters the clusters
	 * @return a map from representatives of clusters to other representatives. The 
	 * 			map contains the (non transitive) import relation between clusters
	 */
	private static Multimap<WPackage, WPackage> calcClusterImports(
			List<WPackage> packs, Partitions<WPackage> packageClusters) {
		Multimap<WPackage, WPackage> result = HashMultimap.create();
		for (WPackage p : packs) {
			for (WImport imp : p.getImports()) {
				WPackage ip = imp.attrImportedPackage();
				if (ip != null) {
					result.put(packageClusters.getRep(p), packageClusters.getRep(ip));
				}
			}
		}
		return result;
	}

	/**
	 * calculates cluster for packages
	 * two packages are in the same cluster if and only if they transitively 
	 * import each other
	 */
	private static Partitions<WPackage> calcPackageClusters(List<WPackage> packs) {
		Partitions<WPackage> packageClusters = new Partitions<WPackage>(packs);
		HashSet<WPackage> visited = Sets.<WPackage>newHashSet();
		Stack<WPackage> active = new Stack<WPackage>();
		for (WPackage p : packs) {
			calculatePackageClusters(packageClusters, visited, active, p);
		}
		return packageClusters;
	}

	private static void calculatePackageClusters(
			Partitions<WPackage> packageClusters, HashSet<WPackage> visited,
			Stack<WPackage> active, WPackage p) {
		if (p == null || visited.contains(p)) {
			return;
		}
		visited.add(p);
		active.push(p);
		for (WImport imp : p.getImports()) {
			WPackage ip = imp.attrImportedPackage();
			if (ip == null) {
				continue;
			}
			int indexOfIp = -1;
			for (int i=0; i<active.size(); i++) {
				if (packageClusters.getRep(ip) == packageClusters.getRep(active.get(i))) {
					indexOfIp = i;
					break;
				}
			}
			if (indexOfIp >= 0) {
				// we have a cycle in the import graph
				// --> union all elements in the cycle 
				for (int i=indexOfIp; i<active.size(); i++) {
					WPackage act = active.get(i);
					packageClusters.union(p, act);	
				}
				
			} else {
				calculatePackageClusters(packageClusters, visited, active, ip);
			}
		}
		active.pop();
	}

	private static List<WPackage> getPackages(WurstModel wurstModel) {
		List<WPackage> result = Lists.newArrayList();
		for (CompilationUnit cu : wurstModel) {
			result.addAll(cu.getPackages());
		}
		return result;
	}


	public static int get(WPackage p) {
		return p.getModel().attrPackageLevels().get(p);
	}

}
