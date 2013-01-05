package de.peeeq.wurstscript.attributes.names;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.AstElementWithParameters;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.LoopStatementWithVarDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WEntities;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.utils.Utils;

public class NameLinks {

	public static Multimap<String, NameLink> calculate(ClassOrModuleOrModuleInstanciation c) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addNamesFromUsedModuleInstantiations(c, result);
		addDefinedNames(result, c);
		if (c instanceof ClassDef) {
			ClassDef classDef = (ClassDef) c;
			addNamesFormSuperClass(result, classDef);
			addNamesFromImplementedInterfaces(result, classDef);
		}
		return result;
	}

	public static Multimap<String, NameLink> calculate(CompilationUnit cu) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addJassNames(result, cu);
		addPackages(result, cu);
		return result;
	}
	
	public static Multimap<String, NameLink> calculate(AstElementWithBody c) {
		Multimap<String, NameLink> result = HashMultimap.create();
		WScope s = (WScope) c;
		addVarDefIfAny(result, s);
		addParametersIfAny(result, s);
		return result;
	}

	
	private static void addVarDefIfAny(Multimap<String, NameLink> result, WScope s) {
		if (s instanceof LoopStatementWithVarDef) {
			LoopStatementWithVarDef l = (LoopStatementWithVarDef) s;
			result.put(l.getLoopVar().getName(), l.getLoopVar().createNameLink(s));
		}
	}

	public static Multimap<String, NameLink> calculate(EnumDef e) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addDefinedNames(result, e, e.getMembers());
		return result;
	}

	public static Multimap<String, NameLink> calculate(InterfaceDef i) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addDefinedNames(result, i, i.getMethods());
		return result;
	}

	public static Multimap<String, NameLink> calculate(NativeFunc nativeFunc) {
		Multimap<String, NameLink> result = HashMultimap.create();
		return result;
	}

	public static Multimap<String, NameLink> calculate(TupleDef t) {
		Multimap<String, NameLink> result = HashMultimap.create();
		addDefinedNames(result, t, t.getParameters());
		return result;
	}

	public static Multimap<String, NameLink> calculate(WPackage p) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (WImport imp : p.getImports()) {
			WPackage importedPackage = imp.attrImportedPackage();
			if (importedPackage == null) {
				System.out.println("could not resolve import: " + Utils.printElementWithSource(imp)); 
				continue;
			}
			if (p.getName().equals("WurstREPL")) {
				// the REPL is special and can use all names
				result.putAll(importedPackage.getElements().attrNameLinks());
				result.putAll(importedPackage.attrNameLinks());
			} else {
				// normal packages can only use the exported names of a package
				result.putAll(importedPackage.attrExportedNameLinks());
			}
		}
		
		return result;
	}

	
	
	public static Multimap<String, NameLink> calculate(WEntities wEntities) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (WEntity e : wEntities) {
			if (e instanceof NameDef) {
				NameDef n = (NameDef) e;
				result.put(n.getName(), n.createNameLink(wEntities));
			}
			if (e instanceof WScope) {
				WScope scope = (WScope) e;
				addHidingPrivate(result, scope.attrNameLinks());
			}
		}
		return result;
	}

	public static Multimap<String, NameLink> calculate(WurstModel model) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (CompilationUnit cu : model) {
			result.putAll(cu.attrNameLinks());
		}
		return result;
	}
	
	public static Multimap<String, NameLink> calculate(WStatements statements) {
		Multimap<String, NameLink> result = HashMultimap.create();
		for (WStatement s : statements) {
			if (s instanceof LocalVarDef) {
				LocalVarDef var = (LocalVarDef) s;
				result.put(var.getName(), var.createNameLink(statements));
			}
		}
		return result;
	}
	
	private static void addParametersIfAny(Multimap<String, NameLink> result, WScope s) {
		if (s instanceof AstElementWithParameters) {
			AstElementWithParameters withParams = (AstElementWithParameters) s;
			for (WParameter p : withParams.getParameters()) {
				result.put(p.getName(), p.createNameLink(s));
			}
		}
		
	}

	private static void addPackages(Multimap<String, NameLink> result, CompilationUnit cu) {
		for (WPackage p : cu.getPackages()) {
			result.put(p.getName(), p.createNameLink(cu));
		}
	}

	private static void addJassNames(Multimap<String, NameLink> result, CompilationUnit cu) {
		for (JassToplevelDeclaration jd : cu.getJassDecls()) {
			if (jd instanceof NameDef) {
				NameDef def = (NameDef) jd;
				result.put(def.getName(), def.createNameLink(cu));
			} else if (jd instanceof JassGlobalBlock) {
				JassGlobalBlock jassGlobalBlock = (JassGlobalBlock) jd;
				addDefinedNames(result, cu, jassGlobalBlock);
			}
		}
	}

	private static void addNamesFromImplementedInterfaces(Multimap<String, NameLink> result, ClassDef classDef) {
		// TODO type param mapping
		for (WurstTypeInterface interfaceType : classDef.attrImplementedInterfaces()) {
			Map<TypeParamDef, WurstType> binding = interfaceType.getTypeArgBinding();
			InterfaceDef i = interfaceType.getInterfaceDef();
			for (Entry<String, NameLink> e : i.attrNameLinks().entries()) {
				result.put(e.getKey(), e.getValue().withTypeArgBinding(binding));
			}
		}
	}

	private static void addNamesFormSuperClass(Multimap<String, NameLink> result, ClassDef classDef) {
		// TODO type param mapping
		if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
			WurstTypeClass wurstTypeClass = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
			ClassDef extendedClass = wurstTypeClass.getClassDef();
			addHidingPrivate(result, extendedClass.attrNameLinks());
		}
	}

	private static void addNamesFromUsedModuleInstantiations(ClassOrModuleOrModuleInstanciation c,
			Multimap<String, NameLink> result) {
		for (ModuleInstanciation m : c.attrModuleInstanciations()) {
			addHidingPrivate(result, m.attrNameLinks());
		}
	}

	private static void addDefinedNames(Multimap<String, NameLink> result, ClassOrModuleOrModuleInstanciation c) {
		addDefinedNames(result, c, c.getMethods());
		addDefinedNames(result, c, c.getVars());
		addDefinedNames(result, c, c.getModuleInstanciations());
	}

	private static void addDefinedNames(Multimap<String, NameLink> result, WScope definedIn, List<? extends NameDef> slots) {
		for (NameDef n : slots) {
			result.put(n.getName(), n.createNameLink(definedIn));
		}
	}


	public static void addHidingPrivate(Multimap<String, NameLink> r, Multimap<String, NameLink> adding) {
		for (Entry<String, NameLink> e : adding.entries()) {
			if (e.getValue().getVisibility() == Visibility.LOCAL) {
				continue;
			}
			r.put(e.getKey(), e.getValue().hidingPrivate());
		}

	}

	public static void addHidingPrivateAndProtected(Multimap<String, NameLink> r, Multimap<String, NameLink> adding) {
		for (Entry<String, NameLink> e : adding.entries()) {
			if (e.getValue().getVisibility() == Visibility.LOCAL) {
				continue;
			}
			r.put(e.getKey(), e.getValue().hidingPrivateAndProtected());
		}
		
	}

	

	


}
