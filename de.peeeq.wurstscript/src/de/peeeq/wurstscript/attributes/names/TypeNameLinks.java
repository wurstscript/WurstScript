package de.peeeq.wurstscript.attributes.names;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WEntities;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.ast.WurstModel;

public class TypeNameLinks {
	
	public static Multimap<String, NameLink> calculate(ClassOrModuleOrModuleInstanciation c) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addTypeParametersIfAny(result, c);
		return result;
	}

	public static Multimap<String, NameLink> calculate(CompilationUnit cu) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addJassTypes(result, cu);
		addPackages(result, cu);
		return result;
	}
	
	public static Multimap<String, NameLink> calculate(AstElementWithBody c) {
		Multimap<String, NameLink> result = HashMultimap.create();
		WScope s = (WScope) c;
		addTypeParametersIfAny(result, s);
		return result;
	}

	
	public static Multimap<String, NameLink> calculate(EnumDef e) {
		Multimap<String, NameLink> result = HashMultimap.create();
		return result;
	}

	public static Multimap<String, NameLink> calculate(InterfaceDef i) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addTypeParametersIfAny(result, i);
		return result;
	}

	public static Multimap<String, NameLink> calculate(NativeFunc nativeFunc) {
		Multimap<String, NameLink> result = HashMultimap.create();
		return result;
	}

	public static Multimap<String, NameLink> calculate(TupleDef t) {
		Multimap<String, NameLink> result = HashMultimap.create();
		return result;
	}

	public static Multimap<String, NameLink> calculate(WPackage p) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (WImport imp : p.getImports()) {
			WPackage importedPackage = imp.attrImportedPackage();
			result.putAll(importedPackage.attrExportedTypeNameLinks());
		}
		return result;
	}
	
	public static Multimap<String, NameLink> calculate(WEntities wEntities) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (WEntity e : wEntities) {
			if (e instanceof TypeDef) {
				TypeDef n = (TypeDef) e;
				result.put(n.getName(), n.createNameLink(wEntities));
			}
		}
		return result;
	}

	public static Multimap<String, NameLink> calculate(WurstModel model) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (CompilationUnit cu : model) {
			result.putAll(cu.attrTypeNameLinks());
		}
		return result;
	}
	
	public static Multimap<String, NameLink> calculate(WStatements statements) {
		Multimap<String, NameLink> result = HashMultimap.create();
		return result;
	}
	
	private static void addTypeParametersIfAny(Multimap<String, NameLink> result, WScope c) {
		if (c instanceof AstElementWithTypeParameters) {
			AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) c;
			for (TypeParamDef i : wtp.getTypeParameters()) {
				result.put(i.getName(), i.createNameLink(c));
			}
		}
		
	}
	
	private static void addJassTypes(Multimap<String, NameLink> result, CompilationUnit cu) {
		for (JassToplevelDeclaration jd : cu.getJassDecls()) {
			if (jd instanceof TypeDef) {
				TypeDef def = (TypeDef) jd;
				result.put(def.getName(), def.createNameLink(cu));
			}
		}
	}

	private static void addPackages(Multimap<String, NameLink> result, CompilationUnit cu) {
		for (WPackage p : cu.getPackages()) {
			result.put(p.getName(), p.createNameLink(cu));
		}
	}
}
