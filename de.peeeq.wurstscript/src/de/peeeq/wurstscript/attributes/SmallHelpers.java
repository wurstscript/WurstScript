package de.peeeq.wurstscript.attributes;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.utils.Utils;

public class SmallHelpers {

	public static boolean hasEmptyBody(FunctionLike f) {
		return f.getBody().size() <= 2;
	}

	public static @Nullable StmtReturn getReturnStatement(ExprStatementsBlock e) {
		if (e.getBody().isEmpty()) {
			return null;
		}
		WStatement lastStatement = e.getBody().get(e.getBody().size()-1);
		if (lastStatement instanceof StmtReturn) {
			return (StmtReturn) lastStatement;
		}
		return null;
	}

	public static boolean isModuleUseTypeArg(TypeExpr e) {
		ModuleUse mUse = Utils.getNearestByType(e, ModuleUse.class);
		if (mUse != null) {
			return mUse.isSubtreeOf(e);
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
		StructureDef sDef = Utils.getNearestByType(tp, StructureDef.class);
		if (sDef instanceof AstElementWithTypeParameters) {
			return tp.isSubtreeOf(((AstElementWithTypeParameters) sDef).getTypeParameters());
		}
		return false;
	}

}
