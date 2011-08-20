package de.peeeq.wurstscript.attributes;

import java.util.Map;

import katja.common.NE;
import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ExprMemberArrayVarPos;
import de.peeeq.wurstscript.ast.ExprMemberVarPos;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.ExprVarAccessPos;
import de.peeeq.wurstscript.ast.ExprVarArrayAccessPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.VarRefPos;
import de.peeeq.wurstscript.ast.WScopePos;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrVarDef extends Attribute<VarRefPos, VarDefPos> {

	public AttrVarDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected VarDefPos calculate(final VarRefPos node) {
		final String varName = node.varName().term();
		VarDefPos result = node.Switch(new VarRefPos.Switch<VarDefPos, NE>() {
			private VarDefPos defaultCase() {
				WScopePos scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Map<String, VarDefPos> vars = attr.varScope.get(scope);
					if (vars.containsKey(varName)) {
						return vars.get(varName);
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			private VarDefPos memberVarCase(ExprPos left) {
				PscriptType leftType = attr.exprType.get(left);
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDefPos classDef = leftTypeC.getClassDef();
					Map<String, VarDefPos> classDefScope = attr.varScope.get(classDef);
					return classDefScope.get(varName);
				} else {
					attr.addError(node.source(), "Cannot acces attribute " + varName + " because " + leftType + " is not a class-type.");
				}
				return null;
			}

			@Override
			public VarDefPos CaseExprVarArrayAccessPos(
					ExprVarArrayAccessPos term) throws NE {
				return defaultCase();
			}

			@Override
			public VarDefPos CaseExprVarAccessPos(ExprVarAccessPos term)
					throws NE {
				return defaultCase();
			}

			@Override
			public VarDefPos CaseExprMemberVarPos(ExprMemberVarPos term)
					throws NE {
				return memberVarCase(term.left());
			}

			

			@Override
			public VarDefPos CaseExprMemberArrayVarPos(
					ExprMemberArrayVarPos term) throws NE {
				return memberVarCase(term.left());
			}
		
		});
		
		if (result == null) {
			attr.addError(node.source(), "Could not resolve reference to variable " + varName);
		}
		return result;
	}

}
