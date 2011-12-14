package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.PscriptTypeModuleInstanciation;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrNameDef {
	
	public static NameDef calculate(final NameRef node) {
		final String varName = node.getVarName();
		
		// check if this a read access:
		final boolean writeAccess = isWriteAccess(node);
		
		NameDef result = node.match(new NameRef.Matcher<NameDef>() {
			private NameDef defaultCase() {
				WScope scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Map<String, NameDef> vars = scope.attrScopeNames();
					if (vars.containsKey(varName)) {
						return vars.get(varName);
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			private NameDef memberVarCase(Expr left) {
				PscriptType leftType = left.attrTyp();
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeNamedScope leftTypeC = (PscriptTypeNamedScope) leftType;
					return getNameInNamedScope(left, leftTypeC.getDef(), varName, writeAccess);
				} else if (leftType instanceof PscriptTypeModule) {
					PscriptTypeModule leftTypeM = (PscriptTypeModule) leftType;
					return getNameInNamedScope(left, leftTypeM.getDef(), varName, writeAccess);
				} else if (leftType instanceof PscriptTypeModuleInstanciation) {
					PscriptTypeModuleInstanciation leftTypeM = (PscriptTypeModuleInstanciation) leftType;
					NamedScope m = leftTypeM.getDef();
					return m.attrScopeNames().get(varName);
				} else {
					attr.addError(node.getSource(), "Cannot acces attribute " + varName + " because " + leftType + " is not a class-type.");
					return null;
				}
				
			}

			private NameDef getNameInNamedScope(Expr access, NamedScope scope, final String varName, final boolean writeAccess) {
				Map<String, NameDef> classDefScope;
				if (scope == access.attrNearestClassDef()) {
					// same class
					classDefScope = scope.attrScopeNames();
				} else if (scope.attrNearestPackage() == access.attrNearestPackage()) {
					// same package
					classDefScope = scope.attrScopePackageNames();
				} else {
					// different package
					if (writeAccess) {
						classDefScope = scope.attrScopePublicNames();
					} else {
						classDefScope = scope.attrScopePublicReadNamess();
					}
				}
				return classDefScope.get(varName);
			}

			@Override
			public NameDef case_ExprVarArrayAccess(
					ExprVarArrayAccess term)  {
				return defaultCase();
			}

			@Override
			public NameDef case_ExprVarAccess(ExprVarAccess term)
					 {
				return defaultCase();
			}

			@Override
			public NameDef case_ExprMemberVar(ExprMemberVar term)
					 {
				return memberVarCase(term.getLeft());
			}

			

			@Override
			public NameDef case_ExprMemberArrayVar(
					ExprMemberArrayVar term)  {
				return memberVarCase(term.getLeft());
			}
		
		});
		
		if (result == null) {
			attr.addError(node.getSource(), "Could not resolve reference to variable " + varName);
		}
		return result;
	}

	private static boolean isWriteAccess(final NameRef node) {
		boolean writeAccess1 = false;
		if (node.getParent() instanceof StmtSet) {
			StmtSet stmtSet = (StmtSet) node.getParent();
			if (stmtSet.getLeft() == node) {
				writeAccess1 = true;
			}
		}
		final boolean writeAccess = writeAccess1;
		return writeAccess;
	}

}
