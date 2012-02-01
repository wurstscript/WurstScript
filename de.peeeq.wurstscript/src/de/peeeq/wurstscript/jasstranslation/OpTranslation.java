package de.peeeq.wurstscript.jasstranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassOpAnd;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpDiv;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpEquals;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreater;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpGreaterEq;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpLess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpLessEq;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMinus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpMult;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpNot;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpOr;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpPlus;
import static de.peeeq.wurstscript.jassAst.JassAst.JassOpUnequals;
import de.peeeq.wurstscript.ast.OpAnd;
import de.peeeq.wurstscript.ast.OpAssign;
import de.peeeq.wurstscript.ast.OpDivInt;
import de.peeeq.wurstscript.ast.OpDivReal;
import de.peeeq.wurstscript.ast.OpEquals;
import de.peeeq.wurstscript.ast.OpGreater;
import de.peeeq.wurstscript.ast.OpGreaterEq;
import de.peeeq.wurstscript.ast.OpLess;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.OpMinus;
import de.peeeq.wurstscript.ast.OpMinusAssign;
import de.peeeq.wurstscript.ast.OpModInt;
import de.peeeq.wurstscript.ast.OpModReal;
import de.peeeq.wurstscript.ast.OpMult;
import de.peeeq.wurstscript.ast.OpMultAssign;
import de.peeeq.wurstscript.ast.OpNot;
import de.peeeq.wurstscript.ast.OpOr;
import de.peeeq.wurstscript.ast.OpPlus;
import de.peeeq.wurstscript.ast.OpPlusAssign;
import de.peeeq.wurstscript.ast.OpUnequals;
import de.peeeq.wurstscript.jassAst.JassOpBinary;
import de.peeeq.wurstscript.jassAst.JassOpUnary;

public class OpTranslation {

	static public JassOpUnary translateOpUnary(OpNot opNot) {
		return JassOpNot();
	}

	static public JassOpUnary translateOpUnary(OpMinus opMinus) {
		return JassOpMinus();
	}

	static public JassOpBinary translateOpBinary(OpDivInt opDivInt) {
		return JassOpDiv();
	}

	static public JassOpBinary translateOpBinary(OpLessEq opLessEq) {
		return JassOpLessEq();
	}

	static public JassOpBinary translateOpBinary(OpEquals opEquals) {
		return JassOpEquals();
	}

	static public JassOpBinary translateOpBinary(OpModReal opModReal) {
		throw new Error("modulo operator cannot be translated");
	}

	static public JassOpBinary translateOpBinary(OpModInt opModInt) {
		throw new Error("modulo operator cannot be translated");
	}

	static public JassOpBinary translateOpBinary(OpUnequals opUnequals) {
		return JassOpUnequals();
	}

	static public JassOpBinary translateOpBinary(OpDivReal opDivReal) {
		return JassOpDiv();
	}

	static public JassOpBinary translateOpBinary(OpAnd opAnd) {
		return JassOpAnd();
	}

	static public JassOpBinary translateOpBinary(OpGreater opGreater) {
		return JassOpGreater();
	}

	static public JassOpBinary translateOpBinary(OpPlus opPlus) {
		return JassOpPlus();
	}

	static public JassOpBinary translateOpBinary(OpMult opMult) {
		return JassOpMult();
	}

	static public JassOpBinary translateOpBinary(OpGreaterEq opGreaterEq) {
		return JassOpGreaterEq();
	}

	static public JassOpBinary translateOpBinary(OpMinus opMinus) {
		return JassOpMinus();
	}

	static public JassOpBinary translateOpBinary(OpLess opLess) {
		return JassOpLess();
	}

	static public JassOpBinary translateOpBinary(OpOr opOr) {
		return JassOpOr();
	}

	static public JassOpBinary translageAssignGetBinary(OpMinusAssign opMinusAssign) {
		return JassOpMinus();
	}

	static public JassOpBinary translageAssignGetBinary(OpPlusAssign opPlusAssign) {
		return JassOpPlus();
	}

	static public JassOpBinary translageAssignGetBinary(OpMultAssign opMultAssign) {
		return JassOpMult();
	}

	static public JassOpBinary translageAssignGetBinary(OpAssign opAssign) {
		return null;
	}

}
