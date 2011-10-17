package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.utils.Utils;

public class ILreturnVoid extends ILStatement {

	public ILreturnVoid() {
	}
	
	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("return\n");
	}

}
