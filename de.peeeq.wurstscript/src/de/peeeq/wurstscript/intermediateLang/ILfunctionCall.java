package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

import com.google.common.base.Function;

import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.Utils;

public class ILfunctionCall extends ILStatementSet {

	private String name;
	private PscriptType[] argumentTypes;
	private ILvar[] argumentVars;

	
	public String getName() {
		return name;
	}

	public PscriptType[] getArgumentTypes() {
		return argumentTypes;
	}

	public List<ILvar> getArgs() {
		return Utils.list(argumentVars);
	}

	public ILfunctionCall(ILvar resultVar, String name, PscriptType[] argumentTypes,
			ILvar[] argumentVars) {
		super(resultVar);
		this.name = name;
		this.argumentTypes = argumentTypes;
		this.argumentVars = argumentVars;
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
		sb.append(name);
		sb.append("(");
		Utils.printSep(sb, ",", argumentVars, new Function<ILvar, String>() {
			@Override
			public String apply(ILvar v) {
				return v.getName();
			}
		});
		sb.append(")\n");
	}

}
