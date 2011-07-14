package de.peeeq.pscript.intermediateLang;

import java.util.List;

import com.google.common.base.Function;

import de.peeeq.pscript.types.PScriptTypeUnknown;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.utils.NotNullList;
import de.peeeq.pscript.utils.Utils;

public class ILfunction {

	private String name;
	private List<ILvar> params = new NotNullList<ILvar>();
	private PscriptType returnType = PScriptTypeUnknown.instance();
	private List<ILvar> locals = new NotNullList<ILvar>();
	private List<ILStatement> body = new NotNullList<ILStatement>();

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

	public ILfunction(String name, final List<ILvar> params, PscriptType returnType, List<ILvar> localsWithParams,
			List<ILStatement> body) {
		this.name = name;
		set(params, returnType, localsWithParams, body);
	}

	public ILfunction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void set(final List<ILvar> params, PscriptType returnType,
			List<ILvar> localsWithParams, List<ILStatement> body) {
		this.params = params;
		this.returnType = returnType;
		this.locals = Utils.filter(localsWithParams, new Function<ILvar, Boolean>() {

			@Override
			public Boolean apply(ILvar v) {
				return !params.contains(v);
			}
		});
		this.body = body;
		
	}

}
