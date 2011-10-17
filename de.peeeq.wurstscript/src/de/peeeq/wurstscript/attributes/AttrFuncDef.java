package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import katja.common.NE;

import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ExprFuncRefPos;
import de.peeeq.wurstscript.ast.ExprFunctionCallPos;
import de.peeeq.wurstscript.ast.ExprMemberMethodPos;
import de.peeeq.wurstscript.ast.ExprPos;
import de.peeeq.wurstscript.ast.FuncRefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.WScopePos;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;


/**
 * this attribute find the variable definition for every variable reference 
 *
 */
public class AttrFuncDef extends Attribute<FuncRefPos, FunctionDefinitionPos> {

	
	public AttrFuncDef(Attributes attr) {
		super(attr);
	}

	@Override
	protected FunctionDefinitionPos calculate(final FuncRefPos node) {
		final String funcName = node.funcName().term();
		FunctionDefinitionPos result = node.Switch(new FuncRefPos.Switch<FunctionDefinitionPos, NE>() {

			private FunctionDefinitionPos defaultCase() {
				WScopePos scope = Scoping.getNearestScope(node);
				while (scope != null) {
					Multimap<String, FunctionDefinitionPos> functions = attr.funcScope.get(scope);
					if (functions.containsKey(funcName)) {
						return selectOverloadedFunction(node, functions.get(funcName));
					}
					scope = Scoping.getNearestScope(scope);
				}
				return null;
			}
			
			
			private FunctionDefinitionPos memberCase(ExprPos left) {
				PscriptType leftType = attr.exprType.get(left);
				if (leftType instanceof PscriptTypeClass) {
					PscriptTypeClass leftTypeC = (PscriptTypeClass) leftType;
					ClassDefPos classDef = leftTypeC.getClassDef();
					Multimap<String, FunctionDefinitionPos> functions = attr.funcScope.get(classDef);
					if (functions.containsKey(funcName)) {
						return selectOverloadedFunction(node, functions.get(funcName));
					}
				}
				// TODO check extension functions
				return null;
			}
			
			@Override
			public FunctionDefinitionPos CaseExprFuncRefPos(ExprFuncRefPos term) throws NE {
				return defaultCase();
			}

			

			@Override
			public FunctionDefinitionPos CaseExprMemberMethodPos(ExprMemberMethodPos term)
					throws NE {
				return memberCase(term.left());
			}

			

			@Override
			public FunctionDefinitionPos CaseExprFunctionCallPos(ExprFunctionCallPos term)
					throws NE {
				return defaultCase();
			}
		});
		
		if (result == null) {
			attr.addError(node.source(), "Could not resolve reference to function " + funcName);
		}
		return result;
	}

	protected FunctionDefinitionPos selectOverloadedFunction(
			FuncRefPos funcCall, Collection<FunctionDefinitionPos> functions) {
		OverloadingResolver.resolveFuncCall(attr, functions, funcCall);
		// TODO overloading - select the right method
		for (FunctionDefinitionPos f : functions) {
			return f;
		}
		attr.addError(funcCall.source(), "Unknown Function " + funcCall.funcName().term() );
		return null;
	}

}
