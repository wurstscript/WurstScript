package de.peeeq.wurstscript.intermediateLang.optimizer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImExitwhen;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImIf;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

public class SimpleRewrites {

	private final ImProg prog;
	private final ImTranslator trans;
	
	public SimpleRewrites(ImTranslator trans) {
		this.prog = trans.getImProg();
		this.trans = trans;
	}
	
	public void optimize() {
		optimizeElement(prog);
		// we need to flatten the program, because we introduced new StatementExprs 
		prog.flatten(trans);
	}

	/** Recursively optimizes the element */
	private void optimizeElement(JassImElement elem) {
		// optimize children:
		for (int i=0; i<elem.size(); i++) {
			optimizeElement(elem.get(i));
		}
		if (elem instanceof ImOperatorCall) {
			ImOperatorCall opc = (ImOperatorCall) elem;
			optimizeOpCall(opc);
		}else if (elem instanceof ImIf) {
			ImIf imIf = (ImIf) elem;
			optimizeIf(imIf);
		}else if (elem instanceof ImExitwhen) {
			ImExitwhen imExitwhen = (ImExitwhen) elem;
			optimizeExitwhen(imExitwhen);
		}
		
	}

	private void optimizeExitwhen(ImExitwhen imExitwhen) {
		ImExpr expr = imExitwhen.getCondition();
		if (expr instanceof ImBoolVal) {
			boolean b = ((ImBoolVal) expr).getValB();
			if (! b) {
				imExitwhen.replaceWith(JassIm.ImNull());
			}
		}
		
	}

	private void optimizeOpCall(ImOperatorCall opc) {
		// Binary
		if (opc.getArguments().size() > 1 ){
			ImExpr left = opc.getArguments().get(0);
			ImExpr right = opc.getArguments().get(1);
			if (left instanceof ImBoolVal && right instanceof ImBoolVal) {
				boolean b1 = ((ImBoolVal) left).getValB();
				boolean b2 = ((ImBoolVal) right).getValB();
				boolean result;
				switch (opc.getOp()) {
					case OR : result = b1 || b2; break;
					case AND : result = b1 && b2; break;
					case EQ : result = b1 == b2; break;
					case NOTEQ : result = b1 != b2; break;
					default : result = false; break;
				}
				opc.replaceWith(JassIm.ImBoolVal(result));
			}else if (left instanceof ImIntVal && right instanceof ImIntVal) {
				int i1 = ((ImIntVal) left).getValI();
				int i2 = ((ImIntVal) right).getValI();
				boolean isConditional = false;
				boolean isArithmetic = false;
				boolean result = false;
				int resultVal = 0;
				switch (opc.getOp()) {
					case GREATER : result = i1 > i2; isConditional = true; break;
					case GREATER_EQ : result = i1 >= i2; isConditional = true; break;
					case LESS : result = i1 < i2; isConditional = true; break;
					case LESS_EQ : result = i1 <= i2; isConditional = true; break;
					case EQ : result = i1 == i2; isConditional = true; break;
					case NOTEQ : result = i1 != i2; isConditional = true; break;
					case PLUS : resultVal = i1 + i2; isArithmetic = true; break;
					case MINUS : resultVal = i1 - i2; isArithmetic = true; break;
					case MULT : resultVal = i1 * i2; isArithmetic = true; break;
					case MOD_INT : if ( i2 != 0 ) {resultVal = i1 % i2; isArithmetic = true;} break;
					case MOD_REAL: 
						float f1 = i1; float f2 = i2;
						if ( f2 != 0 ) {
						float resultF = f1 % f2; opc.replaceWith(JassIm.ImRealVal(String.valueOf(resultF)));} break;
					case DIV_INT : if ( i2 != 0 ) {resultVal = i1 / i2; isArithmetic = true;} break;
					case DIV_REAL : 
						float f3 = i1; float f4 = i2;
						if ( f4 != 0 ) {
						float resultF = f3 / f4; opc.replaceWith(JassIm.ImRealVal(String.valueOf(resultF)));} break;
					default : result = false; isConditional = false; isArithmetic = false; break;
				}
				if (isConditional) {
					opc.replaceWith(JassIm.ImBoolVal(result));
				}else if (isArithmetic) {
					opc.replaceWith(JassIm.ImIntVal(resultVal));
				}
			}else if (left instanceof ImRealVal && right instanceof ImRealVal) {
				float f1 = Float.parseFloat(((ImRealVal) left).getValR());
				float f2 = Float.parseFloat(((ImRealVal) right).getValR());
				boolean isConditional = false;
				boolean isArithmetic = false;
				boolean result = false;
				float resultVal = 0;
				switch (opc.getOp()) {
					case GREATER : result = f1 > f2; isConditional = true; break;
					case GREATER_EQ : result = f1 >= f2; isConditional = true; break;
					case LESS : result = f1 < f2; isConditional = true; break;
					case LESS_EQ : result = f1 <= f2; isConditional = true; break;
					case EQ : result = f1 == f2; isConditional = true; break;
					case NOTEQ : result = f1 != f2; isConditional = true; break;
					case PLUS : resultVal = f1 + f2; isArithmetic = true; break;
					case MINUS : resultVal = f1 - f2; isArithmetic = true; break;
					case MULT : resultVal = f1 * f2; isArithmetic = true; break;
					case MOD_REAL : if ( f2 != 0 ) {resultVal = f1 % f2; isArithmetic = true;} break;
					case DIV_INT : if ( f2 != 0 ) {resultVal = f1 / f2; isArithmetic = true;} break;
					case DIV_REAL : if ( f2 != 0 ) {resultVal = f1 / f2; isArithmetic = true;} break;
					default : result = false; isConditional = false; isArithmetic = false; break;
				}
				if (isConditional) {
					opc.replaceWith(JassIm.ImBoolVal(result));
				} else if (isArithmetic) {
					// convert result to string, using 4 decimal digits
					String s = floatToStringWith4decimalDigits(resultVal);
//					String s = new BigDecimal(resultVal).toPlainString();
					// check if the string representation is exact
					if (Float.parseFloat(s) == resultVal) {
						opc.replaceWith(JassIm.ImRealVal(s));	
					}
				}
			}
		} 
		// Unary
		else {
			ImExpr expr = opc.getArguments().get(0);
			if (expr instanceof ImBoolVal) {
				boolean b1 = ((ImBoolVal) expr).getValB();
				boolean result;
				switch (opc.getOp()) {
					case NOT : result = ! b1; break;
					default : result = false; break;
				}
				opc.replaceWith(JassIm.ImBoolVal(result));
			}
			
		}
		
	}

	private String floatToStringWith4decimalDigits(float resultVal) {
		DecimalFormat format = new DecimalFormat();
		// use the localized pattern, so that it does not randomly replace a dot by comma on German PCs
		// hope this works
		format.applyLocalizedPattern("#.####");
		String s = format.format(resultVal);
		return s;
	}

	private void optimizeIf(ImIf imIf) {
		if (imIf.getCondition() instanceof ImBoolVal) {
			ImBoolVal boolVal = (ImBoolVal) imIf.getCondition();
			if (boolVal.getValB()) {
				// we have something like 'if true ...'
				// replace the if statement with the then-block
				// we have to use ImStatementExpr to get multiple statements into one statement as needed
				// for the replaceWith function
				// we need to copy the thenBlock because otherwise it would have two parents (we have not removed it from the old if-block)
				imIf.replaceWith(JassIm.ImStatementExpr(imIf.getThenBlock().copy(), JassIm.ImNull()));
			}else if ( ! imIf.getElseBlock().isEmpty()){
				imIf.replaceWith(JassIm.ImStatementExpr(imIf.getElseBlock().copy(), JassIm.ImNull()));
			}
			
		}
		
	}
	
	
}
