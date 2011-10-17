package de.peeeq.wurstscript.intermediateLang;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public class ILfunction implements CodePrinting {

	private String name;
	private List<ILvar> params = null;
	private PscriptType returnType = null;
	private List<ILvar> locals = new NotNullList<ILvar>();
	private NotNullList<ILStatement> body = null;
	private Set<String> localNames = new HashSet<String>();
	private WPos source;


	public ILfunction(String name, WPos source) {
		this.name = name;
		this.source = source;
	}

	public void addParam(ILvar param) {
		if (params == null) {
			params = new NotNullList<ILvar>();
		}
		params.add(param);
		localNames.add(param.getName());
	}

	public void addLocalVar(ILvar var) {
		if (locals == null) {
			locals = new NotNullList<ILvar>();
		}
		locals.add(var);
		localNames.add(var.getName());
	}

	public List<ILvar> getParams() {
		if (params == null) throw new Error("Params not initialized");
		return params;
	}

	public PscriptType getReturnType() {
		if (returnType == null) throw new Error("returnType not initialized");
		return returnType;
	}

	public List<ILvar> getLocals() {
		if (locals == null) throw new Error("locals not initialized");
		return locals;
	}

	public List<ILStatement> getBody() {
		if (body == null) throw new Error("body not initialized");
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
		assertInitialized();
		
		sb.append("function " + name + " takes ");
		if (params == null || params.size() == 0) {
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

	private void assertInitialized() {
		if (body == null) throw new Error(name + " not initialized.");
		if (params == null) throw new Error(name + " not initialized.");
		if (returnType == null) throw new Error(name + " not initialized.");
	}

	public void setReturnType(PscriptType typ) {
		returnType = typ;
	}

	public PscriptType[] getParamTypes() {
		if (params == null) throw new Error("Params not initialized");
		PscriptType[] result = new PscriptType[params.size()];
		for (int i=0; i<result.length; i++) {
			result[i] = params.get(i).getType();
		}
		return result ;
	}

	public void addBody(List<? extends ILStatement> statements) {
		if (body == null) {
			body = new NotNullList<ILStatement>();
		}
		body.addAll(statements);
	}

	public void initParams() {
		params = new NotNullList<ILvar>();
	}

	public WPos source() {
		return source;
	}

	public void addBeforeBody(ILfunctionCall iLfunctionCall) {
		if (body == null) {
			body = new NotNullList<ILStatement>();
		}
		body.add(0, iLfunctionCall);
	}
	
	@Override
	public String toString() {
		return "function " + name;
	}

}
