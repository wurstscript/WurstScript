package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

import de.peeeq.wurstscript.utils.Utils;

public class ILloop extends ILStatement {

	private List<ILStatement> loopBody;

	public ILloop(List<ILStatement> loopBody) {
		this.loopBody = loopBody;
	}

	public List<ILStatement> getLoopBody() {
		return loopBody;
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		sb.append("loop\n");
		for (ILStatement s : loopBody) {
			s.printJass(sb, indent+1);
		}
		Utils.printIndent(sb, indent);
		sb.append("endloop\n");
	}

}
