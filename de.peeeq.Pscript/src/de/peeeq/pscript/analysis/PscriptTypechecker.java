//package de.peeeq.pscript.analysis;
//
//import java.util.Hashtable;
//import java.util.Map;
//
//import org.eclipse.emf.ecore.EObject;
//
//import de.peeeq.pscript.pscript.ExprAdditive;
//import de.peeeq.pscript.pscript.ExprFunctioncall;
//import de.peeeq.pscript.pscript.ExprIdentifier;
//import de.peeeq.pscript.pscript.ExprIntVal;
//import de.peeeq.pscript.pscript.NameDef;
//import de.peeeq.pscript.pscript.ParameterDef;
//import de.peeeq.pscript.pscript.PscriptPackage;
//import de.peeeq.pscript.pscript.VarDef;
//import de.peeeq.pscript.pscript.util.PscriptSwitch;
//import de.peeeq.pscript.types.PScriptTypeInt;
//import de.peeeq.pscript.types.PScriptTypeUnknown;
//import de.peeeq.pscript.validation.PscriptJavaValidator;
//
//public class PscriptTypechecker implements ITypechecker {
//
//	
//	Map<EObject, Definition> calculatedTypes = new Hashtable<EObject, Definition>();
//	
//	
//	private boolean done(EObject e) {
//		return calculatedTypes.containsKey(e);
//	}
//	
//	private Definition getType(EObject e) {
//		return calculatedTypes.get(e);
//	}
//	
//	private class Calc extends PscriptSwitch<Definition> {
//
//		private PscriptJavaValidator validator;
//
//		public Calc(PscriptJavaValidator validator) {
//			this.validator = validator;
//		}
//		
//		@Override
//		public Definition caseExprAdditive(ExprAdditive e) {
//			if (done(e)) {
//				return getType(e);
//			}
//			Definition leftType = doSwitch(e.getLeft());
//			Definition rightType = doSwitch(e.getRight());
//			
//			if (leftType instanceof PScriptTypeInt && rightType instanceof PScriptTypeInt) {
//				validator.acceptWarning("compatible types " + leftType + " and " + rightType, e, PscriptPackage.EXPR_ADDITIVE, "");
//				return PScriptTypeInt.instance();
//			} else {
//				validator.acceptError("incompatible types " + leftType + " and " + rightType, e, PscriptPackage.EXPR_ADDITIVE, "");
//			}
//			return PScriptTypeUnknown.instance();
//						
//		}
//		
//		@Override
//		public Definition caseExprIntVal(ExprIntVal e) {
//			return PScriptTypeInt.instance();
//		}
//		
//		
//
//		@Override
//		public Definition caseExprIdentifier(ExprIdentifier e) {
//			if (done(e)) {
//				return getType(e);
//			}
//			
//			NameDef x = e.getNameVal();
//			if (x instanceof VarDef) {
//				VarDef v = (VarDef) x;
//				// TODO eleganter
//				if (v.getType().equals("Int")) {
//					return PScriptTypeInt.instance();
//				} else {
//					return new PScriptTypeUnknown(v.getType().toString());
//				}
//			} else if (x instanceof ParameterDef) {
//				ParameterDef v = (ParameterDef) x;
//				v.eNotify(null);
//				// TODO eleganter
//				if (v.getType().equals("Int")) {
//					return PScriptTypeInt.instance();
//				} else {
//					return new PScriptTypeUnknown(v.getType().toString());
//				}
//			} else {
//				return new PScriptTypeUnknown("No Var " + x.getClass());
//			}
////			return PScriptTypeUnknown.instance();
//		}
//		
//		@Override
//		public Definition caseExprFunctioncall(ExprFunctioncall e) {
//			return null;
//			
//		}
//		
//	}
//	
//	@Override
//	public void check(final EObject x, final PscriptJavaValidator validator) {
////		validator.acceptError("Falsch", x, 0, "a", "asdfsa");
//		new Calc(validator).doSwitch(x);
//	}
//
//}
