package de.peeeq.wurstscript.jassprinter;

import com.google.common.base.Function;

import de.peeeq.wurstscript.jassAst.JassArrayVar;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprBinary;
import de.peeeq.wurstscript.jassAst.JassExprBoolVal;
import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprIntVal;
import de.peeeq.wurstscript.jassAst.JassExprNull;
import de.peeeq.wurstscript.jassAst.JassExprRealVal;
import de.peeeq.wurstscript.jassAst.JassExprStringVal;
import de.peeeq.wurstscript.jassAst.JassExprUnary;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassOp;
import de.peeeq.wurstscript.jassAst.JassOpAnd;
import de.peeeq.wurstscript.jassAst.JassOpDiv;
import de.peeeq.wurstscript.jassAst.JassOpEquals;
import de.peeeq.wurstscript.jassAst.JassOpGreater;
import de.peeeq.wurstscript.jassAst.JassOpGreaterEq;
import de.peeeq.wurstscript.jassAst.JassOpLess;
import de.peeeq.wurstscript.jassAst.JassOpLessEq;
import de.peeeq.wurstscript.jassAst.JassOpMinus;
import de.peeeq.wurstscript.jassAst.JassOpMult;
import de.peeeq.wurstscript.jassAst.JassOpNot;
import de.peeeq.wurstscript.jassAst.JassOpOr;
import de.peeeq.wurstscript.jassAst.JassOpPlus;
import de.peeeq.wurstscript.jassAst.JassOpUnequals;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtExitwhen;
import de.peeeq.wurstscript.jassAst.JassStmtIf;
import de.peeeq.wurstscript.jassAst.JassStmtLoop;
import de.peeeq.wurstscript.jassAst.JassStmtReturn;
import de.peeeq.wurstscript.jassAst.JassStmtReturnVoid;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.utils.Utils;

public class JassPrinter {

	private boolean withSpace;


	public JassPrinter(boolean withSpace) {
		this.withSpace = withSpace;
	}
	
	public void printProg(StringBuilder sb, JassProg prog) {
		printGlobals(sb, prog.getGlobals());
		printFunctions(sb, prog.getFunctions());
	}
	
	private String additionalNewline() {
		return withSpace ? "\n" : "";
	}

	private void printIndent(StringBuilder sb, int i) {
		if (withSpace) {
			Utils.printIndent(sb, i);
		}
	}

	private String comma() {
		return withSpace ? ", " : ",";
	}
	
	private Object assign() {
		return withSpace ? " = " : "=";
	}


	private void printGlobals(StringBuilder sb, JassVars globals) {
		sb.append("globals\n");
		for (JassVar g : globals) {
			printJassGlobalVar(sb, g);
		}
		sb.append("endglobals\n");
	}

	private void printJassGlobalVar(final StringBuilder sb, JassVar g) {
		g.match(new JassVar.MatcherVoid() {
			
			@Override
			public void case_JassSimpleVar(JassSimpleVar v) {
				sb.append(v.getType() + " " + v.getName() + "\n");
			}
			
			@Override
			public void case_JassArrayVar(JassArrayVar v) {
				sb.append(v.getType() + " array " + v.getName() + "\n");
			}
		});
	}
	

	private void printFunctions(StringBuilder sb, JassFunctions functions) {
		for (JassFunction f : functions) {
			printFunction(sb, f);
		}
	}


	private void printFunction(StringBuilder sb, JassFunction f) {
		sb.append("function ");
		sb.append(f.getName());
		sb.append(" takes ");
		if (f.getParams().size() == 0) {
			sb.append("nothing");
		} else {
				Utils.printSep(sb, comma(), f.getParams(), new Function<JassSimpleVar, String>() {

					@Override
					public String apply(JassSimpleVar input) {
						return input.getType() + " " + input.getName();
					}
				});				
		}
		sb.append(" returns ");
		sb.append(f.getReturnType());
		sb.append("\n");
		for (JassVar v : f.getLocals()) {
			printIndent(sb, 1);
			sb.append("local ");
			sb.append(v.getType());
			if (v instanceof JassArrayVar) {
				sb.append(" array");
			}
			sb.append(" ");
			sb.append(v.getName());
			sb.append("\n");
		}
		printStatements(sb, 1, f.getBody());
		sb.append("endfunction\n" + additionalNewline());
	}


	

	private void printStatements(StringBuilder sb, int indent, JassStatements statements) {
		for (JassStatement s : statements) {
			printStatement(sb, indent, s);
		}
	}


	private void printStatement(final StringBuilder sb, final int indent, JassStatement s) {
		printIndent(sb, indent);
		s.match(new JassStatement.MatcherVoid() {
			
			@Override
			public void case_JassStmtSetArray(JassStmtSetArray s) {
				sb.append("set ");
				sb.append(s.getLeft());
				sb.append("[");
				printExpr(sb, s.getIndex());
				sb.append("]");
				sb.append(assign());
				
				printExpr(sb, s.getRight());
			}
			
			

			@Override
			public void case_JassStmtSet(JassStmtSet s) {
				sb.append("set ");
				sb.append(s.getLeft());
				sb.append(assign());
				printExpr(sb, s.getRight());
			}
			
			@Override
			public void case_JassStmtReturnVoid(JassStmtReturnVoid s) {
				sb.append("return");
			}
			
			@Override
			public void case_JassStmtReturn(JassStmtReturn s) {
				sb.append("return ");
				printExpr(sb, s.getReturnValue());
			}
			
			@Override
			public void case_JassStmtLoop(JassStmtLoop s) {
				sb.append("loop\n");
				printStatements(sb, indent+1, s.getBody());
				printIndent(sb, indent);
				sb.append("endloop");
			}
			
			@Override
			public void case_JassStmtIf(JassStmtIf s) {
				sb.append("if ");
				printExpr(sb, s.getCond());
				sb.append(" then\n");
				printStatements(sb, indent+1, s.getThenBlock());
				if (s.getElseBlock().size() > 0) {
					printIndent(sb, indent);
					sb.append("else\n");
					printStatements(sb, indent+1, s.getElseBlock());
				}
				printIndent(sb, indent);
				sb.append("endif");
			}
			
			@Override
			public void case_JassStmtExitwhen(JassStmtExitwhen s) {
				sb.append("exitwhen ");
				printExpr(sb, s.getCond());
			}
			
			@Override
			public void case_JassStmtCall(JassStmtCall s) {
				sb.append("call ");
				sb.append(s.getFunctionName());
				sb.append("(");
				boolean first = true;
				for (JassExpr e : s.getArguments()) {
					if (!first) {
						sb.append(comma());
					}
					printExpr(sb, e);
					first = false;
				}
				sb.append(")");
			}
		});
		sb.append("\n");
	}


	protected void printExpr(final StringBuilder sb, JassExpr expr) {
		expr.match(new JassExpr.MatcherVoid() {
			
			@Override
			public void case_JassExprVarArrayAccess(JassExprVarArrayAccess e) {
				sb.append(e.getVarName());
				sb.append("[");
				printExpr(sb, e.getIndex());
				sb.append("]");
			}
			
			@Override
			public void case_JassExprVarAccess(JassExprVarAccess e) {
				sb.append(e.getVarName());
			}
			
			@Override
			public void case_JassExprUnary(JassExprUnary e) {
				boolean useParantheses = e.getRight() instanceof JassExprBinary;
				printOp(sb, e.getOp(), false, useParantheses);
				sb.append(useParantheses ? "(" : "");
				printExpr(sb, e.getRight());
				sb.append(useParantheses ? ")" : "");
			}
			
			@Override
			public void case_JassExprStringVal(JassExprStringVal e) {
				sb.append("\"");
				sb.append(e.getVal());
				sb.append("\"");
			}
			
			@Override
			public void case_JassExprRealVal(JassExprRealVal e) {
				sb.append(e.getVal());
			}
			
			@Override
			public void case_JassExprNull(JassExprNull e) {
				sb.append("null");
			}
			
			@Override
			public void case_JassExprIntVal(JassExprIntVal e) {
				sb.append(e.getVal());
			}
			
			@Override
			public void case_JassExprFunctionCall(JassExprFunctionCall e) {
				sb.append(e.getFuncName());
				sb.append("(");
				boolean first = true;
				for (JassExpr a : e.getArguments()) {
					if (!first) {
						sb.append(comma());
					}
					printExpr(sb, a);
					first = false;
				}
				sb.append(")");
			}
			
			@Override
			public void case_JassExprFuncRef(JassExprFuncRef e) {
				sb.append("function ");
				sb.append(e.getFuncName());
			}
			
			@Override
			public void case_JassExprBoolVal(JassExprBoolVal e) {
				sb.append(e.getVal() ? "true" : "false");
			}
			
			@Override
			public void case_JassExprBinary(JassExprBinary e) {
				boolean useParanthesesLeft = false;
				boolean useParanthesesRight = false;
				if (e.getLeft() instanceof JassExprBinary) {
					JassExprBinary left = (JassExprBinary) e.getLeft();
					if (precedence(left.getOp()) < precedence(e.getOp())) {
						// if the precedence level on the left is _smaller_ we have to use parentheses
						useParanthesesLeft = true;
					} 
					// if the precedence level is equal we can assume left associativity of all operators
					// so they are treated correctly
				}
				if (e.getRight() instanceof JassExprBinary) {
					JassExprBinary right = (JassExprBinary) e.getRight();
					if (precedence(right.getOp()) < precedence(e.getOp())) {
						// if the precedence level on the right is smaller we have to use parentheses
						useParanthesesLeft = true;
					} else if (precedence(right.getOp()) == precedence(e.getOp())) {
						// if the precedence level is equal we have to parentheses as operators are
						// left associative but for commutative operators (+, *, and, or) we do not
						// need parentheses
						
						if (! ((right.getOp() instanceof JassOpPlus && e.getOp() instanceof JassOpPlus) 
							|| (right.getOp() instanceof JassOpMult && e.getOp() instanceof JassOpMult) 
							|| (right.getOp() instanceof JassOpOr   && e.getOp() instanceof JassOpOr) 
							|| (right.getOp() instanceof JassOpAnd  && e.getOp() instanceof JassOpAnd))) {
							// in other cases use parentheses
							// for example 
							useParanthesesRight = true;
						}
					}
				}
				sb.append(useParanthesesLeft ? "(" : "");
				printExpr(sb, e.getLeft());
				sb.append(useParanthesesLeft ? ")" : "");
				printOp(sb, e.getOp(),useParanthesesLeft, useParanthesesRight);		
				sb.append(useParanthesesRight ? "(" : "");
				printExpr(sb, e.getRight());
				sb.append(useParanthesesRight ? ")" : "");
			}
		});
	}


	protected static int precedence(JassOp op) {
		// 5 not
		// 4 * /
		// 3 + -
		// 2 <= >= > < == !=
		// 1 and 
		// 0 or
		return op.match(new JassOp.Matcher<Integer>() {

			@Override
			public Integer case_JassOpDiv(JassOpDiv jassOpDiv) {
				return 4;
			}

			@Override
			public Integer case_JassOpLess(JassOpLess jassOpLess) {
				return 2;
			}

			@Override
			public Integer case_JassOpAnd(JassOpAnd jassOpAnd) {
				return 1;
			}

			@Override
			public Integer case_JassOpUnequals(JassOpUnequals jassOpUnequals) {
				return 2;
			}

			@Override
			public Integer case_JassOpGreaterEq(JassOpGreaterEq jassOpGreaterEq) {
				return 2;
			}

			@Override
			public Integer case_JassOpMinus(JassOpMinus jassOpMinus) {
				return 3;
			}

			@Override
			public Integer case_JassOpMult(JassOpMult jassOpMult) {
				return 4;
			}

			@Override
			public Integer case_JassOpGreater(JassOpGreater jassOpGreater) {
				return 2;
			}

			@Override
			public Integer case_JassOpPlus(JassOpPlus jassOpPlus) {
				return 3;
			}

			@Override
			public Integer case_JassOpLessEq(JassOpLessEq jassOpLessEq) {
				return 2;
			}

			@Override
			public Integer case_JassOpOr(JassOpOr jassOpOr) {
				return 0;
			}

			@Override
			public Integer case_JassOpEquals(JassOpEquals jassOpEquals) {
				return 2;
			}

			@Override
			public Integer case_JassOpNot(JassOpNot jassOpNot) {
				return 5;
			}
		});
	}


	protected void printOp(final StringBuilder sb, JassOp op, boolean parLeft, boolean parRight) {
		String opString = op.match(new JassOp.Matcher<String>() {

			@Override
			public String case_JassOpUnequals(JassOpUnequals jassOpUnequals) {
				return "!=";
			}
			
			@Override
			public String case_JassOpPlus(JassOpPlus jassOpPlus) {
				return "+";
			}
			
			@Override
			public String case_JassOpOr(JassOpOr jassOpOr) {
				return "or";
			}
			
			@Override
			public String case_JassOpNot(JassOpNot jassOpNot) {
				return "not";
			}
			
			@Override
			public String case_JassOpMult(JassOpMult jassOpMult) {
				return "*";
			}
			
			@Override
			public String case_JassOpMinus(JassOpMinus jassOpMinus) {
				return "-";
			}
			
			@Override
			public String case_JassOpLessEq(JassOpLessEq jassOpLessEq) {
				return "<=";
			}
			
			@Override
			public String case_JassOpLess(JassOpLess jassOpLess) {
				return "<";
			}
			
			@Override
			public String case_JassOpGreaterEq(JassOpGreaterEq jassOpGreaterEq) {
				return ">=";
			}
			
			@Override
			public String case_JassOpGreater(JassOpGreater jassOpGreater) {
				return ">";
			}
			
			@Override
			public String case_JassOpEquals(JassOpEquals jassOpEquals) {
				return "==";
			}
			
			@Override
			public String case_JassOpDiv(JassOpDiv jassOpDiv) {
				return "/";
			}
			
			@Override
			public String case_JassOpAnd(JassOpAnd jassOpAnd) {
				return "and";
			}
		});
		if (withSpace || !parLeft && Character.isLetter(opString.charAt(0))) {
			// if we have no parantheses on the left and an operator like and/or we need a space:
			sb.append(" ");
		}
		sb.append(opString);
		if (withSpace || !parRight && Character.isLetter(opString.charAt(0))) {
			// if we have no parantheses on the right and an operator like and/or we need a space:
			sb.append(" ");
		}
	}


	public CharSequence printProg(JassProg prog) {
		StringBuilder sb = new StringBuilder();
		printProg(sb, prog);
		return sb.toString();
	}
	
}
