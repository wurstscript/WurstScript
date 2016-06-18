package de.peeeq.wurstscript.attributes;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.EnumMember;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarDot;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarDotDot;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.utils.Utils;

public class SmallHelpers {

	public static boolean hasEmptyBody(FunctionLike f) {
		return f.getBody().size() <= 2;
	}

	public static @Nullable StmtReturn getReturnStatement(ExprStatementsBlock e) {
		if (e.getBody().size()<=2) {
			return null;
		}
		WStatement lastStatement = e.getBody().get(e.getBody().size()-2);
		if (lastStatement instanceof StmtReturn) {
			return (StmtReturn) lastStatement;
		}
		return null;
	}

	public static boolean isModuleUseTypeArg(TypeExpr e) {
		Optional<ModuleUse> mUse = Utils.getNearestByType(e, ModuleUse.class);
		if (mUse.isPresent()) {
			return mUse.get().isSubtreeOf(e);
		}
		return false;
	}

	public static boolean isSubtreeOf(@Nullable AstElement subtree, AstElement of) {
		while (subtree!= null) {
			if (subtree == of) return true;
			subtree = subtree.getParent();
		}
		return false;
	}

	public static boolean isStructureDefTypeParam(TypeParamDef tp) {
		Optional<StructureDef> sDef = Utils.getNearestByType(tp, StructureDef.class);
		if (!sDef.isPresent()) {
			return false;
		}
		if (sDef.get() instanceof AstElementWithTypeParameters) {
			return tp.isSubtreeOf(((AstElementWithTypeParameters) sDef.get()).getTypeParameters());
		}
		return false;
	}

	public static String getName(NameDef nd) {
		return nd.getNameId().getName();
	}


	public static String getVarName(ExprMemberArrayVar e) {
		return e.getVarNameId().getName();
	}
	
	public static String getVarName(ExprMemberVar e) {
		return e.getVarNameId().getName();
	}
	
	
	public static String getVarName(ExprMemberMethod e) {
		return e.getFuncNameId().getName();
	}

	public static String getVarName(NameRef e) {
		return e.getVarNameId().getName();
	}

	public static String getFuncName(FuncRef e) {
		return e.getFuncNameId().getName();
	}

	public static String getPackagename(WImport i) {
		return i.getPackagenameId().getName();
	}

	public static String getTypeName(ExprNewObject e) {
		return e.getTypeNameId().getName();
	}

	public static String getModuleName(ModuleUse m) {
		return m.getModuleNameId().getName();
	}

}
