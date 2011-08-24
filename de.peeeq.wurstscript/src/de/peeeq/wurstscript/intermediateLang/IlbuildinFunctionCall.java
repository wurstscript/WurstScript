package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

import com.google.common.base.Function;

import de.peeeq.wurstscript.utils.Utils;

public class IlbuildinFunctionCall extends ILStatementSet implements CodePrinting {

	private String funcName;
	private ILvar[] args;

	public IlbuildinFunctionCall(ILvar resultVar, String funcName, ILvar[] args) {
		super(resultVar);
		this.funcName = funcName;
		this.args = args;
	}

	public String getFuncName() {
		return funcName;
	}

	public List<ILvar> getArgs() {
		return Utils.list(args);
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		Utils.printIndent(sb, indent);
		if (getResultVar() == null) {
			sb.append("call ");
		} else {
			sb.append("set ");
			sb.append(getResultVar().getName());
			sb.append(" = ");
		}
		sb.append(funcName);
		sb.append("(");
		Utils.printSep(sb, ",", args, new Function<ILvar, String>() {
			@Override
			public String apply(ILvar v) {
				return v.getName();
			}
		});
		sb.append(")\n");
	}

}
