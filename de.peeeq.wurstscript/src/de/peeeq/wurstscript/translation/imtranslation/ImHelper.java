package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.jassIm.ImNoExpr;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSetArray;
import de.peeeq.wurstscript.jassIm.ImSetArrayTuple;
import de.peeeq.wurstscript.jassIm.ImSetTuple;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;

public class ImHelper {

	static void translateParameters(WParameters params, ImVars result, ImTranslator t) {
		for (WParameter p : params) {
			result.add(t.getVarFor(p));
		}
	}

//	static ImVar translateParam(WParameter p) {
//		return tra
//		return JassIm.ImVar(p.attrTyp().imTranslateType(), p.getName());
//	}

	public static ImType toArray(ImType t) {
		if (t instanceof ImSimpleType) {
			ImSimpleType imSimpleType = (ImSimpleType) t;
			return JassIm.ImArrayType(imSimpleType.getTypename());
		} else if (t instanceof ImTupleType) {
			ImTupleType imTupleType = (ImTupleType) t;
			ImType result = JassIm.ImTupleArrayType(imTupleType.getTypes(), imTupleType.getNames());
			return result;
		}
		throw new Error("Can't make array type from " + t);
	}

	public static void replaceVar(List<ImStmt> stmts, final ImVar oldVar, final ImVar newVar) {
		for (ImStmt s : stmts) {
			replaceVar(s, oldVar, newVar);
		}
	}

	public static void replaceVar(ImStmt s, final ImVar oldVar, final ImVar newVar) {
		s.accept(new VarReplaceVisitor() {

			@Override
			ImVar getReplaceVar(ImVar v) {
				return v == oldVar ? newVar : null;
			}				
		});
	}
	
	public static void replaceVars(List<ImStmt> stmts, Map<ImVar, ImVar> substitutions) {
		for (ImStmt s : stmts) {
			replaceVar(s, substitutions);
		}
	}

	public static void replaceVar(ImStmt s, final Map<ImVar, ImVar> substitutions) {
		s.accept(new VarReplaceVisitor() {
			@Override
			ImVar getReplaceVar(ImVar v) {
				return substitutions.get(v);
			}				
		});
	}
	
	abstract static class VarReplaceVisitor extends ImStmt.DefaultVisitor {
		
		abstract ImVar getReplaceVar(ImVar v);

		@Override
		public void visit(ImSetTuple e) {
			ImVar newVar = getReplaceVar(e.getLeft());
			if (newVar != null) {
				e.setLeft(newVar);
			}
		}

		@Override
		public void visit(ImSetArray e) {
			ImVar newVar = getReplaceVar(e.getLeft());
			if (newVar != null) {
				e.setLeft(newVar);
			}
		}

		@Override
		public void visit(ImSetArrayTuple e) {
			ImVar newVar = getReplaceVar(e.getLeft());
			if (newVar != null) {
				e.setLeft(newVar);
			}
			
		}

		@Override
		public void visit(ImVars imVars) {
			// TODO ?
		}


		@Override
		public void visit(ImVarArrayAccess e) {
			ImVar newVar = getReplaceVar(e.getVar());
			if (newVar != null) {
				e.setVar(newVar);
			}
			
		}


		@Override
		public void visit(ImVarAccess e) {
			ImVar newVar = getReplaceVar(e.getVar());
			if (newVar != null) {
				e.setVar(newVar);
			}
		}


		@Override
		public void visit(ImSet e) {
			ImVar newVar = getReplaceVar(e.getLeft());
			if (newVar != null) {
				e.setLeft(newVar);
			}
			
		}

		@Override
		public void visit(ImStringVal imStringVal) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void visit(ImNoExpr imNoExpr) {
			// TODO Auto-generated method stub
			
		}

		

		
	}
}
