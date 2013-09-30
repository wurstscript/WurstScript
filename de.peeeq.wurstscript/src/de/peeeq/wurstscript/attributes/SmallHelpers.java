package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprStatementsBlock;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.WStatement;

public class SmallHelpers {

	public static boolean hasEmptyBody(FunctionLike f) {
		return f.getBody().size() <= 2;
	}

	public static StmtReturn getReturnStatement(ExprStatementsBlock e) {
		if (e.getBody().isEmpty()) {
			return null;
		}
		WStatement lastStatement = e.getBody().get(e.getBody().size()-1);
		if (lastStatement instanceof StmtReturn) {
			return (StmtReturn) lastStatement;
		}
		return null;
	}

}
