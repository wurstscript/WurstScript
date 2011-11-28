package de.peeeq.wurstscript.attributes;

import java.util.Map;

import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrNameDef {
	
	public static NameDef calculate(final NameRef node) {
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
				ClassOrModule classDef;
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					classDef = leftTypeC.getClassDef();
				} else if (leftType instanceof PscriptTypeModule) {
					PscriptTypeModule leftTypeM = (PscriptTypeModule) leftType;
					classDef = leftTypeM.getModuleDef();
				} else {
					attr.addError(node.getSource(), "Cannot acces attribute " + varName + " because " + leftType + " is not a class-type.");
					return null;
				}
				Map<String, NameDef> classDefScope;
				if (classDef == left.attrNearestClassDef()) {
					// same class
					classDefScope = classDef.attrScopeNames();
				} else if (classDef.attrNearestPackage() == left.attrNearestPackage()) {
					// same package
					classDefScope = classDef.attrScopePackageNames();
				} else {
					// different package
					if (writeAccess) {
						classDefScope = classDef.attrScopePublicNames();
					} else {
						classDefScope = classDef.attrScopePublicReadNamess();
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

}
