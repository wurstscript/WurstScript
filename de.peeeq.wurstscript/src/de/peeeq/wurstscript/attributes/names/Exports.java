package de.peeeq.wurstscript.attributes.names;


import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;

public class Exports {

	/**
	 * calculates all the namelinks exported by package p 
	 */
	public static ImmutableMultimap<String, NameLink> exportedNameLinks(WPackage p) {
		Builder<String, NameLink> result = ImmutableMultimap.builder();
		addExportedNameLinks(result, p, Sets.<WPackage>newLinkedHashSet());
		return result.build();
	}


	/**
	 * recursively adds all exported namelinks from package p to the result map
	 */
	private static void addExportedNameLinks(Builder<String, NameLink> result,	WPackage p, Set<WPackage> alreadyImported) {
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

	public static ImmutableMultimap<String, NameLink> exportedTypeNameLinks(WPackage p) {
		Builder<String, NameLink> result = ImmutableMultimap.builder();
		addExportedTypeNameLinks(result, p, Sets.<WPackage>newLinkedHashSet());
		return result.build();
	}


	private static void addExportedTypeNameLinks(Builder<String, NameLink> result,	WPackage p, Set<WPackage> alreadyImported) {
		if (alreadyImported.contains(p)) {
			return;
		}
		alreadyImported.add(p);
		
		// add elements:
		NameLinks.addHidingPrivateAndProtected(result, p.getElements().attrTypeNameLinks());
		// import public imports
		for (WImport imp2 : p.getImports()) {
			if (imp2.getIsPublic()) {
				WPackage imported = imp2.attrImportedPackage();
				if (imported != null) {
					addExportedTypeNameLinks(result, imported, alreadyImported);
				}
			}
		}

	}
}
