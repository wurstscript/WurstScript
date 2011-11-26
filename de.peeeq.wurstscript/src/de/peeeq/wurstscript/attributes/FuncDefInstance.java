package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.VisibilityModifier;

public class FuncDefInstance {

	private final FunctionDefinition def;
	private final ImmutableList<ClassOrModule> context;
	private final Visibility visibility;


	public FuncDefInstance(FunctionDefinition f, ImmutableList<ClassOrModule> context, Visibility visibility) {
		this.def = f;
		this.context = context;
		this.visibility = visibility;
	}

	public FunctionDefinition getDef() {
		return def;
	}

	public static FuncDefInstance create(FunctionDefinition f) {
		return new FuncDefInstance(f, ImmutableList.<ClassOrModule>emptyList(), Visibility.get(f.getModifiers()));
	}

	public static FuncDefInstance create(FunctionDefinition f, ImmutableList<ClassOrModule> context) {
		return new FuncDefInstance(f, context, Visibility.get(f.getModifiers()));
	}


	public ImmutableList<ClassOrModule> getContext() {
		return context;
	}
	
	public FuncDefInstance inContext(ImmutableList<ClassOrModule> context) {
		return new FuncDefInstance(def, context.cons(this.context), visibility);
	}
	
	public FuncDefInstance asPrivate() {
		return new FuncDefInstance(def, context, Visibility.PRIVATE);
	}

	public boolean isPrivate() {
		return visibility == Visibility.PRIVATE;
	}

	public Visibility getVisibility() {
		return visibility;
	}
	

}
