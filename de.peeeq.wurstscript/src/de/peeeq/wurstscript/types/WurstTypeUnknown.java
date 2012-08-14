package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImType;


public class WurstTypeUnknown extends WurstType {

	private static final WurstTypeUnknown instance = new WurstTypeUnknown("unknown");

	private String name = "unknown";

	private Error err;
	
	public WurstTypeUnknown(String name) {
		this.name = name;
		try {
			throw new Error("unknown type");
		} catch (Error e) {
			this.err = e; // store for later
		}
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		return false;
	}


	@Override
	public String getName() {
		return "'unknown type'\n(the type " + name + 
				" could not be found, maybe some package misses an import...)";
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static WurstTypeUnknown instance() {
		return instance;
	}


	@Override
	public String[] jassTranslateType() {
		throw err;
	}

	@Override
	public ImType imTranslateType() {
		throw new Error("not implemented");
	}

}
