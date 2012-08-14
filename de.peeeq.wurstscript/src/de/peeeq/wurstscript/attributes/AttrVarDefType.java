package de.peeeq.wurstscript.attributes;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.EnumMember;
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
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.WurstTypeEnum;
import de.peeeq.wurstscript.types.WurstTypePackage;
import de.peeeq.wurstscript.types.WurstNativeType;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeModule;
import de.peeeq.wurstscript.types.WurstTypeModuleInstanciation;
import de.peeeq.wurstscript.types.WurstTypeTuple;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;


/**
 * this attribute can give you the type of a variable definition
 *
 */
public class AttrVarDefType {
	
	public static  WurstType calculate(GlobalVarDef node) {
		return defaultCase(node.getOptTyp(), node.getInitialExpr());
	}
	
	public static  WurstType calculate(LocalVarDef node) {
		return defaultCase(node.getOptTyp(), node.getInitialExpr());
	}
	
	public static  WurstType calculate(WParameter node) {
		return node.getTyp().attrTyp();
	}
	
	public static WurstType calculate(ClassDef c) {
		List<WurstType> typeArgs = Lists.newArrayList();
		for (TypeParamDef tp : c.getTypeParameters()) {
			typeArgs.add(new WurstTypeTypeParam(tp));
		}
		WurstTypeClass t = new WurstTypeClass(c, typeArgs, true);
		return t;
	}
	
	private static WurstType defaultCase(OptTypeExpr typ,
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

	public static WurstType calculate(ModuleDef moduleDef) {
		return new WurstTypeModule(moduleDef, true);
	}

	public static WurstType calculate(ModuleInstanciation m) {
		return new WurstTypeModuleInstanciation(m, true);
	}

	public static WurstType calculate(NativeType n) {
		return WurstNativeType.instance(n.getName(), n.getOptTyp().attrTyp());
	}

	public static WurstType calculate(FunctionDefinition f) {
		return f.getReturnTyp().attrTyp();
	}

	public static WurstType calculate(TypeParamDef t) {
		return new WurstTypeTypeParam(t);
	}

	public static WurstType calculate(InterfaceDef i) {
		List<WurstType> typeArgs = Lists.newArrayList();
		for (TypeParamDef tp : i.getTypeParameters()) {
			typeArgs.add(tp.attrTyp());
		}
		return new WurstTypeInterface(i, typeArgs, true);
	}

	public static WurstType calculate(TupleDef t) {
		return new WurstTypeTuple(t);
	}

	public static WurstType calculate(WPackage p) {
		return new WurstTypePackage(p);
	}

	public static WurstType calculate(EnumDef enumDef) {
		return new WurstTypeEnum(true, enumDef);
	}

	public static WurstType calculate(EnumMember enumMember) {
		return new WurstTypeEnum(false, (EnumDef) enumMember.getParent().getParent());
	}
	

}
