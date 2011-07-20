package de.peeeq.pscript.backend.jass;

import java.util.List;


import de.peeeq.pscript.intermediateLang.*;
import de.peeeq.pscript.types.PscriptType;

public class JassPrinter extends PrintHelper {

	private ILprog prog;
	
	public JassPrinter(StringBuilder sb) {
		super(sb);
	}
	
	public void printProg(ILprog prog) {
		
		this.prog = prog;
		printGlobals(prog.getGlobals());
		
		// TODO topsort functions
		
		printFunctions(prog.getFunctions());
		
	}

	private void printFunctions(List<ILfunction> functions) {
		for (ILfunction f : functions) {
			printFunction(f);
		}
		
	}

	private void printFunction(ILfunction f) {
		print("function ");
		print(f.getName());
		print(" takes ");
		if (f.getParams().size() == 0) {
			print("nothing");
		} else {
			int i = 0;
			for (ILvar p : f.getParams()) {
				if (i>0) {
					print(", ");
				}
				printType(p.getType());
				print(" ");
				print(p.getName());
				i++;
			}
			
		}
		
		print(" returns ");
		printType(f.getReturnType());
		println();
		incIndent();
		// locals
		for (ILvar l : f.getLocals()) {
			printIndent();
			printType(l.getType());
			print(" ");
			println(l.getName());
		}		
		printStatements(f.getBody());		
		decIndent();
		println("endfunction");
		
	}

	private void printStatements(List<ILStatement> statements) {
		for (ILStatement s : statements) {
			printStatement(s);
		}
	}

	private void printStatement(ILStatement s) {
		printIndent();
		s.doSwitchVoid(new ILStatementSwitchVoid() {

			@Override
			public void match(ILfunctionCall f) {
				if (f.getResultVar() == null) {
					print("call ");
				} else {
					print("set ");
					print(f.getResultVar().getName());
					print(" = ");
				}
				print(f.getName());
				print("(");
				int i = 0;
				for (ILvar arg : f.getArgs()) {
					if (i>0) {
						print(", ");
					}
					print(arg.getName());
				}
				print(")");
			}

			@Override
			public void match(IlsetConst c) {
				print("set ");
				print(c.getResultVar().getName());
				print(" = ");
				printConst(c.getConstant());		
				
			}

			@Override
			public void match(ILsetBinary s) {
				print("set ");
				print(s.getResultVar().getName());
				print(" = ");
				print(s.getLeft().getName());
				print(" ");
				printOp(s.getOp());
				print(" ");
				print(s.getRight().getName());
				// TODO modulo extra behandeln
			}

			@Override
			public void match(IlbuildinFunctionCall f) {
				if (f.getResultVar() == null) {
					print("call ");
				} else {
					print("set ");
					print(f.getResultVar().getName());
					print(" = ");
				}
				print(f.getFuncName());
				print("(");
				int i = 0;
				for (ILvar arg : f.getArgs()) {
					if (i>0) {
						print(", ");
					}
					print(arg.getName());
				}
				print(")");
				
			}

			@Override
			public void match(ILsetVar s) {
				print("set ");
				print(s.getResultVar().getName());
				print(" = ");
				print(s.getVar().getName());
			}

			@Override
			public void match(ILexitwhen s) {
				print("exitwhen ");
				print(s.getVar().getName());
				
			}

			@Override
			public void match(ILif s) {
				print("if ");
				print(s.getCond().getName());
				println(" then");
				incIndent();
				printStatements(s.getThenBlock());
				decIndent();
				if (s.getElseBlock().size() > 0) {
					printIndent();
					println("else");
					incIndent();
					printStatements(s.getElseBlock());
					decIndent();
				}
				printIndent();
				print("endif");
			}

			@Override
			public void match(ILloop s) {
				println("loop");
				incIndent();
				printStatements(s.getLoopBody());
				decIndent();
				printIndent();
				print("endloop");
			}

			@Override
			public void match(ILreturn s) {
				print("return ");
				if (s.getVar() != null) {
					print(s.getVar().getName());
				}
			}

			@Override
			public void match(IlsetUnary s) {
				print("set ");
				print(s.getResultVar().getName());
				print(" = ");
				printOp(s.getOp());
				print(" ");
				print(s.getRight().getName());
			}
			
		});		
		println();
	}

	protected void printOp(Iloperator op) {
		if (op == Iloperator.OR) {
			print("or");
		}
		else if (op == Iloperator.AND) {
			print("and");
		}
		else if (op == Iloperator.NOT) {
			print("not");
		}
		else if (op == Iloperator.PLUS) {
			print("+");
		}
		else if (op == Iloperator.MINUS) {
			print("-");
		}
		else if (op == Iloperator.MULT) {
			print("*");
		}
		else if (op == Iloperator.DIV_INT) {
			print("/");
		}
		else if (op == Iloperator.DIV_REAL) {
			print("/");
		}
		else if (op == Iloperator.MOD_INT) {
			throw new Error("modulo not supported in jass");
		}
		else if (op == Iloperator.MOD_REAL) {
			throw new Error("modulo not supported in jass");
		}
		else if (op == Iloperator.EQUALITY) {
			print("==");
		}
		else if (op == Iloperator.UNEQUALITY) {
			print("!=");
		}
		else if (op == Iloperator.LESS) {
			print("<");
		}
		else if (op == Iloperator.LESS_EQ) {
			print("<=");
		}
		else if (op == Iloperator.GREATER) {
			print(">");
		}
		else if (op == Iloperator.GREATER_EQ) {
			print(">=");
		} else {
			throw new Error("Operator did not match.");
		}
	}

	protected void printConst(ILconst c) {
		print(c.print());
	}

	private void printGlobals(List<ILvar> globals) {
		println("globals");
		incIndent();
		for (ILvar v : globals) {
			printGlobal(v);
		}		
		decIndent();
		println("endglobals");
	}

	
	private void printGlobal(ILvar v) {
		printIndent();
		printType(v.getType());
		print(" ");
		print(v.getName());
	}

	private void printType(PscriptType type) {
//		prog.lookupNativeTranslation(type.getName()); TODO
		
	}
}
