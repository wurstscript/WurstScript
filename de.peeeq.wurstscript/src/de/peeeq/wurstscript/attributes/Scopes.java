package de.peeeq.wurstscript.attributes;

import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.AstElementWithParameters;
import de.peeeq.wurstscript.ast.AstElementWithSlots;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ClassSlots;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatements;

public class Scopes {

	


	public static Multimap<String, NameDef> getDefinedNames(AstElementWithBody c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		// add parameters:
		if (c instanceof AstElementWithParameters) {
			AstElementWithParameters wp = (AstElementWithParameters) c;
			for (WParameter p : wp.getParameters()) {
				result.put(p.getName(), p);
			}
		}
		addDefinedNamesInStatements(result, c.getBody());
		return result;
	}
	
	
	private static void addDefinedNamesInStatements(final Multimap<String, NameDef> result, WStatements statements) {
		// OPTIMIZE
		statements.accept(new WStatements.DefaultVisitor() {
			@Override
			public void visit(LocalVarDef localVarDef) {
				result.put(localVarDef.getName(), localVarDef);
			}
		});
	}

	public static Multimap<String, NameDef> getDefinedNames(CompilationUnit c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (TopLevelDeclaration e : c) {
			if (e instanceof NameDef) {
				NameDef n = (NameDef) e;
				result.put(n.getName(), n);
			}
			if (e instanceof JassGlobalBlock) {
				JassGlobalBlock g = (JassGlobalBlock) e;
				for (GlobalVarDef v : g) {
					result.put(v.getName(), v);
				}
			}
		}
		return result;
	}

	public static Multimap<String, NameDef> getDefinedNames(ClassOrModuleOrModuleInstanciation c) {
		return getDefinedNames(c.getSlots());
	}
	
	private static Multimap<String, NameDef> getDefinedNames(ClassSlots slots) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (ClassSlot s : slots) {
			if (s instanceof NameDef) {
				NameDef n = (NameDef) s;
				result.put(n.getName(), n);
			}
		}
		return result;
	}


	public static Multimap<String, NameDef> getDefinedNames(WPackage p) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (WEntity e : p.getElements()) {
			if (e instanceof NameDef) {
				NameDef n = (NameDef) e;
				result.put(n.getName(), n);
			}
		}
		return result;
	}

	// default implementation, override for others:
	public static Multimap<String, NameDef> getVisibleNamesPrivate(WScope s) {
		return s.attrDefinedNames();
	}
	
	public static Multimap<String, NameDef> getVisibleNamesPrivate(WPackage p) {
		Multimap<String, NameDef> result = HashMultimap.create();
		
		// add imported names
		for (WImport i : p.getImports()) {
			WPackage importedPackage = i.attrImportedPackage();
			result.putAll(importedPackage.attrExportedNames());
		}
		
		// add all defined names
		result.putAll(p.attrDefinedNames());
		return result;
	}
	
	public static Multimap<String, NameDef> getVisibleNamesPrivate(ClassOrModuleOrModuleInstanciation c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		
		// add protected names from used module instanciations
		for (ModuleInstanciation m : c.attrModuleInstanciations()) {
			result.putAll(m.attrVisibleNamesProtected());
		}
		
		// add all defined names
		result.putAll(c.attrDefinedNames());
		return result;
	}
	
	
	// TODO getVisibleNamesPrivate for special scopes
	
	public static Multimap<String, NameDef> getVisibleNamesPublic(WScope s) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (Entry<String, NameDef> e : s.attrVisibleNamesPrivate().entries()) {
			if (!e.getValue().attrIsPrivate() && !e.getValue().attrIsProtected()) { // TODO depends on where this is defined
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}
	
	public static Multimap<String, NameDef> getVisibleNamesProtected(WScope s) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (Entry<String, NameDef> e : s.attrVisibleNamesPrivate().entries()) {
			if (!e.getValue().attrIsPrivate()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}


	public static Multimap<String, NameDef> getExportedNames(WPackage p) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (Entry<String, NameDef> e : p.attrVisibleNamesPrivate().entries()) {
			if (e.getValue().attrIsPublic()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}
	
}
