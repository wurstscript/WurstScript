package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.OpBinary;
import de.peeeq.wurstscript.ast.OpUnary;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.ast.WStatement;
import de.peeeq.wurstscript.ast.WStatements;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;

public class Translator {

	public ImFunction getFuncFor(OpBinary op) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getFuncFor(FunctionDefinition funcDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImVar getThisVar(WScope attrNearestScope) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImVar getVarFor(VarDef varDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public int getTupleIndex(VarDef varDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getConstructorFuncFor(ConstructorDef constructorFunc) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getFuncFor(OpUnary opU) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImFunction getDestroyFuncFor(ClassDef classDef) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public ImVar getNewTempVar() {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public List<ImStmt> translateStatements(ImFunction f, WStatements statements) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}


}
