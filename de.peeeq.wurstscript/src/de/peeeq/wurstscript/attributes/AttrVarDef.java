package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VarRef;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrVarDef extends Attribute<VarRef, VarDef> {

	public AttrVarDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected VarDef calculate(final VarRef node) {
		final String varName = node.getVarName();
		VarDef result = node.match(new VarRef.Matcher<VarDef>() {
			private VarDef defaultCase() {
				WScope scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Map<String, VarDef> vars = attr.varScope.get(scope);
					if (vars.containsKey(varName)) {
						return vars.get(varName);
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			private VarDef memberVarCase(Expr left) {
				PscriptType leftType = attr.exprType.get(left);
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDef classDef = leftTypeC.getClassDef();
					Map<String, VarDef> classDefScope = attr.varScope.get(classDef);
					return classDefScope.get(varName);
				} else {
					attr.addError(node.getSource(), "Cannot acces attribute " + varName + " because " + leftType + " is not a class-type.");
				}
				return null;
			}

			@Override
			public VarDef case_ExprVarArrayAccess(
					ExprVarArrayAccess term)  {
				return defaultCase();
			}

			@Override
			public VarDef case_ExprVarAccess(ExprVarAccess term)
					 {
				return defaultCase();
			}

			@Override
			public VarDef case_ExprMemberVar(ExprMemberVar term)
					 {
				return memberVarCase(term.getLeft());
			}

			

			@Override
			public VarDef case_ExprMemberArrayVar(
					ExprMemberArrayVar term)  {
				return memberVarCase(term.getLeft());
			}
		
		});
		
		if (result == null) {
			attr.addError(node.getSource(), "Could not resolve reference to variable " + varName);
		}
		return result;
	}

}
