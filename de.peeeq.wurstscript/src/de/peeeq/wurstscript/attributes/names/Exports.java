package de.peeeq.wurstscript.attributes.names;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;

public class Exports {

	public static Multimap<String, NameLink> exportedNameLinks(WPackage p) {
		Multimap<String, NameLink> result = HashMultimap.create();

		addExportedNameLinks(result, p, Sets.<WPackage>newHashSet());
//		result.putAll(p.getElements().attrNameLinks());
		// TODO hide package privates...
		// TODO add public imports

		return result;
	}

	private static void addExportedNameLinks(Multimap<String, NameLink> result,	WPackage p, HashSet<WPackage> alreadyImported) {
		if (alreadyImported.contains(p)) {
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
		if (alreadyImported.contains(p)) {
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
