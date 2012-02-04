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
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.types.PscriptTypeTuple;

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
			attr.addError(node.getSource(), "Could not resolve reference to variable " + varName);
		}
		return result;
	}

	protected static NameDef searchNameInScope(String varName, NameRef node) {
		return NameResolution.searchTypedNameGetOne(NameDef.class, varName, node);
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
		
		PscriptType leftType = left.attrTyp();
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
			List<NameDef> names = NameResolution.searchTypedName(NameDef.class, varName, ns.getDef());
			if (names.size() == 0) {
				attr.addError(node.getSource(), "Variable " + varName + " not found.");
				return null;
			} else {
				return names.get(0);
			}
		} else if (leftType instanceof PscriptTypeTuple) {
			PscriptTypeTuple tupleType = (PscriptTypeTuple) leftType;
			for (WParameter p : tupleType.getTupleDef().getParameters()) {
				if (p.getName().equals(varName)) {
					return p;
				}
			}
			attr.addError(node.getSource(), "Variable " + varName + " not found.");
			return null;
		} else {
			attr.addError(node.getSource(), "Cannot acces attribute " + varName + " because " + leftType
					+ " is not a class-type.");
			return null;
		}

	}

	
}
