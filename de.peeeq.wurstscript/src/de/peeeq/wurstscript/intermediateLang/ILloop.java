package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

public class ILloop extends ILStatement {

	private List<ILStatement> loopBody;

	public ILloop(List<ILStatement> loopBody) {
		this.loopBody = loopBody;
	}

	public List<ILStatement> getLoopBody() {
		return loopBody;
	}

	@Override
	public void printJass(StringBuilder sb) {
		sb.append("loop\n");
		for (ILStatement s : loopBody) {
			s.printJass(sb);
		}
		sb.append("endloop\n");
	}

}
