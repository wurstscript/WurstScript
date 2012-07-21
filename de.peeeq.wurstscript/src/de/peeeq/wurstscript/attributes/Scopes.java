package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.AstElementWithParameters;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.StructureDefOrModuleInstanciation;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.ast.WurstModel;

public class Scopes {

	


	public static Multimap<String, NameDef> getDefinedNames(AstElementWithBody c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		addTypeParametersIfAny(result, c);
		addParametersIfAny(c, result);
		addDefinedNamesInStatements(result, c.getBody());
		checkForDuplicates(result);
		return result;
	}


	private static void addParametersIfAny(AstElementWithBody c, Multimap<String, NameDef> result) {
		// add parameters:
		if (c instanceof AstElementWithParameters) {
			AstElementWithParameters wp = (AstElementWithParameters) c;
			for (WParameter p : wp.getParameters()) {
				result.put(p.getName(), p);
			}
		}
	}


	private static void addTypeParametersIfAny(Multimap<String, NameDef> result, AstElement c) {
		if (c instanceof AstElementWithTypeParameters){
			AstElementWithTypeParameters ewt = (AstElementWithTypeParameters) c;
			TypeParamDefs ts = ewt.getTypeParameters();
			for (TypeParamDef t : ts) {
				result.put(t.getName(), t);
			}
		}
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
		for (JassToplevelDeclaration e : c.getJassDecls()) {
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
		checkForDuplicates(result);
		return result;
	}

	public static Multimap<String, NameDef> getDefinedNames(StructureDefOrModuleInstanciation c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		addTypeParametersIfAny(result, c);
		addDefinedNames(result, c);
		checkForDuplicates(result);
		return result;
	}


	private static void addDefinedNames(Multimap<String, NameDef> result, StructureDefOrModuleInstanciation c) {
		addDefinedNames(result, c.getMethods());
		addDefinedNames(result, c.getVars());
		addDefinedNames(result, c.getModuleInstanciations());
	}
	
	private static Multimap<String, NameDef> addDefinedNames(Multimap<String, NameDef> result , List<? extends NameDef> slots) {
		for (NameDef n : slots) {
			result.put(n.getName(), n);
//			if (n instanceof StructureDefOrModuleInstanciation) {
//				StructureDefOrModuleInstanciation s = (StructureDefOrModuleInstanciation) n;
//				addDefinedNames(result, s);
//			}
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
		checkForDuplicates(result);
		return result;
	}

	private static void checkForDuplicates(Multimap<String, NameDef> definitions) {
		for (String name : definitions.keySet()) {
			Collection<NameDef> defs = definitions.get(name);
			if (defs.size() > 1) {
				int varCount = 0,
					typeCount = 0,
					funcCount = 0;
				for (NameDef d : defs) {
					if (d instanceof VarDef) {
						varCount++;
						if (varCount > 1) {
							attr.addError(d.getSource(), "Variable " + d.getName() + " is already defined.");
						}
					} else if (d instanceof NotExtensionFunction) {
						funcCount++;
						if (funcCount > 1) {
							attr.addError(d.getSource(), "Function " + d.getName() + " is already defined.");
						}
					} else if (d instanceof ModuleDef || d instanceof TypeDef) {
						typeCount++;
						if (typeCount > 1) {
							attr.addError(d.getSource(), "Type " + d.getName() + " is already defined.");
						}
					}
				}
			}
		}
		
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
			if (importedPackage != null) {
				result.putAll(importedPackage.attrExportedNames());
			}
		}
		
		// add all defined names
		result.putAll(p.attrDefinedNames());
		return result;
	}
	
	public static Multimap<String, NameDef> getVisibleNamesPrivate(WurstModel w) {
		Multimap<String, NameDef> result = HashMultimap.create();
		for (CompilationUnit cu : w) {
			result.putAll(cu.attrVisibleNamesPrivate());
		}
		return result;
	}
	
	public static Multimap<String, NameDef> getVisibleNamesPrivate(ClassOrModuleOrModuleInstanciation c) {
		Multimap<String, NameDef> result = HashMultimap.create();
		
		// add protected names from used module instanciations
		for (ModuleInstanciation m : c.attrModuleInstanciations()) {
			result.putAll(m.attrVisibleNamesProtected());
		}
		
		// add all defined names
		for (Entry<String, NameDef> e : c.attrDefinedNames().entries()) {
			String name = e.getKey();
			NameDef def = e.getValue();
			if (def instanceof FuncDef) {
				// remove the overridden functions
				FuncDef funcDef = (FuncDef) def;
				if (result.containsKey(name)) {
					Collection<NameDef> names = result.get(name);
					Set<NameDef> toRemove = Sets.newHashSet();
					for (NameDef n : names) {
						if (n instanceof FuncDef) {
							FuncDef overriddenFunc = (FuncDef) n;
							funcDef.attrOverriddenFunctions().addAll(overriddenFunc.attrOverriddenFunctions());
							funcDef.attrOverriddenFunctions().add(overriddenFunc);
							
							toRemove.add(overriddenFunc);
						}
					}
					// remove overridden functions
					names.removeAll(toRemove);
					
					if (!funcDef.attrIsOverride()) {
						attr.addError(funcDef.attrSource(), "Function " + name + " needs the 'override' modifier.");
					}
				} else {
					if (funcDef.attrIsOverride() && !(c instanceof ClassDef)) {
						attr.addError(funcDef.attrSource(), "Nothing to override for function " + name + ".");
					}
				}
			}
			result.put(name, def);
		}
		
//		// add default methods from interfaces TODO
//		if (c instanceof ClassDef) {
//			ClassDef cd = (ClassDef) c;
//			for (PscriptTypeInterface interfaceType : cd.attrImplementedInterfaces()) {
//				InterfaceDef i = interfaceType.getInterfaceDef();
//				for (FuncDef i_funcDef : i.getMethods()) {
//					String fname = i_funcDef.getName();
//					if (!result.containsKey(fname)) {
//						if (i_funcDef.getBody().size() > 0) {
//							// add default impl
//							result.put(fname, i_funcDef);
//						} else {
//							// no default impl exists --> error
//							attr.addError(cd.getSource(), "Class " + cd.getName() + " does not implement " +
//									"function " + fname + " defined in interface " + i.getName() + ".");							
//						}
//					}
//				}
//			}
//		}
		
		
		return result;
	}
	
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
		for (Entry<String, NameDef> e : p.attrDefinedNames().entries()) {
			if (e.getValue().attrIsPublic()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		for (WImport imp : p.getImports()) {
			if (imp.getIsPublic()) {
				try {
					if (imp.attrImportedPackage() == null) {
						attr.addError(imp.getSource(), "Could not find package " + imp.getPackagename());
						continue;
					}
					result.putAll(imp.attrImportedPackage().attrExportedNames());
				} catch (Error e) {
					attr.addError(imp.getSource(), e.getMessage());					
				}
			}
		}
		
		return result;
	}


	public static Multimap<String, NameDef> getDefinedNames(InterfaceDef i) {
		Multimap<String, NameDef> result = HashMultimap.create();
		addTypeParametersIfAny(result, i);
		for (FuncDef f : i.getMethods()) {
			result.put(f.getName(), f);
		}
		return result;
	}


	public static Multimap<String, NameDef> getDefinedNames(NativeFunc nativeFunc) {
		return HashMultimap.create();
	}


	public static Multimap<String, NameDef> getDefinedNames(TupleDef tupleDef) {
		return HashMultimap.create();
	}


	public static Multimap<String, NameDef> getDefinedNames(WurstModel wurstModel) {
		return HashMultimap.create();
	}


	
}
