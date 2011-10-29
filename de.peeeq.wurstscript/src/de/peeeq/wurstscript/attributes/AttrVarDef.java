package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VarRef;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrVarDef {
	
	public static VarDef calculate(final VarRef node) {
		final String varName = node.getVarName();
		
		// check if this a read access:
		boolean writeAccess1 = false;
		if (node.getParent() instanceof StmtSet) {
			StmtSet stmtSet = (StmtSet) node.getParent();
			if (stmtSet.getLeft() == node) {
				writeAccess1 = true;
			}
		}
		final boolean writeAccess = writeAccess1;
		
		VarDef result = node.match(new VarRef.Matcher<VarDef>() {
			private VarDef defaultCase() {
				WScope scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Map<String, VarDef> vars = scope.attrScopeVariables();
					if (vars.containsKey(varName)) {
						return vars.get(varName);
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			private VarDef memberVarCase(Expr left) {
				PscriptType leftType = left.attrTyp();
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDef classDef = leftTypeC.getClassDef();
					Map<String, VarDef> classDefScope;
					if (classDef == left.attrNearestClassDef()) {
						// same class
						classDefScope = classDef.attrScopeVariables();
					} else if (classDef.attrNearestPackage() == left.attrNearestPackage()) {
						// same package
						classDefScope = classDef.attrScopePackageVariables();
					} else {
						// different package
						if (writeAccess) {
							classDefScope = classDef.attrScopePublicVariables();
						} else {
							classDefScope = classDef.attrScopePublicReadVariables();
						}
					}
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
