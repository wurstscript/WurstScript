package de.peeeq.wurstscript.galaxy.printer;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstio.gui.About;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyArrayVar;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyAstElement;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyConstantVar;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyFunction;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyFunctions;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyInitializedVar;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyNative;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyNatives;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOp;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpAnd;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpDiv;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpEquals;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpGreater;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpGreaterEq;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpLess;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpLessEq;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpMinus;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpMult;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpNot;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpOr;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpPlus;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyOpUnequals;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyProg;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxySimpleVar;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStatement;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyStmtSet;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyTypeDef;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyTypeDefs;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyVar;
import de.peeeq.wurstscript.galaxy.galaxyAst.GalaxyVars;
import de.peeeq.wurstscript.utils.Utils;

public class GalaxyPrinter {

	public static final String WURST_COMMENT = "// this script was compiled with wurst " + About.version;
	private boolean withSpace;
	private GalaxyProg prog;
   

	public GalaxyPrinter(boolean withSpace) {
		this.withSpace = withSpace;
	}
	
	public void printProg(StringBuilder sb, GalaxyProg prog) {
		Preconditions.checkNotNull(sb);
		Preconditions.checkNotNull(prog);
		this.prog = prog;
		
		sb.append(WURST_COMMENT + "\n");
		printTypes(sb, prog.getDefs());
		printGlobals(sb, prog.getGlobals());
		printNatives(sb, prog.getNatives());
		printFunctions(sb, prog.getFunctions());
	}
	
	private String additionalNewline() {
		return withSpace ? "\n" : "";
	}

	static void printIndent(StringBuilder sb, int i, boolean withSpace) {
		if (withSpace) {
			Utils.printIndent(sb, i);
		}
	}

	static String comma(boolean withSpace) {
		return withSpace ? ", " : ",";
	}
	
	static String assign(boolean withSpace) {
		return withSpace ? " = " : "=";
	}


	private void printGlobals(StringBuilder sb, GalaxyVars globals) {
		for (GalaxyVar g : globals) {
			printGalaxyGlobalVar(sb, g);
		}
	}
	
	private void printTypes(StringBuilder sb, GalaxyTypeDefs defs) {
		for (GalaxyTypeDef d : defs) {
			printTypeDef(d, sb, false);
		}
	}
	
	private void printNatives(StringBuilder sb, GalaxyNatives natives) {
		for (GalaxyNative n : natives) {
			printNative(n, sb, false);
		}
	}

	private void printGalaxyGlobalVar(final StringBuilder sb, GalaxyVar g) {
		if (prog.attrIgnoredVariables().contains(g)) {
			return;
		}
		g.match(new GalaxyVar.MatcherVoid() {
			
			@Override
			public void case_GalaxySimpleVar(GalaxySimpleVar v) {
				sb.append(v.getType() + " " + v.getName() + ";\n");
			}
			
			@Override
			public void case_GalaxyArrayVar(GalaxyArrayVar v) {
				// TODO array sizes
				sb.append(v.getType() + "[8192]" + v.getName() + ";\n");
			}

			@Override
			public void case_GalaxyInitializedVar(GalaxyInitializedVar jassInitializedVar) {
				sb.append(jassInitializedVar.getType() + " " + jassInitializedVar.getName() + "=");
				 jassInitializedVar.getVal().print(sb, withSpace);
				 sb.append(";\n");
				// TODO check if right
			}

			@Override
			public void case_GalaxyConstantVar(GalaxyConstantVar jassConstantVar) {
				sb.append(jassConstantVar.getType() + " " + jassConstantVar.getName() + "=" );
				jassConstantVar.getVal().print(sb, withSpace); 
				 sb.append(";\n");
				// TODO check if right
			}
		});
	}
	

	private void printFunctions(StringBuilder sb, GalaxyFunctions functions) {
		for (GalaxyFunction f : functions) {
			printFunction(sb, f);
		}
	}


	private void printFunction(StringBuilder sb, GalaxyFunction f) {
		if (prog.attrIgnoredFunctions().contains(f)) {
			return;
		}
		printComment(sb, f, 0);
		sb.append(f.getReturnType());
		sb.append(" ");
		sb.append(f.getName());
		sb.append("(");
		Utils.printSep(sb, comma(withSpace), f.getParams(), new Function<GalaxySimpleVar, String>() {

			@Override
			public String apply(GalaxySimpleVar input) {
				return input.getType() + " " + input.getName();
			}
		});				
		sb.append(" {\n");
		
		List<GalaxyStatement> body = Lists.newLinkedList(f.getBody());
		Set<GalaxyVar> locals = Sets.newLinkedHashSet(); 
		
		
		// first print all the initalized vars:
		for (GalaxyVar v : f.getLocals()) {
			if (v instanceof GalaxyInitializedVar) {
				printIndent(sb, 1, withSpace);
				sb.append(v.getType());
				sb.append(" ");
				sb.append(v.getName());

				GalaxyInitializedVar ji = (GalaxyInitializedVar) v;
				sb.append(" = ");
				ji.getVal().print(sb, withSpace);
				sb.append(";\n");
			} else {
				// if var is not initialized, remember for next step:
				locals.add(v);
			}
		}
		
		
		// set statements at the beginning are merged with local variable declarations...
		while (body.size() > 0 && body.get(0) instanceof GalaxyStmtSet) {
			// get first set statement
			GalaxyStmtSet set = (GalaxyStmtSet) body.get(0);
			GalaxyVar localVar = null;
			// look if there is an uninitialized local var 
			for (GalaxyVar v : locals) {
				if (set.getLeft().equals(v.getName()))  {
					localVar = v;
					break; 
				}
			}
			if (localVar == null) {
				break;
			}
			// there is a local var ==> merge
			printIndent(sb, 1, withSpace);
			sb.append(localVar.getType());
			sb.append(" ");
			sb.append(localVar.getName());
			sb.append(" = ");
			set.getRight().print(sb, withSpace);
			sb.append(";\n");
			
			locals.remove(localVar);
			body.remove(0);
		}
		
		// print the remaining locals
		for (GalaxyVar v : locals) {
			printIndent(sb, 1, withSpace);
			sb.append(v.getType());
			if (v instanceof GalaxyArrayVar) {
				sb.append("[8092]"); // TODO array size
			}
			sb.append(" ");
			sb.append(v.getName());
			sb.append(";\n");
		}
		printStatements(sb, 1, body, withSpace);
		sb.append("}\n" + additionalNewline());
	}

	private void printComment(StringBuilder sb, GalaxyAstElement f, int indent) {
		if (withSpace) {
			if (prog.attrComments().containsKey(f)) {
				printIndent(sb, indent, withSpace);
				sb.append("// " + prog.attrComments().get(f) + "\n");
			}
		}
	}


	

	static void printStatements(StringBuilder sb, int indent, List<GalaxyStatement> statements, boolean withSpace) {
		for (GalaxyStatement s : statements) {
			printIndent(sb, indent, withSpace);
			s.print(sb, indent, withSpace);
			sb.append(";\n");
		}
	}


	protected static int precedence(GalaxyOp op) {
		// 5 not
		// 4 * /
		// 3 + -
		// 2 <= >= > < == !=
		// 1 and 
		// 0 or
		return op.match(new GalaxyOp.Matcher<Integer>() {

			@Override
			public Integer case_GalaxyOpDiv(GalaxyOpDiv jassOpDiv) {
				return 4;
			}

			@Override
			public Integer case_GalaxyOpLess(GalaxyOpLess jassOpLess) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpAnd(GalaxyOpAnd jassOpAnd) {
				return 1;
			}

			@Override
			public Integer case_GalaxyOpUnequals(GalaxyOpUnequals jassOpUnequals) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpGreaterEq(GalaxyOpGreaterEq jassOpGreaterEq) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpMinus(GalaxyOpMinus jassOpMinus) {
				return 3;
			}

			@Override
			public Integer case_GalaxyOpMult(GalaxyOpMult jassOpMult) {
				return 4;
			}

			@Override
			public Integer case_GalaxyOpGreater(GalaxyOpGreater jassOpGreater) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpPlus(GalaxyOpPlus jassOpPlus) {
				return 3;
			}

			@Override
			public Integer case_GalaxyOpLessEq(GalaxyOpLessEq jassOpLessEq) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpOr(GalaxyOpOr jassOpOr) {
				return 0;
			}

			@Override
			public Integer case_GalaxyOpEquals(GalaxyOpEquals jassOpEquals) {
				return 2;
			}

			@Override
			public Integer case_GalaxyOpNot(GalaxyOpNot jassOpNot) {
				return 5;
			}
		});
	}


	protected void printOp(final StringBuilder sb, GalaxyOp op, boolean parLeft, boolean parRight) {
		String opString = op.match(new GalaxyOp.Matcher<String>() {

			@Override
			public String case_GalaxyOpUnequals(GalaxyOpUnequals jassOpUnequals) {
				return "!=";
			}
			
			@Override
			public String case_GalaxyOpPlus(GalaxyOpPlus jassOpPlus) {
				return "+";
			}
			
			@Override
			public String case_GalaxyOpOr(GalaxyOpOr jassOpOr) {
				return "||";
			}
			
			@Override
			public String case_GalaxyOpNot(GalaxyOpNot jassOpNot) {
				return "!";
			}
			
			@Override
			public String case_GalaxyOpMult(GalaxyOpMult jassOpMult) {
				return "*";
			}
			
			@Override
			public String case_GalaxyOpMinus(GalaxyOpMinus jassOpMinus) {
				return "-";
			}
			
			@Override
			public String case_GalaxyOpLessEq(GalaxyOpLessEq jassOpLessEq) {
				return "<=";
			}
			
			@Override
			public String case_GalaxyOpLess(GalaxyOpLess jassOpLess) {
				return "<";
			}
			
			@Override
			public String case_GalaxyOpGreaterEq(GalaxyOpGreaterEq jassOpGreaterEq) {
				return ">=";
			}
			
			@Override
			public String case_GalaxyOpGreater(GalaxyOpGreater jassOpGreater) {
				return ">";
			}
			
			@Override
			public String case_GalaxyOpEquals(GalaxyOpEquals jassOpEquals) {
				return "==";
			}
			
			@Override
			public String case_GalaxyOpDiv(GalaxyOpDiv jassOpDiv) {
				return "/";
			}
			
			@Override
			public String case_GalaxyOpAnd(GalaxyOpAnd jassOpAnd) {
				return "&&";
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


	public String printProg(GalaxyProg prog) {
		Preconditions.checkNotNull(prog);
		StringBuilder sb = new StringBuilder();
		printProg(sb, prog);
		return sb.toString();
	}

	public static void printTypeDef(GalaxyTypeDef jassTypeDef,
			StringBuilder sb, boolean withSpace2) {
		// TODO types
		sb.append("type ");
		sb.append(jassTypeDef.getName());
		sb.append(" extends ");
		sb.append(jassTypeDef.getExt());
		sb.append("\n");
	}

	public static void printNative(GalaxyNative jassNative,
			StringBuilder sb, boolean withSpace) {
		// TODO natives
		sb.append("native ");
		sb.append(jassNative.getName());
		sb.append(" takes ");
		if (jassNative.getParams().size() == 0) {
			sb.append("nothing");
		} else {
				Utils.printSep(sb, comma(withSpace), jassNative.getParams(), new Function<GalaxySimpleVar, String>() {

					@Override
					public String apply(GalaxySimpleVar input) {
						return input.getType() + " " + input.getName();
					}
				});				
		}
		sb.append(" returns ");
		sb.append(jassNative.getReturnType());
		sb.append("\n");
		
	}
	
}
