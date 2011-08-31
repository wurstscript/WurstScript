package de.peeeq.wurstscript.intermediateLang;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;

import de.peeeq.wurstscript.types.PScriptTypeUnknown;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public class ILfunction implements CodePrinting {

	private String name;
	private List<ILvar> params = new NotNullList<ILvar>();
	private PscriptType returnType = PScriptTypeUnknown.instance();
	private List<ILvar> locals = new NotNullList<ILvar>();
	private List<ILStatement> body = new NotNullList<ILStatement>();
	private Set<String> localNames = new HashSet<String>();

	
	public ILfunction(String name) {
		this.name = name;
	}
	
	public void addParam(ILvar param) {
		params.add(param);
		localNames.add(param.getName());
	}
	
	public void addLocalVar(ILvar var) {
		locals.add(var);
		localNames.add(var.getName());
	}
	
	public List<ILvar> getParams() {
		return params;
	}

	public PscriptType getReturnType() {
		return returnType;
	}

	public List<ILvar> getLocals() {
		return locals;
	}

	public List<ILStatement> getBody() {
		return body;
	}

	public String getName() {
		return name;
	}

	public Set<String> getLocalNames() {
		return localNames ;
	}

	@Override
	public void printJass(StringBuilder sb, int indent) {
		sb.append("function " + name + " takes ");
		if (params.size() == 0) {
			sb.append("nothing");
		} else {
			Utils.printSep(sb, ", ", params, new Function<ILvar, String>() {
				@Override
				public String apply(ILvar v) {
					return v.getType().printJass() + " " + v.getName();
				}
			});
		}
		sb.append(" returns ");
		if (returnType instanceof PScriptTypeVoid) {
			sb.append("nothing");
		} else {
			sb.append(returnType.printJass());
		}
		sb.append("\n");
		
		// print locals
		for (ILvar l : locals) {
			sb.append("local ");
			l.printJass(sb, 1);
			sb.append("\n");
		}
		// print body:
		for (ILStatement s : body) {
			s.printJass(sb, 1);
		}
		
		
		sb.append("endfunction\n");
	}

	public void setReturnType(PscriptType typ) {
		returnType = typ;
	}

}
