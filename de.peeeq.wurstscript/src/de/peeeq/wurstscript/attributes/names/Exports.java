package de.peeeq.wurstscript.attributes.names;

import java.util.HashSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;

public class Exports {

	/**
	 * calculates all the namelinks exported by package p 
	 */
	public static Multimap<String, NameLink> exportedNameLinks(WPackage p) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addExportedNameLinks(result, p, Sets.<WPackage>newHashSet());
		return result;
	}


	/**
	 * recursively adds all exported namelinks from package p to the result map
	 */
	private static void addExportedNameLinks(Multimap<String, NameLink> result,	WPackage p, HashSet<WPackage> alreadyImported) {
		if (p == null || alreadyImported.contains(p)) {
			return;
		}
		alreadyImported.add(p);
		
		// add elements:
		NameLinks.addHidingPrivateAndProtected(result, p.getElements().attrNameLinks());
		
		// import public imports
		for (WImport imp2 : p.getImports()) {
			if (imp2.getIsPublic()) {
				addExportedNameLinks(result, imp2.attrImportedPackage(), alreadyImported);
			}
		}

	}

	public static Multimap<String, NameLink> exportedTypeNameLinks(WPackage p) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addExportedTypeNameLinks(result, p, new HashSet<WPackage>());
		return result;
	}


	private static void addExportedTypeNameLinks(Multimap<String, NameLink> result,	WPackage p, HashSet<WPackage> alreadyImported) {
		if (p == null || alreadyImported.contains(p)) {
			return;
		}
		alreadyImported.add(p);
		
		// add elements:
		NameLinks.addHidingPrivateAndProtected(result, p.getElements().attrTypeNameLinks());
		// import public imports
		for (WImport imp2 : p.getImports()) {
			if (imp2.getIsPublic()) {
				addExportedTypeNameLinks(result, imp2.attrImportedPackage(), alreadyImported);
			}
		}

	}
}
