package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeModule;

/**
 * this attribute find the variable definition for every variable reference
 * 
 */
public class AttrNameDef {


	public static NameDef calculate(ExprVarArrayAccess term) {
		return searchNameInScope(term.getVarName(), term);
	}

	public static NameDef calculate(ExprVarAccess term) {
		return searchNameInScope(term.getVarName(), term);
	}

	public static NameDef calculate(ExprMemberVar term) {
		return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
	}

	public static NameDef calculate(ExprMemberArrayVar term) {
		return memberVarCase(term.getLeft(), term.getVarName(), isWriteAccess(term), term);
	}



	protected static NameDef searchNameInScope(String varName, NameRef node) {
		boolean showErrors = !varName.startsWith("gg_");
		NameDef result = node.lookupVar(varName, showErrors);
		return result;
	}

	private static boolean isWriteAccess(final NameRef node) {
		boolean writeAccess1 = false;
		if (node.getParent() instanceof StmtSet) {
			StmtSet stmtSet = (StmtSet) node.getParent();
			if (stmtSet.getUpdatedExpr() == node) {
				writeAccess1 = true;
			}
		}
		final boolean writeAccess = writeAccess1;
		return writeAccess;
	}

	private static NameDef memberVarCase(Expr left, String varName, boolean writeAccess, Expr node) {
		WurstType receiverType = left.attrTyp();
		NameDef result = node.lookupMemberVar(receiverType, varName);
		if (result == null) {
			node.addError("Could not resolve reference to variable " + varName + " for receiver of type " + 
					receiverType + ".");
		}
		if (receiverType instanceof WurstTypeModule) {
			WurstTypeModule wurstTypeModule = (WurstTypeModule) receiverType;
			ClassOrModule nearestStructure = left.attrNearestClassOrModule();
			ModuleDef module = wurstTypeModule.getDef();
			if (nearestStructure != module) {
				node.addError("Can only reference module variables from within the module.");
			}
		}
			
		
		return result;
	}


}
