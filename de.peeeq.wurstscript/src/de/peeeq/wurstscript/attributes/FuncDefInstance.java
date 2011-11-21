package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.FunctionDefinition;

public class FuncDefInstance {

	private FunctionDefinition def;
	private ImmutableList<ClassOrModule> context;

	private FuncDefInstance(FunctionDefinition f, ImmutableList<ClassOrModule> context) {
		Preconditions.checkNotNull(f, context);
		this.def = f;
		this.context = context;
	}

	public FunctionDefinition getDef() {
		return def;
	}

	public static FuncDefInstance create(FunctionDefinition f) {
		return new FuncDefInstance(f, ImmutableList.<ClassOrModule>emptyList());
	}

	public static FuncDefInstance create(FunctionDefinition f, ImmutableList<ClassOrModule> context) {
		return new FuncDefInstance(f, context);
	}

	public ImmutableList<ClassOrModule> getContext() {
		return context;
	}

}
