package de.peeeq.pscript.attributes;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.pscript.attributes.infrastructure.AbstractAttribute;
import de.peeeq.pscript.attributes.infrastructure.AttributeDependencies;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprAdditive;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.ExprIntVal;
import de.peeeq.pscript.pscript.ExprMember;
import de.peeeq.pscript.pscript.ExprMult;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.NameDef;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.TypeExpr;
import de.peeeq.pscript.pscript.util.PscriptSwitch;
import de.peeeq.pscript.types.PScriptTypeInt;
import de.peeeq.pscript.types.PScriptTypeUnknown;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeError;

public class AttrExprType extends AbstractAttribute<Expr, PscriptType> {


	@Override
	public
	PscriptType calculate(final AttributeManager attributeManager,
			AttributeDependencies dependencies, Expr node) {
//		if (node instanceof ExprIntVal) {
//			return PScriptTypeInt.instance();
//		}
		//PolymorphicDispatcher<Expr> dispatcher = new PolymorphicDispatcher<Expr>();
		// TODO
		return new PscriptSwitch<PscriptType>() {
			@Override
			public PscriptType defaultCase(EObject e) {
				return new PscriptTypeError("Typing not implemented for " + e);
			}
			
			@Override
			public PscriptType caseExprIntVal(ExprIntVal e) {
				return PScriptTypeInt.instance();
			}
			
			@Override
			public PscriptType caseExprAdditive(ExprAdditive e) {
				// A) left + right
				// B) left -r right
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeInt.instance())
						&& rightType.isSubtypeOf(PScriptTypeInt.instance())) {
					return PScriptTypeInt.instance();
				} else {
					return new PscriptTypeError("incompatible types " + leftType + " and " + rightType);
				}
				
			}
			
			@Override
			public PscriptType caseExprMult(ExprMult e) {
				// A) left * right
				// B) left / right
				PscriptType leftType = attributeManager.getAttValue(AttrExprType.class, e.getLeft());
				PscriptType rightType = attributeManager.getAttValue(AttrExprType.class, e.getRight());
				if (leftType.isSubtypeOf(PScriptTypeInt.instance())
						&& rightType.isSubtypeOf(PScriptTypeInt.instance())) {
					return PScriptTypeInt.instance();
				} else {
					return new PscriptTypeError("incompatible types " + leftType + " and " + rightType);
				}
				
			}
			
			@Override
			public PscriptType caseExprFunctioncall(ExprFunctioncall e) {
				String name = e.getNameVal();
				SymbolTable symTable = attributeManager.getAttValue(AttrSymbolTable.class, e);
				ImmutableList<Declaration> declarations = symTable.lookup(name);
				if (declarations.isEmpty()) {
					String similarNames = "";
					for (Declaration d: symTable.getDeclarations()) {
						similarNames+= d.getName() + "\n";
					}					
					return new PscriptTypeError("Name not found. Some similar names: " + similarNames);
				} 
				
				final EList<Expr> params = e.getParameters().getParams();
				// step 1: collect suitable functions (compatible parameters) 
				final List<FuncDef> suitableFunctions = new LinkedList<FuncDef>();
				final StringBuilder problems = new StringBuilder(declarations + "\n\n");
				for (Declaration d : declarations) {
					d.caseFuncDef(new VoidFunction<FuncDef>() {
						
						@Override
						public void appy(FuncDef f) {

							problems.append("- " + AttrUtil.printFuncDef(f) + "\n");
							
							EList<NameDef> formalParams = f.getParameters();
							boolean ok = true;
							if (formalParams.size() != params.size()) {
								problems.append( "Wrong number of parameters.\n"); 
								ok = false;
							} else {							
								// check parameter types:
								
								for (int i=0; i<params.size(); i++) {
									TypeExpr formalTypeE = ((ParameterDef)formalParams.get(i)).getType();
									PscriptType formalType = attributeManager.getAttValue(AttrTypeExprType.class, formalTypeE);
									PscriptType actualType = attributeManager.getAttValue(AttrExprType.class, params.get(i));
									if (! AttrUtil.isSubType(actualType, formalType)) {
										ok = false;
										problems.append(actualType + " is not a subtype of " + formalType + " \n");
										break;
									}
								}
								
							}
							if (ok) {
								problems.append( "OK!\n");
								suitableFunctions.add(f);
							}
							
						}
					});
				}
				if (suitableFunctions.isEmpty()) {					
					
					return new PscriptTypeError("No suitable function found.\n\n" +
							problems);
				}
				if (suitableFunctions.size() == 1) {
					return AttrUtil.returnType(attributeManager, suitableFunctions.get(0));
				} else {
					// TODO find the function which fits best
					
					// just return the first one:
					return AttrUtil.returnType(attributeManager, suitableFunctions.get(0));
				}
								
			}
			
//			@Override
//			public PscriptType caseExprMember(ExprMember e) {
//				if (e.getRight() instanceof ExprFunctioncall) {
//					
//				}
//				return null;				
//			}
			
				
		}.doSwitch(node);
		
	}

	

}
