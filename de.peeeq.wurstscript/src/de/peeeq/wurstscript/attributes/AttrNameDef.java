package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.types.WurstTypeTuple;

/**
 * this attribute find the variable definition for every variable reference
 * 
 */
public class AttrNameDef {

	public static NameDef calculate(final NameRef node) {
		final String varName = node.getVarName();
		// check if this a read or write access:
		final boolean writeAccess = isWriteAccess(node);

		NameDef result = node.match(new NameRef.Matcher<NameDef>() {

			@Override
			public NameDef case_ExprVarArrayAccess(ExprVarArrayAccess term) {
				return searchNameInScope(varName, node);
			}

			@Override
			public NameDef case_ExprVarAccess(ExprVarAccess term) {
				return searchNameInScope(varName, node);
			}

			@Override
			public NameDef case_ExprMemberVar(ExprMemberVar term) {
				return memberVarCase(term.getLeft(), varName, writeAccess, term);
			}

			@Override
			public NameDef case_ExprMemberArrayVar(ExprMemberArrayVar term) {
				return memberVarCase(term.getLeft(), varName, writeAccess, term);
			}

		});

		if (result == null) {
			node.addError("Could not resolve reference to variable " + varName);
		}
		return result;
	}

	protected static NameDef searchNameInScope(String varName, NameRef node) {
		return NameResolution.searchTypedNameGetOne(NameDef.class, varName, node, true);
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
		
		WurstType leftType = left.attrTyp();
		if (leftType instanceof WurstTypeNamedScope) {
			WurstTypeNamedScope ns = (WurstTypeNamedScope) leftType;
			List<NameDef> names = NameResolution.searchTypedName(NameDef.class, varName, ns.getDef(), true);
			if (names.size() == 0) {
				node.addError("Variable " + varName + " not found.");
				return null;
			} else {
				return names.get(0);
			}
		} else if (leftType instanceof WurstTypeTuple) {
			WurstTypeTuple tupleType = (WurstTypeTuple) leftType;
			for (WParameter p : tupleType.getTupleDef().getParameters()) {
				if (p.getName().equals(varName)) {
					return p;
				}
			}
			node.addError("Variable " + varName + " not found.");
			return null;
		} else {
			node.addError("Cannot acces attribute " + varName + " because " + leftType
			+ " is not a class-type.");
			return null;
		}

	}

	
}
