package de.peeeq.wurstscript.parser;

import java.util.List;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassToplevelDeclaration;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprList;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CompileError;

public class AstHelper {

	public static TypeParamDefs transformTypeExprListToTyppeParamDefs(TypeExprList p) {
		TypeParamDefs result = Ast.TypeParamDefs();
		for (TypeExpr t : p) {
			if (t instanceof TypeExprSimple) {
				TypeExprSimple ts = (TypeExprSimple) t;
				result.add(Ast.TypeParamDef(t.getSource().copy(), Ast.Modifiers(), ts.getTypeName()));
			} else {
				t.addError("Type Parameters must be simple names.");
			}
		}
		return result;
	}

	public static TypeExprList makeTypeArgs(TypeParamDefs typeParams) {
		TypeExprList result = Ast.TypeExprList();
		for (TypeParamDef tp : typeParams) {
			result.add(Ast.TypeExprSimple(tp.getSource().copy(), tp.getName(), Ast.TypeExprList()));
		}
		return result;
	}

	public static ClassDef ClassDef(WPos pos, Modifiers mod, String name, TypeParamDefs typeParams, OptTypeExpr extendedClass, TypeExprList il,
			List<ClassSlot> slots) {
		ClassDef c = Ast.ClassDef(pos, mod, name, typeParams, extendedClass, il, Ast.FuncDefs(), Ast.GlobalVarDefs(), 
				Ast.ConstructorDefs(), Ast.ModuleInstanciations(), Ast.ModuleUses(), Ast.OnDestroyDef(pos.copy(), Ast.WStatements()));
		addClassSlots(slots, c);		
		return c;
	}

	private static void addClassSlots(List<? extends ClassSlot> slots, StructureDef c) throws CompileError {
		for (ClassSlot s : slots) {
			if (s instanceof FuncDef) {
				c.getMethods().add((FuncDef) s);
			} else if (s instanceof GlobalVarDef) {
				c.getVars().add((GlobalVarDef) s);
			} else if (s instanceof ConstructorDef) {
				c.getConstructors().add((ConstructorDef) s);
			} else if (s instanceof ModuleUse) {
				c.getModuleUses().add((ModuleUse) s);
			} else if (s instanceof OnDestroyDef) {
				OnDestroyDef odf = (OnDestroyDef) s;
				c.setOnDestroy(odf);
			} else {
				throw new CompileError(s.getSource(), "Unhandled case for classSlot: " + s.getClass());
			}
		}
	}

	public static ModuleDef ModuleDef(WPos pos, Modifiers mod, String name, TypeParamDefs typeParams, List<ClassSlot> slots) {
		ModuleDef m = Ast.ModuleDef(pos, mod, name, typeParams, Ast.FuncDefs(), Ast.GlobalVarDefs(), 
				Ast.ConstructorDefs(), Ast.ModuleInstanciations(), Ast.ModuleUses(), Ast.OnDestroyDef(pos.copy(), Ast.WStatements()));
		addClassSlots(slots, m);		
		return m;
	}

	public static InterfaceDef InterfaceDef(WPos pos, Modifiers mod, String name, TypeParamDefs typeParams, TypeExprList el,
			List<ClassSlot> slots) {
		InterfaceDef i = Ast.InterfaceDef(pos, mod, name, typeParams, el, Ast.FuncDefs(), Ast.GlobalVarDefs(), 
				Ast.ConstructorDefs(), Ast.ModuleInstanciations(), Ast.ModuleUses(), Ast.OnDestroyDef(pos.copy(), Ast.WStatements()));
		addClassSlots(slots, i);		
		return i;
	}

	public static Arguments inferSuperArgs(WParameters params) {
		Arguments result = Ast.Arguments();
		for (WParameter p : params) {
			result.add(Ast.ExprVarAccess(p.getSource().copy(), p.getName()));
		}
		return result;
	}

	public static CompilationUnit addFront(CompilationUnit c,	TopLevelDeclaration p) {
		// TODO would it be important to add this to the front?
		if (p instanceof WPackage) {
			c.getPackages().add((WPackage) p);
		} else if (p instanceof JassToplevelDeclaration) {
			c.getJassDecls().add((JassToplevelDeclaration) p);
		} else {
			throw new Error("unhandled type: " + p.getClass());
		}
		return c;
	}


}
