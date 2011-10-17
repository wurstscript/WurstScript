package de.peeeq.wurstscript.intermediateLang.translator;

import de.peeeq.wurstscript.intermediateLang.ILStatement;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.utils.Utils;

public class ILerror extends ILStatement {

	public final ILvar msg;

	public ILerror(ILvar msg) {
		this.msg = msg;
	}
	
	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("call BJDebugMsg(\"Error!  \" + " + msg.getName() + " )\n");
//		sb.append("call I2S(100 / 0) // divition by zero to crash thread.\n");
	}

}
