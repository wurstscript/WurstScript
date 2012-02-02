package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OptExpr;
import de.peeeq.wurstscript.ast.OptTypeExpr;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.PscriptNativeType;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeInterface;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;


/**
 * this attribute can give you the type of a variable definition
 *
 */
public class AttrVarDefType {
	
	public static  PscriptType calculate(GlobalVarDef node) {
		return defaultCase(node.getOptTyp(), node.getInitialExpr());
	}
	
	public static  PscriptType calculate(LocalVarDef node) {
		return defaultCase(node.getOptTyp(), node.getInitialExpr());
	}
	
	public static  PscriptType calculate(WParameter node) {
		return node.getTyp().attrTyp();
	}
	
	public static PscriptType calculate(ClassDef c) {
		List<PscriptType> typeArgs = Lists.newArrayList();
		for (TypeParamDef tp : c.getTypeParameters()) {
			typeArgs.add(new PscriptTypeTypeParam(tp));
		}
		PscriptTypeClass t = new PscriptTypeClass(c, typeArgs, true);
		return t;
	}
	
	private static PscriptType defaultCase(OptTypeExpr typ,
			final OptExpr initialExpr) {
		if (typ instanceof TypeExpr) {
			return typ.attrTyp().dynamic();
		} else {
			if (initialExpr instanceof Expr) {
				return ((Expr) initialExpr).attrTyp();
			} else {
				throw new Error("Vardef must either have a type or an initial value");
			}
		}
	}

	public static PscriptType calculate(ModuleDef moduleDef) {
		return new PscriptTypeModule(moduleDef, true);
	}

	public static PscriptType calculate(ModuleInstanciation m) {
		return new PscriptTypeModuleInstanciation(m, true);
	}

	public static PscriptType calculate(NativeType n) {
		return PscriptNativeType.instance(n.getName(), n.getOptTyp().attrTyp());
	}

	public static PscriptType calculate(FunctionDefinition f) {
		return f.getReturnTyp().attrTyp();
	}

	public static PscriptType calculate(TypeParamDef t) {
		return new PscriptTypeTypeParam(t);
	}

	public static PscriptType calculate(InterfaceDef i) {
		List<PscriptType> typeArgs = Lists.newArrayList();
		for (TypeParamDef tp : i.getTypeParameters()) {
			typeArgs.add(tp.attrTyp());
		}
		return new PscriptTypeInterface(i, typeArgs, true);
	}

	
	

}
