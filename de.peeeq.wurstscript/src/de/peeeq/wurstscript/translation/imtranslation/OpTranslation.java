package de.peeeq.wurstscript.translation.imtranslation;

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

public class OpTranslation {
	static public String translateOpUnary(OpNot opNot) {
		return "$not";
	}

	static public String translateOpUnary(OpMinus opMinus) {
		return "$minusUnary";
	}

	static public String translateOpBinary(OpDivInt opDivInt) {
		return "$div";
	}

	static public String translateOpBinary(OpLessEq opLessEq) {
		return "$<=";
	}

	static public String translateOpBinary(OpEquals opEquals) {
		return "$==";
	}

	static public String translateOpBinary(OpModReal opModReal) {
		return "$%";
	}

	static public String translateOpBinary(OpModInt opModInt) {
		return "$mod";
	}

	static public String translateOpBinary(OpUnequals opUnequals) {
		return "$!=";
	}

	static public String translateOpBinary(OpDivReal opDivReal) {
		return "$/";
	}

	static public String translateOpBinary(OpAnd opAnd) {
		return "$and";
	}

	static public String translateOpBinary(OpGreater opGreater) {
		return "$>";
	}

	static public String translateOpBinary(OpPlus opPlus) {
		return "$+";
	}

	static public String translateOpBinary(OpMult opMult) {
		return "$*";
	}

	static public String translateOpBinary(OpGreaterEq opGreaterEq) {
		return "$>=";
	}

	static public String translateOpBinary(OpMinus opMinus) {
		return "$-";
	}

	static public String translateOpBinary(OpLess opLess) {
		return "$<";
	}

	static public String translateOpBinary(OpOr opOr) {
		return "$or";
	}

	static public String translageAssignGetBinary(OpMinusAssign opMinusAssign) {
		return "$-";
	}

	static public String translageAssignGetBinary(OpPlusAssign opPlusAssign) {
		return "$+";
	}

	static public String translageAssignGetBinary(OpMultAssign opMultAssign) {
		return "$*";
	}

	static public String translageAssignGetBinary(OpAssign opAssign) {
		return null;
	}
}
