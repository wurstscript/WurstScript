package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;

import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
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
	
	public static ImmutableMultimap<String, NameLink> calculate(ClassOrModuleOrModuleInstanciation c) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		addTypeParametersIfAny(result, c);
		for (ClassDef innerClass : c.getInnerClasses()) {
			result.put(innerClass.getName(), NameLink.create(innerClass, c));
		}
		WScope nextScope = c.attrNextScope();
		if (nextScope != null) {
			result.put(c.getName(), c.createNameLink(nextScope));
		}
		return result.build();
	}

	public static ImmutableMultimap<String, NameLink> calculate(CompilationUnit cu) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		addJassTypes(result, cu);
		addPackages(result, cu);
		return result.build();
	}
	
	public static ImmutableMultimap<String, NameLink> calculate(AstElementWithBody c) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		WScope s = (WScope) c;
		addTypeParametersIfAny(result, s);
		return result.build();
	}

	
	public static ImmutableMultimap<String, NameLink> calculate(EnumDef e) {
		return ImmutableMultimap.of();
	}

	public static ImmutableMultimap<String, NameLink> calculate(InterfaceDef i) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		addTypeParametersIfAny(result, i);
		return result.build();
	}

	public static ImmutableMultimap<String, NameLink> calculate(NativeFunc nativeFunc) {
		return ImmutableMultimap.of();
	}

	public static ImmutableMultimap<String, NameLink> calculate(TupleDef t) {
		return ImmutableMultimap.of();
	}

	public static ImmutableMultimap<String, NameLink> calculate(WPackage p) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		for (WImport imp : p.getImports()) {
			WPackage importedPackage = imp.attrImportedPackage();
			if (importedPackage == null) {
				continue;
			}
			result.putAll(importedPackage.attrExportedTypeNameLinks());
		}
		return result.build();
	}
	
	public static ImmutableMultimap<String, NameLink> calculate(WEntities wEntities) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		for (WEntity e : wEntities) {
			if (e instanceof TypeDef) {
				TypeDef n = (TypeDef) e;
				result.put(n.getName(), n.createNameLink(wEntities));
			}
		}
		return result.build();
	}

	public static ImmutableMultimap<String, NameLink> calculate(WurstModel model) {
		ImmutableMultimap.Builder<String, NameLink> result = ImmutableSetMultimap.builder();
		for (CompilationUnit cu : model) {
			result.putAll(cu.attrTypeNameLinks());
		}
		return result.build();
	}
	
	public static ImmutableMultimap<String, NameLink> calculate(WStatements statements) {
		return ImmutableMultimap.of();
	}
	
	private static void addTypeParametersIfAny(ImmutableMultimap.Builder<String, NameLink> result, WScope c) {
		if (c instanceof AstElementWithTypeParameters) {
			AstElementWithTypeParameters wtp = (AstElementWithTypeParameters) c;
			for (TypeParamDef i : wtp.getTypeParameters()) {
				result.put(i.getName(), i.createNameLink(c));
			}
		}
		
	}
	
	private static void addJassTypes(ImmutableMultimap.Builder<String, NameLink> result, CompilationUnit cu) {
		for (JassToplevelDeclaration jd : cu.getJassDecls()) {
			if (jd instanceof TypeDef) {
				TypeDef def = (TypeDef) jd;
				result.put(def.getName(), def.createNameLink(cu));
			}
		}
	}

	private static void addPackages(ImmutableMultimap.Builder<String, NameLink> result, CompilationUnit cu) {
		for (WPackage p : cu.getPackages()) {
			result.put(p.getName(), p.createNameLink(cu));
		}
	}

	public static ImmutableMultimap<String, NameLink> calculate(ExprClosure exprClosure) {
		return ImmutableMultimap.of();
	}
}
